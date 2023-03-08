package gms.core.performancemonitoring.ssam.control;

import com.google.common.collect.Lists;

import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.ssam.control.api.DecimationRequestParams;
import gms.core.performancemonitoring.ssam.control.api.HistoricalStationSohAnalysisView;
import gms.core.performancemonitoring.ssam.control.api.StationSohAnalysisManager;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.ssam.control.dataprovider.KafkaConsumer;
import gms.core.performancemonitoring.uimaterializedview.AcknowledgedSohStatusChange;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.PerformanceMonitoringRepositoryInterface;
import gms.shared.frameworks.osd.api.util.HistoricalStationSohRequest;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiSerializer;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.repository.OsdRepositoryFactory;
import gms.shared.frameworks.osd.repository.performancemonitoring.PerformanceMonitoringRepositoryJpa;
import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;

import java.time.Duration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.sender.SenderOptions;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import static com.google.common.base.Preconditions.checkNotNull;

import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.ACKNOWLEDGED_SOH_STATUS_CHANGE_INPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.CAPABILITY_SOH_ROLLUP_INPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.QUIETED_SOH_STATUS_CHANGE_INPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.STATION_SOH_INPUT_TOPIC_KEY;
import gms.shared.frameworks.osd.api.performancemonitoring.CapabilitySohRollupRepositoryInterface;
import gms.shared.frameworks.osd.repository.performancemonitoring.CapabilitySohRollupRepositoryJpa;

/**
 * ReactiveStationSohAnalysisManager(SSAM) is responsible for controlling
 * computation needed for the UI including station acknowledgement and quieting.
 * SSAM tracks changes to quiet and acknowledge stations and publishes to the
 * ui-materialized-view Kafka topic. SSAM also publishes Station SOH related
 * status messages to be viewed using SystemMessagesDisplay.
 */
public class ReactiveStationSohAnalysisManager implements StationSohAnalysisManager {

  public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka-bootstrap-servers";
  public static final String MAX_CACHE_INIT_RETRIES = "max_cache_init_retries";
  private static final Logger logger = LoggerFactory.getLogger(ReactiveStationSohAnalysisManager.class);
  private final StationSohAnalysisManagerConfiguration processingConfig;
  private final PerformanceMonitoringRepositoryInterface sohCacheRepo;
  private final CapabilitySohRollupRepositoryInterface csrCacheRepo;
  private final SystemConfig systemConfig;
  private final long maxDatabaseRetries;

  ReactiveStationSohAnalysisManager(StationSohAnalysisManagerConfiguration processingConfig, SystemConfig systemConfig,
   PerformanceMonitoringRepositoryInterface sohCacheRepo, CapabilitySohRollupRepositoryInterface csrCacheRepo) {
    this.processingConfig = processingConfig;
    this.systemConfig = systemConfig;
    this.sohCacheRepo = sohCacheRepo;
    this.csrCacheRepo = csrCacheRepo;
    maxDatabaseRetries = Long.valueOf(systemConfig.getValue(MAX_CACHE_INIT_RETRIES));
  }

  /**
   * Factory Method for {@link StationSohAnalysisManager}
   *
   * @param controlContext access to externalized dependencies.
   *
   * @return {@link ReactiveStationSohAnalysisManager}
   */
  public static ReactiveStationSohAnalysisManager create(ControlContext controlContext) {
    checkNotNull(controlContext);
    var systemConfig = controlContext.getSystemConfig();

    var stationSohAnalysisManagerConfiguration = StationSohAnalysisManagerConfiguration.create(controlContext.getProcessingConfigurationConsumerUtility(),
      OsdRepositoryFactory.createOsdRepository(systemConfig));

    return new ReactiveStationSohAnalysisManager(
      stationSohAnalysisManagerConfiguration, systemConfig,
      new PerformanceMonitoringRepositoryJpa(CoiEntityManagerFactory.create("gms_ssam", systemConfig)),
      new CapabilitySohRollupRepositoryJpa(CoiEntityManagerFactory.create("gms_ssam", systemConfig)));
  }

  /**
   * Sets up the pipeline to pull the needed data from the database for
   * SSAM-control
   *
   * @return the Mono<DataContainer> to be subscribed to
   */
  public Mono<DataContainer> initializeCacheFromOsd() {

    var stationSohMono = Mono.fromCallable(this::initializeCurrentStationSoh).retryWhen(configurableRetry(logger, StationSoh.class.getSimpleName())).onErrorReturn(new HashMap<>());
    var capabilityMono = Mono.fromCallable(this::initializeCurrentCapabilitySohRollups).retryWhen(configurableRetry(logger, CapabilitySohRollup.class.getSimpleName())).onErrorReturn(new HashMap<>());
    var quietedStatusChangeMono = Mono.fromCallable(this::initializeQuietedSohStatusChanges).retryWhen(configurableRetry(logger, QuietedSohStatusChange.class.getSimpleName())).onErrorReturn(new HashSet<>());
    var unackStatusChangeMono = Mono.fromCallable(this::initializeUnacknowledgedSohStatusChanges).retryWhen(configurableRetry(logger, UnacknowledgedSohStatusChange.class.getSimpleName())).onErrorReturn(new HashSet<>());

    return Mono.zip(stationSohMono, capabilityMono, quietedStatusChangeMono, unackStatusChangeMono)
      .map(tuple -> new DataContainer(
      tuple.getT1(),
      tuple.getT2(),
      tuple.getT3(),
      tuple.getT4()));
  }

  private RetryBackoffSpec configurableRetry(Logger logger, String queryingFor) {
    return Retry.backoff(maxDatabaseRetries, Duration.ofMillis(100))
      .maxBackoff(Duration.ofMillis(2000))
      .transientErrors(true)
      .doBeforeRetry(
        retry -> logger.warn("Failed querying database for {}, attempting re-query...", queryingFor,
          retry.failure()));
  }

  /**
   * Private helper method intended to be used inside the decimate call. Method
   * will return a Map of SohMonitorType to HistoricalStationSoh
   *
   * @param decimationRequestParams the decimationRequestParams
   *
   * @return Map<SohMonitorType, HistoricalStationSoh> HistoricalStationSohData
   */
  static HistoricalStationSoh getHistoricalStationSoh(
    DecimationRequestParams decimationRequestParams,
    OsdRepositoryInterface osdRepositoryInterface) {

    return osdRepositoryInterface.retrieveHistoricalStationSoh(
      HistoricalStationSohRequest.create(
        decimationRequestParams.getStationName(),
        decimationRequestParams.getStartTime(),
        decimationRequestParams.getEndTime(),
        decimationRequestParams.getSohMonitorType()
      )
    );
  }

  /**
   * gets the sender options for the Kafka Sender used in SSAM Control
   *
   * @param <T> the type of the objects being sent
   * @param systemConfig the systemConfig
   *
   * @return the SenderOptions to use
   */
  static <T> SenderOptions<String, T> senderOptions(SystemConfig systemConfig) {
    var properties = new HashMap<String, Object>();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      systemConfig.getValue(KAFKA_BOOTSTRAP_SERVERS));
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
    // By default, a producer doesn't wait for an acknowledgement from kafka when it sends
    // a message to a topic. Setting it to "1" means that it will wait for at least one kafka
    // node to acknowledge. The safest is "all", but that makes sending a little slower.
    properties.put(ProducerConfig.ACKS_CONFIG, "0");
    return SenderOptions.<String, T>create(properties)
      .withValueSerializer(new CoiSerializer<>())
      .stopOnError(false)
      .scheduler(Schedulers.boundedElastic());
  }

  /**
   * Indicates if the service has actually received valid configuration from the
   * Frameworks configuration service
   *
   * @return true or false indicating if the service has received valid
   * configuration yet
   */
  public boolean hasNonEmptyConfiguration() {
    return !this.processingConfig
      .resolveDisplayParameters()
      .getStationSohControlConfiguration()
      .getStationSohDefinitions()
      .isEmpty();
  }

  /**
   * Create and start all necessary caches, providers, and publishers
   *
   * @param dataContainer the DataContainer initialized from the database
   *
   * @return The Tuple of the SsamReactiveKafkaUtility and DataContainer
   */
  public Tuple3<SsamReactiveKafkaUtility, DataContainer, SsamKafkaConsumers> createAndInitializeKafkaUtility(DataContainer dataContainer) {

    var stationSohInputTopic = STATION_SOH_INPUT_TOPIC_KEY.getSystemConfigValue(systemConfig);

    var capabilityRollupInputTopic = CAPABILITY_SOH_ROLLUP_INPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    var ackInputTopic = ACKNOWLEDGED_SOH_STATUS_CHANGE_INPUT_TOPIC_KEY.getSystemConfigValue(systemConfig);

    var quietedInputTopic = QUIETED_SOH_STATUS_CHANGE_INPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    var stationSohKafkaConsumer = KafkaConsumer.getBuilder(systemConfig)
      .withTopic(stationSohInputTopic)
      .build(StationSoh.class);

    var acknowledgedSohStatusChangeReactiveConsumer = KafkaConsumer.getBuilder(systemConfig)
      .withTopic(ackInputTopic)
      .build(AcknowledgedSohStatusChange.class);

    var quietedSohStatusChangeUpdateReactiveConsumer = KafkaConsumer.getBuilder(systemConfig)
      .withTopic(quietedInputTopic)
      .build(QuietedSohStatusChangeUpdate.class);

    var capabilitySohRollupKafkaConsumer = KafkaConsumer.getBuilder(systemConfig)
      .withTopic(capabilityRollupInputTopic)
      .build(CapabilitySohRollup.class);

    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE,
      false);

    Sinks.Many<UnacknowledgedSohStatusChange> unacknowledgedSohStatusChangeSink = Sinks.many()
      .multicast()
      .onBackpressureBuffer(Integer.MAX_VALUE, false);

    Sinks.Many<QuietedSohStatusChangeUpdate> quietedSohStatusChangeUpdateSink = Sinks.many()
      .multicast()
      .onBackpressureBuffer(Integer.MAX_VALUE, false);

    var ssamMessageSinks = SsamMessageSinks.builder()
      .setSystemMessageEmitterSink(systemMessageSink)
      .setUnacknowledgedSohStatusChangeSink(unacknowledgedSohStatusChangeSink)
      .setQuietedSohStatusChangeUpdateSink(quietedSohStatusChangeUpdateSink)
      .build();

    var ssamKafkaConsumers = SsamKafkaConsumers.builder()
      .setStationSohReactiveConsumer(stationSohKafkaConsumer)
      .setCapabilitySohRollupReactiveConsumer(capabilitySohRollupKafkaConsumer)
      .setAcknowledgedSohStatusChangeReactiveConsumer(acknowledgedSohStatusChangeReactiveConsumer)
      .setQuietedSohStatusChangeUpdateReactiveConsumer(quietedSohStatusChangeUpdateReactiveConsumer)
      .build();

    var reactiveKafkaUtility = new SsamReactiveKafkaUtility(
      ssamMessageSinks,
      processingConfig,
      systemConfig,
      ssamKafkaConsumers
    );

    reactiveKafkaUtility.initializeSohQuietAndUnacknowledgedCacheManager(
      dataContainer.quietedSohStatusChanges,
      dataContainer.unacknowledgedSohStatusChanges,
      new HashSet<>(dataContainer.latestStationSohByStation.values())
    );

    return Tuples.of(reactiveKafkaUtility, dataContainer, ssamKafkaConsumers);
  }

  /**
   * Creates all the consumers and all the producers the SSAM control will need
   * and returns a Mono to subscribe on
   *
   * @param tuple the tuple containing the initialized SsamReactiveKafkaUtility
   * the DataContainer, and the SsamKafkaConsumers
   *
   * @return the Mono to subscribe onto start all the consumers and producers
   * that SSAM Control uses
   */
  public Mono<Void> setupReactiveProcessingPipeline(Tuple3<SsamReactiveKafkaUtility, DataContainer, SsamKafkaConsumers> tuple) {
    var stationGroups = this.processingConfig.stationGroups();

    var reactiveUtility = tuple.getT1();
    var dataContainer = tuple.getT2();
    var ssamKafkaConsumers = tuple.getT3();

    var systemMessagesUiProducer = reactiveUtility.createSystemMessagesProducer().sendMessages().then();
    var systemMessagesStorageProducer = reactiveUtility.createSystemMessagesStorageProducer().sendMessages().then();

    var materializedViewProducer = reactiveUtility.createMaterializedViewProducer(
        dataContainer.latestStationSohByStation,
        dataContainer.latestCapabilitySohRollupByStationGroup,
        stationGroups
      ).sendMessages().then();

    var ackProducer = reactiveUtility.createAcknowledgedMaterializedViewProducer(
      dataContainer.latestStationSohByStation,
      dataContainer.latestCapabilitySohRollupByStationGroup,
      stationGroups
    ).sendMessages().then();

    var quietedProducer = reactiveUtility.createQuietedMaterializedViewProducer(
      dataContainer.latestStationSohByStation,
      dataContainer.latestCapabilitySohRollupByStationGroup,
      stationGroups
    ).sendMessages().then();

    var quietedStorageProducer = reactiveUtility.createQuietedProducer().sendMessages().then();
    var unackStorageProducer = reactiveUtility.createUnackProducer().sendMessages().then();

    return Mono.firstWithSignal(
      systemMessagesUiProducer,
      systemMessagesStorageProducer,
      materializedViewProducer,
      ackProducer,
      quietedProducer,
      quietedStorageProducer,
      unackStorageProducer,
      ssamKafkaConsumers.getAcknowledgedSohStatusChangeReactiveConsumer().receive(),
      ssamKafkaConsumers.getCapabilitySohRollupReactiveConsumer().receive(),
      ssamKafkaConsumers.getQuietedSohStatusChangeUpdateReactiveConsumer().receive(),
      ssamKafkaConsumers.getStationSohReactiveConsumer().receive()
    );
  }

  /**
   * Populates the current {@link StationSoh} map for each station contained in
   * the {@link
   * StationSohMonitoringDefinition}.
   */
  Map<String, StationSoh> initializeCurrentStationSoh() {

    var latestStationSohByStation = new ConcurrentHashMap<String, StationSoh>();

    var stationSohDefinitions = this.processingConfig
      .resolveDisplayParameters().getStationSohControlConfiguration().getStationSohDefinitions();

    var stationNames = stationSohDefinitions.stream()
      .map(StationSohDefinition::getStationName)
      .collect(Collectors.toList());

    Lists.partition(stationNames, (stationNames.size() / 4) + 1)
      .stream()
      .parallel()
      .filter(names -> !names.isEmpty())
      .map(names -> {
        logger.info("Retrieving latest StationSoh for {} stations", names.size());
        return this.sohCacheRepo.retrieveByStationId(names);
      })
      .flatMap(List::stream)
      .forEach(
        stationSoh -> latestStationSohByStation.put(stationSoh.getStationName(), stationSoh));
    logger.info("StationSoh DB retrieval  returned {} entries.", latestStationSohByStation.size());

    return latestStationSohByStation;
  }

  /**
   * Populates the most current {@link CapabilitySohRollup}s for the configured
   * station groups.
   */
  Map<String, CapabilitySohRollup> initializeCurrentCapabilitySohRollups() {

    var latestCapabilitySohRollupByStationGroup = new ConcurrentHashMap<String, CapabilitySohRollup>();

    var stationGroups = new HashSet<>(
      this.processingConfig.resolveDisplayParameters().getStationSohControlConfiguration().getDisplayedStationGroups());

    List<CapabilitySohRollup> capabilitySohRollups;
    if (stationGroups.isEmpty()) {
      logger.warn("No displayed station groups have been defined");
      capabilitySohRollups = Collections.emptyList();
    } else {
      logger.info("Retrieving CapabilitySohRollups for {} StationGroups", stationGroups.size());
      capabilitySohRollups = this.csrCacheRepo.retrieveLatestCapabilitySohRollupByStationGroup(
        stationGroups);
      logger.info("CapabilitySohRollup DB retrieval returned {} entries.",
        capabilitySohRollups.size());
    }

    for (CapabilitySohRollup capabilitySohRollup : capabilitySohRollups) {
      latestCapabilitySohRollupByStationGroup.put(capabilitySohRollup.getForStationGroup(),
        capabilitySohRollup);
    }

    return latestCapabilitySohRollupByStationGroup;
  }

  /**
   * Retrieves unacknowledged SOH status change events from the db.
   */
  Set<UnacknowledgedSohStatusChange> initializeUnacknowledgedSohStatusChanges() {

    var stationSohDefinitions = 
      this.processingConfig.resolveDisplayParameters().getStationSohControlConfiguration().getStationSohDefinitions();

    var stationNames = stationSohDefinitions.stream()
      .map(StationSohDefinition::getStationName)
      .collect(Collectors.toList());

    logger.info("Retrieving UnacknowledgedSohStatusChanges for {} stations", stationNames.size());
    var unacknowledgedSohStatusChanges = !stationNames.isEmpty() ? new HashSet<>(this.processingConfig.getSohRepositoryInterface().retrieveUnacknowledgedSohStatusChanges(stationNames))
      : Collections.<UnacknowledgedSohStatusChange>emptySet();

    logger.info("UnacknowledgedSohStatusChanges DB retrieval returned {} entries.",
      unacknowledgedSohStatusChanges.size());

    return unacknowledgedSohStatusChanges;
  }

  /**
   * Retrieves quieted SOH status changes for the current instant minus the
   * specified duration.
   */
  Set<QuietedSohStatusChange> initializeQuietedSohStatusChanges() {

    logger.info("Retrieving active QuietedSohStatusChanges");
    var quietedSohStatusChanges = new HashSet<>(this.processingConfig.getSohRepositoryInterface().retrieveQuietedSohStatusChangesByTime(Instant.now()));
    logger.info("QuitedSohStatusChange DB retrieval returned {} entries.",
      quietedSohStatusChanges.size());

    return quietedSohStatusChanges;
  }

  @Override
  public StationSohMonitoringUiClientParameters resolveStationSohMonitoringUiClientParameters(
    String placeholder) {
    return this.processingConfig.resolveDisplayParameters();
  }

  @Override
  public HistoricalStationSohAnalysisView retrieveDecimatedHistoricalStationSoh(
    DecimationRequestParams decimationRequestParams) {

    return DecimationUtility.decimateHistoricalStationSoh(
      decimationRequestParams,
      getHistoricalStationSoh(decimationRequestParams,
        this.processingConfig.getSohRepositoryInterface())
    );
  }

  public SystemConfig getSystemConfig() {
    return this.systemConfig;
  }

  /**
   * A container class for all the data that SSAM Control needs in order to
   * start up
   */
  public static class DataContainer {

    // Cache of latest StationSoh. Should contain the most recent StationSoh for each station being
    // monitored. Initialized at startup from the OSD and kept up to date with StationSoh received
    // from a kafka topic. Keyed by station name.
    final Map<String, StationSoh> latestStationSohByStation;

    // Cache of latest CapabilitySohRollup. Should contain the most recent
    // CapabilitySohRollup for each station group being monitored.
    // Initialized at startup from the OSD and kept up to date with CapabilitySohRollup
    // received from a kafka topic. Keyed by station group name.
    final Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup;

    // Contains quieted Soh status changes. Initialized from the OSD at
    // startup and used in the list manager initialization.
    final Set<QuietedSohStatusChange> quietedSohStatusChanges;

    // Contains unacknowledged SohStatus changes. This is initialized from
    // the OSD at startup and used in the list manager initialization.
    final Set<UnacknowledgedSohStatusChange> unacknowledgedSohStatusChanges;

    DataContainer(
      Map<String, StationSoh> latestStationSohByStation,
      Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup,
      Set<QuietedSohStatusChange> quietedSohStatusChanges,
      Set<UnacknowledgedSohStatusChange> unacknowledgedSohStatusChanges) {
      this.latestStationSohByStation = latestStationSohByStation;
      this.latestCapabilitySohRollupByStationGroup = latestCapabilitySohRollupByStationGroup;
      this.quietedSohStatusChanges = quietedSohStatusChanges;
      this.unacknowledgedSohStatusChanges = unacknowledgedSohStatusChanges;
    }
  }
}

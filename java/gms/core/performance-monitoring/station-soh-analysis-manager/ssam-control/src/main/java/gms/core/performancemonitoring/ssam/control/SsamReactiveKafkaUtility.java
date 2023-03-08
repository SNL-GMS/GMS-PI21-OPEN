package gms.core.performancemonitoring.ssam.control;

import com.google.auto.value.AutoOneOf;
import gms.core.performancemonitoring.ssam.control.SsamReactiveKafkaUtility.SohWrapper.SohType;
import gms.core.performancemonitoring.ssam.control.dataprovider.ReactiveConsumer;
import gms.core.performancemonitoring.ssam.control.datapublisher.KafkaProducer;
import gms.core.performancemonitoring.ssam.control.datapublisher.SystemEvent;
import gms.core.performancemonitoring.ssam.control.processor.AcknowledgeSohStatusChangeMaterializedViewProcessor;
import gms.core.performancemonitoring.ssam.control.processor.MaterializedViewProcessor;
import gms.core.performancemonitoring.ssam.control.processor.QuietedSohStatusChangeUpdateMaterializedViewProcessor;
import gms.core.performancemonitoring.uimaterializedview.AcknowledgedSohStatusChange;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.core.performancemonitoring.uimaterializedview.SohQuietAndUnacknowledgedCacheManager;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroups;
import gms.core.performancemonitoring.uimaterializedview.UiStationSoh;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.reactor.EmitFailureHandlerUtility;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.SOH_SYSTEM_MESSAGE_OUTPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.SOH_SYSTEM_MESSAGE_UI_OUTPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.STATION_SOH_ANALYSIS_VIEW_OUTPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.STATION_SOH_QUIETED_OUTPUT_TOPIC_KEY;
import static gms.core.performancemonitoring.ssam.control.KafkaTopicConfigurationKeys.STATION_SOH_STATUS_CHANGE_OUTPUT_TOPIC_KEY;

/**
 * Utility class (that gets instantiated) containing the bulk of SSAM controls
 * Kafka and Reactive functionality (like creating Kafka consumers and producers
 * and the Fluxes associated with them)
 */
public class SsamReactiveKafkaUtility {

  // Uses the legacy logger for now to create timing logs, until the structed logs
  // can be read by the timing scripts
  private static final org.apache.logging.log4j.Logger legacyLogger = LogManager
    .getLogger(SsamReactiveKafkaUtility.class);

  private static final Level TIMING = Level.getLevel("TIMING");

  private static final Logger logger = LoggerFactory.getLogger(SsamReactiveKafkaUtility.class);
  public static final String SOH_MESSAGE = "soh-message";
  public static final String SYSTEM_MESSAGE = "system-message";

  // Get a single sink for all "sinkers" to use
  private final Sinks.Many<SystemMessage> systemMessageSink;

  // Get a single sink for all "sinkers" to use
  private final Sinks.Many<QuietedSohStatusChangeUpdate> quietedSohStatusChangeUpdateSink;

  private final Sinks.Many<UnacknowledgedSohStatusChange> unacknowledgedSohStatusChangeSink;

  private final StationSohAnalysisManagerConfiguration processingConfig;
  private final SystemConfig systemConfig;

  // List manager to handle unacknowledged and quieted states needed for the
  // UiStationAndStationGroupGenerator class
  private SohQuietAndUnacknowledgedCacheManager sohQuietAndUnacknowledgedCacheManager;
  private final ReactiveConsumer<StationSoh> stationSohKafkaConsumer;
  private final ReactiveConsumer<CapabilitySohRollup> capabilitySohRollupKafkaConsumer;
  private final ReactiveConsumer<AcknowledgedSohStatusChange> acknowledgedSohStatusChangeReactiveConsumer;
  private final ReactiveConsumer<QuietedSohStatusChangeUpdate> quietedSohStatusChangeUpdateReactiveConsumer;

  /**
   * Create a new instance of the utility
   *
   * @param ssamMessageSinks wrapper class for all sinks used in the utility
   * @param processingConfig processing configuration
   * @param systemConfig systemConfig system configuration
   * @param ssamKafkaConsumers contains all the kafkaConsumers used in this
   * utility
   */
  public SsamReactiveKafkaUtility(
    SsamMessageSinks ssamMessageSinks,
    StationSohAnalysisManagerConfiguration processingConfig,
    SystemConfig systemConfig,
    SsamKafkaConsumers ssamKafkaConsumers) {

    this.unacknowledgedSohStatusChangeSink = ssamMessageSinks.getUnacknowledgedSohStatusChangeSink();
    this.quietedSohStatusChangeUpdateSink = ssamMessageSinks.getQuietedSohStatusChangeUpdateSink();
    this.processingConfig = processingConfig;
    this.systemConfig = systemConfig;

    this.systemMessageSink = ssamMessageSinks.getSystemMessageEmitterSink();

    this.stationSohKafkaConsumer = ssamKafkaConsumers.getStationSohReactiveConsumer();
    this.capabilitySohRollupKafkaConsumer = ssamKafkaConsumers.getCapabilitySohRollupReactiveConsumer();
    this.quietedSohStatusChangeUpdateReactiveConsumer = ssamKafkaConsumers.getQuietedSohStatusChangeUpdateReactiveConsumer();
    this.acknowledgedSohStatusChangeReactiveConsumer = ssamKafkaConsumers.getAcknowledgedSohStatusChangeReactiveConsumer();

  }
  
  /**
   * Turn a Flux of SohWrappers into a Flux of SohCorrelations by associating
   * StationSoh and CapabilitySohRollups from the same calculation interval to
   * each other.
   *
   * @return Flux of SohCorrelation
   */
  static Flux<SohPackage> createSohPackageFlux(Flux<SohWrapper> sohWrapperFlux, int bufferSize) {
    return Flux.merge(sohWrapperFlux
      .groupBy(SohWrapper::wrapperTimeStamp)
      .map(groupedFlux -> groupedFlux.bufferTimeout(
      bufferSize,
      //
      // The timeout is somewhat arbitrary but should allow enough time for all objects to be
      // generated by SohControl
      //
      Duration.ofSeconds(5))
      .take(1) // There will only ever be one buffered list per calc. interval
      .map(sohWrappers -> {

        if (sohWrappers.isEmpty()) {
          return Optional.<SohPackage>empty();
        }

        var capabilitySohRollupSet = new HashSet<CapabilitySohRollup>();
        var stationSohSet = new HashSet<StationSoh>();

        sohWrappers.forEach(
          sohWrapper -> {
            if (sohWrapper.getSohType() == SohType.CAPABILITY_SOH_ROLLUP) {
              capabilitySohRollupSet.add(sohWrapper.capabilitySohRollup());
            } else {
              stationSohSet.add(sohWrapper.stationSoh());
            }
          }
        );

        return Optional.of(SohPackage.create(
          capabilitySohRollupSet,
          stationSohSet
        ));
      }
      )
      .doOnNext(sohPackageOptional -> {
        if (sohPackageOptional.isEmpty()) {
          logger.warn("Received no data whatsoever for calculation interval {}",
            groupedFlux.key());
        }
      })
      .filter(Optional::isPresent)
      .map(Optional::get)
      //
      // If we received no StationSohs, something is wrong
      //
      .doOnNext(sohPackage -> {
        if (sohPackage.getStationSohs().isEmpty()) {
          logger
            .warn("Received no StationSohs for calculation interval {}", groupedFlux.key());
        }
      })
      .filter(sohPackage -> !sohPackage.getStationSohs().isEmpty())
      )
    );

  }

  /**
   * Create a flux that pairs a Flux of StationSoh with a Flux of
   * CapabilitySohRollup into a single Flux of SohWrapper. Also populates the
   * given maps.
   *
   * @param stationSohFlux Flux of StationSoH
   * @param capabilitySohRollupFlux Flux of CapabilitySohRollup
   * @param latestStationSohByStation Map of StationSoh, by station name, to
   * update
   * @param latestCapabilitySohRollupByStationGroup Map of CapabilitySohRollup,
   * by group name, to update
   *
   * @return a Flux of SohWrapper. The two maps will also be upadated as data
   * arrives.
   */
  static Flux<SohWrapper> createSohWrapperFlux(
    Flux<StationSoh> stationSohFlux,
    Flux<CapabilitySohRollup> capabilitySohRollupFlux,
    Map<String, StationSoh> latestStationSohByStation,
    Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup
  ) {
    return Flux.merge(
      stationSohFlux
        .doOnNext(
          stationSoh -> latestStationSohByStation.put(stationSoh.getStationName(), stationSoh)
        )
        .map(SohWrapper::ofStationSoh),
      capabilitySohRollupFlux
        .doOnNext(
          capabilitySohRollup
          -> latestCapabilitySohRollupByStationGroup.put(
            capabilitySohRollup.getForStationGroup(), capabilitySohRollup
          )
        )
        .map(SohWrapper::ofCapabilitySohRollup)
    );
  }

  private static Flux<UiStationAndStationGroups> createAcknowledgedMaterializedViewFlux(
    Flux<AcknowledgedSohStatusChange> acknowledgedSohStatusChangeFlux,
    SohQuietAndUnacknowledgedCacheManager sohQuietAndUnacknowledgedCacheManager,
    AcknowledgeSohStatusChangeMaterializedViewProcessor acknowledgeSohStatusChangeMaterializedViewProcessor) {

    return acknowledgedSohStatusChangeFlux
      .doOnNext(sohQuietAndUnacknowledgedCacheManager::addAcknowledgedStationToQuietList)
      .onErrorContinue(
        (throwable, object)
        -> logger.error(
          "Error with acknowledgement " + object,
          throwable
        )
      )
      .map(acknowledgeSohStatusChangeMaterializedViewProcessor)
      .flatMap(Flux::fromIterable);
  }

  /**
   * Generating the UI materialized view of incoming soh data. The
   * MaterializedViewProcessor will potentially create more than one
   * UiStationAndStationGroups object per soh package. So, turn its collection
   * into a flux and merge with the other generated Fluxes.
   *
   * @param sohPackageFlux Flux of SohPackage
   * @param materializedViewProcessor MaterializedViewProcessor to map a
   * SohPackage to a List of UiStationAndStationGroups objects
   *
   * @return Flux of "UiStationAndStationGroups" objects
   */
  static Flux<UiStationAndStationGroups> createMaterializedViewFlux(
    Flux<SohPackage> sohPackageFlux,
    MaterializedViewProcessor materializedViewProcessor
  ) {
    return Flux.merge(
      sohPackageFlux
        .map(materializedViewProcessor)
        .map(Flux::fromIterable)
    );

  }

  /**
   * Create the materialized view flux, from a Flux of SohPackage. This version
   * creates uses the "default" MaterializedViewProcessor.
   *
   * @param sohPackageFlux Flux of SohPackage
   * @param sohQuietAndUnacknowledgedCacheManager Manager for tracking quiet and
   * unack changes
   * @param processingConfig processing configuration
   * @param stationGroups station groups from configuration
   * @param systemMessageSink Sink to send created SystemMesssages to
   *
   * @return Flux of "UiStationAndStationGroups" objects
   */
  private static Flux<UiStationAndStationGroups> createMaterializedViewFlux(
    Flux<SohPackage> sohPackageFlux,
    SohQuietAndUnacknowledgedCacheManager sohQuietAndUnacknowledgedCacheManager,
    StationSohAnalysisManagerConfiguration processingConfig,
    List<StationGroup> stationGroups,
    Sinks.Many<SystemMessage> systemMessageSink
  ) {

    var matViewProcessor = MaterializedViewProcessor
      .create(sohQuietAndUnacknowledgedCacheManager,
        processingConfig.resolveDisplayParameters(),
        stationGroups,
        systemMessageSink
      );

    //
    // The MaterializedViewProcessor will potentially create more than one UiStationAndStationGroups
    // object per soh package. So, turn its collection into a flux and merge with the other generated
    // Fluxes
    //
    var uiStationAndStationGroupsFlux = createMaterializedViewFlux(
      sohPackageFlux,
      matViewProcessor
    );

    if (legacyLogger.isEnabled(TIMING)) {

      uiStationAndStationGroupsFlux = uiStationAndStationGroupsFlux
        .doOnNext(uiStationAndStationGroups -> Flux
        .fromIterable(uiStationAndStationGroups.getStationSoh())
        .distinct(UiStationSoh::getUuid)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(uiStationSoh -> legacyLogger.log(
        TIMING,
        "SOH object {} with timestamp {}; now: {}",
        uiStationSoh.getUuid(),
        Instant.ofEpochMilli(uiStationSoh.getTime()),
        Instant.now()
      )));
    }

    return uiStationAndStationGroupsFlux;
  }

  /**
   * Start the internal SohQuietAndUnacknowledgedCacheManager for detecting
   * changes in quiet/unack status
   *
   * @param initialQuietedSohStatusChanges initial set of quiet status changes
   * @param initialUnacknowledgedSohStatusChanges initial set of unack status
   * changes
   * @param initialStationSohs Initial set of station sohs
   */
  void initializeSohQuietAndUnacknowledgedCacheManager(Set<QuietedSohStatusChange> initialQuietedSohStatusChanges,
    Set<UnacknowledgedSohStatusChange> initialUnacknowledgedSohStatusChanges,
    Set<StationSoh> initialStationSohs) {

    this.sohQuietAndUnacknowledgedCacheManager = new SohQuietAndUnacknowledgedCacheManager(
      initialQuietedSohStatusChanges,
      initialUnacknowledgedSohStatusChanges,
      initialStationSohs,
      this.processingConfig.resolveDisplayParameters(),
      systemMessageSink,
      unacknowledgedSohStatusChangeSink,
      quietedSohStatusChangeUpdateSink
    );
   
  }

  /**
   * Create the System Messages KafkaProducer that will publish messages
   * provided by systemMessageFlux for display in the UI
   *
   * @return The SystemEvent<SystemMessage> KafkaProducer
   */
  KafkaProducer<SystemEvent<SystemMessage>> createSystemMessagesProducer() {
    var systemMessagesOutputTopic = SOH_SYSTEM_MESSAGE_UI_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    return KafkaProducer.<SystemEvent<SystemMessage>>builder()
      .setDataFlux(systemMessageSink.asFlux()
        .map(systemMessage -> SystemEvent.from(SYSTEM_MESSAGE, systemMessage)))
      .setTopic(systemMessagesOutputTopic)
      .keyless()
      .setKafkaSender(KafkaSender.<String, SystemEvent<SystemMessage>>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Create the System Messages KafkaProducer that will publish messages
   * provided by systemMessageFlux for storage in the OSD
   *
   * @return The System Messages KafkaProducer
   */
  KafkaProducer<SystemMessage> createSystemMessagesStorageProducer() {
    var systemMessagesOutputTopic = SOH_SYSTEM_MESSAGE_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    return KafkaProducer.<SystemMessage>builder()
      .setDataFlux(systemMessageSink.asFlux())
      .setTopic(systemMessagesOutputTopic)
      .keyless()
      .setKafkaSender(KafkaSender.<String, SystemMessage>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Create the materialized view producer.
   *
   * @param latestStationSohByStation Initial map of stations, by station name
   * @param latestCapabilitySohRollupByStationGroup Initial map of capability
   * roll-ups, by station group name
   * @param stationGroups Set of configured station groups
   *
   * @return The KafkaProducer for the materialized ui view of Station and
   * Station Groups
   */
  KafkaProducer<SystemEvent<UiStationAndStationGroups>> createMaterializedViewProducer(
    Map<String, StationSoh> latestStationSohByStation,
    Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup, List<StationGroup> stationGroups) {

    var stationSohControlConfiguration = this.processingConfig
      .resolveDisplayParameters()
      .getStationSohControlConfiguration();
    var stationCount = stationSohControlConfiguration.getStationSohDefinitions().size();
    var groupCount = stationSohControlConfiguration.getDisplayedStationGroups().size();

    Flux<SohPackage> sohPackageFlux = createSohPackageFlux(
      createStateOfHealthConsumersFlux(latestStationSohByStation, latestCapabilitySohRollupByStationGroup,
        this.stationSohKafkaConsumer, this.capabilitySohRollupKafkaConsumer),
      //
      // SohWrapper will wrap either a StationSoh or CapabilitySohRollup. Not neither, and not
      // both. So, there should be a total of (configured stations + configured station groups)
      // SohWrappers for each calculation interval.
      //
      stationCount + groupCount
    );

    return createMaterializedViewProducer(sohPackageFlux, stationGroups);
  }

  /**
   * Start the CapabilitySohRollup and StatonSoh suppliers, merge the into a
   * single Flux of SohWrapper, And return that Flux
   *
   * @return Flux of SohWrappers
   */
  Flux<SohWrapper> createStateOfHealthConsumersFlux(Map<String, StationSoh> latestStationSohByStation,
    Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup,
    ReactiveConsumer<StationSoh> stationSohReactiveConsumer,
    ReactiveConsumer<CapabilitySohRollup> capabilitySohRollupReactiveConsumer
  ) {

    return createSohWrapperFlux(
      stationSohReactiveConsumer.getFlux(),
      capabilitySohRollupReactiveConsumer.getFlux(),
      latestStationSohByStation,
      latestCapabilitySohRollupByStationGroup
    );
  }

  /**
   * Internal method to create the main materialized view producer - the one
   * that contains the most recent state-of-health info from upstream.
   *
   * @param sohPackageFlux Flux of SohCorrelations
   * @param stationGroups The StationGroups we are working with, for reference
   *
   * @return The KafkaProducer for the main materialized view
   */
  private KafkaProducer<SystemEvent<UiStationAndStationGroups>> createMaterializedViewProducer(
    Flux<SohPackage> sohPackageFlux,
    List<StationGroup> stationGroups) {

    var materializedViewOutputTopic = STATION_SOH_ANALYSIS_VIEW_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    var uiStationAndStationGroupsFlux = createMaterializedViewFlux(
      sohPackageFlux,
      sohQuietAndUnacknowledgedCacheManager,
      processingConfig,
      stationGroups,
      systemMessageSink
    );

    return KafkaProducer.<SystemEvent<UiStationAndStationGroups>>builder()
      .setDataFlux(uiStationAndStationGroupsFlux
        .map(uiStationAndStationGroups -> SystemEvent.from(SOH_MESSAGE, uiStationAndStationGroups)))
      .setTopic(materializedViewOutputTopic)
      .keyless()
      .setKafkaSender(KafkaSender.<String, SystemEvent<UiStationAndStationGroups>>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Create the "Acknowledged" materialized view KafkaProducer, which publishes
   * the materialized view with only acknowledged state-of-health statuses.
   *
   * @param latestStationSohByStation The latest StationSohs, by station name
   * @param latestCapabilitySohRollupByStationGroup The latest
   * CapabilitySohRollups, by group name
   * @param stationGroups The StationGroups we are working with, for reference
   *
   * @return The Acknowledged Kafka Producer
   */
  KafkaProducer<SystemEvent<UiStationAndStationGroups>> createAcknowledgedMaterializedViewProducer(
    Map<String, StationSoh> latestStationSohByStation,
    Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup,
    List<StationGroup> stationGroups) {

    var acknowledgedMaterializedViewOutputTopic = STATION_SOH_ANALYSIS_VIEW_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    Flux<UiStationAndStationGroups> dataFlux = createAcknowledgedMaterializedViewFlux(
      this.acknowledgedSohStatusChangeReactiveConsumer.getFlux(),
      sohQuietAndUnacknowledgedCacheManager,
      AcknowledgeSohStatusChangeMaterializedViewProcessor.create(
        processingConfig.resolveDisplayParameters(),
        sohQuietAndUnacknowledgedCacheManager,
        latestStationSohByStation,
        latestCapabilitySohRollupByStationGroup,
        systemMessageSink,
        stationGroups
      )
    );

    return KafkaProducer.<SystemEvent<UiStationAndStationGroups>>builder()
      .setDataFlux(dataFlux
        .map(uiStationAndStationGroups -> SystemEvent.from(SOH_MESSAGE, uiStationAndStationGroups)))
      .setTopic(acknowledgedMaterializedViewOutputTopic)
      .keyless()
      .setKafkaSender(KafkaSender.<String, SystemEvent<UiStationAndStationGroups>>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Create the "Quieted" materialized view producer, which publishes the
   * materialized view with only quieted state-of-health statuses.
   *
   * @param latestStationSohByStation The latest StationSohs, by station name
   * @param latestCapabilitySohRollupByStationGroup The latest
   * CapabilitySohRollups, by group name
   * @param stationGroups The StationGroups we are working with, for reference
   *
   * @return KafkaProducer for quieted materialized UI views
   */
  KafkaProducer<SystemEvent<UiStationAndStationGroups>> createQuietedMaterializedViewProducer(
    Map<String, StationSoh> latestStationSohByStation,
    Map<String, CapabilitySohRollup> latestCapabilitySohRollupByStationGroup,
    List<StationGroup> stationGroups
  ) {

    var quietedMaterializedViewOutputTopic = STATION_SOH_ANALYSIS_VIEW_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    var quietedFlux = this.quietedSohStatusChangeUpdateReactiveConsumer.getFlux()
      .doOnTerminate(() -> quietedSohStatusChangeUpdateSink.emitComplete(EmitFailureHandlerUtility.getInstance()))
      .doOnNext(quietedSohStatusChangeUpdate -> quietedSohStatusChangeUpdateSink.emitNext(quietedSohStatusChangeUpdate,
      EmitFailureHandlerUtility.getInstance()))
      .doOnNext(sohQuietAndUnacknowledgedCacheManager::addQuietSohStatusChange)
      .onErrorContinue(
        (throwable, object)
        -> logger.error(
          "Error with quieted status change " + object,
          throwable
        )
      );

    Flux<UiStationAndStationGroups> dataFlux = quietedFlux
      .map(QuietedSohStatusChangeUpdateMaterializedViewProcessor.create(
        processingConfig.resolveDisplayParameters(),
        sohQuietAndUnacknowledgedCacheManager,
        latestStationSohByStation,
        latestCapabilitySohRollupByStationGroup,
        systemMessageSink,
        stationGroups))
      .flatMap(Flux::fromIterable);


    return KafkaProducer.<SystemEvent<UiStationAndStationGroups>>builder()
      .setDataFlux(dataFlux
        .map(uiStationAndStationGroups -> SystemEvent.from(SOH_MESSAGE, uiStationAndStationGroups)))
      .setTopic(quietedMaterializedViewOutputTopic)
      .keyless()
      .setKafkaSender(KafkaSender.<String, SystemEvent<UiStationAndStationGroups>>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Create the relatively simple Quieted producer.
   *
   * @return The KafkaProducer for QuietedSohStatusChangeUpdates
   */
  KafkaProducer<QuietedSohStatusChangeUpdate> createQuietedProducer() {

    var quietedOutputTopic = STATION_SOH_QUIETED_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    return KafkaProducer.<QuietedSohStatusChangeUpdate>builder()
      .setDataFlux(quietedSohStatusChangeUpdateSink.asFlux())
      .setTopic(quietedOutputTopic)
      .setKeyMapper(QuietedSohStatusChangeUpdate::getStationName)
      .setKafkaSender(KafkaSender.<String, QuietedSohStatusChangeUpdate>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Create the relatively simple Unack kafka producer.
   *
   * @return The KafkaProducer for UnacknwoledgedSohStatusChanges
   */
  KafkaProducer<UnacknowledgedSohStatusChange> createUnackProducer() {

    var unackOutputTopic = STATION_SOH_STATUS_CHANGE_OUTPUT_TOPIC_KEY
      .getSystemConfigValue(systemConfig);

    return KafkaProducer.<UnacknowledgedSohStatusChange>builder()
      .setDataFlux(unacknowledgedSohStatusChangeSink.asFlux())
      .setTopic(unackOutputTopic)
      .setKeyMapper(UnacknowledgedSohStatusChange::getStation)
      .setKafkaSender(KafkaSender.<String, UnacknowledgedSohStatusChange>create(ReactiveStationSohAnalysisManager.senderOptions(systemConfig)))
      .build();
  }

  /**
   * Wrap either a CapabilitySohRollup or StationSoh into an instance of this
   * one class. This helps greatly with packaging Capability rollups and station
   * sohs together.
   */
  @AutoOneOf(SohWrapper.SohType.class)
  abstract static class SohWrapper {

    public static SohWrapper ofStationSoh(StationSoh stationSoh) {
      return AutoOneOf_SsamReactiveKafkaUtility_SohWrapper.stationSoh(stationSoh);
    }

    public static SohWrapper ofCapabilitySohRollup(CapabilitySohRollup capabilitySohRollup) {
      return AutoOneOf_SsamReactiveKafkaUtility_SohWrapper.capabilitySohRollup(capabilitySohRollup);
    }

    public abstract SohType getSohType();

    public Instant wrapperTimeStamp() {
      if (this.getSohType() == SohType.STATION_SOH) {
        return stationSoh().getTime();
      } else {
        return capabilitySohRollup().getTime();
      }
    }

    public abstract StationSoh stationSoh();

    public abstract CapabilitySohRollup capabilitySohRollup();

    public enum SohType {
      CAPABILITY_SOH_ROLLUP,
      STATION_SOH
    }
  }

}

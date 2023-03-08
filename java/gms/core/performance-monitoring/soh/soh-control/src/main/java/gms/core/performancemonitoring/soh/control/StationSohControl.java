package gms.core.performancemonitoring.soh.control;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.google.common.util.concurrent.Uninterruptibles;
import gms.core.performancemonitoring.soh.control.StationSohControlConfiguration.ConfigurationPair;
import gms.core.performancemonitoring.soh.control.api.StationSohControlInterface;
import gms.core.performancemonitoring.soh.control.api.StationSohMonitoringResultsFluxPair;
import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.core.performancemonitoring.soh.control.kafka.KafkaSohExtractConsumerFactory;
import gms.core.performancemonitoring.soh.control.kafka.ReactorKafkaSohExtractReceiver;
import gms.core.performancemonitoring.soh.control.kafka.SohExtractReceiver;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.util.ChannelsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.repository.OsdRepositoryFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.frameworks.utilities.SumStatsAccumulator;
import net.logstash.logback.marker.Markers;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.APPLICATION_ID;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC_DEFAULT;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.INPUT_TOPIC;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.INPUT_TOPIC_DEFAULT;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.KAFKA_BOOTSTRAP_SERVERS;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.MONITOR_LOGGING_DEFAULT_OUTPUT_PERIOD;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.MONITOR_LOGGING_FORMAT;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.MONITOR_LOGGING_PERIOD;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.STATION_SOH_OUTPUT_TOPIC;
import static gms.core.performancemonitoring.soh.control.StationSohControlConstants.STATION_SOH_OUTPUT_TOPIC_DEFAULT;

/**
 * StationSohControl is responsible for controlling computation of StationSoh from
 * AcquiredStationSohExtracts.
 */
public class StationSohControl implements StationSohControlInterface {

  // Used as keys for the SumStatsAccumulator
  enum MonitorBenchmarks {
    CALLS_TO_MONITOR,
    NUM_EXTRACTS,
    NUM_STATION_SOH,
    REFRESH_STATION_SOH_TIME,
    STATION_SOH_COMPUTATION_TIME,
    CAPABILITY_SOH_ROLLUP_COMPUTATION_TIME,
  }

  private static final Logger logger = LoggerFactory.getLogger(StationSohControl.class);

  //
  // We use a supplier here, so that the StationSohControlConfiguration object can be created at a time
  // after the StationSohControl object has been created. This is so that creation of the
  // StationSohControlConfiguration object, which involves reaching out to the OSD for a large
  // number of records, does not hold up creation of StationSohControl.
  //
  private final Supplier<StationSohControlConfiguration> stationSohControlConfigurationSupplier;

  private final SystemConfig systemConfig;

  private final KafkaSohExtractConsumerFactory kafkaSohExtractConsumerFactory;

  private final AcquiredSampleTimesByChannel acquiredSampleTimesByChannel;

  private final boolean startAtNextMinute;

  private final OsdRepositoryInterface sohRepository;

  // Used in the monitor method to benchmark various items.
  private final SumStatsAccumulator<MonitorBenchmarks> monitorStatsAccumulator =
    new SumStatsAccumulator<>(new EnumMap<>(
      MonitorBenchmarks.class));

  private final Duration monitorLoggingPeriod;

  private ConfigurationPair configurationPair;

  private SohExtractReceiver sohExtractReceiver;

  private Instant nextMonitorLoggingInstant;

  private volatile boolean started = false;

  /**
   * Constructor which accepts a receiver and senders, so that they can be moocked for unit
   * testing.
   *
   * @param stationSohControlConfigurationSupplier Supplier for a StationSohControlConfiguration
   * object.
   * @param systemConfig the system configuration, which may not be null.
   * @param sohRepository the {@link OsdRepositoryInterface} used in creating the
   * stationSohControlConfiguration and for retrieving the latest acquisition time of channels.
   * @param sohExtractReceiver if non-null, a consumer of extracts. This is provided mainly for unit
   * testing.
   * @param preconfiguredKafkaSender if non-null, a sender for the StationSoh messages. This is
   * provided mainly for unit testing. It will normally be non-null when the sohExtractReceiver is
   * non-null.
   */
  StationSohControl(
    Supplier<StationSohControlConfiguration> stationSohControlConfigurationSupplier,
    SystemConfig systemConfig,
    OsdRepositoryInterface sohRepository,
    SohExtractReceiver sohExtractReceiver,
    KafkaSender<String, String> preconfiguredKafkaSender) {

    this.startAtNextMinute = false;

    this.stationSohControlConfigurationSupplier = stationSohControlConfigurationSupplier;

    this.systemConfig = systemConfig;

    this.sohRepository = sohRepository;

    this.sohExtractReceiver = sohExtractReceiver;
    this.kafkaSohExtractConsumerFactory = new KafkaSohExtractConsumerFactory(
      preconfiguredKafkaSender,
      getSystemConfig(systemConfig, STATION_SOH_OUTPUT_TOPIC,
        STATION_SOH_OUTPUT_TOPIC_DEFAULT),
      getSystemConfig(systemConfig,
        CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC, CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC_DEFAULT),
      this::monitor
    );

    this.acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();

    // Use a systemConfig parameter for now, but we might consider adding this
    // to the SohControlDefinition.
    Duration tentativMonitorLoggingPeriod = null;
    String monitorLogginPeriodStr = getSystemConfig(systemConfig, MONITOR_LOGGING_PERIOD,
      MONITOR_LOGGING_DEFAULT_OUTPUT_PERIOD.toString());
    if (monitorLogginPeriodStr != null) {
      try {
        tentativMonitorLoggingPeriod = Duration.parse(monitorLogginPeriodStr);
      } catch (DateTimeParseException pe) {
        logger.error("Monitor Logging Period from system config is not a valid duration");
      }
    }

    this.monitorLoggingPeriod = tentativMonitorLoggingPeriod != null ?
      tentativMonitorLoggingPeriod : MONITOR_LOGGING_DEFAULT_OUTPUT_PERIOD;

  }

  /**
   * Constructor which only takes configuration. Use this for production - it creates its own
   * receiver and senders.
   *
   * @param stationSohControlConfigurationSupplier Supplier for a StationSohControlConfiguration
   * object.
   * @param systemConfig system configuration
   */
  StationSohControl(
    Supplier<StationSohControlConfiguration> stationSohControlConfigurationSupplier,
    SystemConfig systemConfig,
    OsdRepositoryInterface sohRepository) {

    this.sohRepository = sohRepository;

    this.startAtNextMinute = true;

    this.stationSohControlConfigurationSupplier = stationSohControlConfigurationSupplier;

    this.systemConfig = systemConfig;

    this.kafkaSohExtractConsumerFactory = new KafkaSohExtractConsumerFactory(
      KafkaSender.create(SenderOptions.create(senderProperties())),
      getSystemConfig(systemConfig, STATION_SOH_OUTPUT_TOPIC,
        STATION_SOH_OUTPUT_TOPIC_DEFAULT),
      getSystemConfig(systemConfig,
        CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC, CAPABILITY_SOH_ROLLUP_OUTPUT_TOPIC_DEFAULT),
      this::monitor
    );

    this.acquiredSampleTimesByChannel = new AcquiredSampleTimesByChannel();

    // Use a systemConfig parameter for now, but we might consider adding this
    // to the SohControlDefinition.
    Duration dur = null;
    String s = getSystemConfig(systemConfig, MONITOR_LOGGING_PERIOD,
      MONITOR_LOGGING_DEFAULT_OUTPUT_PERIOD.toString());
    if (s != null) {
      try {
        dur = Duration.parse(s);
      } catch (DateTimeParseException pe) {
        logger.error("Monitor Logging Period from system config is not a valid duration");
      }
    }

    this.monitorLoggingPeriod = dur != null ? dur : MONITOR_LOGGING_DEFAULT_OUTPUT_PERIOD;

  }

  /**
   * Factory method for {@code StationSohControl}
   *
   * @param controlContext a control context used to obtain configuration info. This must not be
   * null.
   * @return a new instance of {@code StationSohControl}
   */
  public static StationSohControl create(ControlContext controlContext) {

    checkNotNull(controlContext, "ControlContext Cannot be null");

    var systemConfig = controlContext.getSystemConfig();

    var sohRepositoryInterface =
      OsdRepositoryFactory.createOsdRepository(controlContext.getSystemConfig());

    var configurationSupplier = new Supplier<StationSohControlConfiguration>() {

      private StationSohControlConfiguration stationSohControlConfiguration;

      @Override
      public StationSohControlConfiguration get() {

        if (Objects.isNull(stationSohControlConfiguration)) {
          stationSohControlConfiguration = StationSohControlConfiguration.create(
            controlContext.getProcessingConfigurationConsumerUtility(),
            sohRepositoryInterface);
        }

        return stationSohControlConfiguration;
      }
    };

    return new StationSohControl(
      configurationSupplier,
      systemConfig,
      sohRepositoryInterface);

  }

  public boolean hasNonEmptyConfiguration() {
    return stationSohControlConfigurationSupplier.get().hasNonEmptyConfiguration();
  }

  /**
   * Return the system configuration used to configure this instance
   *
   * @return SystemConfig
   */
  public SystemConfig getSystemConfig() {
    return systemConfig;
  }

  List<AcquiredStationSohExtract> restoreCache() {
    var cachePullTime = Instant.now();
    logger.info("Starting cache population at {}", cachePullTime);

    var stationSohDefinitions =
      this.configurationPair.getStationSohMonitoringDefinition().getStationSohDefinitions();

    var stationDurations = getStationCacheDurations(stationSohDefinitions);

    Map<String, RangeMap<Instant, AcquiredChannelEnvironmentIssueBoolean>> aceiByChannelAndTimeRange
      = new HashMap<>();

    stationSohDefinitions.stream()
      .map(definition ->
        ChannelsTimeRangeRequest.create(
          definition.getChannelSohDefinitions().stream()
            .map(ChannelSohDefinition::getChannelName)
            .collect(Collectors.toList()),
          cachePullTime.minus(stationDurations.get(definition.getStationName())),
          cachePullTime
        ))
      .map(sohRepository::findBooleanAceiByChannelsAndTimeRange)
      .forEach(stationAceiList ->
        stationAceiList.forEach(acei -> aceiByChannelAndTimeRange
          .computeIfAbsent(
            acei.getChannelName(),
            channelName -> TreeRangeMap.create()
          ).put(Range.closed(acei.getStartTime(), acei.getEndTime()), acei)
        )
      );

    var channelsToQuery = stationSohDefinitions.stream()
      .flatMap(stationSohDefinition -> stationSohDefinition.getChannelSohDefinitions().stream())
      .map(ChannelSohDefinition::getChannelName)
      .distinct()
      .collect(Collectors.toList());

    logger.info("Starting off with {} channels to query", channelsToQuery.size());

    var asseList = stationSohDefinitions.stream()
      .map(definition ->
        StationTimeRangeRequest.create(
          definition.getStationName(),
          cachePullTime.minus(stationDurations.get(definition.getStationName())),
          cachePullTime
        ))
      .map(sohRepository::retrieveRawStationDataFrameMetadataByStationAndTime)
      .flatMap(List::stream)
      .map(rsdfMetadata -> {

        rsdfMetadata.getChannelNames()
          .forEach(channelsToQuery::remove);

        // Not using var here so that this will implicitly cast AcquiredChannelEnvironmentIssueBoolean
        // to AcquiredChannelEnvironmentIssue<?> for the sake of AcquiredStationSohExtract.create
        List<AcquiredChannelEnvironmentIssue<?>> aceisForAsse = rsdfMetadata.getChannelNames()
          .stream()
          .map(aceiByChannelAndTimeRange::get)
          .filter(Objects::nonNull)
          .map(rangeMap -> rangeMap.subRangeMap(
            Range.closed(
              rsdfMetadata.getPayloadStartTime(),
              rsdfMetadata.getPayloadEndTime())))
          .map(RangeMap::asMapOfRanges)
          .map(Map::values)
          .flatMap(Collection::stream)
          .collect(Collectors.toList());

        return AcquiredStationSohExtract.create(
          List.of(rsdfMetadata),
          aceisForAsse
        );
      }).collect(Collectors.toList());

    // The cache for timeliness is only initially loaded with the stations it does not have at first
    // when timeliness is calculated the cache is updated with the remaining
    // extracts that flow through the system
    if (!channelsToQuery.isEmpty()) {
      logger.info("Pulling latest sample times for {} channels", channelsToQuery.size());
      // This uses a triply nested query, so small batch sizes
      Map<String, Instant> latestSampleTimeByChannel = Lists.partition(channelsToQuery, 10)
        .parallelStream()
        .map(sohRepository::retrieveLatestSampleTimeByChannel)
        .map(Map::entrySet)
        .flatMap(Set::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      this.acquiredSampleTimesByChannel.setLatestChannelToEndTime(latestSampleTimeByChannel);
    }

    var completionTime = Instant.now();
    var cachePullExecutionLength = Duration.between(cachePullTime, completionTime);
    logger.info("Finished cache retrieval at {} taking {}; retrieved {} extracts", completionTime,
      cachePullExecutionLength, asseList.size());

    return asseList;
  }

  /**
   * Asynchronously Receives AcquiredStationSohExtract objects and produces the StationSoh objects
   * from and to the publish-subscribe topics. Consider this the start method for the control.
   * Generally, this method will be called once during an application run. If called multiple times,
   * {@code shutdownKafkaThreads()} must be called between calls to this method.
   *
   * @throws IllegalStateException if already called and {@code shutdownKafkaThreads()} has not been
   * called.
   */
  public synchronized void start() {

    if (started) {
      throw new IllegalStateException("Already acquiring and publishing");
    }

    finishInitialization();

    started = true;

    // In production, delay until the top of the minute before continuing.
    if (this.startAtNextMinute) {
      Uninterruptibles.sleepUninterruptibly(sleepToNextMinute(), TimeUnit.SECONDS);
    }

    // Sets the start time in the accumulator to now.
    monitorStatsAccumulator.reset();
    // This must be set to prevent a NullPointerException in the monitor method.
    nextMonitorLoggingInstant = monitorStatsAccumulator.getStartTime()
      .plus(monitorLoggingPeriod);

    // A shutdown hook to gracefully shutdown both workers and the thread pool
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

    // Finally, kick off the extract receiver.
    sohExtractReceiver.receive(
      this.configurationPair
        .getSohControlDefinition()
        .getReprocessingPeriod(),
      this.kafkaSohExtractConsumerFactory.getConsumer(),
      restoreCache()
    );

    if (logger.isInfoEnabled()) {
      logger.info("Started up with {} processors", Runtime.getRuntime().availableProcessors());
    }
  }

  /**
   * Attempts to shutdown all kafka producer and consumer threads. This method can be thought of as
   * the shutdown or stop method.
   */
  public synchronized void stop() {
    if (started && sohExtractReceiver.isReceiving()) {
      sohExtractReceiver.stop();
    }
  }

  /**
   * This method contains the business logic for the control. It processes the contents of the
   * AcquiredStationSohExtracts to calculate StationSoh. It also uses a preconfigured
   * StationSohDefinition provided by StationSohControlConfiguration and any additional input
   * processing results (e.g. RawStationDataFrameMetadata, AcquiredChannelEnvironmentIssues,
   * ChannelSegments, etc.) necessary to compute the SOH Monitor Values. It publishes the computed
   * StationSohResult objects to a publish-subscribe topic.
   */
  @Override
  public StationSohMonitoringResultsFluxPair monitor(
    Set<AcquiredStationSohExtract> acquiredStationSohExtracts) {

    //string constant used in structured logger for extracts
    final var extracts = "extracts";

    if (logger.isDebugEnabled()) {
      logger.debug("monitor called with {} extracts", acquiredStationSohExtracts.size());
    }

    var methodStartMs = System.currentTimeMillis();

    if (acquiredStationSohExtracts.isEmpty()) {
      logger.info("Monitor received no extracts.");
    }

    try {

      monitorStatsAccumulator.addValue(
        MonitorBenchmarks.NUM_EXTRACTS,
        acquiredStationSohExtracts.size());

      // Get the configuration from the AtomicReference and use it for the duration of the
      // method, since it's possible the ref may be updated by another thread before the
      // completion of the method.

      var stationSohDefinitions =
        configurationPair.getStationSohMonitoringDefinition()
          .getStationSohDefinitions();

      if (stationSohDefinitions == null || stationSohDefinitions.isEmpty()) {
        logger.warn("Not configured to monitor any stations");
        return new StationSohMonitoringResultsFluxPair(
          Flux.empty(),
          Flux.empty()
        );
      }

      //
      // Use RollupFluxBuilder to build up our StationSoh Flux and CapabilitySohRollup Flux
      //
      // TODO: git rid of rollupStationSohTimeTolerance config
      var rollupFluxBuilder = new RollupFluxBuilder(
        acquiredStationSohExtracts,
        stationSohDefinitions,
        configurationPair.getStationSohMonitoringDefinition()
          .getCapabilitySohRollupDefinitions(),
        acquiredSampleTimesByChannel
      );

      var startMs = System.currentTimeMillis();

      var stationSohCount = new AtomicInteger(0);

      var capabilitySohCount = new AtomicInteger(0);

      return new StationSohMonitoringResultsFluxPair(
        rollupFluxBuilder.getStationSohFlux()
          .doOnNext(stationSoh -> {
            stationSohCount.incrementAndGet();
            logger.debug("Emitted Station soh for station {}", stationSoh.getStationName());
          })
          .onErrorContinue((t, o) -> {

            // Dont necessarily  want to print out the entire, potentially huge object.
            // But some of it could still be useful to see.
            var objectString = o.toString()
              .substring(0, Math.min(100, o.toString().length()));

            // Collect the stack trace into a single string
            // Note that structured logging may be doing this already
            var stackTrace = Arrays.stream(t.getStackTrace()).reduce("",
              (prev, current) -> prev + System.lineSeparator() + current.toString(),
              String::concat);

            String message = "ERROR processing object : " + System.lineSeparator()
              + objectString + System.lineSeparator()
              + stackTrace;

            logger.error(message);
          })
          .doOnComplete(
            () -> {

              double computationMs = (double) System.currentTimeMillis() - startMs;
              // Keep track of the avg amount of time to compute station soh objects.
              var tempValue = computationMs / stationSohCount.get();

              //structured log to keep track of the stationSoh calculation Time, # of extracts
              logger.info(Markers.aggregate(
                Markers.append("StationSohCompMsec", tempValue),
                Markers.append(extracts, acquiredStationSohExtracts.size())),
                "COMPLETED stationSoh flux!");

              monitorStatsAccumulator.addValue(
                MonitorBenchmarks.STATION_SOH_COMPUTATION_TIME,
                tempValue
              );
            }
          ),

        rollupFluxBuilder.getCapabilitySohRollupFlux()
          .doOnNext(capabilitySohRollup -> {
            capabilitySohCount.incrementAndGet();
            logger.debug("Emitted Capability for station group{}",
              capabilitySohRollup.getForStationGroup());
          })
          .onErrorContinue((t, o) -> {
            String message = "Error emitting CapabilitySohRollup " + o;
            logger.error(
              message,
              t
            );
          })
          .doOnComplete(
            () -> {

              double computationMs = (double) System.currentTimeMillis() - startMs;
              // Keep track of the avg amount of time to compute station soh objects.
              var tempValue = computationMs / capabilitySohCount.get();

              //structured log the total Capability SohRollup Calculation Time, # of extracts
              logger.info(Markers.aggregate(
                Markers.append("CapSohRollupCompMsec", tempValue),
                Markers.append(extracts, acquiredStationSohExtracts.size())),
                "COMPLETED capability flux!");

              monitorStatsAccumulator.addValue(
                MonitorBenchmarks.CAPABILITY_SOH_ROLLUP_COMPUTATION_TIME,
                tempValue
              );
            }
          )
      );

    } finally {

      // For this benchmark, keep track of the number of seconds to complete the call.
      //
      monitorStatsAccumulator.addValue(
        MonitorBenchmarks.CALLS_TO_MONITOR,
        ((double) (System.currentTimeMillis() - methodStartMs)) / 1000.0);

      // Handle periodic logging.
      handleMonitorLogging();
    }
  }

  /**
   * Get a value from the system config, returning a default value if not defined.
   */
  private static String getSystemConfig(SystemConfig systemConfig, String key,
    String defaultValue) {
    String value = defaultValue;
    try {
      value = systemConfig.getValue(key);
    } catch (MissingResourceException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("{} is not defined in SystemConfig, using default value: {}",
          key, defaultValue);
      }
    }
    return value;
  }

  /**
   * Determine the time between now and the next minute and return the difference in seconds.
   *
   * @return the duration in seconds between now and the next minute.
   */
  private static long sleepToNextMinute() {
    var now = Instant.now();
    var nextMinute = now.truncatedTo(ChronoUnit.MINUTES).plus(1, ChronoUnit.MINUTES);
    var sleepDuration = Duration.between(now, nextMinute);

    logger.info("now {} : nextMinute {}", now, nextMinute);
    logger.info("Waiting {} second(s) before Kafka consumer connection ...", sleepDuration.getSeconds());

    return sleepDuration.getSeconds();
  }

  /**
   * At fairly fixed intervals (~ 10 minutes) outputs to the log some statistics on calculations
   * done by the monitor method.
   */
  private void handleMonitorLogging() {

    var now = Instant.now();

    if (now.isAfter(nextMonitorLoggingInstant)) {

      // Only do these calculations if the log level is INFO or something more sensitive.
      //
      if (logger.isInfoEnabled()) {

        var duration = Duration.between(monitorStatsAccumulator.getStartTime(), now);
        var msec = duration.toMillis();
        var minutes = msec / 60_000L;
        var seconds = (msec % 60_000L) / 1000;

        var minExtracts = (int) monitorStatsAccumulator.getMin(MonitorBenchmarks.NUM_EXTRACTS);
        var avgExtracts = monitorStatsAccumulator.getMean(MonitorBenchmarks.NUM_EXTRACTS);
        var maxExtracts = (int) monitorStatsAccumulator.getMax(MonitorBenchmarks.NUM_EXTRACTS);

        // The number of calls to monitor() and the min, avg, max seconds per call.
        var numCallsToMonitor = monitorStatsAccumulator.getN(
          MonitorBenchmarks.CALLS_TO_MONITOR);
        var minSecondsPerCallToMonitor = monitorStatsAccumulator.getMin(
          MonitorBenchmarks.CALLS_TO_MONITOR
        );
        var avgSecondsPerCallToMonitor = monitorStatsAccumulator.getMean(
          MonitorBenchmarks.CALLS_TO_MONITOR
        );
        var maxSecondsPerCallToMonitor = monitorStatsAccumulator.getMax(
          MonitorBenchmarks.CALLS_TO_MONITOR
        );

        // The min, avg, max milliseconds taken to compute each StationSoh
        var minStationSohCompMsec = monitorStatsAccumulator.getMin(
          MonitorBenchmarks.STATION_SOH_COMPUTATION_TIME
        );
        var avgStationSohCompMsec = monitorStatsAccumulator.getMean(
          MonitorBenchmarks.STATION_SOH_COMPUTATION_TIME
        );
        var maxStationSohCompMsec = monitorStatsAccumulator.getMax(
          MonitorBenchmarks.STATION_SOH_COMPUTATION_TIME
        );

        // The min, avg, max milliseconds taken to compute each CapabilitySohRollup
        var minCapSohRollupCompMsec = monitorStatsAccumulator.getMin(
          MonitorBenchmarks.CAPABILITY_SOH_ROLLUP_COMPUTATION_TIME
        );
        var avgCapSohRollupCompMsec = monitorStatsAccumulator.getMean(
          MonitorBenchmarks.CAPABILITY_SOH_ROLLUP_COMPUTATION_TIME
        );
        var maxCapSohRollupCompMsec = monitorStatsAccumulator.getMax(
          MonitorBenchmarks.CAPABILITY_SOH_ROLLUP_COMPUTATION_TIME
        );

        logger.info(String.format(MONITOR_LOGGING_FORMAT,
          minutes,
          seconds,
          numCallsToMonitor,
          minSecondsPerCallToMonitor,
          avgSecondsPerCallToMonitor,
          maxSecondsPerCallToMonitor,
          minExtracts,
          avgExtracts,
          maxExtracts,
          minStationSohCompMsec,
          avgStationSohCompMsec,
          maxStationSohCompMsec,
          minCapSohRollupCompMsec,
          avgCapSohRollupCompMsec,
          maxCapSohRollupCompMsec
        ));
      }

      monitorStatsAccumulator.reset();
      nextMonitorLoggingInstant = monitorStatsAccumulator.getStartTime()
        .plus(monitorLoggingPeriod);
    }
  }

  private void finishInitialization() {

    // This is the main piece that could hold things up, because it reaches out to the OSD
    // to get a large number of records (stations)
    this.configurationPair =
      stationSohControlConfigurationSupplier.get().getInitialConfigurationPair();

    //
    // If sohExtractReceiver is null by the time this method is called, it means we need to
    // initialize it using production configuration.
    //
    if (Objects.isNull(this.sohExtractReceiver)) {
      this.sohExtractReceiver =
        new ReactorKafkaSohExtractReceiver(
          systemConfig.getValue(KAFKA_BOOTSTRAP_SERVERS),
          getSystemConfig(systemConfig, INPUT_TOPIC, INPUT_TOPIC_DEFAULT),
          systemConfig.getValue(APPLICATION_ID),
          getStationCacheDurations(
            configurationPair.getStationSohMonitoringDefinition().getStationSohDefinitions()
          )
        );
    }
  }

  static Map<String, Duration> getStationCacheDurations(
    Set<StationSohDefinition> stationSohDefinitionSet) {

    return stationSohDefinitionSet.stream()
      .map(
        stationSohDefinition -> {

          var maxCalculationInterval = stationSohDefinition.getTimeWindowBySohMonitorType()
            .values().stream()
            .map(TimeWindowDefinition::getCalculationInterval)
            .max(Comparator.naturalOrder())
            .orElse(Duration.ZERO);

          var maxBackoffDuration = stationSohDefinition.getTimeWindowBySohMonitorType().values()
            .stream()
            .map(TimeWindowDefinition::getBackOffDuration)
            .max(Comparator.naturalOrder())
            .orElse(Duration.ZERO);

          var cacheDuration = maxCalculationInterval
            .plus(maxBackoffDuration)
            // Add a "safety zone" of one minute.
            .plus(Duration.ofMinutes(1));

          return Map.entry(
            stationSohDefinition.getStationName(),
            cacheDuration
          );
        }
      ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private Properties senderProperties() {
    var properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      systemConfig.getValue(KAFKA_BOOTSTRAP_SERVERS));
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties
      .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // By default, a producer doesn't wait for an acknowledgement from kafka when it sends
    // a message to a topic. Setting it to "1" means that it will wait for at least one kafka
    // node to acknowledge. The safest is "all", but that makes sending a little slower.
    properties.put(ProducerConfig.ACKS_CONFIG, "1");
    return properties;
  }
}

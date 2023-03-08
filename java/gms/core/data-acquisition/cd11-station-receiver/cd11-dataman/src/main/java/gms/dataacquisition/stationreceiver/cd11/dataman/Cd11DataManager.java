package gms.dataacquisition.stationreceiver.cd11.dataman;

import com.google.common.annotations.VisibleForTesting;
import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParameters;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapList;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapListUtility;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapManagementBundle;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.handlers.ReactorCd11FrameHandler;
import gms.dataacquisition.stationreceiver.cd11.dataman.processors.MalformedFrameKafkaProcessor;
import gms.dataacquisition.stationreceiver.cd11.dataman.processors.SequencedRsdfKafkaProcessor;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiSerializer;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.utilities.kafka.KafkaConfiguration;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactor.util.function.Tuple2;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static net.logstash.logback.marker.Markers.append;

/**
 * High level CD 1.1 Data management application class. Responsible for managing tcp connections,
 * configuration of connection handling, kafka connections and messaging, and lifecycle management
 * of all of the above.
 */
public class Cd11DataManager {

  public static final String STATION_LOGGING_KEY = "station";
  private static final String GAP_LIST_STORAGE_FILE_EXTENSION = ".json";
  private static final Logger logger = LoggerFactory.getLogger(Cd11DataManager.class);

  private final DataManConfig dataManConfig;
  private final DataFrameReceiverConfiguration receiverConfig;
  private final KafkaConfiguration kafkaConfiguration;

  // ---Fields Set During Initialization---
  // Reactor Sinks use to emit frame data from our server handlers to be sent as kafka messages
  private Sinks.Many<Tuple2<RawStationDataFrame, Long>> sequencedRsdfSink;
  private Sinks.Many<MalformedFrame> malformedFrameSink;

  //Station connection servers and their related frame handlers
  private List<ReactorCd11FrameHandler> handlers;
  private Map<String, TcpServer> serversByStation;
  private Map<String, Cd11GapList> cd11GapListsByStation;
  private SequencedRsdfKafkaProcessor rsdfKafkaProcessor;
  private MalformedFrameKafkaProcessor malformedFrameKafkaProcessor;

  private Cd11DataManager(DataManConfig dataManConfig, DataFrameReceiverConfiguration receiverConfig,
    KafkaConfiguration kafkaConfiguration) {
    this.dataManConfig = dataManConfig;
    this.receiverConfig = receiverConfig;
    this.kafkaConfiguration = kafkaConfiguration;
  }

  public static Cd11DataManager create(DataManConfig dataManConfig,
    DataFrameReceiverConfiguration dataFrameReceiverConfiguration, KafkaConfiguration kafkaConfiguration) {
    return new Cd11DataManager(dataManConfig, dataFrameReceiverConfiguration, kafkaConfiguration);
  }

  /**
   * Initialization of manager state via reading configuration. Sets up kafka senders, station data
   * sinks, frame processors, and configures tcp connections to be ready for binding.
   */
  public void initialize() {
    cd11GapListsByStation = new HashMap<>();
    handlers = new ArrayList<>();
    //Max Value buffer sizes translate to an unbounded backpressure Queue
    sequencedRsdfSink = Sinks.many().multicast().onBackpressureBuffer(dataManConfig.getBackpressureBufferSize(), false);
    malformedFrameSink = Sinks.many().multicast().onBackpressureBuffer(dataManConfig.getBackpressureBufferSize(),
      false);

    serversByStation = dataManConfig.cd11DataConsumerParameters()
      .filter(Cd11DataConsumerParameters::isAcquired)
      .collect(toMap(Cd11DataConsumerParameters::getStationName, this::initializeDataConsumerServer));

    initializeRsdfProcessing();
    initializeMalformedProcessing();
  }

  private TcpServer initializeDataConsumerServer(Cd11DataConsumerParameters consumerParameters) {
    int stationPort = consumerParameters.getPort();
    String stationName = consumerParameters.getStationName();
    var gapListUtility = Cd11GapListUtility.create(dataManConfig.getGapListStoragePath(),
      GAP_LIST_STORAGE_FILE_EXTENSION);
    var stationGapList = gapListUtility.loadGapState(stationName);
    cd11GapListsByStation.put(stationName, stationGapList);
    var gapListBundle = Cd11GapManagementBundle.builder()
      .setCd11GapList(stationGapList)
      .setCd11GapListUtility(gapListUtility)
      .setGapListStoragePath(dataManConfig.getGapListStoragePath())
      .setGapExpirationDays(dataManConfig.getGapExpirationDays())
      .setGapStorageIntervalMinutes(dataManConfig.getGapStorageIntervalMinutes())
      .build();
    var handler = ReactorCd11FrameHandler.create(stationName, stationPort, receiverConfig, sequencedRsdfSink,
      malformedFrameSink, gapListBundle);
    handlers.add(handler);

    return TcpServer.create().port(stationPort)
      .doOnBound(server ->
        logger.debug("Server for station {} bound successfully on port {}",
          StructuredArguments.value(STATION_LOGGING_KEY, stationName),
          server.port()))
      .doOnConnection(connection -> logConnection(connection, stationName, stationPort))
      .handle(handler);
  }

  private void logConnection(Connection connection, String stationName, int stationPort) {
    var stationNameStructArg = StructuredArguments.value(STATION_LOGGING_KEY, stationName);
    logger.info("Data Manager connection established for station {} on port {}",
      stationNameStructArg, stationPort);
    connection.onDispose(() -> logger.info("Data Manager connection for station {} closed on port {}",
      stationNameStructArg, stationPort));
  }

  private void initializeRsdfProcessing() {
    String rsdfTopic = kafkaConfiguration.getTopic(KafkaConfiguration.Topic.RSDF).orElseThrow(
      () -> new IllegalStateException("Error Creating Cd11DataManager: No topic info found for RSDF"));

    SenderOptions<String, RawStationDataFrame> senderOptions = kafkaConfiguration
      .<RawStationDataFrame>getSenderOptions(new CoiSerializer<>())
      .scheduler(Schedulers.boundedElastic())
      .stopOnError(false);

    KafkaSender<String, RawStationDataFrame> rsdfSender = KafkaSender.create(
      senderOptions);

    var configuration = new SequencedRsdfKafkaProcessor.Configuration(rsdfTopic,
      dataManConfig.getBackpressureBufferSize(), retryWithBackoff("Error persisting gap state"));

    rsdfKafkaProcessor = new SequencedRsdfKafkaProcessor(rsdfSender, cd11GapListsByStation, configuration);
  }

  private void initializeMalformedProcessing() {
    String malformedFrameTopic = kafkaConfiguration.getTopic(KafkaConfiguration.Topic.MALFORMED).orElseThrow(
      () -> new IllegalStateException("Error Creating Cd11DataManager: No topic info found for Malformed Frames"));

    SenderOptions<String, MalformedFrame> malformedFrameSenderOptions = kafkaConfiguration
      .<MalformedFrame>getSenderOptions(new CoiSerializer<>())
      .scheduler(Schedulers.boundedElastic())
      .stopOnError(false)
      .producerProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip")
      .producerProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 10485760);

    KafkaSender<String, MalformedFrame> malformedFrameSender = KafkaSender.create(malformedFrameSenderOptions);

    var configuration = new MalformedFrameKafkaProcessor.Configuration(malformedFrameTopic,
      dataManConfig.getBackpressureBufferSize());

    malformedFrameKafkaProcessor = new MalformedFrameKafkaProcessor(malformedFrameSender, configuration);
  }

  private Retry retryWithBackoff(String message) {
    return dataManConfig.getRetryPolicy()
      .doBeforeRetry(retry -> logger.warn(message + ", resubscribing... [count:{}]",
        retry.totalRetriesInARow(), retry.failure()));
  }

  /**
   * Starts the Data manager, subscribing all data sinks to the kafka senders and binds the tcp
   * connections to their ports, making them available for connection.
   */
  public Mono<Void> startFrameSending() {

    Mono<Void> startRsdfSending = rsdfKafkaProcessor.process(sequencedRsdfSink.asFlux())
      .buffer(Duration.ofSeconds(30))
      .doOnNext(this::logRsdfSendResults)
      .doOnNext(this::logRsdfGapResults)
      .then();

    Mono<Void> startMalformedSending = malformedFrameKafkaProcessor.process(malformedFrameSink.asFlux())
      .doOnNext(this::logMalformedFrameProcessingResult)
      .then();

    return Mono.firstWithSignal(startRsdfSending, startMalformedSending)
      .retryWhen(retryWithBackoff("Error encountered in frame sending"));
  }

  private void logRsdfSendResults(List<SequencedRsdfKafkaProcessor.Result> results) {
    var sendResults = results.stream()
      .collect(partitioningBy(SequencedRsdfKafkaProcessor.Result::isRecordSent));

    List<SequencedRsdfKafkaProcessor.Result> successfulResults = sendResults.get(true);
    if (!successfulResults.isEmpty()) {
      List<String> successfulStations = successfulResults.stream()
        .map(SequencedRsdfKafkaProcessor.Result::getStationName)
        .distinct()
        .collect(toList());

      logger.info(append(STATION_LOGGING_KEY, successfulStations),
        "Published {} Data Frames", successfulResults.size());
    }

    List<SequencedRsdfKafkaProcessor.Result> failedResults = sendResults.get(false);
    if (!failedResults.isEmpty()) {
      List<String> failedStations = failedResults.stream()
        .map(SequencedRsdfKafkaProcessor.Result::getStationName)
        .distinct()
        .collect(toList());

      logger.warn(append(STATION_LOGGING_KEY, failedStations),
        "Failed to publish {} Data Frames", failedResults);
    }
  }

  private void logRsdfGapResults(List<SequencedRsdfKafkaProcessor.Result> results) {
    var gapResults = results.stream()
      .collect(partitioningBy(SequencedRsdfKafkaProcessor.Result::isGapProcessed));


    var gapsProcessed = gapResults.get(true);
    if (!gapsProcessed.isEmpty()) {
      var stationsProcessed = gapsProcessed.stream()
        .map(SequencedRsdfKafkaProcessor.Result::getStationName)
        .distinct()
        .collect(toList());

      logger.debug(append(STATION_LOGGING_KEY, stationsProcessed), "{} Gaps processed", gapsProcessed.size());
    }

    var gapsNotProcessed = gapResults.get(false);
    if (!gapsNotProcessed.isEmpty()) {
      var stationsNotProcessed = gapsNotProcessed.stream()
        .map(SequencedRsdfKafkaProcessor.Result::getStationName)
        .distinct()
        .collect(toList());

      logger.debug(append(STATION_LOGGING_KEY, stationsNotProcessed), "{} Gaps not processed",
        gapsNotProcessed.size());
    }
  }

  private void logMalformedFrameProcessingResult(MalformedFrameKafkaProcessor.Result result) {
    if (result.isRecordSent()) {
      logger.debug("Published Malformed Frame for station {}",
        StructuredArguments.value(STATION_LOGGING_KEY, result.getStationName()));
    }
  }

  /**
   * Executes {@link TcpServer#bind()} on all servers and their ports
   *
   * @return Single mono representing the state of binding for every server
   */
  public Mono<List<DisposableServer>> bindAllPorts() {
    var bindMonos = serversByStation.entrySet().stream()
      .map(entry -> bind(entry.getKey(), entry.getValue()));

    return Flux.fromStream(bindMonos)
      .flatMap(Function.identity())
      .collect(toList());
  }

  private Mono<? extends DisposableServer> bind(String stationName, TcpServer tcpServer) {
    return tcpServer.bind()
      .doOnError(e -> logger.error("Error binding server for station {}",
        StructuredArguments.value(STATION_LOGGING_KEY, stationName), e))
      .retryWhen(dataManConfig.getRetryPolicy());
  }

  public void shutdownKafkaSending() {
    logger.info("Shutting down Kafka Processors...");
    rsdfKafkaProcessor.shutdown();
    malformedFrameKafkaProcessor.shutdown();
  }

  public void shutdownFrameHandling() {
    logger.info("Shutting down Frame Handlers...");
    handlers.forEach(ReactorCd11FrameHandler::shutdown);
    handlers.clear();
  }

  @VisibleForTesting
  void initialize(Sinks.Many<Tuple2<RawStationDataFrame, Long>> sequencedRsdfSink,
    Sinks.Many<MalformedFrame> malformedFrameSink, SequencedRsdfKafkaProcessor rsdfKafkaProcessor,
    MalformedFrameKafkaProcessor malformedFrameKafkaProcessor) {
    this.sequencedRsdfSink = sequencedRsdfSink;
    this.malformedFrameSink = malformedFrameSink;
    this.rsdfKafkaProcessor = rsdfKafkaProcessor;
    this.malformedFrameKafkaProcessor = malformedFrameKafkaProcessor;
  }
}

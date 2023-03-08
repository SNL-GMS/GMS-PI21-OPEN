package gms.dataacquisition.stationreceiver.cd11.dataman.handlers;

import com.google.common.annotations.VisibleForTesting;
import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapList;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapListUtility;
import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapManagementBundle;
import gms.dataacquisition.stationreceiver.cd11.common.reactor.Cd11CompositeFrameHandler;
import gms.dataacquisition.stationreceiver.cd11.common.reactor.Cd11FrameHandler;
import gms.dataacquisition.stationreceiver.cd11.common.reactor.netty.Cd11Connection;
import gms.dataacquisition.stationreceiver.cd11.parser.Cd11RawStationDataFrameUtility;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.utilities.logging.CleanStructuredArgument;
import gms.shared.utilities.logging.StructuredLoggingWrapper;
import gms.shared.utilities.reactor.EmitFailureHandlerUtility;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Empty;
import reactor.core.publisher.Sinks.Many;
import reactor.core.scheduler.Schedulers;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities.asPayloadType;
import static java.lang.String.format;

/**
 * High level class responsible for representing proper CD1.1 protocol behavior for managing a
 * connection and the sending and receiving of different types of CD1.1 frames.
 */
public class ReactorCd11FrameHandler implements BiFunction<NettyInbound, NettyOutbound, Mono<Void>> {
  public static final String STATION_LOGGING_KEY = "station";
  public static final String SEQUENCE_LOGGING_KEY = "sequence";
  public static final String STATION_PORT_KEY = "port";
  public static final int PERIODIC_MAX_ATTEMPTS = 10;
  public static final Duration PERIODIC_MIN_BACKOFF = Duration.ofMillis(500);
  static final Duration HEARTBEAT_DURATION = Duration.ofSeconds(120);
  // CD1.1 Protocol provisional policy states that ACKNACK frames are sent at least once every minute
  static final int ACKNACK_TIME_SECONDS = 55;
  private static final String DEFAULT_FRAME_SET = "0:0";
  private final StructuredLoggingWrapper logger = StructuredLoggingWrapper
    .create(LoggerFactory.getLogger(ReactorCd11FrameHandler.class));

  private final String stationName;

  private final int stationPort;
  private final DataFrameReceiverConfiguration receiverConfig;
  private final Cd11FrameFactory frameFactory;
  private final Many<Tuple2<RawStationDataFrame, Long>> sequencedRsdfSink;
  private final Many<MalformedFrame> malformedFrameSink;
  private final Cd11GapList cd11GapList;
  private final Cd11GapListUtility gapListUtility;

  //In the future we will want to make these values configurable
  private final int gapExpirationDays; // Never expire.
  private final Duration gapStateStorageInterval;

  private final AtomicReference<String> frameSet;
  private Cd11Connection cd11Connection;


  ReactorCd11FrameHandler(String stationName,
    int stationPort, DataFrameReceiverConfiguration receiverConfig,
    Cd11FrameFactory frameFactory,
    Many<Tuple2<RawStationDataFrame, Long>> sequencedRsdfSink,
    Many<MalformedFrame> malformedFrameSink,
    Cd11GapManagementBundle gapsListBundle) {
    this.stationName = stationName;
    this.stationPort = stationPort;
    this.receiverConfig = receiverConfig;
    this.frameFactory = frameFactory;
    this.sequencedRsdfSink = sequencedRsdfSink;
    this.malformedFrameSink = malformedFrameSink;
    this.cd11GapList = gapsListBundle.getCd11GapList();
    this.gapListUtility = gapsListBundle.getCd11GapListUtility();
    this.gapExpirationDays = gapsListBundle.getGapExpirationDays();
    this.gapStateStorageInterval = Duration.ofMinutes(gapsListBundle.getGapStorageIntervalMinutes());
    this.frameSet = new AtomicReference<>(DEFAULT_FRAME_SET);
    logger.addValueArgument(STATION_LOGGING_KEY, stationName);
    logger.addValueArgument(STATION_PORT_KEY, stationPort);
  }

  public static ReactorCd11FrameHandler create(String stationName,
    int stationPort, DataFrameReceiverConfiguration receiverConfig,
    Many<Tuple2<RawStationDataFrame, Long>> sequencedRsdfSink,
    Many<MalformedFrame> malformedFrameSink, Cd11GapManagementBundle gapsListBundle) {

    var cd11FrameFactory = Cd11FrameFactory.createUnauthDefaultDest(stationName);

    return new ReactorCd11FrameHandler(stationName, stationPort, receiverConfig,
      cd11FrameFactory, sequencedRsdfSink, malformedFrameSink, gapsListBundle);
  }

  /**
   * The BiFunction leveraged by reactor netty to handle an active tcp connection.
   *
   * @param inbound Inbound tcp connection for receiving packets
   * @param outbound Outbound tcp connection for sending packets
   * @return IMPORTANT: The publisher provided here controls the overall lifecycle of the tcp
   * connection. Triggering completion of this publisher will shut down the connection.
   */
  @Override
  public Mono<Void> apply(NettyInbound inbound, NettyOutbound outbound) {
    Empty<Void> completionSink = Sinks.empty();
    cd11Connection = new Cd11Connection(stationName, inbound, outbound, completionSink);
    Cd11CompositeFrameHandler frameHandler = assembleCompositeFrameHandler(cd11Connection);

    Mono<Void> handleFrames = cd11Connection.receive()
      .timeout(HEARTBEAT_DURATION, handleTimeout())
      .publishOn(Schedulers.boundedElastic())
      .concatMap(frameHandler::handle)
      .then();

    Mono<Void> sendAcknacks = sendAcknackPeriodically(cd11Connection);
    Mono<Void> removeExpiredGaps = getGapExpiration().map(this::removeExpiredGapsPeriodically).orElse(Mono.never());
    Mono<Void> persistGaps = persistGapsPeriodically(gapStateStorageInterval);

    return Mono.firstWithSignal(completionSink.asMono(), handleFrames, sendAcknacks, removeExpiredGaps, persistGaps);
  }

  Mono<Void> debug(String message, Object... arguments) {
    return Mono.fromRunnable(() -> logger.debug(message, arguments));
  }

  Mono<Void> info(String message, Object... arguments) {
    return Mono.fromRunnable(() -> logger.info(message, arguments));
  }

  Mono<Void> warn(String message, Object... arguments) {
    return Mono.fromRunnable(() -> logger.warn(message, arguments));
  }

  private <T> Mono<T> handleTimeout() {
    return warn("Acknack Heartbeat Timeout {} reached.", HEARTBEAT_DURATION)
      .then(alert())
      .then(dispose());
  }

  private Mono<Void> sendAcknackPeriodically(Cd11Connection connection) {
    return debug("Starting periodic ACKNACK sending")
      .thenMany(Flux.interval(Duration.ofSeconds(ACKNACK_TIME_SECONDS)))
      .map(i -> buildLatestAcknack())
      .map(frameFactory::wrap)
      .flatMap(connection::send)
      .then();
  }

  private Cd11Acknack buildLatestAcknack() {
    return Cd11Acknack.withGapList(cd11GapList)
      .setFrameSetAcked(frameSet.get())
      .build();
  }

  private Cd11CompositeFrameHandler assembleCompositeFrameHandler(Cd11Connection connection) {
    var compositeFrameHandler = new Cd11CompositeFrameHandler();

    //shutdown situations
    compositeFrameHandler.registerFrameHandler(FrameType.CUSTOM_RESET_FRAME, this::handleReset);
    compositeFrameHandler.registerFrameHandler(FrameType.ALERT, this::handleAlert);

    compositeFrameHandler.registerFrameHandler(FrameType.ACKNACK, this::handleAcknack);

    Cd11FrameHandler optionRequestHandler = optionRequest -> handleOptionRequest(optionRequest, connection);
    compositeFrameHandler.registerFrameHandler(FrameType.OPTION_REQUEST, optionRequestHandler);
    compositeFrameHandler.registerFrameHandler(FrameType.COMMAND_RESPONSE, this::handleCommandResponse);

    compositeFrameHandler.registerFrameHandler(FrameType.DATA, this::handleData);
    compositeFrameHandler.registerFrameHandler(FrameType.CD_ONE_ENCAPSULATION, this::handleData);

    compositeFrameHandler.registerMalformedFrameHandler(this::handleMalformed);

    return compositeFrameHandler;
  }

  /**
   * Public shutdown method for resource cleanup from higher-level shutdown calls.
   */
  public void shutdown() {
    alert().then(dispose()).subscribe();
  }

  Mono<Void> alert() {
    return Mono.defer(() -> getCd11Connection()
      .map(conn -> conn
        .send(frameFactory.wrap(Cd11Alert.create(format("Shutdown triggered for station %s", stationName))))
        .doOnError(
          err -> logger.warn("Message sending failed before ALERT was sent. Likely due to already severed connection.",
            err))
        .onErrorResume(e -> Mono.empty()))
      .orElse(Mono.empty()));
  }

  Optional<Cd11Connection> getCd11Connection() {
    return Optional.ofNullable(cd11Connection);
  }

  @VisibleForTesting
  void setCd11Connection(Cd11Connection connection) {
    this.cd11Connection = connection;
  }

  <T> Mono<T> dispose() {
    return Mono.fromRunnable(() -> getCd11Connection().ifPresent(Cd11Connection::close));
  }

  private Mono<Void> handleReset(Cd11Frame reset) {
    return info("Received Reset Frame")
      .then(reset())
      .then(dispose());
  }

  private Mono<Void> reset() {
    return gapListUtility.clearGapState(stationName)
      .retryWhen(Retry.backoff(10, Duration.ofMillis(500))
        .doAfterRetry(s -> logger.warn("Failed to clear gaps, retrying...", s.failure())))
      .onErrorResume(err -> warn("Attempt to clear gap state before reset failed.", err))
      .then(cd11GapList.resetGapsList());
  }

  Mono<Void> handleAlert(Cd11Frame alert) {
    return info("Received Alert Frame")
      .then(dispose());
  }

  Mono<Void> handleAcknack(Cd11Frame acknack) {
    Cd11Acknack casted = asPayloadType(acknack.getPayload(), FrameType.ACKNACK);
    return Mono.fromRunnable(() -> frameSet.compareAndSet(DEFAULT_FRAME_SET, casted.getFrameSetAcked()))
      .then(cd11GapList.checkForReset(casted))
      .onErrorResume(err -> warn("Failure handling Acknack Frame", err));
  }

  public String getFrameSet() {
    return frameSet.get();
  }

  Mono<Void> handleOptionRequest(Cd11Frame optionRequest, Cd11Connection connection) {
    return debug("Received Option Request Frame, replying with Option Response Frame")
      .then(Mono.just(optionRequest))
      .map(frame -> FrameUtilities.<Cd11OptionExchange>asPayloadType(frame.getPayload(), FrameType.OPTION_REQUEST))
      .map(frameFactory::wrapResponse)
      .flatMap(connection::send);
  }

  void handleCommandResponse(Cd11Frame commandResponse) {
    logger.debug("Received Command Response Frame, recorded the sequence number to gap list");
    cd11GapList.processSequenceNumber(commandResponse.getHeader().getSequenceNumber());
  }

  void handleData(Cd11Frame dataFrame) {
    long sequenceNumber = dataFrame.getHeader().getSequenceNumber();
    logger.debug("Received Data Frame",
      CleanStructuredArgument.value(SEQUENCE_LOGGING_KEY, sequenceNumber));

    if (!dataFrame.getHeader().getFrameCreator().equals(stationName)) {
      logger.warn("Data frame {}#{} was received on the {} station receiver (port {})." +
          " Ensure sender and receiver are in agreement on station/port assignment," +
          " and that the frame creator is in agreement with the station receiver",
        dataFrame.getHeader().getFrameCreator(),
        CleanStructuredArgument.value(SEQUENCE_LOGGING_KEY, sequenceNumber), stationName, stationPort);
    }

    var rsdf = Cd11RawStationDataFrameUtility.parseAcquiredDataFrame(dataFrame, stationName,
      Instant.now(), receiverConfig::getChannelName);

    sequencedRsdfSink.emitNext(Tuples.of(rsdf, sequenceNumber),
      EmitFailureHandlerUtility.getInstance());
  }

  void handleMalformed(MalformedFrame malformed) {
    logger.warn("Received Malformed Frame", malformed.getCause());
    var malformedBuilder = malformed.toBuilder();

    malformed.getStation().ifPresentOrElse(malformedStationName -> {
      if (!malformedStationName.equals(stationName)) {
        logger.warn("Malformed frame's parsed station {} differs from expected station for receiver",
          malformedStationName);
      }
    }, () -> malformedBuilder.setStation(stationName));

    malformedFrameSink.emitNext(malformedBuilder.build(), EmitFailureHandlerUtility.getInstance());
  }

  private Optional<Duration> getGapExpiration() {
    return gapExpirationDays > 0 ? Optional.of(Duration.ofDays(gapExpirationDays))
      : Optional.empty();
  }

  //copying the original functionality of dataman
  //would like to modify this so that it is scheduled to remove gaps based on the oldest gap
  private Mono<Void> removeExpiredGapsPeriodically(Duration expirationPeriod) {
    return Flux.interval(expirationPeriod, expirationPeriod)
      .concatMap(i -> cd11GapList.removeExpiredGaps(expirationPeriod)
        .retryWhen(Retry.backoff(PERIODIC_MAX_ATTEMPTS, PERIODIC_MIN_BACKOFF)
          .doAfterRetry(
            s -> logger.warn("Failed to remove expired gaps for station {}, retrying...", stationName, s.failure())))
        .onErrorResume(err -> warn("Attempt to to remove expired gaps failed.", err)))
      .then();
  }

  private Mono<Void> persistGapsPeriodically(Duration persistPeriod) {
    return Flux.interval(persistPeriod, persistPeriod)
      .concatMap(i -> tryPersist()
        .retryWhen(Retry.backoff(PERIODIC_MAX_ATTEMPTS, PERIODIC_MIN_BACKOFF)
          .doAfterRetry(
            s -> logger.warn("Failed to persist gaps for station {}, retrying...", stationName, s.failure())))
        .onErrorResume(err -> warn("Could not persist gaps for station {}: {}", stationName, err)))
      .then();
  }

  Mono<Void> tryPersist() {
    return gapListUtility.persistGapState(stationName, cd11GapList.getGapList());
  }
}

package gms.dataacquisition.stationreceiver.cd11.common.reactor.netty;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.FrameParsingDecoder;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.shared.utilities.logging.StructuredLoggingWrapper;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.core.publisher.Sinks.Empty;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wrapper class for Reactor Netty connection apis directed at managing sending and receiving of
 * Cd11Frames, and closing of the tcp connection.
 */
public class Cd11Connection {

  public static final String STATION_NAME_KEY = "station";
  private final StructuredLoggingWrapper logger = StructuredLoggingWrapper
    .create(LoggerFactory.getLogger(Cd11Connection.class));

  private final NettyInbound inbound;
  private final NettyOutbound outbound;
  private final Empty<Void> completionSink;

  private final AtomicBoolean isClosing = new AtomicBoolean(false);

  /**
   * Main Constructor
   *
   * @param stationName Station name to include with logs. If null, no station name will be included in logs.
   * @param inbound Inbound connection interface
   * @param outbound Outbound connection interface
   * @param completionSink Sink to be completed on closure of the {@link Cd11Connection}
   */
  public Cd11Connection(@Nullable String stationName, NettyInbound inbound, NettyOutbound outbound,
    Empty<Void> completionSink) {
    this.inbound = inbound.withConnection(x ->
      x.addHandlerFirst(new FrameParsingDecoder()));
    this.outbound = outbound;
    this.completionSink = completionSink;

    Optional.ofNullable(stationName).ifPresent(name -> logger.addValueArgument(STATION_NAME_KEY, stationName));
  }

  /**
   * Factory method that instantiates a {@link Cd11Connection} with no station name logged.
   *
   * @param inbound Inbound connection interface
   * @param outbound Outbound connection interface
   * @param completionSink Sink to be completed on closure of the {@link Cd11Connection}
   * @return A {@link Cd11Connection} with no station name configured to be logged
   */
  public static Cd11Connection create(NettyInbound inbound, NettyOutbound outbound, Empty<Void> completionSink) {
    return new Cd11Connection(null, inbound, outbound, completionSink);
  }

  /**
   * Sends a Cd11Frame on the tcp outbound
   *
   * @param frame Frame to send
   * @return a void Mono representing completion status of the send.
   */
  public Mono<Void> send(Cd11Frame frame) {
    return outbound.sendByteArray(Mono.just(frame).map(Cd11Frame::toBytes)).then();
  }

  /**
   * Receives packets from the tcp inbound and parses them into Cd11Frames. Note that this parsing
   * includes the different types of frames each Cd11Frame can represent.
   *
   * @return Flux of parsed Cd11Frames of all incoming types.
   */
  public Flux<Cd11OrMalformedFrame> receive() {
    return inbound
      .receive()
      .asByteBuffer()
      .map(Cd11FrameReader::readFrame)
      .onErrorContinue((e, obj) -> logger.error(
        "Inbound frame construction failed. Handling here to avoid canceling subscription. Returned object: {}",
        obj, e));
  }

  public void close() {
    if (isClosing.compareAndSet(false, true)) {
      EmitResult result = completionSink.tryEmitEmpty();
      if (result.isSuccess()) {
        logger.info("Connection close signal emitted");
      } else {
        logger.warn("Emission failed for connection close signal... Result {}", result);
        isClosing.set(false);
      }
    } else {
      logger.debug("Connection already closed");
    }
  }

}

package gms.dataacquisition.stationreceiver.cd11.common.reactor;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A handler for {@link Cd11OrMalformedFrame}s, which registers handlers for different types of {@link Cd11Frame}s,
 * and a single handler for {@link MalformedFrame}s. Provides opinionated behavior of logging and dropping unregistered
 * frame types.
 */
public class Cd11CompositeFrameHandler {

  private static final Logger logger = LoggerFactory.getLogger(Cd11CompositeFrameHandler.class);

  private final Map<FrameType, Cd11FrameHandler> frameHandlersByType;

  @Nullable
  private MalformedFrameHandler malformedFrameHandler;

  public Cd11CompositeFrameHandler() {
    this(new EnumMap<>(FrameType.class));
  }

  private Cd11CompositeFrameHandler(
    Map<FrameType, Cd11FrameHandler> fluxHandlersByType) {
    this.frameHandlersByType = fluxHandlersByType;
  }

  /**
   * Opinionated registration of a non-reactive {@link Consumer} of {@link Cd11Frame} for a given frame type. Wraps the consumer
   * in a runnable {@link Mono} and registers it as a {@link Cd11FrameHandler}.
   *
   * @param frameType Type of frame to manage
   * @param frameConsumer Frame processing represented as a Consumer
   */
  public void registerFrameHandler(FrameType frameType, Consumer<Cd11Frame> frameConsumer) {
    Cd11FrameHandler frameHandler = frame -> Mono.fromRunnable(() -> frameConsumer.accept(frame));
    registerFrameHandler(frameType, frameHandler);
  }

  /**
   * Register a frame handler for a particular frame type.
   *
   * @param frameType Type of frame to manage
   * @param handler Handler for the frame
   */
  public void registerFrameHandler(FrameType frameType, Cd11FrameHandler handler) {
    logger.debug("Registering frame handler for type {}", frameType);
    frameHandlersByType.put(frameType, handler);
  }

  /**
   * Opinionated registration of a non-reactive {@link Consumer} of {@link MalformedFrame}.
   * Wraps in a runnable {@link Mono} and registers it as a {@link MalformedFrameHandler}
   *
   * @param malformedConsumer Malformed frame processing represented as a Consumer
   */
  public void registerMalformedFrameHandler(Consumer<MalformedFrame> malformedConsumer) {
    MalformedFrameHandler malformedHandler = malformed -> Mono.fromRunnable(() -> malformedConsumer.accept(malformed));
    registerMalformedFrameHandler(malformedHandler);
  }

  /**
   * Register a frame handler for Malformed frames
   *
   * @param handler Handler for the frame
   */
  public void registerMalformedFrameHandler(MalformedFrameHandler handler) {
    logger.debug("Registering malformed frame handler");
    malformedFrameHandler = handler;
  }

  private Optional<MalformedFrameHandler> getMalformedFrameHandler() {
    return Optional.ofNullable(malformedFrameHandler);
  }

  /**
   * Triggers the handler logic for the high-level tagged union of {@link Cd11Frame} or {@link MalformedFrame}
   * by first inspecting the union, then unwrapping it based on its Kind.
   *
   * @param frame Tagged union frame of either Cd11 or Malformed frame
   * @return Mono for the handling processing that will occur on subscription
   */
  public Mono<Void> handle(Cd11OrMalformedFrame frame) {
    return Cd11OrMalformedFrame.Kind.CD11.equals(frame.getKind())
      ? handle(frame.cd11())
      : handle(frame.malformed());
  }

  private Mono<Void> handle(Cd11Frame frame) {
    return frameHandlersByType.computeIfAbsent(frame.getType(), this::doNothing).handle(frame);
  }

  private Cd11FrameHandler doNothing(FrameType type) {
    return frame -> Mono.fromRunnable(() -> logger.debug("Doing nothing for unhandled frame type {}", type));
  }

  private Mono<Void> handle(MalformedFrame frame) {
    return getMalformedFrameHandler()
      .orElseGet(this::doNothing)
      .handle(frame);
  }

  private MalformedFrameHandler doNothing() {
    return frame -> Mono.fromRunnable(() -> logger.debug("Doing nothing for malformed frame type"));
  }
}

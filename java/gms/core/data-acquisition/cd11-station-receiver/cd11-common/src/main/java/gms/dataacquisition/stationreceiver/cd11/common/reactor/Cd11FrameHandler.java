package gms.dataacquisition.stationreceiver.cd11.common.reactor;

import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import reactor.core.publisher.Mono;

/**
 * Functional interface providing the handling of a single Cd11Frame
 */
@FunctionalInterface
public interface Cd11FrameHandler {

  /**
   * Provide the handling behavior for an individual Cd11Frame, informing the default behavior of a
   * flux of Cd11Frames
   *
   * @param frame Input frame to handle
   */
  Mono<Void> handle(Cd11Frame frame);
}

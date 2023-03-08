package gms.dataacquisition.stationreceiver.cd11.common.reactor;

import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface MalformedFrameHandler {

  Mono<Void> handle(MalformedFrame malformedFrame);
}

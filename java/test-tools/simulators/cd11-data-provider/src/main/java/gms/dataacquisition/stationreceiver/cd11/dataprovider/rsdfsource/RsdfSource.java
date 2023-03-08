package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource;

import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import reactor.core.publisher.Flux;

/**
 * Interface for provision of an infinite stream of {@link RawStationDataFrame}s from a source
 */
@FunctionalInterface
public interface RsdfSource {

  Flux<RawStationDataFrame> getRsdfFlux();
}

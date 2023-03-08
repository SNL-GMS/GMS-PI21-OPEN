package gms.core.performancemonitoring.soh.control.api;

import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import reactor.core.publisher.Flux;

/**
 * Pairs the StationSoh and CapabilitySohRollup fluxes so they can be passed along together.
 */
public class StationSohMonitoringResultsFluxPair {

  private final Flux<StationSoh> stationSohPublisher;

  private final Flux<CapabilitySohRollup> capabilitySohRollupPublisher;

  /**
   * Pair a StationSoh publisher and a CapabilitySohRollup publisher.
   *
   * @param stationSohPublisher
   * @param capabilitySohRollupPublisher
   */
  public StationSohMonitoringResultsFluxPair(
    Flux<StationSoh> stationSohPublisher,
    Flux<CapabilitySohRollup> capabilitySohRollupPublisher) {
    this.stationSohPublisher = stationSohPublisher;
    this.capabilitySohRollupPublisher = capabilitySohRollupPublisher;
  }

  public Flux<StationSoh> getStationSohPublisher() {
    return stationSohPublisher;
  }

  public Flux<CapabilitySohRollup> getCapabilitySohRollupPublisher() {
    return capabilitySohRollupPublisher;
  }
}

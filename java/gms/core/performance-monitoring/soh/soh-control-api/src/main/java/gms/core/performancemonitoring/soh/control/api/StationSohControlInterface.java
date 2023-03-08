package gms.core.performancemonitoring.soh.control.api;

import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;

import javax.ws.rs.Path;
import java.util.Set;

/**
 * Interface for station SOH monitoring functionality.
 */
@Component("soh-control")
@Path("/soh-control")
public interface StationSohControlInterface {

  /**
   * Create station state of health objects from a received set of acquired station soh extracts.
   *
   * @param acquiredStationSohExtracts The {@link AcquiredStationSohExtract}s to process into
   * monitoring results
   * @return The {@link StationSohMonitoringResultsFluxPair} calculated from the provided {@link AcquiredStationSohExtract}
   */
  StationSohMonitoringResultsFluxPair monitor(
    Set<AcquiredStationSohExtract> acquiredStationSohExtracts);
}

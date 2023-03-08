package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

import java.time.Duration;
import java.util.Set;

/**
 * Represents overall SOH monitoring processing configuration.
 */
@AutoValue
public abstract class StationSohMonitoringDefinition {

  /**
   * @return a Duration specifying how old a StationSoh may be and still be included in the
   * CapabilitySohRollup calculation.
   */
  public abstract Duration getRollupStationSohTimeTolerance();

  /**
   * @return an ordered collection of StationGroups displayed in the UI. The StationSohControl uses
   * this collection to determine which Stations it needs to process.
   */
  public abstract Set<String> getDisplayedStationGroups();

  /**
   * @return a Set of StationSoh definitions, corresponding to some number of stations.
   */
  public abstract Set<StationSohDefinition> getStationSohDefinitions();

  /**
   * @return a Set of CapabilitySohRollupDefinitions, corresponding to each capability SOH rollup.
   */
  public abstract Set<CapabilitySohRollupDefinition> getCapabilitySohRollupDefinitions();

  /**
   * Create a new StationSohMonitoringDefinition object.
   *
   * @param stationSohDefinitions the set of StationSohDefinitions
   * @return a StationSohMonitoringDefinition object.
   */
  public static StationSohMonitoringDefinition create(
    Duration rollupStationSohTimeTolerance,
    Set<String> displayedStationGroups,
    Set<StationSohDefinition> stationSohDefinitions,
    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitions
  ) {

    return new AutoValue_StationSohMonitoringDefinition(
      rollupStationSohTimeTolerance, displayedStationGroups,
      stationSohDefinitions, capabilitySohRollupDefinitions
    );
  }

}

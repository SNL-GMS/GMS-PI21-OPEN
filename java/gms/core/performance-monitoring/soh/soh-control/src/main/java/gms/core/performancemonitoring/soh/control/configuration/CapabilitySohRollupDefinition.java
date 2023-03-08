package gms.core.performancemonitoring.soh.control.configuration;

import com.google.auto.value.AutoValue;

import java.util.Map;

/**
 * Describes a capability rollup calculation for a single StationGroup.
 */
@AutoValue
public abstract class CapabilitySohRollupDefinition {

  /**
   * @return the StationGroup.
   */
  public abstract String getStationGroup();

  /**
   * @return the RollupOperator used to compute the StationGroup's SohStatus rollup from Station
   * SohStatus rollups.
   */
  public abstract RollupOperator getStationsToGroupRollupOperator();

  /**
   * @return collection of StationRollupDefinition objects which describe the calculations used to
   * produce the Station rollup SohStatus objects that are input to the StationGroup rollup
   * calculation.
   */
  public abstract Map<String, StationRollupDefinition> getStationRollupDefinitionsByStation();

  /**
   * Create a new CapabilitySohRollupDefinition object.
   *
   * @param stationGroup the StationGroup.
   * @return a CapabilitySohRollupDefinition object.
   */
  public static CapabilitySohRollupDefinition from(
    String stationGroup,
    RollupOperator stationsToGroupRollupOperator,
    Map<String, StationRollupDefinition> stationRollupDefinitionsByStation
  ) {
    return new AutoValue_CapabilitySohRollupDefinition(
      stationGroup, stationsToGroupRollupOperator, stationRollupDefinitionsByStation
    );
  }

}

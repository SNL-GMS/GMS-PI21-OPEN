package gms.core.performancemonitoring.ssam.control.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@AutoValue
public abstract class StationSohMonitoringDefinition {

  public abstract Duration getReprocessingPeriod();

  /* Declare as List to maintain order loaded */
  public abstract List<String> getDisplayedStationGroups();

  public abstract Duration getRollupStationSohTimeTolerance();

  public abstract Set<StationSohDefinition> getStationSohDefinitions();

  @JsonCreator
  public static StationSohMonitoringDefinition from(
    @JsonProperty("reprocessingPeriod") Duration reprocessingPeriod,
    @JsonProperty("displayedStationGroups") List<String> displayedStationGroups,
    @JsonProperty("rollupStationSohTimeTolerance") Duration rollupStationSohTimeTolerance,
    @JsonProperty("stationSohDefinitions") Set<StationSohDefinition> stationSohDefinitions) {
    return new AutoValue_StationSohMonitoringDefinition(reprocessingPeriod,
      displayedStationGroups, rollupStationSohTimeTolerance, stationSohDefinitions);
  }
}

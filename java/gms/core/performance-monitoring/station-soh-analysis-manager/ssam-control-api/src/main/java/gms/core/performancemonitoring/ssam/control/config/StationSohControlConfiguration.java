package gms.core.performancemonitoring.ssam.control.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@AutoValue
public abstract class StationSohControlConfiguration {

  public abstract Duration getReprocessingPeriod();

  public abstract List<String> getDisplayedStationGroups();

  public abstract Duration getRollupStationSohTimeTolerance();

  public abstract Set<StationSohDefinition> getStationSohDefinitions();

  @JsonCreator
  public static AutoValue_StationSohControlConfiguration from(
    @JsonProperty("reprocessingPeriod") Duration reprocessingPeriod,
    @JsonProperty("displayedStationGroups") List<String> displayedStationGroups,
    @JsonProperty("rollupStationSohTimeTolerance") Duration rollupStationSohTimeTolerance,
    @JsonProperty("stationSohDefinitions") Set<StationSohDefinition> stationSohDefinitions) {
    return new AutoValue_StationSohControlConfiguration(reprocessingPeriod, displayedStationGroups,
      rollupStationSohTimeTolerance, stationSohDefinitions);
  }
}

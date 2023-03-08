package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.station.StationGroup;

@AutoValue
public abstract class Activity {

  public abstract String getName();

  public abstract StationGroup getStationGroup();

  public abstract AnalysisMode getAnalysisMode();

  @JsonCreator
  public static Activity from(
    @JsonProperty("name") String name,
    @JsonProperty("stationGroup") StationGroup stationGroup,
    @JsonProperty("analysisMode") AnalysisMode analysisMode) {
    return new AutoValue_Activity(name, stationGroup, analysisMode);
  }

}

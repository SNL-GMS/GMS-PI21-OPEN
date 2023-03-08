package gms.core.performancemonitoring.ssam.control.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;


@AutoValue
public abstract class StationSohMonitoringUiClientParameters {

  public abstract StationSohMonitoringDefinition getStationSohControlConfiguration();

  public abstract StationSohMonitoringDisplayParameters getStationSohMonitoringDisplayParameters();

  @JsonCreator
  public static StationSohMonitoringUiClientParameters from(
    @JsonProperty("stationSohControlConfiguration") StationSohMonitoringDefinition stationSohControlConfiguration,
    @JsonProperty("stationSohMonitoringDisplayParameters") StationSohMonitoringDisplayParameters stationSohMonitoringDisplayParameters) {
    return new AutoValue_StationSohMonitoringUiClientParameters(stationSohControlConfiguration,
      stationSohMonitoringDisplayParameters);
  }

}

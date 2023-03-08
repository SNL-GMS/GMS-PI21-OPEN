package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * Contains configuration values for which {@link gms.shared.frameworks.osd.coi.signaldetection.StationGroup}
 * names to process state of health information for.
 */
@AutoValue
public abstract class StationGroupNamesConfigurationOption {


  public abstract List<String> getStationGroupNames();


  @JsonCreator
  public static StationGroupNamesConfigurationOption create(
    @JsonProperty("stationGroupNames") List<String> stationGroupNames) {

    return new AutoValue_StationGroupNamesConfigurationOption(stationGroupNames);
  }
}

package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class StationGroupDefinition {
  public abstract String getName();

  public abstract String getDescription();

  public abstract List<String> getStationNames();

  @JsonCreator
  public static StationGroupDefinition from(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("stationNames") List<String> stationNames) {
    Validate.notEmpty(name);
    Validate.notEmpty(description);
    Validate.notEmpty(stationNames);
    return new AutoValue_StationGroupDefinition(name, description, Collections.unmodifiableList(stationNames));
  }
}

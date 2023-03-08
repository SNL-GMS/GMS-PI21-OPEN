package gms.shared.stationdefinition.coi.station;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@AutoValue
@JsonPropertyOrder(alphabetic = true)
public abstract class StationGroupDefinition {
  public abstract String getName();

  public abstract String getDescription();

  public abstract Instant getEffectiveAt();

  public abstract List<String> getStationNames();

  @JsonCreator
  public static StationGroupDefinition from(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("effectiveAt") Instant effectiveAt,
    @JsonProperty("stationNames") List<String> stationNames) {
    Validate.notEmpty(name);
    Validate.notEmpty(description);
    Validate.notEmpty(stationNames);
    return new AutoValue_StationGroupDefinition(name, description, effectiveAt,
      Collections.unmodifiableList(stationNames));
  }
}

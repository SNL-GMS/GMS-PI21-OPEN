package gms.shared.stationdefinition.coi.station;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@AutoValue
@JsonSerialize(as = StationId.class)
@JsonDeserialize(builder = AutoValue_StationId.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class StationId {

  public static StationId createEntityReference(String name) {
    return StationId.builder()
      .setName(name)
      .build();
  }

  public static StationId createVersionReference(String name, Instant effectiveAt) {
    Objects.requireNonNull(effectiveAt);
    return StationId.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .build();
  }

  public Station toStationEntityReference() {
    return Station
      .createEntityReference(this.getName());
  }

  public Station toStationVersionReference() {
    final Optional<Instant> effectiveAt = this.getEffectiveAt();
    Preconditions.checkArgument(effectiveAt.isPresent(),
      "EffectiveAt is missing from the channel's station version reference.");
    return Station
      .createVersionReference(this.getName(), effectiveAt.get());
  }

  public abstract String getName();

  public abstract Optional<Instant> getEffectiveAt();

  public static Builder builder() {
    return new AutoValue_StationId.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setName(String name);

    String getName();

    default Builder setEffectiveAt(Instant effectiveAt) {
      return setEffectiveAt(Optional.ofNullable(effectiveAt));
    }

    Builder setEffectiveAt(Optional<Instant> effectiveAt);

    StationId autoBuild();

    default StationId build() {
      StationId station = autoBuild();
      Validate.notEmpty(station.getName(), "Channel Station must be provided a name");
      return station;
    }
  }
}

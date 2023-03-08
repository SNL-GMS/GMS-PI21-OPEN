package gms.shared.frameworks.osd.coi.stationreference;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A class to represent a GMS reference site.
 */
@AutoValue
@JsonSerialize(as = ReferenceSite.class)
@JsonDeserialize(builder = AutoValue_ReferenceSite.Builder.class)
public abstract class ReferenceSite {

  @JsonIgnore
  @Memoized
  public UUID getEntityId() {
    return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_16LE));
  }

  @JsonIgnore
  @Memoized
  public UUID getVersionId() {
    return UUID.nameUUIDFromBytes((getName() + getLatitude() + getLongitude() + getElevation()
      + getActualChangeTime())
      .getBytes(StandardCharsets.UTF_16LE));
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract String getComment();

  public abstract InformationSource getSource();

  public abstract double getLatitude();

  public abstract double getLongitude();

  public abstract double getElevation();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract boolean isActive();

  public abstract RelativePosition getPosition();

  public abstract List<ReferenceAlias> getAliases();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceSite.Builder();
  }


  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setName(String name);

    Builder setDescription(String description);

    Builder setComment(String comment);

    Builder setSource(InformationSource source);

    Builder setLatitude(double latitude);

    Builder setLongitude(double longitude);

    Builder setElevation(double elevation);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    Builder setActive(boolean active);

    Builder setPosition(RelativePosition position);

    Builder setAliases(List<ReferenceAlias> aliases);

    ReferenceSite build();
  }
}

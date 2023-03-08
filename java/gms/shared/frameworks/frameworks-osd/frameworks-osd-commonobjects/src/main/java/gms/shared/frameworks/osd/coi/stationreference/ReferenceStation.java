package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;
import org.apache.commons.lang3.Validate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Define a class to represent a GMS remote monitoring station.
 */
@AutoValue
@JsonSerialize(as = ReferenceStation.class)
@JsonDeserialize(builder = AutoValue_ReferenceStation.Builder.class)
public abstract class ReferenceStation {

  public abstract String getName();

  public abstract String getDescription();

  public abstract StationType getStationType();

  public abstract InformationSource getSource();

  public abstract String getComment();

  public abstract double getLatitude();

  public abstract double getLongitude();

  public abstract double getElevation();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract boolean isActive();

  public abstract List<ReferenceAlias> getAliases();

  /**
   * Sets defaults, for non required fields. Properties
   * not listed here are required and if not provided
   * build() will throw an IllegalStateException
   *
   * @return ReferenceStation
   */
  public static Builder builder() {
    return new AutoValue_ReferenceStation.Builder()
      .setDescription("")
      .setSource(InformationSource.from("UNKNOWN", Instant.EPOCH, "UNKNOWN"))
      .setComment("")
      .setSystemChangeTime(Instant.EPOCH)
      .setAliases(List.of());
  }

  public abstract Builder toBuilder();

  @JsonIgnore
  @Memoized
  public UUID getEntityId() {
    return UUID.nameUUIDFromBytes(this.getName().getBytes(StandardCharsets.UTF_16LE));
  }

  @JsonIgnore
  @Memoized
  public UUID getVersionId() {
    return UUID.nameUUIDFromBytes((this.getName() + this.getStationType()
      + this.getLatitude() + this.getLongitude() + this.getElevation()
      + this.getActualChangeTime()).getBytes(StandardCharsets.UTF_16LE));
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setName(String name);

    abstract String getName();

    public abstract Builder setDescription(String description);

    public abstract Builder setStationType(StationType stationType);

    public abstract Builder setSource(InformationSource source);

    public abstract Builder setComment(String comment);

    public abstract Builder setLatitude(double latitude);

    public abstract Builder setLongitude(double longitude);

    public abstract Builder setElevation(double elevation);

    public abstract Builder setActualChangeTime(Instant actualChangeTime);

    public abstract Builder setSystemChangeTime(Instant systemChangeTime);

    public abstract Builder setActive(boolean active);

    public abstract Builder setAliases(List<ReferenceAlias> aliases);

    abstract ReferenceStation autoBuild();

    public ReferenceStation build() {
      setName(getName().trim());
      ReferenceStation station = autoBuild();
      Validate.notEmpty(station.getName());
      return station;
    }
  }
}



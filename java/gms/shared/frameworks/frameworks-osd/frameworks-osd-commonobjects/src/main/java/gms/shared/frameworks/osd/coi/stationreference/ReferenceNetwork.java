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
import java.util.UUID;

/**
 * Define a class which represents a network, which is a collection of monitoring stations.
 */
@AutoValue
@JsonSerialize(as = ReferenceNetwork.class)
@JsonDeserialize(builder = AutoValue_ReferenceNetwork.Builder.class)
public abstract class ReferenceNetwork {

  @JsonIgnore
  @Memoized
  public UUID getEntityId() {
    return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_16LE));
  }

  @JsonIgnore
  @Memoized
  public UUID getVersionId() {
    return UUID.nameUUIDFromBytes(
      (getName() + getOrganization() + getRegion() + getActualChangeTime())
        .getBytes(StandardCharsets.UTF_16LE));
  }

  public abstract String getName();

  public abstract String getDescription();

  public abstract NetworkOrganization getOrganization();

  public abstract NetworkRegion getRegion();

  public abstract String getComment();

  public abstract InformationSource getSource();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract boolean isActive();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceNetwork.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setName(String name);

    public abstract Builder setDescription(String description);

    public abstract Builder setOrganization(NetworkOrganization networkOrganization);

    public abstract Builder setRegion(NetworkRegion networkRegion);

    public abstract Builder setComment(String comment);

    public abstract Builder setSource(InformationSource informationSource);

    public abstract Builder setActualChangeTime(Instant actualChangeTime);

    public abstract Builder setSystemChangeTime(Instant systemChangeTime);

    public abstract Builder setActive(boolean active);

    protected abstract ReferenceNetwork autoBuild();

    public ReferenceNetwork build() {
      ReferenceNetwork referenceNetwork = autoBuild();

      // Validate that entityId is valid
      final UUID expectedEntityId = UUID.nameUUIDFromBytes(
        referenceNetwork.getName().getBytes(StandardCharsets.UTF_16LE));
      Validate.isTrue(expectedEntityId.equals(referenceNetwork.getEntityId()),
        "Expected entityId to be " + expectedEntityId + " for name " + referenceNetwork
          .getName());

      // Validate that versionId is valid
      final UUID expectedVersionId = UUID.nameUUIDFromBytes((
        referenceNetwork.getName() + referenceNetwork.getOrganization() +
          referenceNetwork.getRegion() + referenceNetwork.getActualChangeTime())
        .getBytes(StandardCharsets.UTF_16LE));
      Validate.isTrue(expectedVersionId.equals(referenceNetwork.getVersionId()),
        "Expected versionId to be " + expectedVersionId + " for other attributes");

      return referenceNetwork;
    }
  }
}

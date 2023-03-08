package gms.shared.stationdefinition.coi.id;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;

import java.time.Instant;

@AutoValue
public abstract class VersionId {
  public abstract String getEntityId();

  public abstract Instant getEffectiveAt();

  public static Builder builder() {
    return new AutoValue_VersionId.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    Builder setEntityId(String entityId);

    Builder setEffectiveAt(Instant instant);

    VersionId build();
  }

  public String toVersionIdString() {
    return String.format("%s::%s", getEntityId(), getEffectiveAt());
  }
}

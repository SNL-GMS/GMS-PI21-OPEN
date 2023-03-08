package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Optional;
import java.util.UUID;

@AutoValue
public abstract class SignalDetectionHypothesisConverterId {

  public abstract String getLegacyDatabaseAccountId();

  public abstract UUID getDetectionId();

  public abstract Optional<UUID> getParentId();

  @JsonCreator
  public static SignalDetectionHypothesisConverterId from(
    @JsonProperty("legacyDatabaseAccountId") String legacyDatabaseAccountId,
    @JsonProperty("detectionId") UUID detectionId,
    @JsonProperty("parentId)") Optional<UUID> parentId) {
    return new AutoValue_SignalDetectionHypothesisConverterId(legacyDatabaseAccountId, detectionId, parentId);
  }
}

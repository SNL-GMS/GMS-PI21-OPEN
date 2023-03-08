package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class SignalDetectionHypothesisId {

  public abstract UUID getSignalDetectionId();

  public abstract UUID getId();

  @JsonCreator
  public static SignalDetectionHypothesisId from(@JsonProperty("signalDetectionId") UUID signalDetectionId,
    @JsonProperty("id") UUID id) {
    return new AutoValue_SignalDetectionHypothesisId(signalDetectionId, id);
  }

}

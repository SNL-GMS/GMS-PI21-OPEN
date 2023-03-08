package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import org.apache.commons.lang3.Validate;

@AutoValue
@JsonSerialize(as = SignalDetectionHypothesisReference.class)
@JsonDeserialize(builder = AutoValue_SignalDetectionHypothesisReference.Builder.class)
public abstract class SignalDetectionHypothesisReference {

  public abstract SignalDetectionHypothesisId getId();

  public static SignalDetectionHypothesisReference.Builder builder() {
    return new AutoValue_SignalDetectionHypothesisReference.Builder();
  }

  public abstract SignalDetectionHypothesisReference.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(SignalDetectionHypothesisId id);

    SignalDetectionHypothesisId getId();

    SignalDetectionHypothesisReference autoBuild();

    default SignalDetectionHypothesisReference build() {
      final var signalDetectionHypothesisReference = autoBuild();
      Validate.notNull(signalDetectionHypothesisReference.getId(),
        "SignalDetectionHypothesisId must be populated");
      return signalDetectionHypothesisReference;
    }
  }
}

package gms.shared.event.coi.featureprediction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonTypeName("ELLIPTICTY_CORRECTION")
public abstract class EllipticityCorrectionDefinition implements FeaturePredictionCorrectionDefinition {

  @JsonCreator
  public static EllipticityCorrectionDefinition from(
    @JsonProperty("ellipticityCorrectionType") EllipticityCorrectionType type
  ) {
    return new AutoValue_EllipticityCorrectionDefinition(type);
  }

  @Override
  public FeaturePredictionComponentType getCorrectionType() {
    return FeaturePredictionComponentType.ELLIPTICITY_CORRECTION;
  }

  public abstract EllipticityCorrectionType getEllipticityCorrectionType();
}

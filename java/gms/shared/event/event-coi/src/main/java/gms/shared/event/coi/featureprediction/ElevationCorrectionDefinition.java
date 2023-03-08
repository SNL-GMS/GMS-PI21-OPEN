package gms.shared.event.coi.featureprediction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonTypeName("ELEVATION_CORRECTION")
public abstract class ElevationCorrectionDefinition implements FeaturePredictionCorrectionDefinition {

  @JsonCreator
  public static ElevationCorrectionDefinition from(
    @JsonProperty("mediumVelocityEarthModel") String mediumVelocityEarthModel
  ) {
    return new AutoValue_ElevationCorrectionDefinition(mediumVelocityEarthModel);
  }

  public abstract String getMediumVelocityEarthModel();

  @Override
  public FeaturePredictionComponentType getCorrectionType() {
    return FeaturePredictionComponentType.ELEVATION_CORRECTION;
  }

}

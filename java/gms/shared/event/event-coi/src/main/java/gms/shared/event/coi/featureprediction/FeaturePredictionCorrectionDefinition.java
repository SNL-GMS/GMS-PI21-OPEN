package gms.shared.event.coi.featureprediction;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "correctionType")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ElevationCorrectionDefinition.class, name = "ELEVATION_CORRECTION"),
  @JsonSubTypes.Type(value = EllipticityCorrectionDefinition.class, name = "ELLIPTICITY_CORRECTION")
})
public interface FeaturePredictionCorrectionDefinition {

  FeaturePredictionComponentType getCorrectionType();

}

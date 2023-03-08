package gms.shared.event.manager.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.featureprediction.FeaturePredictionCorrectionDefinition;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;

import java.util.List;

/**
 * Deserializes JSON data provided for FeaturePredictionsDefinitions
 */
@AutoValue
public abstract class FeaturePredictionsDefinitions {

  public abstract String getEarthModel();

  public abstract List<FeaturePredictionCorrectionDefinition> getCorrectionDefinitions();

  public abstract List<FeaturePredictionType<?>> getPredictionTypes();

  @JsonCreator
  public static FeaturePredictionsDefinitions create(
    @JsonProperty("earthModel") String earthModel,
    @JsonProperty("correctionDefinitions") List<FeaturePredictionCorrectionDefinition> correctionDefinitions,
    @JsonProperty("predictionTypes") List<FeaturePredictionType<?>> predictionTypes
  ) {
    return new AutoValue_FeaturePredictionsDefinitions(earthModel, correctionDefinitions, predictionTypes);
  }
}

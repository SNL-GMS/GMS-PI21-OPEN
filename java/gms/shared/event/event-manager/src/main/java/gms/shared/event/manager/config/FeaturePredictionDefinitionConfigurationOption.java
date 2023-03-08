package gms.shared.event.manager.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

/**
 * Deserializes optional list of {@link FeaturePredictionsDefinitions}
 */
@AutoValue
public abstract class FeaturePredictionDefinitionConfigurationOption {

  public abstract List<FeaturePredictionsDefinitions> getPredictionsForLocationSolutionDefinitions();

  @JsonCreator
  public static FeaturePredictionDefinitionConfigurationOption create(
    @JsonProperty("featurePredictionsDefinitions") List<FeaturePredictionsDefinitions> featurePredictionsDefinitions
  ) {
    return new AutoValue_FeaturePredictionDefinitionConfigurationOption(featurePredictionsDefinitions);
  }
}

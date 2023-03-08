package gms.shared.featureprediction.configuration;

import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType.TypeNames;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Component of Feature Predictor Service that loads the main FPS configuration.
 */
@Configuration
public class FeaturePredictorConfiguration {

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  @Autowired
  public FeaturePredictorConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility
  ) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Gets the FeaturePredictorDefinition that represents configuration
   *
   * @return FeaturePredictorDefinition
   */
  @Bean
  public FeaturePredictorDefinition getCurrentFeaturePredictorDefinition() {

    var rawObject = configurationConsumerUtility.resolve(
      "feature-prediction-service",
      List.of()
    ).get("pluginByFeatureMeasurementType");

    if (rawObject instanceof Map) {

      // This will be a map of String -> String, but keep it generic and just use `toString` on
      // its entries to avoid type warnings.
      var rawPluginMap = (Map<?, ?>) rawObject;

      Map<FeaturePredictionType<?>, String> typedPluginMap = rawPluginMap.entrySet().stream()
        .map(entry -> {
            FeaturePredictionType<?> featurePredictionType;
            var name = entry.getKey().toString();
            if (TypeNames.ARRIVAL_TIME.name().equals(name)) {
              featurePredictionType = FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE;
            } else if (TypeNames.SLOWNESS.name().equals(name)) {
              featurePredictionType = FeaturePredictionType.SLOWNESS_PREDICTION_TYPE;
            } else {
              throw new IllegalStateException();
            }

            return Map.entry(featurePredictionType, entry.getValue().toString());
          }
        )
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      return FeaturePredictorDefinition.from(typedPluginMap);
    } else {
      throw new IllegalStateException("configuration field pluginByFeatureMeasurementType is not map!");
    }
  }
}

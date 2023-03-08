package gms.shared.featureprediction.plugin.correction.elevation;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Loads configuration for the elevation corrector
 */
@Configuration
public class ElevationCorrectorConfiguration {

  private final ConfigurationConsumerUtility configurationConsumerUtility;


  @Autowired
  public ElevationCorrectorConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * +   * Gets the ElevationCorrectorDefinition that represents the configuration
   *
   * @return a new ElevationCorrectorDefinition object
   */
  public ElevationCorrectorDefinition getCurrentElevationCorrectorDefinition() {
    var rawObject = configurationConsumerUtility.resolve(
      "feature-prediction-elevation-corrector",
      List.of()
    ).get("mediumVelocityEarthModelPluginNameByModelName");

    if (rawObject instanceof Map) {

      // This will be a map of String -> String, but keep it generic and just use `toString` on
      // its entries to avoid type warnings.
      var rawPluginMap = (Map<?, ?>) rawObject;

      // To avoid type warnings/code smells just use toString to convert to a map of String -> String
      var nameMap = rawPluginMap.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue().toString()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      return ElevationCorrectorDefinition.from(nameMap);
    } else {
      throw new IllegalStateException("configuration field mediumVelocityEarthModelPluginNameByModelName is not map!");
    }
  }
}

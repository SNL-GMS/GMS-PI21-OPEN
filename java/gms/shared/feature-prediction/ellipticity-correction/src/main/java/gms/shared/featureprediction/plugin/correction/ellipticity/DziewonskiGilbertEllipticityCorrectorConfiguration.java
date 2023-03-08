package gms.shared.featureprediction.plugin.correction.ellipticity;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Component of Feature Predictor Service that loads the Dziewonski-Gilbert Ellipticity Corrector
 * plugin configuration.
 */
@Configuration
public class DziewonskiGilbertEllipticityCorrectorConfiguration {

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  @Autowired
  public DziewonskiGilbertEllipticityCorrectorConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility
  ) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Gets the DziewonskiGilbertEllipticityCorrectorDefinition that represents configuration
   *
   * @return DziewonskiGilbertEllipticityCorrectorDefinition
   */
  @Bean
  public DziewonskiGilbertEllipticityCorrectorDefinition getCurrentDziewonskiGilbertEllipticityCorrectorDefinition() {
    var rawObject = configurationConsumerUtility.resolve(
      "feature-prediction-service.dziewonski-gilbert-ellipticity-corrector",
      List.of()
    ).get("correctionModelPluginNameByModelName");

    if (rawObject instanceof Map) {

      // This will be a map of String -> String, but keep it generic and just use `toString` on
      // its entries to avoid type warnings.
      var rawPluginMap = (Map<?, ?>) rawObject;

      // To avoid type warnings/code smells just use toString to convert to a map of String -> String
      var nameMap = rawPluginMap.entrySet().stream()
        .map(entry -> Map.entry(entry.getKey().toString(), entry.getValue().toString()))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

      return DziewonskiGilbertEllipticityCorrectorDefinition.from(nameMap);
    } else {
      throw new IllegalStateException(
        "configuration field correctionModelPluginNameByModelName is not a map!");
    }
  }
}

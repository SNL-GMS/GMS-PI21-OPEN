package gms.shared.featureprediction.plugin.prediction;

import gms.shared.event.coi.featureprediction.EllipticityCorrectionType;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class BicubicSplineFeaturePredictorConfiguration {

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  public BicubicSplineFeaturePredictorConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }


  /**
   * Gets the BicubicSplineFeaturePredictorDefinition that represents configuration
   *
   * @return BicubicSplineFeaturePredictorDefinition
   */
  @Bean
  public BicubicSplineFeaturePredictorDefinition getCurrentBicubicSplineFeaturePredictorDefinition() {

    var configName = "bicubic-spline-feature-predictor";

    boolean extrapolate = (boolean) configurationConsumerUtility.resolve(
      configName, List.of()
    ).get("extrapolate");

    var rawElliptictyCorrectionMap = (Map<?, ?>) configurationConsumerUtility.resolve(
      configName, List.of()
    ).get("ellipticityCorrectorPluginNameByCorrectionType");

    var ellipticityCorrectionsMap = rawElliptictyCorrectionMap.entrySet().stream().map(
      entry -> {
        var key = entry.getKey().toString();
        return Map.entry(EllipticityCorrectionType.valueOf(key), entry.getValue().toString());
      }
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    var rawTravelTimePluginsMap = (Map<?, ?>) configurationConsumerUtility.resolve(
      configName, List.of()
    ).get("travelTimeLookupTableByEarthModel");

    var travelTimePluginsMap = rawTravelTimePluginsMap.entrySet().stream().map(
      entry -> Map.entry(entry.getKey().toString(), entry.getValue().toString())
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return BicubicSplineFeaturePredictorDefinition.builder()
      .setExtrapolate(extrapolate)
      .setEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType(ellipticityCorrectionsMap)
      .setTravelTimeDepthDistanceLookupTablePluginNameByEarthModel(travelTimePluginsMap)
      .build();
  }

}

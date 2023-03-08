package gms.shared.featureprediction.configuration;

import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeaturePredictorConfigurationTests {

  @Test
  void testGetCurrentFeaturePredictorDefinition() {
    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var featurePredictorConfiguration = new FeaturePredictorConfiguration(
      configurationConsumerUtility);

    var expectedDefinition = FeaturePredictorDefinition.from(
      Map.of(FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE, "BicubicSplineFeaturePredictor")
    );

    var actualDefinition = featurePredictorConfiguration.getCurrentFeaturePredictorDefinition();

    Assertions.assertEquals(expectedDefinition, actualDefinition);

    Assertions.assertEquals("BicubicSplineFeaturePredictor", actualDefinition.getPluginNameByType(
      FeaturePredictionType.ARRIVAL_TIME_PREDICTION_TYPE
    ));
  }

  @Test
  void testGetCurrentFeaturePredictorDefinitionThrowsErrorForMalformedMap() {

    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("pathological-configuration-base/not-a-map")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var featurePredictorConfiguration = new FeaturePredictorConfiguration(
      configurationConsumerUtility);

    Assertions.assertThrows(
      IllegalStateException.class,
      featurePredictorConfiguration::getCurrentFeaturePredictorDefinition
    );
  }

  @Test
  void testGetCurrentFeaturePredictorDefinitionThrowsErrorForBadFeatureMeasurement() {

    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("pathological-configuration-base/no-matching-feature-measurement")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var featurePredictorConfiguration = new FeaturePredictorConfiguration(
      configurationConsumerUtility);

    Assertions.assertThrows(
      IllegalStateException.class,
      featurePredictorConfiguration::getCurrentFeaturePredictorDefinition
    );
  }
}

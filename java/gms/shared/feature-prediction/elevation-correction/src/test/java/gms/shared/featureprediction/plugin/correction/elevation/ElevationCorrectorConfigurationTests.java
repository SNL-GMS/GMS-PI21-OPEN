package gms.shared.featureprediction.plugin.correction.elevation;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ElevationCorrectorConfigurationTests {

  @Test
  void testGetCurrentElevationCorrectorDefinition() {
    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var elevationCorrectorConfiguration = new ElevationCorrectorConfiguration(configurationConsumerUtility);

    var expectedDefinition = ElevationCorrectorDefinition.from(
      Map.of("Model1Name", "PluginForModel1Name")
    );

    var actualDefinition = elevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition();

    Assertions.assertEquals(expectedDefinition, actualDefinition);
  }

  @Test
  void testGetCurrentElevationCorrectorDefinitionForMalformedMap() {
    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("pathological-configuration-base")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var elevationCorrectorConfiguration = new ElevationCorrectorConfiguration(configurationConsumerUtility);

    Assertions.assertThrows(
      IllegalStateException.class,
      elevationCorrectorConfiguration::getCurrentElevationCorrectorDefinition
    );
  }
}

package gms.shared.featureprediction.plugin.correction.ellipticity;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DziewonskiGilbertEllipticityCorrectorConfigurationTests {

  @Test
  void testGetCurrentDziewonskiGilbertEllipticityCorrectorDefinition() {
    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var dziewonskiGilbertEllipticityCorrectorConfiguration =
      new DziewonskiGilbertEllipticityCorrectorConfiguration(configurationConsumerUtility);

    var expectedDefinition = DziewonskiGilbertEllipticityCorrectorDefinition.from(
      Map.of("Model1Name", "PluginForModel1Name")
    );

    var actualDefinition = dziewonskiGilbertEllipticityCorrectorConfiguration.getCurrentDziewonskiGilbertEllipticityCorrectorDefinition();

    Assertions.assertEquals(expectedDefinition, actualDefinition);
  }

  @Test
  void testGetCurrentDziewonskiGilbertEllipticityCorrectorDefinitionForMalformedMap() {
    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader()
        .getResource("pathological-configuration-base")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var dziewonskiGilbertEllipticityCorrectorConfiguration =
      new DziewonskiGilbertEllipticityCorrectorConfiguration(configurationConsumerUtility);

    Assertions.assertThrows(
      IllegalStateException.class,
      dziewonskiGilbertEllipticityCorrectorConfiguration::getCurrentDziewonskiGilbertEllipticityCorrectorDefinition
    );
  }
}

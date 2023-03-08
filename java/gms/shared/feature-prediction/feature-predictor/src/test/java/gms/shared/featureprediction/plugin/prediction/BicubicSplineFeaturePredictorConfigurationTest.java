package gms.shared.featureprediction.plugin.prediction;

import gms.shared.event.coi.featureprediction.EllipticityCorrectionType;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BicubicSplineFeaturePredictorConfigurationTest {

  private ConfigurationConsumerUtility configurationConsumerUtility;

  @BeforeAll
  void setup() {

    String configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

  }

  @Test
  void testHappyConfig() {

    var bicubicSplineConfiguration = new BicubicSplineFeaturePredictorConfiguration(
      configurationConsumerUtility);

    var actualDefinition = bicubicSplineConfiguration.getCurrentBicubicSplineFeaturePredictorDefinition();

    Assertions.assertNotNull(actualDefinition);

    Assertions.assertFalse(actualDefinition.getExtrapolate());

    Assertions.assertEquals("MyAmazingPlugin",
      actualDefinition.getEllipticityCorrectorPluginNameByEllipticityCorrectionPluginType()
        .get(EllipticityCorrectionType.DZIEWONSKI_GILBERT));

    Assertions.assertEquals("Ak135TravelTimeLookupTable",
      actualDefinition.getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel()
        .get("Ak135"));

    Assertions.assertEquals("IaspeiTravelTimeLookupTable",
      actualDefinition.getTravelTimeDepthDistanceLookupTablePluginNameByEarthModel()
        .get("Iaspei"));


  }
}
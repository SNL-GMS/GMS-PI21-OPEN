package gms.shared.featureprediction.plugin.correction.elevation.mediumvelocity;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Ak135GlobalMediumVelocityConfigurationTests {

  @Mock
  private SystemConfig systemConfig;

  private Ak135GlobalMediumVelocityConfiguration configuration;

  @BeforeEach
  void init() {
    var configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    configuration = new Ak135GlobalMediumVelocityConfiguration(configurationConsumerUtility, systemConfig);
  }

  @Test
  void testGetDefinition() {

    var expectedDefinition = Ak135GlobalMediumVelocityDefinition.create("feature-prediction-models/global-medium-velocity/ak135");

    Assertions.assertEquals(
      expectedDefinition,
      configuration.ak135GlobalMediumVelocityDefinition()
    );
  }

  @Test
  void testGetMinIoBucketName() {
    var expectedBucket = "chumBucket";
    Mockito.when(systemConfig.getValue("minio-bucket-name")).thenReturn(expectedBucket);
    Assertions.assertEquals(expectedBucket, configuration.minIoBucketName());
  }
}

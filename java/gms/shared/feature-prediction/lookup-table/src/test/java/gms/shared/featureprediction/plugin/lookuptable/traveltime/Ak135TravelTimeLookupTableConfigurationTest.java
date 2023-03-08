package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Ak135TravelTimeLookupTableConfigurationTest {

  ConfigurationConsumerUtility configurationConsumerUtility;

  String configurationRoot;

  @Mock
  SystemConfig systemConfig;

  @BeforeAll
  void setup() {

    configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();


  }

  @Test
  void testHappyConfig() {

    when(systemConfig.getValue("minio-bucket-name")).thenReturn("feature-prediction-models");

    var ak135TravelTimeLookupTableConfiguration = new Ak135TravelTimeLookupTableConfiguration(
      configurationConsumerUtility, systemConfig);

    var actualDefinition = ak135TravelTimeLookupTableConfiguration.getTravelTimeLookupTableDefinition();

    Assertions.assertNotNull(actualDefinition);

    Assertions.assertEquals("feature-prediction-models",
      actualDefinition.getFileDescriptor().getBucket());

    Assertions.assertEquals("travel-time/ak135/",
      actualDefinition.getFileDescriptor().getKey());

  }

  @Test
  void testBadBucket() {

    when(systemConfig.getValue("minio-bucket-name")).thenReturn(null);

    var ak135TravelTimeLookupTableConfiguration = new Ak135TravelTimeLookupTableConfiguration(
      configurationConsumerUtility, systemConfig);

    Assertions.assertThrows(NullPointerException.class,
      () -> ak135TravelTimeLookupTableConfiguration.getTravelTimeLookupTableDefinition());

  }

  @Test
  void testBadPrefix() {

    configurationRoot = Objects.requireNonNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base-bad")
    ).getPath();

    configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();

    var ak135TravelTimeLookupTableConfiguration = new Ak135TravelTimeLookupTableConfiguration(
      configurationConsumerUtility, systemConfig);

    Assertions.assertThrows(IllegalArgumentException.class,
      () -> ak135TravelTimeLookupTableConfiguration.getTravelTimeLookupTableDefinition());
  }
}
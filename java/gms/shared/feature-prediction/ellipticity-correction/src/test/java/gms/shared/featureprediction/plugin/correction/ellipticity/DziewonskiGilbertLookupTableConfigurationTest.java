package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DziewonskiGilbertLookupTableConfigurationTest {

  private DziewonskiGilbertLookupTableConfiguration configuration;

  @Mock
  SystemConfig systemConfig;

  @BeforeEach
  void setUp() {
    var configurationRoot = checkNotNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();
    var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();
    configuration = new DziewonskiGilbertLookupTableConfiguration(configurationConsumerUtility, systemConfig);
  }

  @Test
  void testGetCurrentDiezwonskiGilbertLookupTableDefinition() throws JsonProcessingException {

    var expectedLookupTableDef = DziewonskiGilbertLookupTableDefinition.create(Map.of("Ak135", "location/one", "Iaspei", "location/two"));
    var dGLookupTableDef = configuration.getCurrentDiezwonskiGilbertLookupTableDefinition();
    assertEquals(expectedLookupTableDef, dGLookupTableDef);
  }

  @Test
  void testGetMinIoBucketName() {
    var expectedBucket = "chumBucket";
    Mockito.when(systemConfig.getValue("minio-bucket-name")).thenReturn(expectedBucket);
    Assertions.assertEquals(expectedBucket, configuration.minIoBucketName());
  }

}

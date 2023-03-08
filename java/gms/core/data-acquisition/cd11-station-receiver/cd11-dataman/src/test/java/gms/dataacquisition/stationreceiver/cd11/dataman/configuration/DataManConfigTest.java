package gms.dataacquisition.stationreceiver.cd11.dataman.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParameters;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.FileSystemConfigRepository;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DataManConfigTest {

  private static final String COMPONENT_NAME = "dataman";
  private static final String configurationBase = "src/test/resources/test-configuration-base";
  private static final String configurationFile = "src/test/resources/test-configuration.properties";
  private static final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);

  @Test
  void testCreate() {

    var processingConfig = createProcessingConfigFromFile();
    var systemConfig = createSystemConfigFromFile();

    var dataManConfig = assertDoesNotThrow(
      () -> DataManConfig.create(processingConfig, systemConfig));

    assertThat(dataManConfig)
      .returns(1, DataManConfig::getBackpressureBufferSize)
      .returns(6, DataManConfig::getGapExpirationDays)
      .returns("test/path", DataManConfig::getGapListStoragePath)
      .returns(7L, DataManConfig::getGapStorageIntervalMinutes);

    assertThat(dataManConfig.getRetryPolicy())
      .returns(2L, r -> r.maxAttempts)
      .returns(Duration.ofSeconds(3), r -> r.minBackoff)
      .returns(Duration.ofSeconds(4), r -> r.maxBackoff);

    var dataConsumerParameters = dataManConfig.cd11DataConsumerParameters().collect(toList());
    assertThat(dataConsumerParameters)
      .hasSize(1)
      .element(0)
      .returns("H04N", Cd11DataConsumerParameters::getStationName)
      .returns(9055, Cd11DataConsumerParameters::getPort)
      .returns(true, Cd11DataConsumerParameters::isAcquired)
      .returns(false, Cd11DataConsumerParameters::isFrameProcessingDisabled);
  }

  private static ConfigurationConsumerUtility createProcessingConfigFromFile() {
    var fileConfigRepo = FileConfigurationRepository.create(Path.of(configurationBase));
    return ConfigurationConsumerUtility
      .builder(fileConfigRepo)
      .configurationNamePrefixes(List.of(COMPONENT_NAME + "."))
      .retryConfiguration(retryConfig)
      .build();
  }

  private static SystemConfig createSystemConfigFromFile() {
    var fileConfigRepo = FileSystemConfigRepository.builder()
      .setFilename(configurationFile)
      .build();

    return SystemConfig.create(COMPONENT_NAME, fileConfigRepo);
  }
}

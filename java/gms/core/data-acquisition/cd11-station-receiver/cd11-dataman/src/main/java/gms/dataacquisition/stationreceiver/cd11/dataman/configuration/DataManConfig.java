package gms.dataacquisition.stationreceiver.cd11.dataman.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParameters;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParametersTemplatesFile;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DataManConfig {

  Logger logger = LoggerFactory.getLogger(DataManConfig.class);
  private static final String SEPARATOR = ".";
  private static final String CONFIGURATION_NAME_PREFIX = "dataman" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> CONFIGURATION_NAME_PREFIX + s;

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private final RetryBackoffSpec retryPolicy;

  private final int backpressureBufferSize;
  private final int cd11DataConsumerBasePort;
  private final String gapListStoragePath;
  private final int gapExpirationDays;
  private final long gapStorageIntervalMinutes;

  private static final String CONFIGURATION_NAME =
    KEY_BUILDER.apply("station-parameters");


  private DataManConfig(ConfigurationConsumerUtility configurationConsumerUtility, RetryBackoffSpec retryPolicy,
    int backpressureBufferSize, int cd11DataConsumerBasePort, String gapListStoragePath, int gapExpirationDays,
    long gapStorageIntervalMinutes) {
    this.configurationConsumerUtility = configurationConsumerUtility;
    this.retryPolicy = retryPolicy;
    this.backpressureBufferSize = backpressureBufferSize;
    this.cd11DataConsumerBasePort = cd11DataConsumerBasePort;
    this.gapListStoragePath = gapListStoragePath;
    this.gapExpirationDays = gapExpirationDays;
    this.gapStorageIntervalMinutes = gapStorageIntervalMinutes;
  }

  public static DataManConfig create(
    ConfigurationConsumerUtility processingConfigurationConsumerUtility,
    SystemConfig systemConfig) {

    RetryBackoffSpec retryPolicy = createRetryPolicy(systemConfig);

    return new DataManConfig(processingConfigurationConsumerUtility,
      retryPolicy,
      systemConfig.getValueAsInt("reactor-backpressure-buffer"),
      systemConfig.getValueAsInt("cd11-dataconsumer-baseport"),
      systemConfig.getValue("gap-storage-path"),
      systemConfig.getValueAsInt("gap-expiration-days"),
      systemConfig.getValueAsLong("gap-storage-interval-minutes"));
  }

  private static RetryBackoffSpec createRetryPolicy(SystemConfig systemConfig) {
    var retryBackoffUnits = ChronoUnit.valueOf(systemConfig.getValue("retry-backoff-units"));

    return Retry.backoff(systemConfig.getValueAsInt("retry-max-attempts"),
        Duration.of(systemConfig.getValueAsInt("retry-min-backoff"), retryBackoffUnits))
      .maxBackoff(Duration.of(systemConfig.getValueAsInt("retry-max-backoff"), retryBackoffUnits));
  }

  public RetryBackoffSpec getRetryPolicy() {
    return retryPolicy;
  }

  public int getBackpressureBufferSize() {
    return backpressureBufferSize;
  }

  public String getGapListStoragePath() {
    return this.gapListStoragePath;
  }

  public int getGapExpirationDays() {
    return this.gapExpirationDays;
  }

  public long getGapStorageIntervalMinutes() {
    return this.gapStorageIntervalMinutes;
  }

  public Stream<Cd11DataConsumerParameters> cd11DataConsumerParameters() {
    Cd11DataConsumerParametersTemplatesFile stationConfig = configurationConsumerUtility
      .resolve(CONFIGURATION_NAME, Collections.emptyList(),
        Cd11DataConsumerParametersTemplatesFile.class);

    return stationConfig.getCd11DataConsumerParametersTemplates().stream()
      .map(template -> {
        logger.debug("Constructing Consumer parameters for station {} and port {} (basePort {} + offset {})",
          template.getStationName(), cd11DataConsumerBasePort + template.getPortOffset(),
          cd11DataConsumerBasePort, template.getPortOffset());
        return Cd11DataConsumerParameters.create(template, cd11DataConsumerBasePort);
      });
  }
}

package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Optional;


@AutoValue
public abstract class Cd11DataProviderConfig {

  public abstract String getProviderInputMode();

  public abstract Optional<FileRsdfSourceConfig> getFileConfig();

  public abstract Optional<KafkaRsdfSourceConfig> getKafkaConfig();

  public abstract String getConnectionFrameCreator();

  public abstract String getConnectionFrameDestination();

  @JsonCreator
  public static Cd11DataProviderConfig from(
    @JsonProperty("providerInputMode") String providerInputMode,
    @JsonProperty("fileConfig") Optional<FileRsdfSourceConfig> fileConfig,
    @JsonProperty("autoOffsetResetConfig") Optional<KafkaRsdfSourceConfig> kafkaConfig,
    @JsonProperty("connectionFrameCreator") String connectionFrameCreator,
    @JsonProperty("connectionFrameDestination") String connectionFrameDestination
  ) {
    return new AutoValue_Cd11DataProviderConfig(providerInputMode, fileConfig,
      kafkaConfig, connectionFrameCreator, connectionFrameDestination);
  }
}

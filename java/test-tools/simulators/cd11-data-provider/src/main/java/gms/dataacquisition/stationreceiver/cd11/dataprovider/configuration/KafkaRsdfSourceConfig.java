package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Optional;

@AutoValue
public abstract class KafkaRsdfSourceConfig {

  public abstract String getApplicationId();

  public abstract String getSourceTopic();

  public abstract Optional<String> getAutoOffsetResetConfig();

  @JsonCreator
  public static KafkaRsdfSourceConfig from(
    @JsonProperty("applicationId") String applicationId,
    @JsonProperty("sourceTopic") String sourceTopic,
    @JsonProperty("autoOffsetResetConfig") Optional<String> autoOffsetResetConfig
  ) {
    return new AutoValue_KafkaRsdfSourceConfig(applicationId, sourceTopic, autoOffsetResetConfig);
  }
}

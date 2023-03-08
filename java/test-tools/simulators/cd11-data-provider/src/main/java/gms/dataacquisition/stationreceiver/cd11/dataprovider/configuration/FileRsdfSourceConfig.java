package gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Optional;

@AutoValue
public abstract class FileRsdfSourceConfig {

  public abstract String getDataLocation();

  public abstract Instant getReferenceTime();

  public abstract Optional<Long> getInitialDelaySeconds();

  @JsonCreator
  public static FileRsdfSourceConfig from(
    @JsonProperty("dataLocation") String dataLocation,
    @JsonProperty("referenceTime") Instant referenceTime,
    @JsonProperty("initialDelaySeconds") Optional<Long> initialDelaySeconds) {
    return new AutoValue_FileRsdfSourceConfig(dataLocation, referenceTime, initialDelaySeconds);
  }
}

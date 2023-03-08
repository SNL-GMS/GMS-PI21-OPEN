package gms.core.performancemonitoring.ssam.control.datapublisher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;


@AutoValue
public abstract class SystemEvent<T> {
  public abstract String getSpecversion();

  public abstract String getType();

  public abstract String getSource();

  public abstract String getId();

  public abstract T getData();

  public static <T> SystemEvent<T> from(
    String eventType, T data
  ) {
    return SystemEvent.create("0.2", eventType,
      "system", UUID.randomUUID().toString(), data);
  }

  @JsonCreator
  public static <T> SystemEvent<T> create(
    @JsonProperty("specversion") String specversion,
    @JsonProperty("type") String type,
    @JsonProperty("source") String source,
    @JsonProperty("id") String id,
    @JsonProperty("data") T data) {

    return new AutoValue_SystemEvent<>(specversion, type,
      source, id, data);
  }
}

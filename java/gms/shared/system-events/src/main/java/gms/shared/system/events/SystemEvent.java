package gms.shared.system.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SystemEvent {
  public abstract String getSpecversion();

  public abstract String getType();

  public abstract String getSource();

  public abstract String getId();

  public abstract Object getData();

  public static SystemEvent from(
    String eventType, Object data, int id
  ) {
    return SystemEvent.create("0.2", eventType,
      "rig", "" + id, data);
  }

  @JsonCreator
  public static SystemEvent create(
    @JsonProperty("specversion") String specversion,
    @JsonProperty("type") String type,
    @JsonProperty("source") String source,
    @JsonProperty("id") String id,
    @JsonProperty("data") Object data) {

    return new AutoValue_SystemEvent(specversion, type,
      source, id, data);
  }
}

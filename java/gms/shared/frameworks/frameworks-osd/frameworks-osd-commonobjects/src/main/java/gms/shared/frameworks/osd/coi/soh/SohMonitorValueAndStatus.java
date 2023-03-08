package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Optional;

/**
 * Represents a generic monitored value and status.
 *
 * @param <T> The type of the monitored value.
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = As.EXISTING_PROPERTY,
  property = "monitorType",

  visible = true
)
@JsonTypeIdResolver(SohValueIdResolver.class)
public interface SohMonitorValueAndStatus<T extends Comparable<T>> {

  /**
   * @return Monitored value
   */
  Optional<T> getValue();

  /**
   * @return monitored status
   */
  SohStatus getStatus();

  /**
   * @return monitor type
   */
  SohMonitorType getMonitorType();

  //
  // Convenience fields - not part of the COI but useful to those who use this object.
  //

  /**
   * return the SohValueType of the monitor type. This is used where SohMonitorType.getValueType()
   * is not accessible, as in UI code that processes the serialized form.
   */
  @JsonProperty("sohValueType")
  default SohMonitorType.SohValueType sohValueType() {
    return getMonitorType().getSohValueType();
  }

}

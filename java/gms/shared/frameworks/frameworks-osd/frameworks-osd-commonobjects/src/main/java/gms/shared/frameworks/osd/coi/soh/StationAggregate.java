package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Optional;

/**
 * Represents a generic aggregate value and status.
 *
 * @param <T> The type of the aggregated value.
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = As.EXISTING_PROPERTY,
  property = "aggregateType",

  visible = true
)
@JsonTypeIdResolver(StationValueIdResolver.class)
public interface StationAggregate<T extends Comparable<T>> {

  /**
   * @return Aggregate value
   */
  Optional<T> getValue();

  /**
   * @return station aggregate type
   */
  StationAggregateType getAggregateType();

  //
  // Convenience fields - not part of the COI but useful to those who use this object.
  //

  /**
   * return the StationValueType. This is used where StationAggregateType.getValueType() is not accessible, as in UI
   * code that processes the serialized form.
   */
  @JsonProperty("stationValueType")
  default StationAggregateType.StationValueType stationValueType() {
    return getAggregateType().getStationValueType();
  }
}

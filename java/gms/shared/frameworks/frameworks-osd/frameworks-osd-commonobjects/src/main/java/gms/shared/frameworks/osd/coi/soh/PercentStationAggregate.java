package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType.StationValueType;
import org.msgpack.core.Preconditions;

import java.util.Optional;

/**
 * Represents a aggregate value that is a percentage, such as the percentage of missing data.
 */
@AutoValue
public abstract class PercentStationAggregate implements StationAggregate<Double> {

  // hiding public default constructor
  PercentStationAggregate() {
  }

  /**
   * Create a new PercentStationAggregate object
   *
   * @param value aggregate value
   * @param stationAggregateType monitor type
   */
  @JsonCreator
  public static PercentStationAggregate from(
    @JsonProperty("value") Double value,
    @JsonProperty("aggregateType") StationAggregateType stationAggregateType
  ) {
    Preconditions.checkState(stationAggregateType.getStationValueType() == StationValueType.PERCENT,
      String.format("%s  is of %s.  Must be of type %s",
        StationAggregateType.class.getName(),
        stationAggregateType.toString(),
        StationValueType.PERCENT.toString()));

    if (value != null && !value.isNaN()) {
      Preconditions.checkArgument(value >= 0 && value <= 100,
        "Requires a value between 0 and 100");
    }

    return new AutoValue_PercentStationAggregate(
      Optional.ofNullable(value),
      stationAggregateType
    );
  }
}

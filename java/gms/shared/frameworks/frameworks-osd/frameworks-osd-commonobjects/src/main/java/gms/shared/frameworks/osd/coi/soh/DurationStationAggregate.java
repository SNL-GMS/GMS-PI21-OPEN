package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType.StationValueType;
import org.msgpack.core.Preconditions;

import java.time.Duration;
import java.util.Optional;

/**
 * Represents a station aggregate value of type Duration, such as latency.
 */
@AutoValue
public abstract class DurationStationAggregate implements StationAggregate<Duration> {

  DurationStationAggregate() {
  }

  /**
   * Create a new DurationStationAggregate object
   *
   * @param value station aggregate value
   * @param stationAggregateType type
   */
  @JsonCreator
  public static DurationStationAggregate from(
    @JsonProperty("value") Duration value,
    @JsonProperty("aggregateType") StationAggregateType stationAggregateType
  ) {

    Preconditions.checkState(stationAggregateType.getStationValueType() == StationValueType.DURATION,
      String.format("%s is of %s. Must be of type %s",
        StationAggregateType.class.getName(),
        stationAggregateType.getStationValueType().toString(),
        StationValueType.DURATION.toString()));

    return new AutoValue_DurationStationAggregate(
      Optional.ofNullable(value),
      stationAggregateType
    );
  }
}

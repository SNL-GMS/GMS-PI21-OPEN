package gms.shared.signaldetection.coi.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represents an arrival time measurement.
 */
@AutoValue
public abstract class ArrivalTimeMeasurementValue implements Serializable {
  /**
   * Gets the value of the arrivalTime instant value
   *
   * @return arrival time
   */
  public abstract InstantValue getArrivalTime();

  /**
   * Gets the value of the travleTime duration value
   *
   * @return travel time
   */
  public abstract Optional<DurationValue> getTravelTime();

  @JsonCreator
  public static ArrivalTimeMeasurementValue from(
    @JsonProperty("arrivalTime") InstantValue arrivalTime,
    @JsonProperty("travelTime") Optional<DurationValue> travelTime
  ) {
    return new AutoValue_ArrivalTimeMeasurementValue(arrivalTime, travelTime);
  }

  public static ArrivalTimeMeasurementValue fromFeatureMeasurement(InstantValue arrivalTime) {
    return from(arrivalTime, Optional.empty());
  }

  public static ArrivalTimeMeasurementValue fromFeaturePrediction(
    InstantValue arrivalTime, DurationValue travelTime) {
    return from(arrivalTime, Optional.of(travelTime));
  }
}

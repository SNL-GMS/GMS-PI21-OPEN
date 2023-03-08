package gms.shared.signaldetection.coi.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents a numerical measurement made at a particular time.
 * <p>
 * Corresponds to SOURCE_TO_RECEIVER_AZIMUTH, RECEIVER_TO_SOURCE_AZIMUTH, SLOWNESS,
 * SOURCE_TO_RECEIVER_DISTANCE, EMERGENCE_ANGLE, RECTILINEARITY, SNR FeatureMeasurementTypes
 */
@AutoValue
public abstract class NumericMeasurementValue implements Serializable {

  /**
   * Gets the time this measurement was made at.
   *
   * @return reference time
   */
  public abstract Optional<Instant> getReferenceTime();

  /**
   * Gets the measured value of this measurement
   *
   * @return the value
   */
  public abstract DoubleValue getMeasuredValue();

  /**
   * Recreation factory method (sets the NumericalMeasurement entity identity). Used for
   * deserialization and recreating from persistence.
   *
   * @param referenceTime The reference time
   * @param measuredValue The measured value
   * @return NumericMeasurementValue
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static NumericMeasurementValue from(
    @JsonProperty("referenceTime") Optional<Instant> referenceTime,
    @JsonProperty("measuredValue") DoubleValue measuredValue
  ) {
    return new AutoValue_NumericMeasurementValue(referenceTime, measuredValue);
  }

  public static NumericMeasurementValue fromFeatureMeasurement(
    Instant referenceTime,
    DoubleValue measuredValue) {
    return from(Optional.of(referenceTime), measuredValue);
  }

  public static NumericMeasurementValue fromFeaturePrediction(
    DoubleValue measuredValue) {
    return from(Optional.empty(), measuredValue);
  }
}

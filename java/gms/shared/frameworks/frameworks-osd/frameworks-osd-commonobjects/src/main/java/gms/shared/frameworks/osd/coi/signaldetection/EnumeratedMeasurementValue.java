package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.PhaseType;

import java.io.Serializable;

/**
 * An enumerated measurement is a measurement that has a value that is an enumerated type and has a
 * confidence on that selection of that value.
 *
 * @param <E> the type of Enum
 */
public abstract class EnumeratedMeasurementValue<E extends Enum<E>> implements Serializable {

  /**
   * Gets the value of this enumerated measurement.
   *
   * @return the value
   */
  public abstract E getValue();

  /**
   * Gets the confidence level of this enumerated measurement.
   *
   * @return the confidence level
   */
  public abstract double getConfidence();

  /**
   * An enumerated measurement of PhaseType.
   */
  @AutoValue
  public abstract static class PhaseTypeMeasurementValue extends
    EnumeratedMeasurementValue<PhaseType> {

    @JsonCreator
    public static PhaseTypeMeasurementValue from(
      @JsonProperty("value") PhaseType value,
      @JsonProperty("confidence") double confidence) {
      return new AutoValue_EnumeratedMeasurementValue_PhaseTypeMeasurementValue(value, confidence);
    }
  }

  /**
   * An enumerated measurement of FirstMotionType.
   */
  @AutoValue
  public abstract static class FirstMotionMeasurementValue extends
    EnumeratedMeasurementValue<FirstMotionType> {

    @JsonCreator
    public static FirstMotionMeasurementValue from(
      @JsonProperty("value") FirstMotionType value,
      @JsonProperty("confidence") double confidence) {
      return new AutoValue_EnumeratedMeasurementValue_FirstMotionMeasurementValue(value,
        confidence);
    }
  }

}

package gms.shared.signaldetection.coi.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.types.FirstMotionType;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * An enumerated measurement of FirstMotionType.
 */
@AutoValue
public abstract class FirstMotionMeasurementValue implements
  EnumeratedMeasurementValue<FirstMotionType> {

  /**
   * FirstMotionMeasurementValue from creator
   *
   * @param value FirstMotionType enum
   * @param confidence confidence value for measurement [0.0, 1.0]
   * @param referenceTime sample time for where in the ChannelSegment the measurement was made
   * @return FirstMotionMeasurementValue
   */
  @JsonCreator
  public static FirstMotionMeasurementValue from(
    @JsonProperty("value") FirstMotionType value,
    @JsonProperty("confidence") Optional<Double> confidence,
    @JsonProperty("referenceTime") Optional<Instant> referenceTime) {
    confidence.ifPresent(confidenceVal -> checkState(0.0 <= confidenceVal && confidenceVal <= 1.0,
      "Confidence must be between 0.0 and 1.0 inclusive"));
    return new AutoValue_FirstMotionMeasurementValue(value, confidence, referenceTime);
  }

  public static FirstMotionMeasurementValue fromFeatureMeasurement(FirstMotionType value,
    Optional<Double> confidence, Instant referenceTime) {
    return from(value, confidence, Optional.of(referenceTime));
  }

  public static FirstMotionMeasurementValue fromFeaturePrediction(FirstMotionType value,
    Optional<Double> confidence) {
    return from(value, confidence, Optional.empty());
  }
}

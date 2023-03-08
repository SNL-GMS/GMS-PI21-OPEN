package gms.shared.signaldetection.coi.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.types.PhaseType;

import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * An enumerated measurement of PhaseType.
 */
@AutoValue
public abstract class PhaseTypeMeasurementValue implements
  EnumeratedMeasurementValue<PhaseType> {

  /**
   * PhaseTypeMeasurementValue from creator
   *
   * @param value PhaseType enum
   * @param confidence confidence value for measuremet [0.0, 1.0]
   * @param referenceTime sample time for where in the ChannelSegment the measurement was made
   * @return FirstMotionMeasurementValue
   */
  @JsonCreator
  public static PhaseTypeMeasurementValue from(
    @JsonProperty("value") PhaseType value,
    @JsonProperty("confidence") Optional<Double> confidence,
    @JsonProperty("referenceTime") Optional<Instant> referenceTime) {
    confidence.ifPresent(confidenceVal -> checkState(0.0 <= confidenceVal && confidenceVal <= 1.0,
      "Confidence must be between 0.0 and 1.0 inclusive"));
    return new AutoValue_PhaseTypeMeasurementValue(value, confidence, referenceTime);
  }

  /**
   * Create {@link PhaseTypeMeasurementValue} feature measurement from
   * phase type, confidence and reference time
   *
   * @param value {@link PhaseType} input
   * @param confidence optional confidence value
   * @param referenceTime reference time for the phase
   * @return {@link PhaseTypeMeasurementValue} feature measurement value
   */
  public static PhaseTypeMeasurementValue fromFeatureMeasurement(PhaseType value,
    Optional<Double> confidence, Instant referenceTime) {
    return from(value, confidence, Optional.of(referenceTime));
  }

  /**
   * Create {@link PhaseTypeMeasurementValue} feature measurement from
   * phase type and confidence
   *
   * @param value {@link PhaseType} input
   * @param confidence optional confidence value
   * @return {@link PhaseTypeMeasurementValue} feature measurement value
   */
  public static PhaseTypeMeasurementValue fromFeaturePrediction(PhaseType value,
    Optional<Double> confidence) {
    return from(value, confidence, Optional.empty());
  }
}

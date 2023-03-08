package gms.shared.signaldetection.coi.values;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Represents an amplitude measurement.
 * <p>
 * Corresponds to AMPLITUDE_A5_OVER_2 FeatureMeasurementType
 */
@AutoValue
public abstract class AmplitudeMeasurementValue implements Serializable {
  private static final String PERIOD_NON_ZERO = "Period should be non-zero";

  /**
   * Gets the value of the measurement
   *
   * @return the amplitude value
   */
  public abstract DoubleValue getAmplitude();

  /**
   * Gets the period of this amplitude measurement
   *
   * @return the period
   */
  public abstract Duration getPeriod();


  /**
   * Gets the measurement time this amplitude measurement was made.
   *
   * @return the measurement time of the measurement
   */
  public abstract Optional<Instant> getMeasurementTime();

  /**
   * Measurement window start time where amplitude was measured
   *
   * @return the measurement window start time
   */
  public abstract Optional<Instant> getMeasurementWindowStart();


  /**
   * Measurement window duration where amplitude was measured
   *
   * @return the measurement window duration
   */
  public abstract Optional<Duration> getMeasurementWindowDuration();

  /**
   * A boolean flag specifying whether the measured data was clipped
   *
   * @return is clipped boolean flag
   */
  public abstract Optional<Boolean> isClipped();

  /**
   * Creates an AmplitudeMeasurementValue
   *
   * @param amplitude The amplitude
   * @param period The period
   * @param measurementTime The measurement time
   * @param measurementWindowStart The measurement window start time
   * @param measurementWindowDuration The measurement window duration
   * @param clipped Boolean on whether the amplitude was clipped
   * @return AmplitudeMeasurementValue
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static AmplitudeMeasurementValue from(
    @JsonProperty("amplitude") DoubleValue amplitude,
    @JsonProperty("period") Duration period,
    @JsonProperty("measurementTime") Optional<Instant> measurementTime,
    @JsonProperty("measurementWindowStart") Optional<Instant> measurementWindowStart,
    @JsonProperty("measurementWindowDuration") Optional<Duration> measurementWindowDuration,
    @JsonProperty("clipped") Optional<Boolean> clipped
  ) {
    checkState(!period.isZero(), PERIOD_NON_ZERO);

    return new AutoValue_AmplitudeMeasurementValue(amplitude, period, measurementTime,
      measurementWindowStart, measurementWindowDuration, clipped);
  }

  public static AmplitudeMeasurementValue fromFeatureMeasurement(
    DoubleValue amplitude,
    Duration period,
    Instant measurementTime,
    Instant measurementWindowStart,
    Duration measurementWindowDuration,
    Boolean isClipped
  ) {
    return from(amplitude, period, Optional.of(measurementTime),
      Optional.of(measurementWindowStart), Optional.of(measurementWindowDuration), Optional.of(isClipped));
  }

  public static AmplitudeMeasurementValue fromFeaturePrediction(
    DoubleValue amplitude,
    Duration period
  ) {
    return from(amplitude, period, Optional.empty(), Optional.empty(),
      Optional.empty(), Optional.empty());
  }
}

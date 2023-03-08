package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.DoubleValue;

import java.time.Duration;
import java.util.Objects;

/**
 * Represents a limited set of calibration information used during the acquisition of data streams.
 */

@AutoValue
public abstract class Calibration {

  public abstract double getCalibrationPeriodSec();

  public abstract Duration getCalibrationTimeShift();

  public abstract DoubleValue getCalibrationFactor();

  @JsonCreator
  public static Calibration from(
    @JsonProperty("calibrationPeriodSec") double calibrationPeriodSec,
    @JsonProperty("calibrationTimeShift") Duration calibrationTimeShift,
    @JsonProperty("calibrationFactor") DoubleValue calibrationFactor) {

    Objects.requireNonNull(calibrationPeriodSec, "Cannot create Calibration from null calibrationPeriodSec");
    Objects.requireNonNull(calibrationTimeShift, "Cannot create Calibration from null calibrationTimeShift");
    Objects.requireNonNull(calibrationFactor, "Cannot create Calibration from null calibrationFactor");

    return new AutoValue_Calibration(calibrationPeriodSec, calibrationTimeShift, calibrationFactor);
  }

}

package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.signaldetection.Calibration;

import java.time.Duration;
import java.util.Objects;

/**
 * As with the manufacturer-provided calibration information, this calibration information is stored
 * in the Calibration and Response classes. Calibration information is used to convert the output of
 * the instrument (e.g., volts, counts) into the phenomenon that the instrument is measuring (e.g.,
 * seismic ground displacement). The Calibration class includes information about when an instrument
 * was calibrated (actual change time) and what the response (calibration factor) was for a
 * particular calibration period (i.e., inverse of frequency). Response includes the full response
 * function across a range of periods/frequencies. The actual change time attribute in both the
 * Calibration and Response classes captures when the calibration was actually performed, and both
 * classes also include system change time as attributes, in order to track when the response
 * information was available for use by the System.
 */

@AutoValue
public abstract class ReferenceCalibration {

  public abstract Duration getCalibrationInterval();

  public abstract Calibration getCalibration();

  @JsonCreator
  public static ReferenceCalibration from(
    @JsonProperty("calibrationInterval") Duration calibrationInterval,
    @JsonProperty("calibration") Calibration calibration) {

    Objects.requireNonNull(calibrationInterval,
      "Cannot create ReferenceCalibration with null calibrationInterval");
    Objects.requireNonNull(calibration,
      "Cannot create ReferenceCalibration with null calibration");

    return new AutoValue_ReferenceCalibration(calibrationInterval, calibration);
  }

}

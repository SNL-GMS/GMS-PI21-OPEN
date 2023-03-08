package gms.shared.frameworks.osd.coi.signaldetection;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Optional;

/**
 * Periodically, the instrument corresponding to a Channel is calibrated to characterize the true
 * relationship between the underlying phenomenon the instrument is measuring and the actual output
 * of the instrument. As with the manufacturer-provided calibration information, this calibration
 * information is stored in the Calibration and Response classes. Response includes the full
 * response function across a range of periods/frequencies.
 */

@AutoValue
public abstract class Response {

  public abstract String getChannelName();

  public abstract Calibration getCalibration();

  public abstract Optional<FrequencyAmplitudePhase> getFapResponse();

  @JsonCreator
  public static Response from(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("calibration") Calibration calibration,
    @JsonProperty("fapResponse") FrequencyAmplitudePhase fapResponse) {

    return new AutoValue_Response(channelName, calibration,
      Optional.ofNullable(fapResponse));
  }
}

package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.signaldetection.FrequencyAmplitudePhase;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
@JsonSerialize(as = ReferenceResponse.class)
@JsonDeserialize(builder = AutoValue_ReferenceResponse.Builder.class)
public abstract class ReferenceResponse {

  public abstract UUID getReferenceResponseId();

  public abstract String getChannelName();

  public abstract Instant getActualTime();

  public abstract Instant getSystemTime();

  public abstract String getComment();

  public abstract Optional<ReferenceSourceResponse> getSourceResponse();

  public abstract ReferenceCalibration getReferenceCalibration();

  public abstract Optional<FrequencyAmplitudePhase> getFapResponse();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceResponse.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    public abstract Builder setReferenceResponseId(UUID referenceResponseId);

    public abstract Builder setChannelName(String channelName);

    abstract String getChannelName();

    public abstract Builder setActualTime(Instant actualTime);

    public abstract Instant getActualTime();

    public abstract Builder setSystemTime(Instant systemTime);

    public abstract Instant getSystemTime();

    public abstract Builder setComment(String comment);

    public abstract String getComment();

    public abstract Builder setSourceResponse(ReferenceSourceResponse sourceResponse);

    @JsonProperty
    public abstract Builder setSourceResponse(Optional<ReferenceSourceResponse> sourceResponse);

    public abstract Builder setReferenceCalibration(ReferenceCalibration referenceCalibration);

    @JsonProperty
    public abstract Builder setFapResponse(Optional<FrequencyAmplitudePhase> response);

    public abstract Builder setFapResponse(FrequencyAmplitudePhase response);

    abstract ReferenceResponse autoBuild();

    public ReferenceResponse build() {
      setReferenceResponseId(UUID.nameUUIDFromBytes((this.getChannelName() + this.getActualTime()
        + this.getSystemTime() + this.getComment())
        .getBytes(StandardCharsets.UTF_16LE)));

      return autoBuild();
    }
  }
}

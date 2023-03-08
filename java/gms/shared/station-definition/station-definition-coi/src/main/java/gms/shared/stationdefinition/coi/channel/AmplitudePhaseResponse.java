package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

/**
 * A value class for storing an amplitude and phase, used in creating a {@link
 * FrequencyAmplitudePhase} object.
 */
@AutoValue
@JsonPropertyOrder(alphabetic = true)
public abstract class AmplitudePhaseResponse {

  public abstract DoubleValue getAmplitude();

  public abstract DoubleValue getPhase();

  @JsonCreator
  public static AmplitudePhaseResponse from(
    @JsonProperty("amplitude") DoubleValue amplitude,
    @JsonProperty("phase") DoubleValue phase) {
    return new AutoValue_AmplitudePhaseResponse(amplitude, phase);
  }
}

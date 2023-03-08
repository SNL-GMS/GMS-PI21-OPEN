package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.DoubleValue;

import java.util.Objects;

/**
 * A value class for storing an amplitude and phase, used in creating a {@link
 * FrequencyAmplitudePhase} object.
 */
@AutoValue
public abstract class AmplitudePhaseResponse {

  public abstract DoubleValue getAmplitude();

  public abstract DoubleValue getPhase();

  @JsonCreator
  public static AmplitudePhaseResponse from(
    @JsonProperty("amplitude") DoubleValue amplitude,
    @JsonProperty("phase") DoubleValue phase) {

    Objects.requireNonNull(amplitude, "Cannot create AmplitudePhaseResponse from null amplitude");
    Objects.requireNonNull(phase, "Cannot create AmplitudePhaseResponse from null phase");

    return new AutoValue_AmplitudePhaseResponse(amplitude, phase);
  }
}

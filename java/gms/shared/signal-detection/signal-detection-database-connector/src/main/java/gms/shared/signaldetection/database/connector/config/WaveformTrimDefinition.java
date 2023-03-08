package gms.shared.signaldetection.database.connector.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

import java.time.Duration;

@AutoValue
public abstract class WaveformTrimDefinition {

  public abstract Duration getMeasuredWaveformLeadDuration();

  public abstract Duration getMeasuredWaveformLagDuration();

  @JsonCreator
  public static WaveformTrimDefinition create(
    @JsonProperty("measuredWaveformLeadDuration") Duration measuredWaveformLeadDuration,
    @JsonProperty("measuredWaveformLagDuration") Duration measuredWaveformLagDuration) {

    Preconditions.checkState(!measuredWaveformLeadDuration.isNegative(),
      "Measured waveform lead duration cannot be negative");
    Preconditions.checkState(!measuredWaveformLagDuration.isNegative(),
      "Measured waveform lag duration cannot be negative");

    return new AutoValue_WaveformTrimDefinition(measuredWaveformLeadDuration,
      measuredWaveformLagDuration);
  }

}

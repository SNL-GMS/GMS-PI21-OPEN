package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Duration;
import java.util.Optional;

@AutoValue
@JsonSerialize(as = CascadeFilterParameters.class)
public abstract class CascadeFilterParameters {
  public abstract double getSampleRateHz();

  public abstract double getSampleRateToleranceHz();

  public abstract Optional<Duration> getGroupDelaySec();

  @JsonCreator
  public static CascadeFilterParameters from(
    @JsonProperty("sampleRateHz") double sampleRateHz,
    @JsonProperty("sampleRateToleranceHz") double sampleRateToleranceHz,
    @JsonProperty("groupDelaySec") Optional<Duration> groupDelaySec) {

    return new AutoValue_CascadeFilterParameters(sampleRateHz, sampleRateToleranceHz, groupDelaySec);
  }

}

package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Duration;

@AutoValue
@JsonSerialize(as = LinearFilterParameters.class)
public abstract class LinearFilterParameters {

  public abstract double getSampleRateHz();

  public abstract double getSampleRateToleranceHz();

  public abstract ImmutableList<Double> getaCoefficients();

  public abstract ImmutableList<Double> getbCoefficients();

  public abstract Duration getGroupDelaySec();

  @JsonCreator
  public static LinearFilterParameters from(
    @JsonProperty("sampleRateHz") double sampleRateHz,
    @JsonProperty("sampleRateToleranceHz") double sampleRateToleranceHz,
    @JsonProperty("aCoefficients") ImmutableList<Double> aCoefficients,
    @JsonProperty("bCoefficients") ImmutableList<Double> bCoefficients,
    @JsonProperty("groupDelaySec") Duration groupDelaySec) {

    return new AutoValue_LinearFilterParameters(sampleRateHz, sampleRateToleranceHz, aCoefficients,
      bCoefficients, groupDelaySec);
  }
}

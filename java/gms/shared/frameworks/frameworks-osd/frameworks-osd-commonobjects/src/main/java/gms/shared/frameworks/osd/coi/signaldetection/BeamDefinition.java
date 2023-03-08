package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.frameworks.osd.coi.PhaseType;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkState;

@AutoValue
public abstract class BeamDefinition {

  public abstract PhaseType getPhaseType();

  public abstract double getAzimuth();

  public abstract double getSlowness();

  public abstract boolean isCoherent();

  public abstract boolean isSnappedSampling();

  public abstract boolean isTwoDimensional();

  public abstract double getNominalWaveformSampleRate();

  public abstract double getWaveformSampleRateTolerance();

  public abstract int getMinimumWaveformsForBeam();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_BeamDefinition.Builder();
  }

  @Memoized
  public Duration computeNominalWaveformSamplePeriod() {
    return Duration.ofNanos((long) (1E9 / getNominalWaveformSampleRate()));
  }

  @JsonCreator
  public static BeamDefinition from(
    @JsonProperty("phaseType") PhaseType phaseType,
    @JsonProperty("azimuth") double azimuth,
    @JsonProperty("slowness") double slowness,
    @JsonProperty("coherent") boolean coherent,
    @JsonProperty("snappedSampling") boolean snappedSampling,
    @JsonProperty("twoDimensional") boolean twoDimensional,
    @JsonProperty("nominalWaveformSampleRate") double nominalWaveformSampleRate,
    @JsonProperty("waveformSampleRateTolerance") double waveformSampleRateTolerance,
    @JsonProperty("minimumWaveformsForBeam") int minimumWaveformsForBeam) {

    return BeamDefinition.builder()
      .setPhaseType(phaseType)
      .setAzimuth(azimuth)
      .setSlowness(slowness)
      .setCoherent(coherent)
      .setSnappedSampling(snappedSampling)
      .setTwoDimensional(twoDimensional)
      .setNominalWaveformSampleRate(nominalWaveformSampleRate)
      .setWaveformSampleRateTolerance(waveformSampleRateTolerance)
      .setMinimumWaveformsForBeam(minimumWaveformsForBeam)
      .build();
  }


  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setPhaseType(PhaseType phaseType);

    public abstract Builder setAzimuth(double azimuth);

    public abstract Builder setSlowness(double slowness);

    public abstract Builder setCoherent(boolean coherent);

    public abstract Builder setSnappedSampling(boolean snappedSampling);

    public abstract Builder setTwoDimensional(boolean twoDimensional);

    public abstract Builder setNominalWaveformSampleRate(double nominalSampleRate);

    public abstract Builder setWaveformSampleRateTolerance(double sampleRateTolerance);

    public abstract Builder setMinimumWaveformsForBeam(int minimumWaveformsForBeam);

    abstract BeamDefinition autoBuild();

    public BeamDefinition build() {
      BeamDefinition beamDefinition = autoBuild();

      checkState(beamDefinition.getAzimuth() >= 0 && beamDefinition.getAzimuth() <= 360,
        "Error creating BeamDefinition, azimuth must be between 0 and 360, inclusive");

      checkState(beamDefinition.getMinimumWaveformsForBeam() >= 1,
        "Error creating BeamDefinition, minimum waveforms for beam must be at least 1");

      return beamDefinition;
    }

  }

}


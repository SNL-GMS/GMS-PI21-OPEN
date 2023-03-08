package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkState;

@AutoValue
@JsonSerialize(as = AmplitudeMeasurementDefinition.class)
@JsonDeserialize(builder = AutoValue_AmplitudeMeasurementDefinition.Builder.class)
public abstract class AmplitudeMeasurementDefinition {

  public abstract Duration getArrivalTimeLag();

  public abstract Duration getWindowLength();

  public abstract PeakTroughType getPeakTroughType();

  public abstract Duration getMinPeriod();

  public abstract Duration getMaxPeriod();

  public abstract AmplitudeMeasurementType getAmplitudeMeasurementType();

  public static Builder builder() {
    return new AutoValue_AmplitudeMeasurementDefinition.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setArrivalTimeLag(Duration arrivalTimeLag);

    public abstract Builder setWindowLength(Duration windowLength);

    public abstract Builder setPeakTroughType(PeakTroughType peakTroughType);

    public abstract Builder setMinPeriod(Duration minPeriod);

    public abstract Builder setMaxPeriod(Duration maxPeriod);

    public abstract Builder setAmplitudeMeasurementType(
      AmplitudeMeasurementType amplitudeMeasurementType);

    abstract AmplitudeMeasurementDefinition autoBuild();

    public AmplitudeMeasurementDefinition build() {
      AmplitudeMeasurementDefinition amplitudeMeasurementDefinition = autoBuild();

      checkState(!amplitudeMeasurementDefinition.getWindowLength().isNegative()
          && !amplitudeMeasurementDefinition.getWindowLength().isZero(),
        "AmplitudeMeasurementDefinition requires non-zero, non-negative window length");

      checkState(!amplitudeMeasurementDefinition.getMinPeriod().isNegative()
          && !amplitudeMeasurementDefinition.getWindowLength().isZero(),
        "AmplitudeMeasurementDefinition requires non-zero, non-negative minimum period");

      checkState(amplitudeMeasurementDefinition.getMinPeriod()
          .compareTo(amplitudeMeasurementDefinition.getMaxPeriod()) <= 0,
        "AmplitudeMeasurementDefinition requires minPeriod <= maxPeriod");

      return amplitudeMeasurementDefinition;
    }
  }
}

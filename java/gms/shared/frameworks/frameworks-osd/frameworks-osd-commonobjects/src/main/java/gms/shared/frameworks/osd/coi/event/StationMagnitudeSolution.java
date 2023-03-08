package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.signaldetection.AmplitudeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurement;

import static com.google.common.base.Preconditions.checkState;

/**
 * @deprecated As of PI 17.5, the current model of this COI has been migrated into the event-coi package.
 * All usage of this COI outside the Frameworks area should be avoided and the alternative in event-coi used instead
 */
@Deprecated(since = "17.5", forRemoval = true)
@AutoValue
@JsonSerialize(as = StationMagnitudeSolution.class)
@JsonDeserialize(builder = AutoValue_StationMagnitudeSolution.Builder.class)
public abstract class StationMagnitudeSolution {

  public abstract MagnitudeType getType();

  public abstract MagnitudeModel getModel();

  public abstract String getStationName();

  public abstract PhaseType getPhase();

  public abstract double getMagnitude();

  public abstract double getMagnitudeUncertainty();

  public abstract double getModelCorrection();

  public abstract double getStationCorrection();

  public abstract FeatureMeasurement<AmplitudeMeasurementValue> getMeasurement();

  public static Builder builder() {
    return new AutoValue_StationMagnitudeSolution.Builder();
  }

  public abstract Builder toBuilder();

  /**
   * @deprecated As of PI 17.5, the current model of this builder has been migrated into the event-coi package.
   * All usage of this builder outside the Frameworks area should be avoided and the alternative in event-coi used instead
   */
  @Deprecated(since = "17.5", forRemoval = true)
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setType(MagnitudeType type);

    public abstract Builder setModel(MagnitudeModel model);

    public abstract Builder setStationName(String stationName);

    public abstract Builder setPhase(PhaseType phase);

    public abstract Builder setMagnitude(double magnitude);

    public abstract Builder setMagnitudeUncertainty(double magnitudeUncertainty);

    public abstract Builder setModelCorrection(double modelCorrection);

    public abstract Builder setStationCorrection(double stationCorrection);

    public abstract Builder setMeasurement(
      FeatureMeasurement<AmplitudeMeasurementValue> measurement);

    protected abstract StationMagnitudeSolution autoBuild();

    public StationMagnitudeSolution build() {
      StationMagnitudeSolution solution = autoBuild();

      checkState(solution.getMagnitude() >= -9.99 && solution.getMagnitude() <= 50.0,
        "Error creating StationMagnitudeSolution: magnitude must be >= -9.99 and <= 50, but was %s",
        solution.getMagnitude());

      checkState(solution.getMagnitudeUncertainty() > 0,
        "Error creating StationMagnitudeSolution: magnitudeUncertainty must be > 0, but was %s",
        solution.getMagnitudeUncertainty());

      checkState(solution.getModelCorrection() >= 0,
        "Error creating StationMagnitudeSolution: modelCorrection must be >= 0, but was %s",
        solution.getModelCorrection());

      checkState(solution.getStationCorrection() >= 0,
        "Error creating StationMagnitudeSolution: stationCorrection must be >= 0, but was %s",
        solution.getStationCorrection());
      return solution;
    }
  }
}

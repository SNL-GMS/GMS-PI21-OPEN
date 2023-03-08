package gms.shared.event.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;

/**
 * Defines the StationMagnitudeSolution class - measures the size of an {@link Event} occuring at a {@link LocationSolution}
 */
@AutoValue
@JsonSerialize(as = StationMagnitudeSolution.class)
@JsonDeserialize(builder = AutoValue_StationMagnitudeSolution.Builder.class)
public abstract class StationMagnitudeSolution {

  public abstract MagnitudeType getType();

  public abstract MagnitudeModel getModel();

  public abstract Station getStation();

  public abstract PhaseType getPhase();

  public abstract DoubleValue getMagnitude();

  public abstract Optional<Double> getModelCorrection();

  public abstract Optional<Double> getStationCorrection();

  public abstract FeatureMeasurement<AmplitudeMeasurementValue> getMeasurement();

  public static Builder builder() {
    return new AutoValue_StationMagnitudeSolution.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setType(MagnitudeType type);

    public abstract Builder setModel(MagnitudeModel model);

    public abstract Builder setStation(Station station);

    public abstract Builder setPhase(PhaseType phase);

    public abstract Builder setMagnitude(DoubleValue magnitude);

    public Builder setModelCorrection(double modelCorrection) {
      setModelCorrection(Optional.of(modelCorrection));
      return this;
    }

    public abstract Builder setModelCorrection(Optional<Double> modelCorrection);

    public Builder setStationCorrection(double stationCorrection) {
      setModelCorrection(Optional.of(stationCorrection));
      return this;
    }

    public abstract Builder setStationCorrection(Optional<Double> stationCorrection);

    public abstract Builder setMeasurement(
      FeatureMeasurement<AmplitudeMeasurementValue> measurement);

    protected abstract StationMagnitudeSolution autoBuild();

    public StationMagnitudeSolution build() {
      StationMagnitudeSolution solution = autoBuild();

      var magValue = solution.getMagnitude().getValue();
      checkState(magValue >= -9.99 && magValue <= 50.0,
        "Error creating StationMagnitudeSolution: magnitude must be >= -9.99 and <= 50, but was %s",
        solution.getMagnitude());

      checkState(solution.getModelCorrection().map(mc -> mc >= 0).orElse(true),
        "Error creating StationMagnitudeSolution: modelCorrection must be >= 0 if present, but was %s",
        solution.getModelCorrection().orElse(-1.0));

      checkState(solution.getStationCorrection().map(sc -> sc >= 0).orElse(true),
        "Error creating StationMagnitudeSolution: stationCorrection must be >= 0 if present, but was %s",
        solution.getStationCorrection().orElse(-1.0));
      return solution;
    }
  }
}

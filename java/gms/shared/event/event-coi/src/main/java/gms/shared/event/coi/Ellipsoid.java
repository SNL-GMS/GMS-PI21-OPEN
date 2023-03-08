package gms.shared.event.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkState;

/**
 * Defines the Ellipsoid class - represents a 3D projection of the {@link LocationUncertainty} covariance matrix
 */
@AutoValue
@JsonSerialize(as = Ellipsoid.class)
@JsonDeserialize(builder = AutoValue_Ellipsoid.Builder.class)
public abstract class Ellipsoid {

  public abstract ScalingFactorType getScalingFactorType();

  public abstract double getkWeight();

  public abstract double getConfidenceLevel();

  public abstract double getSemiMajorAxisLengthKm();

  public abstract double getSemiMajorAxisTrendDeg();

  public abstract double getSemiMajorAxisPlungeDeg();

  public abstract double getSemiIntermediateAxisLengthKm();

  public abstract double getSemiIntermediateAxisTrendDeg();

  public abstract double getSemiIntermediateAxisPlungeDeg();

  public abstract double getSemiMinorAxisLengthKm();

  public abstract double getSemiMinorAxisTrendDeg();

  public abstract double getSemiMinorAxisPlungeDeg();

  public abstract Duration getTimeUncertainty();

  public static Builder builder() {
    return new AutoValue_Ellipsoid.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    public abstract Builder setScalingFactorType(ScalingFactorType scalingFactorType);

    public abstract Builder setkWeight(double kWeight);

    public abstract Builder setConfidenceLevel(double confidenceLevel);

    public abstract Builder setSemiMajorAxisLengthKm(double semiMajorAxisLengthKm);

    public abstract Builder setSemiMajorAxisTrendDeg(double semiMajorAxisTrendDeg);

    public abstract Builder setSemiMajorAxisPlungeDeg(double semiMajorAxisPlungeDeg);

    public abstract Builder setSemiIntermediateAxisLengthKm(double semiIntermediateAxisLengthKm);

    public abstract Builder setSemiIntermediateAxisTrendDeg(double semiIntermediateAxisTrendDeg);

    public abstract Builder setSemiIntermediateAxisPlungeDeg(double semiIntermediateAxisPlungeDeg);

    public abstract Builder setSemiMinorAxisLengthKm(double semiMinorAxisLengthKm);

    public abstract Builder setSemiMinorAxisTrendDeg(double semiMinorAxisTrendDeg);

    public abstract Builder setSemiMinorAxisPlungeDeg(double semiMinorAxisPlungeDeg);

    public abstract Builder setTimeUncertainty(Duration timeUncertainty);

    protected abstract Ellipsoid autoBuild();

    public Ellipsoid build() {
      var ellipsoid = autoBuild();

      checkState(!Double.isNaN(ellipsoid.getkWeight()),
        "The validated kWeight is not a number");
      checkState(!Double.isNaN(ellipsoid.getConfidenceLevel()),
        "The validated confidenceLevel is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiMajorAxisLengthKm()),
        "The validated semiMajorAxisLengthKm is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiMajorAxisTrendDeg()),
        "The validated semiMajorAxisTrendDeg is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiMajorAxisPlungeDeg()),
        "The validated semiMajorAxisPlungeDeg is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiIntermediateAxisLengthKm()),
        "The validated semiIntermediateAxisLengthKm is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiIntermediateAxisTrendDeg()),
        "The validated semiIntermediateAxisTrendDeg is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiIntermediateAxisPlungeDeg()),
        "The validated semiIntermediateAxisPlungeDeg is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiMinorAxisLengthKm()),
        "The validated semiMinorAxisLengthKm is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiMinorAxisTrendDeg()),
        "The validated semiMinorAxisTrendDeg is not a number");
      checkState(!Double.isNaN(ellipsoid.getSemiMinorAxisPlungeDeg()),
        "The validated semiMinorAxisPlungeDeg is not a number");

      var confidenceLevel = ellipsoid.getConfidenceLevel();
      checkState(confidenceLevel >= 0.5 && confidenceLevel <= 1.0,
        "confidence level must be in range [0.5, 1]");

      var scalingFactorType = ellipsoid.getScalingFactorType();
      var kWeight = ellipsoid.getkWeight();
      if (scalingFactorType == ScalingFactorType.CONFIDENCE) {
        checkState(kWeight == 0.0,
          "If scaling factor type is CONFIDENCE, kWeight must be 0.0");
      } else if (scalingFactorType == ScalingFactorType.COVERAGE) {
        checkState(kWeight == Double.POSITIVE_INFINITY,
          "If scaling factor type is COVERAGE, kWeight must be infinity");
      } else if (scalingFactorType == ScalingFactorType.K_WEIGHTED) {
        checkState(kWeight >= 0.0,
          "If scaling factor type is K_WEIGHTED, kWeight must be >= 0.0");
      }

      checkState(!ellipsoid.getTimeUncertainty().isNegative(),
        "Time uncertainty must be non-negative");

      return ellipsoid;
    }
  }
}

package gms.shared.event.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkState;

/**
 * Defines the Ellipse class - represents a 2D projection of the {@link LocationUncertainty} covariance matrix
 */
@AutoValue
@JsonSerialize(as = Ellipse.class)
@JsonDeserialize(builder = AutoValue_Ellipse.Builder.class)
public abstract class Ellipse {

  public abstract ScalingFactorType getScalingFactorType();

  public abstract double getkWeight();

  public abstract double getConfidenceLevel();

  public abstract double getSemiMajorAxisLengthKm();

  public abstract double getSemiMajorAxisTrendDeg();

  public abstract double getSemiMinorAxisLengthKm();

  public abstract double getDepthUncertaintyKm();

  public abstract Duration getTimeUncertainty();

  public static Builder builder() {
    return new AutoValue_Ellipse.Builder();
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

    public abstract Builder setSemiMinorAxisLengthKm(double semiMinorAxisLengthKm);

    public abstract Builder setDepthUncertaintyKm(double depthUncertaintyKm);

    public abstract Builder setTimeUncertainty(Duration timeUncertainty);

    protected abstract Ellipse autoBuild();

    public Ellipse build() {
      var ellipse = autoBuild();

      checkState(!Double.isNaN(ellipse.getkWeight()),
        "The validated kWeight is not a number");
      checkState(!Double.isNaN(ellipse.getConfidenceLevel()),
        "The validated confidenceLevel is not a number");
      checkState(!Double.isNaN(ellipse.getSemiMajorAxisLengthKm()),
        "The validated semiMajorAxisLengthKm is not a number");
      checkState(!Double.isNaN(ellipse.getSemiMajorAxisTrendDeg()),
        "The validated semiMajorAxisTrendDeg is not a number");
      checkState(!Double.isNaN(ellipse.getSemiMinorAxisLengthKm()),
        "The validated semiMinorAxisLengthKm is not a number");
      checkState(!Double.isNaN(ellipse.getDepthUncertaintyKm()),
        "The validated depthUncertaintyKm is not a number");

      var confidenceLevel = ellipse.getConfidenceLevel();
      checkState(confidenceLevel >= 0.5 && confidenceLevel <= 1.0,
        "confidence level must be in range [0.5, 1]");

      var scalingFactorType = ellipse.getScalingFactorType();
      var kWeight = ellipse.getkWeight();
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

      checkState(!ellipse.getTimeUncertainty().isNegative(),
        "Time uncertainty must be non-negative");

      return ellipse;
    }
  }
}

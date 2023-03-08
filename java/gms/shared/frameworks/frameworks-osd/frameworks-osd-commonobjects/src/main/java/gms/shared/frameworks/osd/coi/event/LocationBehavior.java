package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurement;


/**
 * Define a class for LocationBehavior for the processing results location solution.
 *
 * @deprecated Will be replaced by gms.shared.event.coi.LocationBehavior
 */
@AutoValue
@Deprecated(forRemoval = true)
public abstract class LocationBehavior {

  public abstract double getResidual();

  public abstract double getWeight();

  public abstract boolean isDefining();

  public abstract FeaturePrediction<?> getFeaturePrediction();

  public abstract FeatureMeasurement<?> getFeatureMeasurement();

  /**
   * Define a LocationBehavior from known attributes.
   * TODO: describe parameters
   *
   * @param residual The difference between the feature measurement and the prediction.
   * @param weight
   * @param isDefining
   * @param featurePrediction Not null.
   * @param featureMeasurement Not null.
   * @return A LocationBehavior object.
   */
  @JsonCreator
  public static LocationBehavior from(
    @JsonProperty("residual") double residual,
    @JsonProperty("weight") double weight,
    @JsonProperty("defining") boolean isDefining,
    @JsonProperty("featurePrediction") FeaturePrediction<?> featurePrediction,
    @JsonProperty("featureMeasurement") FeatureMeasurement<?> featureMeasurement) {
    return new AutoValue_LocationBehavior(
      residual,
      weight,
      isDefining,
      featurePrediction,
      featureMeasurement);
  }
}

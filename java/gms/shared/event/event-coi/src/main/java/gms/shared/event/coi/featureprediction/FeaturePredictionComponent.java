package gms.shared.event.coi.featureprediction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.io.Serializable;


/**
 * A single component of a feature prediction.  The actual {@link FeaturePrediction} is equal to the
 * sum of its {@link FeaturePredictionComponentType}s which may represent baseline values or corrections
 * to the baseline values.
 *
 * @param <T> the valueType of the FeaturePredictionComponent
 */
@AutoValue
public abstract class FeaturePredictionComponent<T> implements Serializable {

  public abstract T getValue();

  public abstract boolean isExtrapolated();

  public abstract FeaturePredictionComponentType getFeaturePredictionComponent();

  @JsonCreator
  public static <T> FeaturePredictionComponent<T> from(
    @JsonProperty("value") T value,
    @JsonProperty("extrapolated") boolean extrapolated,
    @JsonProperty("featurePredictionComponent") FeaturePredictionComponentType featurePredictionComponent) {
    return new AutoValue_FeaturePredictionComponent<>(value, extrapolated, featurePredictionComponent);
  }
}

package gms.shared.event.coi.featureprediction.value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionDerivativeType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Represents a feature prediction value, which is a combination of the predicted value itself,
 * the components (such as corrections) that went into calculating the value, and the derivatives
 * around the point where the value is being calculated.
 *
 * @param <T> Type of the predicted value
 * @param <U> Type of the derivatives
 * @param <V> Type of the components
 */
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "featureMeasurementType",
  visible = true
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ArrivalTimeFeaturePredictionValue.class, name = "ARRIVAL_TIME"),
  @JsonSubTypes.Type(value = NumericFeaturePredictionValue.class, name = "SLOWNESS"),
  @JsonSubTypes.Type(value = NumericFeaturePredictionValue.class, name = "EMERGENCE_ANGLE"),
  @JsonSubTypes.Type(value = NumericFeaturePredictionValue.class, name = "RECEIVER_TO_SOURCE_AZIMUTH"),
  @JsonSubTypes.Type(value = NumericFeaturePredictionValue.class, name = "SOURCE_TO_RECEIVER_AZIMUTH"),
  @JsonSubTypes.Type(value = NumericFeaturePredictionValue.class, name = "SOURCE_TO_RECEIVER_DISTANCE")
})
public abstract class FeaturePredictionValue<T, U, V> {

  private final FeatureMeasurementType<T> featureMeasurementType;
  private final T predictedValue;
  private final Map<FeaturePredictionDerivativeType, U> derivativeMap;
  private final Set<FeaturePredictionComponent<V>> featurePredictionComponentSet;

  protected FeaturePredictionValue(
    FeatureMeasurementType<T> featureMeasurementType,
    T predictedValue,
    Map<FeaturePredictionDerivativeType, U> derivativeMap,
    Set<FeaturePredictionComponent<V>> featurePredictionComponentSet) {
    this.featureMeasurementType = featureMeasurementType;
    this.predictedValue = predictedValue;
    this.derivativeMap = derivativeMap;
    this.featurePredictionComponentSet = featurePredictionComponentSet;
  }

  public FeatureMeasurementType<T> getFeatureMeasurementType() {
    return featureMeasurementType;
  }

  public T getPredictedValue() {
    return predictedValue;
  }

  public Map<FeaturePredictionDerivativeType, U> getDerivativeMap() {
    return derivativeMap;
  }

  public Set<FeaturePredictionComponent<V>> getFeaturePredictionComponentSet() {
    return featurePredictionComponentSet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FeaturePredictionValue)) {
      return false;
    }
    FeaturePredictionValue<?, ?, ?> that = (FeaturePredictionValue<?, ?, ?>) o;
    return Objects.equals(featureMeasurementType, that.featureMeasurementType)
      && Objects.equals(predictedValue, that.predictedValue) && Objects.equals(
      derivativeMap, that.derivativeMap) && Objects.equals(featurePredictionComponentSet,
      that.featurePredictionComponentSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(featureMeasurementType, predictedValue, derivativeMap,
      featurePredictionComponentSet);
  }
}

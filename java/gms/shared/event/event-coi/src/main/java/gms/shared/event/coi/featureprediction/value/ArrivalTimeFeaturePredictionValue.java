package gms.shared.event.coi.featureprediction.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionDerivativeType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.util.Map;
import java.util.Set;

/**
 * Represents an arrival time feature prediction, its value, its derivatives, and its components.
 */
public class ArrivalTimeFeaturePredictionValue
  extends FeaturePredictionValue<ArrivalTimeMeasurementValue, DoubleValue, DurationValue> {

  private ArrivalTimeFeaturePredictionValue(
    ArrivalTimeMeasurementValue predictedValue,
    Map<FeaturePredictionDerivativeType, DoubleValue> derivativeMap,
    Set<FeaturePredictionComponent<DurationValue>> featurePredictionComponents) {

    super(FeatureMeasurementTypes.ARRIVAL_TIME, predictedValue, derivativeMap, featurePredictionComponents);
  }

  public static ArrivalTimeFeaturePredictionValue create(
    ArrivalTimeMeasurementValue predictedValue,
    Map<FeaturePredictionDerivativeType, DoubleValue> derivativeMap,
    Set<FeaturePredictionComponent<DurationValue>> featurePredictionComponents) {

    return new ArrivalTimeFeaturePredictionValue(predictedValue, derivativeMap, featurePredictionComponents);
  }

  @JsonCreator
  public static ArrivalTimeFeaturePredictionValue from(
    @JsonProperty("featureMeasurementType") FeatureMeasurementType<?> featureMeasurementType,
    @JsonProperty("predictedValue")
    ArrivalTimeMeasurementValue predictedValue,
    @JsonProperty("derivativeMap")
    Map<FeaturePredictionDerivativeType, DoubleValue> derivativeMap,
    @JsonProperty("featurePredictionComponents")
    Set<FeaturePredictionComponent<DurationValue>> featurePredictionComponents
  ) {

    if (!FeatureMeasurementTypes.ARRIVAL_TIME.getFeatureMeasurementTypeName().equals(
      featureMeasurementType.getFeatureMeasurementTypeName()
    )) {
      throw new IllegalArgumentException("ArrivalTimeFeaturePredictionValue can only have an ARRIVAL_TIME FeatureMeasurementType");
    }

    return new ArrivalTimeFeaturePredictionValue(
      predictedValue,
      derivativeMap,
      featurePredictionComponents
    );
  }

}

package gms.shared.event.coi.featureprediction.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionDerivativeType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.util.Map;
import java.util.Set;

/**
 * Represents a slowness feature prediction, its value, its derivatives, and its components.
 */
public class NumericFeaturePredictionValue extends FeaturePredictionValue<NumericMeasurementValue, DoubleValue, DoubleValue> {

  private NumericFeaturePredictionValue(
    NumericMeasurementValue predictedValue,
    Map<FeaturePredictionDerivativeType, DoubleValue> derivativeMap,
    Set<FeaturePredictionComponent<DoubleValue>> featurePredictionComponents,
    FeatureMeasurementType<NumericMeasurementValue> featureMeasurementType) {
    super(featureMeasurementType, predictedValue, derivativeMap, featurePredictionComponents);
  }

  @JsonCreator
  public static NumericFeaturePredictionValue from(
    @JsonProperty("featureMeasurementType") FeatureMeasurementType<NumericMeasurementValue> featureMeasurementType,
    @JsonProperty("predictedValue")
    NumericMeasurementValue predictedValue,
    @JsonProperty("derivativeMap")
    Map<FeaturePredictionDerivativeType, DoubleValue> derivativeMap,
    @JsonProperty("featurePredictionComponents")
    Set<FeaturePredictionComponent<DoubleValue>> featurePredictionComponents
  ) {

    if (!FeatureMeasurementTypes.SLOWNESS.getFeatureMeasurementTypeName().equals(
      featureMeasurementType.getFeatureMeasurementTypeName()
    ) && !FeatureMeasurementTypes.EMERGENCE_ANGLE.getFeatureMeasurementTypeName().equals(
      featureMeasurementType.getFeatureMeasurementTypeName()
    ) && !FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH.getFeatureMeasurementTypeName().equals(
      featureMeasurementType.getFeatureMeasurementTypeName()
    ) && !FeatureMeasurementTypes.SOURCE_TO_RECEIVER_AZIMUTH.getFeatureMeasurementTypeName().equals(
      featureMeasurementType.getFeatureMeasurementTypeName()
    ) && !FeatureMeasurementTypes.SOURCE_TO_RECEIVER_DISTANCE.getFeatureMeasurementTypeName().equals(
      featureMeasurementType.getFeatureMeasurementTypeName()
    )) {
      throw new IllegalArgumentException("NumericFeaturePredictionValue can only have an SLOWNESS, EMERGENCE_ANGLE, " +
        "RECEIVER_TO_SOURCE_AZIMUTH, SOURCE_TO_RECEIVER_AZIMUTH, or SOURCE_TO_RECEIVER_DISTANCE FeatureMeasurementTypes");
    }

    return new NumericFeaturePredictionValue(
      predictedValue,
      derivativeMap,
      featurePredictionComponents,
      featureMeasurementType
    );
  }

}

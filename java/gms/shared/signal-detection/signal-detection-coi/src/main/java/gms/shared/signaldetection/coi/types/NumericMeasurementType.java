package gms.shared.signaldetection.coi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;

/**
 * {@link FeatureMeasurementType} for {@link NumericMeasurementValue}
 */
@AutoValue
public abstract class NumericMeasurementType implements FeatureMeasurementType<NumericMeasurementValue> {

  @JsonCreator
  static NumericMeasurementType from(String featureMeasurementTypeName) {
    return new AutoValue_NumericMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<NumericMeasurementValue> getMeasurementValueType() {
    return NumericMeasurementValue.class;
  }
}

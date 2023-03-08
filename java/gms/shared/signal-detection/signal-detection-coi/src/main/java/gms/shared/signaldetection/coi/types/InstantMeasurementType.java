package gms.shared.signaldetection.coi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.values.InstantValue;

/**
 * {@link FeatureMeasurementType} for {@link InstantValue}
 */
@AutoValue
public abstract class InstantMeasurementType implements FeatureMeasurementType<InstantValue> {

  @JsonCreator
  static InstantMeasurementType from(String featureMeasurementTypeName) {
    return new AutoValue_InstantMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<InstantValue> getMeasurementValueType() {
    return InstantValue.class;
  }
}

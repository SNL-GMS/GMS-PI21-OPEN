package gms.shared.signaldetection.coi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;

@AutoValue
public abstract class ArrivalTimeMeasurementType implements FeatureMeasurementType<ArrivalTimeMeasurementValue> {

  @JsonCreator
  static ArrivalTimeMeasurementType from(String featureMeasurementTypeName) {
    return new AutoValue_ArrivalTimeMeasurementType(featureMeasurementTypeName);
  }

  @Override
  public Class<ArrivalTimeMeasurementValue> getMeasurementValueType() {
    return ArrivalTimeMeasurementValue.class;
  }

}

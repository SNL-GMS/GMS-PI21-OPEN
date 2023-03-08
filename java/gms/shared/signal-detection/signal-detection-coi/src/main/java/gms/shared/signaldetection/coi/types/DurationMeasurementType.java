package gms.shared.signaldetection.coi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;

/**
 * {@link FeatureMeasurementType} for {@link DurationMeasurementType}
 */
@AutoValue
public abstract class DurationMeasurementType implements FeatureMeasurementType<DurationMeasurementType> {

  @JsonCreator
  static DurationMeasurementType from(String featureMeasurementTypeName) {
    return new AutoValue_DurationMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<DurationMeasurementType> getMeasurementValueType() {
    return DurationMeasurementType.class;
  }
}

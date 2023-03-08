package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.InstantValue;

/**
 * {@link FeatureMeasurementType} for {@link InstantValue}
 */
@AutoValue
public abstract class InstantMeasurementType implements
  FeatureMeasurementType<InstantValue> {

  @JsonCreator
  public static InstantMeasurementType from(
    @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_InstantMeasurementType(featureMeasurementTypeName);
  }


  @Override
  public Class<InstantValue> getMeasurementValueType() {
    return InstantValue.class;
  }
}

package gms.shared.signaldetection.coi.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;

/**
 * {@link FeatureMeasurementType} for {@link PhaseTypeMeasurementValue}
 */
@AutoValue
public abstract class PhaseMeasurementType implements FeatureMeasurementType<PhaseTypeMeasurementValue> {

  @JsonCreator
  static PhaseMeasurementType from(String featureMeasurementTypeName) {
    return new AutoValue_PhaseMeasurementType(featureMeasurementTypeName);
  }

  @Override
  public Class<PhaseTypeMeasurementValue> getMeasurementValueType() {
    return PhaseTypeMeasurementValue.class;
  }
}

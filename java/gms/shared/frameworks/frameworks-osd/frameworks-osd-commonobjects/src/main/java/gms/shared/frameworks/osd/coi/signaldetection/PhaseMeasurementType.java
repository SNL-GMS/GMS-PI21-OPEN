package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;

/**
 * {@link FeatureMeasurementType} for {@link PhaseTypeMeasurementValue}
 */
@AutoValue
public abstract class PhaseMeasurementType implements
  FeatureMeasurementType<PhaseTypeMeasurementValue> {

  @JsonCreator
  public static PhaseMeasurementType from(
    @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_PhaseMeasurementType(featureMeasurementTypeName);
  }

  @Override
  public Class<PhaseTypeMeasurementValue> getMeasurementValueType() {
    return PhaseTypeMeasurementValue.class;
  }
}

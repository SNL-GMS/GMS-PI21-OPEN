package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * {@link FeatureMeasurementType} for {@link AmplitudeMeasurementValue}
 */
@AutoValue
public abstract class AmplitudeMeasurementType implements
  FeatureMeasurementType<AmplitudeMeasurementValue> {

  @JsonCreator
  public static AmplitudeMeasurementType from(
    @JsonProperty("featureMeasurementTypeName") String featureMeasurementTypeName) {
    return new AutoValue_AmplitudeMeasurementType(featureMeasurementTypeName);
  }

  @Override
  public Class<AmplitudeMeasurementValue> getMeasurementValueType() {
    return AmplitudeMeasurementValue.class;
  }
}

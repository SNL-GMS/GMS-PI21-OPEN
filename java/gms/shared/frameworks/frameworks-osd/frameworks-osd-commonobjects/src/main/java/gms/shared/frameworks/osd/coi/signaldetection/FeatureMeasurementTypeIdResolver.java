package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class FeatureMeasurementTypeIdResolver extends TypeIdResolverBase {

  @Override
  public Id getMechanism() {
    return Id.NAME;
  }

  @Override
  public String idFromValue(Object value) {
    return idFromValueAndType(value, value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return ((FeatureMeasurementType) value).getFeatureMeasurementTypeName();
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    final FeatureMeasurementType<?> measurementType = FeatureMeasurementTypesChecking
      .featureMeasurementTypeFromMeasurementTypeString(id);

    return context.getTypeFactory()
      .constructType(measurementType.getClass().getSuperclass());
  }
}

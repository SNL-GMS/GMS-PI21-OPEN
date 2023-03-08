package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypesChecking;

/**
 * Used for JSON/Jackson purposes to read a particular type parameter
 * instantiation of {@link FeatureMeasurement}.
 */
public class FeatureMeasurementIdResolver extends TypeIdResolverBase {

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
    return ((FeatureMeasurement) value).getFeatureMeasurementType().getFeatureMeasurementTypeName();
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    final Class<?> measurementClass = FeatureMeasurementTypesChecking
      .measurementValueClassFromMeasurementTypeString(id);

    return context.getTypeFactory()
      .constructParametricType(FeatureMeasurement.class, measurementClass);
  }
}

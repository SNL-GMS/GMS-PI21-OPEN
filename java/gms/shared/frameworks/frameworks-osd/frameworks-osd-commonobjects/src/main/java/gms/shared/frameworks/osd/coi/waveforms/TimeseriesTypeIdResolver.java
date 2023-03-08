package gms.shared.frameworks.osd.coi.waveforms;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.io.IOException;

public class TimeseriesTypeIdResolver extends TypeIdResolverBase {

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.NAME;
  }

  @Override
  public String idFromValue(Object value) {
    return idFromValueAndType(value, value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    return ((Timeseries) value).getType().name();
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    var type = Timeseries.Type.fromName(id);
    if (type == null || type.getImplementingClass() == null) {
      throw new IllegalArgumentException("Unknown type for timeseries: " + id);
    }

    Class<? extends Timeseries> timeseriesClass = type.getImplementingClass();
    return context.getTypeFactory().constructType(timeseriesClass);
  }
}

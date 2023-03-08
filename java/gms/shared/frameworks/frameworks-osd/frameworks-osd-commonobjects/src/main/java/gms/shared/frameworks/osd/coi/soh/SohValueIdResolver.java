package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;

/**
 * Used for JSON/Jackson purposes to read a particular type parameter instantiation of {@link
 * SohMonitorValueAndStatus}.
 */
public class SohValueIdResolver extends TypeIdResolverBase {

  private JavaType superType;

  @Override
  public void init(JavaType baseType) {
    superType = baseType;
  }

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

    SohValueType sohValueType = ((SohMonitorValueAndStatus) value).getMonitorType().getSohValueType();

    if (sohValueType == SohValueType.INVALID) {
      throw new IllegalArgumentException("SohValueType.INVALID is not allowed.");
    }

    return sohValueType.toString();
  }


  @Override
  public JavaType typeFromId(DatabindContext context, String id) {

    Class<?> subType;
    switch (SohMonitorType.valueOf(id).getSohValueType()) {
      case DURATION:
        subType = DurationSohMonitorValueAndStatus.class;
        break;
      case PERCENT:
        subType = PercentSohMonitorValueAndStatus.class;
        break;
      case INVALID:
        throw new IllegalArgumentException("SohValueType.INVALID is not allowed.");
      default:
        throw new IllegalArgumentException("Unrecognized SohValueType.");
    }

    return context.constructSpecializedType(superType, subType);
  }
}

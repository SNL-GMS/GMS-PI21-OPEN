package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType.StationValueType;

public class StationValueIdResolver extends TypeIdResolverBase {

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

    StationValueType stationValueType = ((StationAggregate) value).getAggregateType().getStationValueType();

    if (stationValueType == StationValueType.INVALID) {
      throw new IllegalArgumentException(String.format("%s.%s is not allowed.",
        StationValueType.class.getName(),
        StationValueType.INVALID));
    }

    return stationValueType.toString();
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {

    Class<?> subType;
    switch (StationAggregateType.valueOf(id).getStationValueType()) {
      case DURATION:
        subType = DurationStationAggregate.class;
        break;
      case PERCENT:
        subType = PercentStationAggregate.class;
        break;
      case INVALID:
        throw new IllegalArgumentException(
          String.format("%s.%s is not allowed.",
            StationValueType.class.getName(),
            StationValueType.INVALID));
      default:
        throw new IllegalArgumentException(
          String.format("Unrecognized %s", StationAggregateType.class.getName()));
    }

    return context.constructSpecializedType(superType, subType);
  }
}

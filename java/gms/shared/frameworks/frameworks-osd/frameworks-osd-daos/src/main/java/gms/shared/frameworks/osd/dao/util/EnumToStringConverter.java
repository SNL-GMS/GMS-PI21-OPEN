package gms.shared.frameworks.osd.dao.util;

import com.google.common.collect.ImmutableBiMap;

import javax.persistence.AttributeConverter;
import java.util.Objects;

public abstract class EnumToStringConverter<T extends Enum> implements AttributeConverter<T, String> {
  private final ImmutableBiMap<T, String> columnsByValue;

  protected EnumToStringConverter(Class<T> enumClass) {
    Objects.requireNonNull(enumClass,
      "Cannot create EnumToStringConverter with a null enum class");

    ImmutableBiMap.Builder<T, String> builder = ImmutableBiMap.builder();

    for (T value : enumClass.getEnumConstants()) {
      builder.put(value, value.name());
    }

    columnsByValue = builder.build();
  }

  @Override
  public String convertToDatabaseColumn(T attribute) {
    return columnsByValue.get(attribute);
  }

  @Override
  public T convertToEntityAttribute(String dbData) {
    return columnsByValue.inverse().get(dbData);
  }

}

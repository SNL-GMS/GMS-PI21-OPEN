package gms.shared.utilities.bridge.database.converter;

import com.google.common.collect.ImmutableBiMap;

import javax.persistence.AttributeConverter;
import java.util.Objects;
import java.util.function.Function;

public class EnumToCharConverter<T extends Enum> implements AttributeConverter<T, Character> {

  private final ImmutableBiMap<T, Character> columnsByValue;

  public EnumToCharConverter(Class<T> enumClass, Function<T, Character> valueExtractor) {
    Objects.requireNonNull(enumClass,
      "Cannot create EnumToCharConverter with a null enum class");

    ImmutableBiMap.Builder<T, Character> builder = ImmutableBiMap.builder();

    for (T value : enumClass.getEnumConstants()) {
      builder.put(value, valueExtractor.apply(value));
    }

    columnsByValue = builder.build();
  }

  @Override
  public Character convertToDatabaseColumn(T attribute) {
    return columnsByValue.get(attribute);
  }

  @Override
  public T convertToEntityAttribute(Character dbData) {
    return columnsByValue.inverse().get(dbData);
  }
}

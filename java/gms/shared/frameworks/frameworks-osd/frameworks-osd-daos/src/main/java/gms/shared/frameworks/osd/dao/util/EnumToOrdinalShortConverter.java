package gms.shared.frameworks.osd.dao.util;

import javax.persistence.AttributeConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class EnumToOrdinalShortConverter<T extends Enum> implements AttributeConverter<T, Short> {

  private final Map<T, Short> toColumn;
  private final Map<Short, T> fromColumn;

  protected EnumToOrdinalShortConverter(Map<T, Short> forwardMapping) {
    Objects.requireNonNull(forwardMapping,
      "Cannot create EnumToOrdinalShortConverter with a null forwardMapping");

    toColumn = new HashMap<>(forwardMapping);
    fromColumn = new HashMap<>();
    toColumn.forEach((k, v) -> fromColumn.put(v, k));
  }

  @Override
  public Short convertToDatabaseColumn(T filterType) {
    return toColumn.get(filterType);
  }

  @Override
  public T convertToEntityAttribute(Short id) {
    return fromColumn.get(id);
  }
}

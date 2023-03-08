package gms.shared.utilities.bridge.database.converter;

import javax.persistence.AttributeConverter;

public class BooleanYNConverter implements AttributeConverter<Boolean, String> {

  @Override
  public String convertToDatabaseColumn(Boolean value) {
    if (value == null) {
      return null;
    }
    return value ? "y" : "n";
  }

  @Override
  public Boolean convertToEntityAttribute(String value) {
    if (value == null) {
      return null;
    } else if ("y".equals(value)) {
      return true;
    } else if ("n".equals(value)) {
      return false;
    } else {
      throw new IllegalArgumentException("Invalid yes/no value: " + value);
    }
  }


}
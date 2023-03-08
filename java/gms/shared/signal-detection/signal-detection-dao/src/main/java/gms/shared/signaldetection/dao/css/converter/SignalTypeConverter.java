package gms.shared.signaldetection.dao.css.converter;

import gms.shared.signaldetection.dao.css.enums.SignalType;

import javax.persistence.AttributeConverter;

public class SignalTypeConverter implements AttributeConverter<SignalType, String> {
  @Override
  public String convertToDatabaseColumn(SignalType attribute) {
    if (attribute == null) {
      return null;
    }

    return String.valueOf(attribute.getCodes().stream().findFirst().orElseThrow());
  }

  @Override
  public SignalType convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return null;
    }

    return SignalType.fromCode(dbData.charAt(0));
  }
}

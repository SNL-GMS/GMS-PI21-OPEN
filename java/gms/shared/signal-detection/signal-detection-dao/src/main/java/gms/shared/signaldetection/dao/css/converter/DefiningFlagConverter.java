package gms.shared.signaldetection.dao.css.converter;

import gms.shared.signaldetection.dao.css.enums.DefiningFlag;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

public class DefiningFlagConverter extends EnumToStringConverter<DefiningFlag> {
  public DefiningFlagConverter() {
    super(DefiningFlag.class, DefiningFlag::getCode);
  }
}

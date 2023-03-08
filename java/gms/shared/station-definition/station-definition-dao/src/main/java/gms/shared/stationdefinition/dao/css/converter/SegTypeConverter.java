package gms.shared.stationdefinition.dao.css.converter;


import gms.shared.stationdefinition.dao.css.enums.SegType;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class SegTypeConverter extends EnumToStringConverter<SegType> {
  public SegTypeConverter() {
    super(SegType.class, SegType::getName);
  }
}

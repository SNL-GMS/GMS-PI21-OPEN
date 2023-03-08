package gms.shared.stationdefinition.dao.css.converter;

import gms.shared.stationdefinition.dao.css.enums.Digital;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class DigitalConverter extends EnumToStringConverter<Digital> {
  public DigitalConverter() {
    super(Digital.class, Digital::getName);
  }
}

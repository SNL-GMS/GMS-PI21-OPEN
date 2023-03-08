package gms.shared.stationdefinition.dao.css.converter;

import gms.shared.stationdefinition.dao.css.enums.Band;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class BandConverter extends EnumToStringConverter<Band> {
  public BandConverter() {
    super(Band.class, Band::getName);
  }
}

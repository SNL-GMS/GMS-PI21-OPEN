package gms.shared.stationdefinition.dao.css.converter;


import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class ChannelTypeConverter extends EnumToStringConverter<ChannelType> {
  public ChannelTypeConverter() {
    super(ChannelType.class, ChannelType::getName);
  }
}

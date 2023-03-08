package gms.shared.frameworks.osd.dao.channel.converter;

import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.dao.util.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class ChannelDataTypeConverter extends EnumToStringConverter<ChannelDataType> {
  public ChannelDataTypeConverter() {
    super(ChannelDataType.class);
  }
}

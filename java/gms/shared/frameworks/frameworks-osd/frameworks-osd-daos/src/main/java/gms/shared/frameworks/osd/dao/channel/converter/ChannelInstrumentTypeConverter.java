package gms.shared.frameworks.osd.dao.channel.converter;

import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.dao.util.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class ChannelInstrumentTypeConverter extends EnumToStringConverter<ChannelInstrumentType> {
  public ChannelInstrumentTypeConverter() {
    super(ChannelInstrumentType.class);
  }
}

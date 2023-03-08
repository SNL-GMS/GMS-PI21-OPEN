package gms.shared.stationdefinition.dao.css.converter;

import gms.shared.stationdefinition.dao.css.enums.NetworkType;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class NetworkTypeConverter extends EnumToStringConverter<NetworkType> {
  public NetworkTypeConverter() {
    super(NetworkType.class, NetworkType::getName);
  }
}

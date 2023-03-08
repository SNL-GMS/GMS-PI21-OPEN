package gms.shared.stationdefinition.dao.css.converter;


import gms.shared.stationdefinition.dao.css.enums.DataType;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

import javax.persistence.Converter;

@Converter
public class DataTypeConverter extends EnumToStringConverter<DataType> {
  public DataTypeConverter() {
    super(DataType.class, DataType::getName);
  }
}

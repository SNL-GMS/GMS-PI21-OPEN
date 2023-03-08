package gms.shared.stationdefinition.dao.css.converter;

import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Converter;

@Converter
public class StationTypeConverter extends EnumToStringConverter<StaType> {
  public StationTypeConverter() {
    super(StaType.class, StaType::getName);
  }

  private static final Logger logger = LoggerFactory.getLogger(StationTypeConverter.class);

  public static void main(String[] args) {
    for (StaType type : StaType.values()) {
      if (logger.isInfoEnabled())
        logger.info(type.toString());
    }
  }
}

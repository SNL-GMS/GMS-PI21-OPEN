package gms.shared.signaldetection.dao.css.converter;

import gms.shared.signaldetection.dao.css.enums.AmplitudeUnits;
import gms.shared.utilities.bridge.database.converter.EnumToStringConverter;

public class AmplitudeUnitsConverter extends EnumToStringConverter<AmplitudeUnits> {

  public AmplitudeUnitsConverter() {
    super(AmplitudeUnits.class, AmplitudeUnits::getName);
  }

}

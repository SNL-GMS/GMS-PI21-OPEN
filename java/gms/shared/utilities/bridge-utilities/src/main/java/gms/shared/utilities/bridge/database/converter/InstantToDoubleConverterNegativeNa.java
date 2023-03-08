package gms.shared.utilities.bridge.database.converter;

import java.time.Instant;

public class InstantToDoubleConverterNegativeNa extends InstantToDoubleConverter {

  protected static final Double NA_VALUE = -9999999999.999d;
  protected static final Instant NA_TIME = Instant.MIN;

  protected Instant getDefaultValue() {
    return NA_TIME;
  }

  protected Double getNaValue() {
    return NA_VALUE;
  }
}

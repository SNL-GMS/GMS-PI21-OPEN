package gms.shared.utilities.bridge.database.converter;

import java.time.Instant;

public class InstantToDoubleConverterPositiveNa extends InstantToDoubleConverter {

  protected static final Double NA_VALUE = 9999999999.999d;
  public static final Instant NA_TIME = Instant.MAX;

  protected Instant getDefaultValue() {
    return NA_TIME;
  }

  protected Double getNaValue() {
    return NA_VALUE;
  }

}
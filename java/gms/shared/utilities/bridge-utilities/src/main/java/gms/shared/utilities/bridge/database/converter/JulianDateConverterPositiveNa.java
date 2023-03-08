package gms.shared.utilities.bridge.database.converter;

import javax.persistence.Converter;
import java.time.Instant;

@Converter
public class JulianDateConverterPositiveNa extends JulianDateConverter {

  public static final int NA_VALUE = 2286324;

  protected Instant getDefaultValue() {
    return Instant.MAX;
  }

  protected int getNaValue() {
    return NA_VALUE;
  }
}
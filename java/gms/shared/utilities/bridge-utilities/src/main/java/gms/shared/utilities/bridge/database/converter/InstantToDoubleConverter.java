package gms.shared.utilities.bridge.database.converter;

import com.google.common.math.DoubleMath;

import javax.persistence.AttributeConverter;
import java.math.BigDecimal;
import java.time.Instant;

public abstract class InstantToDoubleConverter implements AttributeConverter<Instant, Double> {

  @Override
  public Double convertToDatabaseColumn(Instant instant) {
    if (instant == null || getDefaultValue().equals(instant)) {
      return getNaValue();
    }

    //The values in the database are floating point numbers of the form `1234567890.123`.
    //The method `instant.toEpochMilli()` returns and epoch time as a long of the form `1234567890123`.
    //So we have to shift the decimal place 3 places to the left to match the expected format.
    return BigDecimal.valueOf(instant.toEpochMilli())
      .movePointLeft(3)
      .doubleValue();
  }

  @Override
  public Instant convertToEntityAttribute(Double value) {
    if (DoubleMath.fuzzyEquals(getNaValue(), value, 0.0005f)) {
      return getDefaultValue();
    }

    final BigDecimal bigDecimal = BigDecimal.valueOf(value);
    final long epochSecond = bigDecimal.longValue();
    final long nanoAdjustment = bigDecimal.subtract(BigDecimal.valueOf(epochSecond))
      .movePointRight(3)
      .longValue();
    return Instant.ofEpochSecond(epochSecond).plusMillis(nanoAdjustment);
  }

  protected abstract Instant getDefaultValue();

  protected abstract Double getNaValue();
}

package gms.shared.signaldetection.dao.css.converter;

import com.google.common.math.DoubleMath;

import javax.persistence.AttributeConverter;
import java.time.Duration;

public class DurationToDoubleConverter implements AttributeConverter<Duration, Double> {
  private static final long NANOS_PER_SECOND = 1_000_000_000;

  @Override
  public Double convertToDatabaseColumn(Duration attribute) {
    if (attribute == null) {
      return -1.0;
    }

    return (double) attribute.toNanos() / NANOS_PER_SECOND;
  }

  @Override
  public Duration convertToEntityAttribute(Double dbData) {
    if (DoubleMath.fuzzyEquals(-1.0, dbData, .00001)) {
      return null;
    }

    return Duration.ofNanos((long) (dbData * NANOS_PER_SECOND));
  }
}

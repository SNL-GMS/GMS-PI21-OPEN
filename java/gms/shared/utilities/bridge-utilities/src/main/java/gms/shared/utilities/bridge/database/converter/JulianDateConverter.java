package gms.shared.utilities.bridge.database.converter;

import javax.persistence.AttributeConverter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

public abstract class JulianDateConverter implements AttributeConverter<Instant, Integer> {

  private static final Instant MAX_DATE = Instant.parse("+1000000000-12-31T23:59:59.999999999Z");
  /**
   * The maximum value of a Julian date in the database, according to the customer's definition of 'end of time'
   */
  private static final int MAX_JDATE = 2286324;

  @Override
  public Integer convertToDatabaseColumn(Instant attribute) {
    if (attribute == null || getDefaultValue().equals(attribute)) {
      return getNaValue();
    } else if (attribute.equals(MAX_DATE)) {
      return MAX_JDATE;
    } else {
      LocalDate dateValue = LocalDate.ofInstant(attribute, ZoneOffset.UTC);
      return (dateValue.get(ChronoField.YEAR) * 1000) + dateValue.get(ChronoField.DAY_OF_YEAR);
    }
  }

  @Override
  public Instant convertToEntityAttribute(Integer dbData) {
    if (dbData == null) {
      return null;
    } else if (dbData == getNaValue()) {
      return getDefaultValue();
    } else {
      int days = dbData % 1000;
      int years = (dbData - days) / 1000;

      return Instant.from(LocalDate.ofYearDay(years, days).atStartOfDay().toInstant(ZoneOffset.UTC));
    }
  }

  protected abstract Instant getDefaultValue();

  protected abstract int getNaValue();

}

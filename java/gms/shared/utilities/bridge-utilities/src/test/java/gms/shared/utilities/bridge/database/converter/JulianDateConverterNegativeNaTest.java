package gms.shared.utilities.bridge.database.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JulianDateConverterNegativeNaTest {

  private JulianDateConverterNegativeNa converter;

  @BeforeEach
  void setup() {
    converter = new JulianDateConverterNegativeNa();
  }

  @Test
  void testConvertToDatabaseColumnDefault() {
    assertEquals(-1, converter.convertToDatabaseColumn(null));
  }

  @Test
  void testConvertToDatabaseColumnDefault_min() {
    assertEquals(-1, converter.convertToDatabaseColumn(Instant.MIN));
  }

  @Test
  void testConvertToDatabaseColumn() {
    assertEquals(1970001, converter.convertToDatabaseColumn(Instant.EPOCH.truncatedTo(ChronoUnit.DAYS)));
  }

  @Test
  void testConvertToEntityAttributeDefault() {
    assertEquals(Instant.MIN, converter.convertToEntityAttribute(-1));
  }

  @Test
  void testConvertToEntityAttribute() {
    assertEquals(LocalDate.ofYearDay(2005, 65).atStartOfDay(ZoneOffset.UTC).toInstant(), converter.convertToEntityAttribute(2005065));
  }
}
package gms.shared.utilities.bridge.database.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BooleanYNConverterTest {

  private BooleanYNConverter converter;

  @BeforeEach
  void setup() {
    converter = new BooleanYNConverter();
  }

  @Test
  void testConvertToDatabaseColumn() {
    assertNull(converter.convertToDatabaseColumn(null));
  }

  @Test
  void testConvertToDatabaseColumn_val() {
    assertEquals("y", converter.convertToDatabaseColumn(Boolean.TRUE));
  }

  @Test
  void testConvertToDatabaseColumn_val2() {
    assertEquals("n", converter.convertToDatabaseColumn(Boolean.FALSE));
  }

  @Test
  void testConvertToEntityAttributeNull() {
    assertNull(converter.convertToEntityAttribute(null));
  }

  @Test
  void testConvertToEntityAttributeDefault() {
    assertEquals(true, converter.convertToEntityAttribute("y"));
  }

  @Test
  void testConvertToEntityAttribute() {
    assertEquals(false, converter.convertToEntityAttribute("n"));
  }

  @Test
  void testConvertToEntityAttributeException() {
    assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("yn"));
  }
}
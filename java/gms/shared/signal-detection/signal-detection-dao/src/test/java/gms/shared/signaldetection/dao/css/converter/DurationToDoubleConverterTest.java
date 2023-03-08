package gms.shared.signaldetection.dao.css.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DurationToDoubleConverterTest {

  private DurationToDoubleConverter converter;

  @BeforeEach
  void setup() {
    converter = new DurationToDoubleConverter();
  }

  @ParameterizedTest
  @MethodSource("getConvertToDatabaseColumnArguments")
  void testConvertToDatabaseColumn(Duration original, Double expected) {
    assertEquals(expected, converter.convertToDatabaseColumn(original));
  }

  static Stream<Arguments> getConvertToDatabaseColumnArguments() {
    return Stream.of(arguments(null, -1.0),
      arguments(Duration.ofMillis(500), 0.5));
  }

  @ParameterizedTest
  @MethodSource("getConvertToEntityAttributeArguments")
  void testConvertToEntityAttribute(Duration expected, Double original) {
    assertEquals(expected, converter.convertToEntityAttribute(original));
  }

  static Stream<Arguments> getConvertToEntityAttributeArguments() {
    return Stream.of(arguments(null, -1.0),
      arguments(Duration.ofMillis((30 * 1000) + 500), 30.5));
  }

}
package gms.shared.signaldetection.dao.css.converter;

import gms.shared.signaldetection.dao.css.enums.SignalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SignalTypeConverterTest {

  private SignalTypeConverter converter;

  @BeforeEach
  void setup() {
    converter = new SignalTypeConverter();
  }

  @ParameterizedTest
  @MethodSource("getConvertToDatabaseColumnArguments")
  void testConvertToDatabaseColumn(String expected, SignalType signalType) {
    assertEquals(expected, converter.convertToDatabaseColumn(signalType));
  }

  static Stream<Arguments> getConvertToDatabaseColumnArguments() {
    return Stream.of(arguments(null, null),
      arguments("l", SignalType.LOCAL_EVENT),
      arguments("r", SignalType.REGIONAL_EVENT),
      arguments("t", SignalType.TELESEISMID_EVENT),
      arguments("m", SignalType.MIXED_EVENT),
      arguments("g", SignalType.GLITCH),
      arguments("e", SignalType.CALIBRATION_ACTIVITY_OBFUSCATED));
  }

  @ParameterizedTest
  @MethodSource("getConvertToEntityAttributeArguments")
  void testConvertToEntityAttribute(SignalType expected, String dbData) {
    assertEquals(expected, converter.convertToEntityAttribute(dbData));
  }

  static Stream<Arguments> getConvertToEntityAttributeArguments() {
    return Stream.of(arguments(null, null),
      arguments(null, ""),
      arguments(null, " "),
      arguments(SignalType.LOCAL_EVENT, "l"),
      arguments(SignalType.REGIONAL_EVENT, "r"),
      arguments(SignalType.TELESEISMID_EVENT, "t"),
      arguments(SignalType.MIXED_EVENT, "m"),
      arguments(SignalType.GLITCH, "g"),
      arguments(SignalType.CALIBRATION_ACTIVITY_OBFUSCATED, "e"));
  }

}
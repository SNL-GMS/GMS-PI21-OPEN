package gms.shared.utilities.bridge.database.converter;

import gms.shared.utilities.bridge.database.enums.ClipFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ClipFlagConverterTest {

  private ClipFlagConverter converter;

  @BeforeEach
  void setup() {
    converter = new ClipFlagConverter();
  }

  @ParameterizedTest
  @MethodSource("getConvertToDatabaseColumnArguments")
  void testConvertToDatabaseColumnArguments(String expected, ClipFlag flag) {
    assertEquals(expected, converter.convertToDatabaseColumn(flag));
  }

  static Stream<Arguments> getConvertToDatabaseColumnArguments() {
    return Stream.of(arguments(ClipFlag.NA.getName(), ClipFlag.NA),
      arguments(ClipFlag.CLIPPED.getName(), ClipFlag.CLIPPED),
      arguments(ClipFlag.NOT_CLIPPED.getName(), ClipFlag.NOT_CLIPPED),
      arguments(null, null));
  }

  @ParameterizedTest
  @MethodSource("getConvertToEntityAttributeArguments")
  void testConvertToEntityAttribute(ClipFlag expected, String data) {
    assertEquals(expected, converter.convertToEntityAttribute(data));
  }

  static Stream<Arguments> getConvertToEntityAttributeArguments() {
    return Stream.of(arguments(ClipFlag.NA, ClipFlag.NA.getName()),
      arguments(ClipFlag.CLIPPED, ClipFlag.CLIPPED.getName()),
      arguments(ClipFlag.NOT_CLIPPED, ClipFlag.NOT_CLIPPED.getName()),
      arguments(null, null),
      arguments(null, "not a flag"));
  }
}
package gms.shared.signaldetection.dao.css.converter;

import gms.shared.stationdefinition.dao.css.converter.TagNameConverter;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TagNameConverterTest {

  private TagNameConverter converter;

  @BeforeEach
  void setup() {
    converter = new TagNameConverter();
  }

  @ParameterizedTest
  @MethodSource("getConvertToDatabaseColumnArguments")
  void testConvertToDatabaseColumn(String expected, TagName tagName) {
    assertEquals(expected, converter.convertToDatabaseColumn(tagName));
  }

  static Stream<Arguments> getConvertToDatabaseColumnArguments() {
    return Stream.of(arguments(null, null),
      arguments("arid", TagName.ARID),
      arguments("evid", TagName.EVID),
      arguments("orid", TagName.ORID),
      arguments("stassid", TagName.STASSID),
      arguments("msig", TagName.MSGID),
      arguments("clustaid", TagName.CLUSTAID),
      arguments("-", TagName.UNKNOWN));
  }

  @ParameterizedTest
  @MethodSource("getConvertToEntityAttributeArguments")
  void testConvertToEntityAttribute(TagName expected, String dbData) {
    assertEquals(expected, converter.convertToEntityAttribute(dbData));
  }

  static Stream<Arguments> getConvertToEntityAttributeArguments() {
    return Stream.of(arguments(null, null),
      arguments(null, ""),
      arguments(null, " "),
      arguments(TagName.ARID, "arid"),
      arguments(TagName.EVID, "evid"),
      arguments(TagName.ORID, "orid"),
      arguments(TagName.STASSID, "stassid"),
      arguments(TagName.MSGID, "msig"),
      arguments(TagName.CLUSTAID, "clustaid"),
      arguments(TagName.UNKNOWN, "-"));
  }
}

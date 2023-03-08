package gms.shared.utilities.bridge.database.converter;

import gms.shared.utilities.bridge.database.enums.IdTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class IdTagConverterTest {
  private IdTagConverter idTagConverter;

  @BeforeEach
  void setUpConverter() {
    idTagConverter = new IdTagConverter();
  }

  @ParameterizedTest
  @MethodSource("convertToDatabaseColumnArguments")
  void testConvertToDatabaseColumnArguments(String expected, IdTag tag) {
    assertEquals(expected, idTagConverter.convertToDatabaseColumn(tag));
  }

  static Stream<Arguments> convertToDatabaseColumnArguments() {
    return Stream.of(
      arguments(IdTag.ARID.getName(), IdTag.ARID),
      arguments(IdTag.ORID.getName(), IdTag.ORID),
      arguments(IdTag.EVID.getName(), IdTag.EVID),
      arguments(null, null)
    );
  }

  @ParameterizedTest
  @MethodSource("getConvertToEntityAttributeArguments")
  void testConvertToEntityAttribute(IdTag expected, String data) {
    assertEquals(expected, idTagConverter.convertToEntityAttribute(data));
  }

  static Stream<Arguments> getConvertToEntityAttributeArguments() {
    return Stream.of(
      arguments(IdTag.ARID, IdTag.ARID.getName()),
      arguments(IdTag.ORID, IdTag.ORID.getName()),
      arguments(IdTag.EVID, IdTag.EVID.getName()),
      arguments(null, null),
      arguments(null, "not a tag")
    );
  }
}

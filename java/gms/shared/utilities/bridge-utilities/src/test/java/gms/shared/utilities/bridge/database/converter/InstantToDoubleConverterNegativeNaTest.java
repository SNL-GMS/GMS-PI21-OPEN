package gms.shared.utilities.bridge.database.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantToDoubleConverterNegativeNaTest extends
  InstantToDoubleConverterTest<InstantToDoubleConverterNegativeNa> {

  @Override
  protected InstantToDoubleConverterNegativeNa getConverter() {
    return new InstantToDoubleConverterNegativeNa();
  }

  @ParameterizedTest
  @MethodSource("convertToDatabaseColumnArguments")
  void testConvertToDatabaseColumn_NA_Values(Instant providedInput, Double expectedResult) {
    assertEquals(expectedResult, converter.convertToDatabaseColumn(providedInput));
  }

  private static Stream<Arguments> convertToDatabaseColumnArguments() {
    return Stream.of(
      Arguments.arguments(null, InstantToDoubleConverterNegativeNa.NA_VALUE),
      Arguments.arguments(InstantToDoubleConverterNegativeNa.NA_TIME,
        InstantToDoubleConverterNegativeNa.NA_VALUE)
    );
  }

  @ParameterizedTest
  @MethodSource("convertToEntityAttributeArguments")
  void testConvertToEntityAttribute_NA_Values(Double providedInput, Instant expectedResult) {
    assertEquals(expectedResult, converter.convertToEntityAttribute(providedInput));
  }

  private static Stream<Arguments> convertToEntityAttributeArguments() {
    return Stream.of(
      Arguments.arguments(InstantToDoubleConverterNegativeNa.NA_VALUE,
        InstantToDoubleConverterNegativeNa.NA_TIME)
    );
  }
}

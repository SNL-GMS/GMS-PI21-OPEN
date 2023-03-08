package gms.shared.utilities.bridge.database.converter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantToDoubleConverterPositiveNaTest extends
  InstantToDoubleConverterTest<InstantToDoubleConverterPositiveNa> {

  @Override
  protected InstantToDoubleConverterPositiveNa getConverter() {
    return new InstantToDoubleConverterPositiveNa();
  }

  @ParameterizedTest
  @MethodSource("convertToDatabaseColumnArguments_NA_Values")
  void testConvertToDatabaseColumn_NA_Values(Instant providedInput, Double expectedResult) {
    assertEquals(expectedResult, converter.convertToDatabaseColumn(providedInput));
  }

  private static Stream<Arguments> convertToDatabaseColumnArguments_NA_Values() {
    return Stream.of(
      Arguments.arguments(null, InstantToDoubleConverterPositiveNa.NA_VALUE),
      Arguments.arguments(InstantToDoubleConverterPositiveNa.NA_TIME,
        InstantToDoubleConverterPositiveNa.NA_VALUE)
    );
  }

  @ParameterizedTest
  @MethodSource("convertToEntityAttributeArguments_NA_Values")
  void testConvertToEntityAttribute_NA_Values(Double providedInput, Instant expectedResult) {
    assertEquals(expectedResult, converter.convertToEntityAttribute(providedInput));
  }

  private static Stream<Arguments> convertToEntityAttributeArguments_NA_Values() {
    return Stream.of(
      Arguments.arguments(InstantToDoubleConverterPositiveNa.NA_VALUE,
        InstantToDoubleConverterPositiveNa.NA_TIME)
    );
  }
}

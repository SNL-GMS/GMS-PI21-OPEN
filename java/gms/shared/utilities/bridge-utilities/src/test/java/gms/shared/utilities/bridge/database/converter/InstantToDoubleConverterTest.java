package gms.shared.utilities.bridge.database.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class InstantToDoubleConverterTest<T extends InstantToDoubleConverter> {

  protected static final Double RANDOM_DATE = 1205224320d;
  protected static final Instant CORRESPONDING_INSTANT = Instant.parse("2008-03-11T08:32:00Z");
  protected static final Double TEST_END_VALUE = 1274414400d;
  protected static final Instant TEST_START_TIME = Instant.parse("2010-05-21T00:00:00Z");
  protected static final Double TEST_START_VALUE = 1274400000d;
  protected static final Instant TEST_END_TIME = Instant.parse("2010-05-21T04:00:00Z");
  protected static final Double EPOCH_VALUE = 0d;
  protected static final Instant EPOCH_TIME = Instant.parse("1970-01-01T00:00:00Z");
  protected static final Double TEST_WRITTEN_AT_VALUE = 1615469785.999d;
  protected static final Instant TEST_WRITTEN_AT_TIME = Instant.parse("2021-03-11T13:36:25.999Z");
  protected static final Double TEST_WRITTEN_AT_VALUE_2 = 1615494809.576d;
  protected static final Instant TEST_WRITTEN_AT_TIME_2 = Instant.parse("2021-03-11T20:33:29.576Z");

  protected T converter;

  @BeforeEach
  void setup() {
    converter = getConverter();
  }


  @ParameterizedTest
  @MethodSource("convertToDatabaseColumnArguments")
  void testConvertToDatabaseColumn(Instant providedInput, Double expectedResult) {
    assertEquals(expectedResult, converter.convertToDatabaseColumn(providedInput));
  }

  private static Stream<Arguments> convertToDatabaseColumnArguments() {
    return Stream.of(
      Arguments.arguments(CORRESPONDING_INSTANT, RANDOM_DATE),
      Arguments.arguments(EPOCH_TIME, EPOCH_VALUE),
      Arguments.arguments(Instant.EPOCH, EPOCH_VALUE),
      Arguments.arguments(TEST_WRITTEN_AT_TIME, TEST_WRITTEN_AT_VALUE),
      Arguments.arguments(TEST_WRITTEN_AT_TIME_2, TEST_WRITTEN_AT_VALUE_2),
      Arguments.arguments(TEST_START_TIME, TEST_START_VALUE),
      Arguments.arguments(TEST_END_TIME, TEST_END_VALUE)
    );
  }

  @ParameterizedTest
  @MethodSource("convertToEntityAttributeArguments")
  void testConvertToEntityAttribute(Double providedInput, Instant expectedResult) {
    assertEquals(expectedResult, converter.convertToEntityAttribute(providedInput));
  }

  private static Stream<Arguments> convertToEntityAttributeArguments() {
    return Stream.of(
      Arguments.arguments(RANDOM_DATE, CORRESPONDING_INSTANT),
      Arguments.arguments(EPOCH_VALUE, EPOCH_TIME),
      Arguments.arguments(EPOCH_VALUE, Instant.EPOCH),
      Arguments.arguments(TEST_WRITTEN_AT_VALUE, TEST_WRITTEN_AT_TIME),
      Arguments.arguments(TEST_WRITTEN_AT_VALUE_2, TEST_WRITTEN_AT_TIME_2),
      Arguments.arguments(TEST_START_VALUE, TEST_START_TIME),
      Arguments.arguments(TEST_END_VALUE, TEST_END_TIME)
    );
  }

  protected abstract T getConverter();
}

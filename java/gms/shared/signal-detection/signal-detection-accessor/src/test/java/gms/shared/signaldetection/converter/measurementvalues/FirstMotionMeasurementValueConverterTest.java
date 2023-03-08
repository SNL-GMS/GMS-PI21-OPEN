package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.FirstMotionType;
import gms.shared.signaldetection.coi.values.FirstMotionMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_4;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FirstMotionMeasurementValueConverterTest
  extends SignalDetectionMeasurementValueConverterTest<FirstMotionMeasurementValueConverter> {

  private static final FirstMotionMeasurementValue VALUE_1 =
    FirstMotionMeasurementValue.from(FirstMotionType.COMPRESSION, Optional.empty(),
      Optional.of(Instant.EPOCH));
  private static final FirstMotionMeasurementValue VALUE_21 =
    FirstMotionMeasurementValue.from(FirstMotionType.COMPRESSION, Optional.empty(),
      Optional.of(Instant.EPOCH.plusSeconds(1)));
  private static final FirstMotionMeasurementValue VALUE_22 =
    FirstMotionMeasurementValue.from(
      FirstMotionType.DILATION, Optional.empty(),
      Optional.of(Instant.EPOCH.plusSeconds(1)));
  private static final FirstMotionMeasurementValue VALUE_3 =
    FirstMotionMeasurementValue.from(FirstMotionType.DILATION, Optional.empty(),
      Optional.of(Instant.EPOCH));
  private static final FirstMotionMeasurementValue VALUE_4 = FirstMotionMeasurementValue.from(
    FirstMotionType.INDETERMINATE, Optional.empty(),
    Optional.of(Instant.EPOCH.plusSeconds(100)));

  @BeforeEach
  void setup() {
    converter = FirstMotionMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    FirstMotionMeasurementValueConverter converter =
      assertDoesNotThrow(FirstMotionMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @ParameterizedTest
  @MethodSource("getConvertValidationArguments")
  void testConvertValidation(Class<? extends Exception> expectedException,
    MeasurementValueSpec<FirstMotionMeasurementValue> spec) {

    assertThrows(expectedException, () -> converter.convert(spec));
  }

  @ParameterizedTest
  @MethodSource("getTestConvertArguments")
  void testConvert(FirstMotionMeasurementValue expectedValue,
    MeasurementValueSpec<FirstMotionMeasurementValue> spec) {

    Optional<FirstMotionMeasurementValue> actualValue = converter.convert(spec);
    assertTrue(actualValue.isPresent());
    assertEquals(expectedValue, actualValue.get());
  }

  static Stream<Arguments> getConvertValidationArguments() {
    MeasurementValueSpec<FirstMotionMeasurementValue> spec1 = MeasurementValueSpec.<FirstMotionMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION)
      .build();

    return Stream.of(
      arguments(NoSuchElementException.class, spec1)
    );
  }

  static Stream<Arguments> getTestConvertArguments() {

    // Create the measurement value specs for multiple converters
    MeasurementValueSpec<FirstMotionMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<FirstMotionMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("c")
      .build();
    MeasurementValueSpec<FirstMotionMeasurementValue> measurementValueSpec21 = MeasurementValueSpec.<FirstMotionMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("c")
      .build();
    MeasurementValueSpec<FirstMotionMeasurementValue> measurementValueSpec22 = MeasurementValueSpec.<FirstMotionMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("r")
      .build();
    MeasurementValueSpec<FirstMotionMeasurementValue> measurementValueSpec3 = MeasurementValueSpec.<FirstMotionMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_3)
      .setFeatureMeasurementType(FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("r")
      .build();
    MeasurementValueSpec<FirstMotionMeasurementValue> measurementValueSpec4 = MeasurementValueSpec.<FirstMotionMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_4)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION)
      .setFeatureMeasurementTypeCode("-")
      .build();

    return Stream.of(
      arguments(VALUE_1, measurementValueSpec1),
      arguments(VALUE_21, measurementValueSpec21),
      arguments(VALUE_22, measurementValueSpec22),
      arguments(VALUE_3, measurementValueSpec3),
      arguments(VALUE_4, measurementValueSpec4));
  }

  @Test
  void testNullArguments() {
    testConvertNull(FirstMotionMeasurementValueConverter.create());
  }
}

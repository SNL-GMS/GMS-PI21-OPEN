package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_2;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NumericMeasurementValueConverterTest
  extends SignalDetectionMeasurementValueConverterTest<NumericMeasurementValueConverter> {

  private static final NumericMeasurementValue VALUE_1 = NumericMeasurementValue.from(Optional.empty(),
    DoubleValue.from(37.0, Optional.empty(), Units.DEGREES));
  private static final NumericMeasurementValue VALUE_2 = NumericMeasurementValue.from(Optional.empty(),
    DoubleValue.from(32.1, Optional.empty(), Units.SECONDS));
  private static final NumericMeasurementValue VALUE_3 = NumericMeasurementValue.from(Optional.empty(),
    DoubleValue.from(180, Optional.of(0.5), Units.DEGREES));

  @BeforeEach
  void setup() {
    converter = NumericMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    NumericMeasurementValueConverter converter =
      assertDoesNotThrow(NumericMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @ParameterizedTest
  @MethodSource("getConvertValidationArguments")
  void testConvertValidation(Class<? extends Exception> expectedException,
    MeasurementValueSpec<NumericMeasurementValue> spec) {

    assertThrows(expectedException, () -> converter.convert(spec));
  }

  @ParameterizedTest
  @MethodSource("getTestConvertArguments")
  void testConvert(NumericMeasurementValue expectedValue,
    MeasurementValueSpec<NumericMeasurementValue> spec) {

    Optional<NumericMeasurementValue> actualValue = converter.convert(spec);

    assertTrue(actualValue.isPresent());
    assertEquals(expectedValue, actualValue.get());
  }

  static Stream<Arguments> getConvertValidationArguments() {
    MeasurementValueSpec<NumericMeasurementValue> spec1 = MeasurementValueSpec.<NumericMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.EMERGENCE_ANGLE)
      .setUnits(Units.SECONDS_PER_DEGREE)
      .build();
    MeasurementValueSpec<NumericMeasurementValue> spec2 = MeasurementValueSpec.<NumericMeasurementValue>builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.EMERGENCE_ANGLE)
      .setMeasuredValueExtractor(ArrivalDao::getEmergenceAngle)
      .build();

    return Stream.of(
      arguments(NoSuchElementException.class, spec1),
      arguments(NoSuchElementException.class, spec2)
    );
  }

  static Stream<Arguments> getTestConvertArguments() {
    // measurement value specs for each numeric measurement value converter
    MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<NumericMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.EMERGENCE_ANGLE)
      .setMeasuredValueExtractor(Optional.of(ArrivalDao::getEmergenceAngle))
      .setUnits(Optional.of(Units.DEGREES))
      .build();
    MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec2 = MeasurementValueSpec.<NumericMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.SLOWNESS)
      .setMeasuredValueExtractor(Optional.of(ArrivalDao::getSlowness))
      .setUnits(Optional.of(Units.SECONDS))
      .build();
    MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec3 = MeasurementValueSpec.<NumericMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH)
      .setMeasuredValueExtractor(Optional.of(ArrivalDao::getAzimuth))
      .setUncertaintyValueExtractor(Optional.of(ArrivalDao::getAzimuthUncertainty))
      .setUnits(Optional.of(Units.DEGREES))
      .build();

    return Stream.of(
      arguments(VALUE_1, measurementValueSpec1),
      arguments(VALUE_2, measurementValueSpec2),
      arguments(VALUE_3, measurementValueSpec3)
    );
  }

  @Test
  void testNullArguments() {
    testConvertNull(NumericMeasurementValueConverter.create());
  }
}

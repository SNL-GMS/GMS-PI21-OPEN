package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_2;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ArrivalTimeMeasurementValueConverterTest
  extends SignalDetectionMeasurementValueConverterTest<ArrivalTimeMeasurementValueConverter> {

  private ArrivalTimeMeasurementValueConverter converter;

  @BeforeEach
  void setup() {
    converter = ArrivalTimeMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    ArrivalTimeMeasurementValueConverter converter =
      assertDoesNotThrow(ArrivalTimeMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @ParameterizedTest
  @MethodSource("getTestConvertArguments")
  void testConvert(ArrivalTimeMeasurementValue expectedValue,
    MeasurementValueSpec<ArrivalTimeMeasurementValue> measurementValueSpec) {

    final Optional<ArrivalTimeMeasurementValue> actualValue;
    actualValue = converter.convert(measurementValueSpec);

    assertTrue(actualValue.isPresent());
    assertEquals(expectedValue, actualValue.get());
  }

  static Stream<Arguments> getTestConvertArguments() {
    MeasurementValueSpec<ArrivalTimeMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<ArrivalTimeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.ARRIVAL_TIME)
      .build();
    MeasurementValueSpec<ArrivalTimeMeasurementValue> measurementValueSpec2 = MeasurementValueSpec.<ArrivalTimeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.ARRIVAL_TIME)
      .build();
    Duration stdDev1 = Duration.ofMillis((long) (ARRIVAL_1.getTimeUncertainty() * Math.pow(10, 3)));
    Duration stdDev2 = Duration.ofMillis((long) (ARRIVAL_2.getTimeUncertainty() * Math.pow(10, 3)));
    final ArrivalTimeMeasurementValue atmVal1 = ArrivalTimeMeasurementValue.fromFeatureMeasurement(
      InstantValue.from(ARRIVAL_1.getArrivalKey().getTime(), stdDev1));
    final ArrivalTimeMeasurementValue atmVal2 = ArrivalTimeMeasurementValue.fromFeatureMeasurement(
      InstantValue.from(ARRIVAL_2.getArrivalKey().getTime(), stdDev2));

    return Stream.of(
      arguments(atmVal1, measurementValueSpec1),
      arguments(atmVal2, measurementValueSpec2)
    );
  }

  @Test
  void testNullArguments() {
    testConvertNull(ArrivalTimeMeasurementValueConverter.create());
  }
}

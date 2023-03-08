package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_4;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_4;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PhaseTypeMeasurementValueConverterTest
  extends SignalDetectionMeasurementValueConverterTest<PhaseTypeMeasurementValueConverter> {

  private PhaseTypeMeasurementValueConverter converter;

  @BeforeEach
  void setup() {
    converter = PhaseTypeMeasurementValueConverter.create();
  }

  @Test
  void testCreate() {
    PhaseTypeMeasurementValueConverter converter =
      assertDoesNotThrow(PhaseTypeMeasurementValueConverter::create);
    assertNotNull(converter);
  }

  @ParameterizedTest
  @MethodSource("getTestConvertArgumentsArrival")
  void testConvertArrival(PhaseTypeMeasurementValue expectedValue,
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec) {

    final Optional<PhaseTypeMeasurementValue> actualValue;
    actualValue = converter.convert(measurementValueSpec);

    assertTrue(actualValue.isPresent());
    assertEquals(expectedValue, actualValue.get());
  }

  static Stream<Arguments> getTestConvertArgumentsArrival() {
    final PhaseTypeMeasurementValue ptmValue1 = PhaseTypeMeasurementValue.fromFeatureMeasurement(PhaseType.P,
      Optional.empty(), Instant.EPOCH);
    final PhaseTypeMeasurementValue ptmValue2 = PhaseTypeMeasurementValue.fromFeatureMeasurement(PhaseType.I,
      Optional.empty(), Instant.EPOCH.plusSeconds(100));

    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec2 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_4)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();

    return Stream.of(
      arguments(ptmValue1, measurementValueSpec1),
      arguments(ptmValue2, measurementValueSpec2)
    );
  }

  @ParameterizedTest
  @MethodSource("getTestConvertArgumentsAssoc")
  void testConvertAssoc(PhaseTypeMeasurementValue expectedValue,
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec) {

    final Optional<PhaseTypeMeasurementValue> actualValue;
    actualValue = converter.convert(measurementValueSpec);

    assertTrue(actualValue.isPresent());
    assertEquals(expectedValue, actualValue.get());
  }

  static Stream<Arguments> getTestConvertArgumentsAssoc() {
    final PhaseTypeMeasurementValue ptmValue1 = PhaseTypeMeasurementValue.fromFeaturePrediction(PhaseType.S,
      Optional.of(0.8));
    final PhaseTypeMeasurementValue ptmValue2 = PhaseTypeMeasurementValue.fromFeaturePrediction(PhaseType.P,
      Optional.of(0.5));
    final PhaseTypeMeasurementValue ptmValue3 = PhaseTypeMeasurementValue.fromFeatureMeasurement(PhaseType.Tx,
      Optional.empty(), Instant.EPOCH);
    final PhaseTypeMeasurementValue ptmValue4 = PhaseTypeMeasurementValue.fromFeaturePrediction(PhaseType.P,
      Optional.empty());

    ArrivalDao arrivalDao2 = new ArrivalDao(ARRIVAL_1);
    arrivalDao2.setPhase("Tx");

    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec1 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setAssocDao(ASSOC_DAO_1)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec2 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setAssocDao(ASSOC_DAO_2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec3 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(arrivalDao2)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec4 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setAssocDao(ASSOC_DAO_4)
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();

    return Stream.of(
      arguments(ptmValue1, measurementValueSpec1),
      arguments(ptmValue2, measurementValueSpec2),
      arguments(ptmValue3, measurementValueSpec3),
      arguments(ptmValue4, measurementValueSpec4)
    );
  }

  @Test
  void testNullArguments() {
    testConvertNull(PhaseTypeMeasurementValueConverter.create());
  }

  @ParameterizedTest
  @MethodSource("getTestIllegalStateArguments")
  void testIllegalState(MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec) {

    assertThrows(IllegalStateException.class,
      () -> converter.convert(measurementValueSpec));

  }

  static Stream<Arguments> getTestIllegalStateArguments() {
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec2 = MeasurementValueSpec.<PhaseTypeMeasurementValue>
        builder()
      .setArrivalDao(ARRIVAL_1)
      .setAssocDao(Optional.of(ASSOC_DAO_3))
      .setFeatureMeasurementType(FeatureMeasurementTypes.PHASE)
      .build();

    return Stream.of(
      arguments(measurementValueSpec2)
    );
  }
}

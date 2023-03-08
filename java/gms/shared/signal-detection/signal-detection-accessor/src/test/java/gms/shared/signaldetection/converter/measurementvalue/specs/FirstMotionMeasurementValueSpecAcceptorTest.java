package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.FirstMotionMeasurementValue;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.LONG_PERIOD_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.SHORT_PERIOD_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_3;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirstMotionMeasurementValueSpecAcceptorTest extends MeasurementValueSpecAcceptorTest<FirstMotionMeasurementValue> {
  protected FirstMotionMeasurementValueSpecAcceptor converterSpec;

  @BeforeEach
  void setup() {
    converterSpec = FirstMotionMeasurementValueSpecAcceptor.create();
  }

  @ParameterizedTest
  @MethodSource("getTestSpecArguments")
  void testAcceptor(
    MeasurementValueSpec<FirstMotionMeasurementValue> measurementValueSpec,
    FeatureMeasurementType<FirstMotionMeasurementValue> featureMeasurementType,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {

    testSpecAcceptor(measurementValueSpec,
      featureMeasurementType,
      converterSpec,
      buildSpecVisitorConsumer(measurementValueSpec, featureMeasurementType, arrivalDao, assocDao),
      arrivalDao,
      assocDao);
  }

  static Stream<Arguments> getTestSpecArguments() {
    return Stream.of(
      arguments(SHORT_PERIOD_MEASUREMENT_SPEC,
        FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION,
        ARRIVAL_1,
        Optional.empty()),
      arguments(LONG_PERIOD_MEASUREMENT_SPEC,
        FeatureMeasurementTypes.LONG_PERIOD_FIRST_MOTION,
        ARRIVAL_3,
        Optional.empty())
    );
  }

  @Override
  Consumer<MeasurementValueSpecVisitorInterface<FirstMotionMeasurementValue>> buildSpecVisitorConsumer(
    MeasurementValueSpec<FirstMotionMeasurementValue> expectedMeasurementValueSpec,
    FeatureMeasurementType<FirstMotionMeasurementValue> featureMeasurementType,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {

    // Measurement value spec visitor setup
    return visitor ->
      when(visitor.visit(converterSpec,
        featureMeasurementType,
        arrivalDao,
        assocDao))
        .thenReturn(Stream.of(expectedMeasurementValueSpec));
  }
}
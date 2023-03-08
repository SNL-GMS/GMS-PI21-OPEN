package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.PHASE_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.PHASE_MEASUREMENT_SPEC_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_2;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhaseTypeMeasurementValueSpecAcceptorTest extends MeasurementValueSpecAcceptorTest<PhaseTypeMeasurementValue> {
  protected PhaseTypeMeasurementValueSpecAcceptor converterSpec;

  @Mock
  private MeasurementValueSpecVisitorInterface<PhaseTypeMeasurementValue> specVisitor;

  @BeforeEach
  void setup() {
    converterSpec = PhaseTypeMeasurementValueSpecAcceptor.create();
  }

  @ParameterizedTest
  @MethodSource("getTestSpecArguments")
  void testAcceptor(
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec,
    FeatureMeasurementType<PhaseTypeMeasurementValue> featureMeasurementType,
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
      arguments(PHASE_MEASUREMENT_SPEC,
        FeatureMeasurementTypes.PHASE,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1)),
      arguments(PHASE_MEASUREMENT_SPEC,
        FeatureMeasurementTypes.PHASE,
        ARRIVAL_2,
        Optional.of(ASSOC_DAO_2)),
      arguments(PHASE_MEASUREMENT_SPEC_2,
        FeatureMeasurementTypes.PHASE,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_2))
    );
  }

  @Override
  Consumer<MeasurementValueSpecVisitorInterface<PhaseTypeMeasurementValue>> buildSpecVisitorConsumer(
    MeasurementValueSpec<PhaseTypeMeasurementValue> measurementValueSpec,
    FeatureMeasurementType<PhaseTypeMeasurementValue> featureMeasurementType,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {
    // Measurement value spec visitor setup
    return visitor ->
      when(visitor.visit(converterSpec,
        featureMeasurementType,
        arrivalDao,
        assocDao))
        .thenReturn(Stream.of(measurementValueSpec));
  }
}
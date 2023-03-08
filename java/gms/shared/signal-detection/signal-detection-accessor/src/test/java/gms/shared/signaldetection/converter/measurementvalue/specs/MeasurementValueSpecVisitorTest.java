package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.FirstMotionMeasurementValue;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.ARRIVAL_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.EMERGENCE_ANGLE_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.PHASE_MEASUREMENT_SPEC_2;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.RECEIVER_AZIMUTH_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.RECTILINEARITY_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.SHORT_PERIOD_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.SLOWNESS_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_2;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class MeasurementValueSpecVisitorTest {
  @Mock
  private ArrivalTimeMeasurementValueSpecAcceptor arrivalSpec;
  @Mock
  private EmergenceAngleMeasurementValueSpecAcceptor emergenceSpec;
  @Mock
  private FirstMotionMeasurementValueSpecAcceptor firstMotionSpec;
  @Mock
  private PhaseTypeMeasurementValueSpecAcceptor phaseSpec;
  @Mock
  private ReceiverToSourceAzimuthMeasurementValueSpecAcceptor receiverSpec;
  @Mock
  private RectilinearityMeasurementValueSpecAcceptor rectilinearitySpec;
  @Mock
  private SlownessMeasurementValueSpecAcceptor slownessSpec;
  @Mock
  private AmplitudeMeasurementValueSpecAcceptor ampSpec;

  @Test
  <V>
  void testCreate() {
    MeasurementValueSpecVisitor<V> converterVisitor =
      assertDoesNotThrow((ThrowingSupplier<MeasurementValueSpecVisitor<V>>) MeasurementValueSpecVisitor::create);
    assertNotNull(converterVisitor);
  }

  // ------------------------------------------
  // ArrivalDao visitor tests
  // ------------------------------------------

  @Test
  void test_visitArrival() {
    MeasurementValueSpecVisitor<ArrivalTimeMeasurementValue> converterVisitor =
      MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<ArrivalTimeMeasurementValue>> spec = converterVisitor.visit(arrivalSpec,
      FeatureMeasurementTypes.ARRIVAL_TIME, ARRIVAL_1, Optional.empty());
    assertEquals(ARRIVAL_MEASUREMENT_SPEC, spec.findFirst().orElseThrow());
  }

  @Test
  void test_visitEmergence() {
    MeasurementValueSpecVisitor<NumericMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<NumericMeasurementValue>> specStream = converterVisitor.visit(emergenceSpec,
      FeatureMeasurementTypes.EMERGENCE_ANGLE, ARRIVAL_1, Optional.empty());
    MeasurementValueSpec<NumericMeasurementValue> spec = specStream.findFirst().orElseThrow();
    assertEquals(EMERGENCE_ANGLE_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
    assertEquals(EMERGENCE_ANGLE_MEASUREMENT_SPEC.getFeatureMeasurementType(), spec.getFeatureMeasurementType());
    assertEquals(EMERGENCE_ANGLE_MEASUREMENT_SPEC.getMeasuredValueExtractor().isPresent(), spec.getMeasuredValueExtractor().isPresent());
    assertEquals(EMERGENCE_ANGLE_MEASUREMENT_SPEC.getUnits(), spec.getUnits());
  }

  @Test
  void test_visitFirstMotion() {
    MeasurementValueSpecVisitor<FirstMotionMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<FirstMotionMeasurementValue>> spec = converterVisitor.visit(firstMotionSpec,
      FeatureMeasurementTypes.SHORT_PERIOD_FIRST_MOTION, ARRIVAL_1, Optional.empty());
    assertEquals(SHORT_PERIOD_MEASUREMENT_SPEC, spec.findFirst().orElseThrow());
  }

  @Test
  void test_visitPhase() {
    MeasurementValueSpecVisitor<PhaseTypeMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<PhaseTypeMeasurementValue>> spec = converterVisitor.visit(phaseSpec,
      FeatureMeasurementTypes.PHASE, ARRIVAL_2, Optional.of(ASSOC_DAO_2));
    assertEquals(PHASE_MEASUREMENT_SPEC_2, spec.findFirst().orElseThrow());
  }

  @Test
  void test_visitReceiver() {
    MeasurementValueSpecVisitor<NumericMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<NumericMeasurementValue>> specStream = converterVisitor.visit(receiverSpec,
      FeatureMeasurementTypes.RECEIVER_TO_SOURCE_AZIMUTH, ARRIVAL_2, Optional.empty());
    MeasurementValueSpec<NumericMeasurementValue> spec = specStream.findFirst().orElseThrow();
    assertEquals(RECEIVER_AZIMUTH_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
    assertEquals(RECEIVER_AZIMUTH_MEASUREMENT_SPEC.getFeatureMeasurementType(), spec.getFeatureMeasurementType());
    assertEquals(RECEIVER_AZIMUTH_MEASUREMENT_SPEC.getMeasuredValueExtractor().isPresent(), spec.getMeasuredValueExtractor().isPresent());
    assertEquals(RECEIVER_AZIMUTH_MEASUREMENT_SPEC.getUncertaintyValueExtractor().isPresent(), spec.getUncertaintyValueExtractor().isPresent());
    assertEquals(RECEIVER_AZIMUTH_MEASUREMENT_SPEC.getUnits(), spec.getUnits());
  }

  @Test
  void test_visitRectilinearity() {
    MeasurementValueSpecVisitor<NumericMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<NumericMeasurementValue>> specStream = converterVisitor.visit(rectilinearitySpec,
      FeatureMeasurementTypes.RECTILINEARITY, ARRIVAL_1, Optional.empty());
    MeasurementValueSpec<NumericMeasurementValue> spec = specStream.findFirst().orElseThrow();
    assertEquals(RECTILINEARITY_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
    assertEquals(RECTILINEARITY_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
    assertEquals(RECTILINEARITY_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
    assertEquals(RECTILINEARITY_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
  }

  @Test
  void test_visitSlowness() {
    MeasurementValueSpecVisitor<NumericMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<NumericMeasurementValue>> specStream = converterVisitor.visit(slownessSpec,
      FeatureMeasurementTypes.SLOWNESS, ARRIVAL_1, Optional.empty());
    MeasurementValueSpec<NumericMeasurementValue> spec = specStream.findFirst().orElseThrow();
    assertEquals(SLOWNESS_MEASUREMENT_SPEC.getArrivalDao(), spec.getArrivalDao());
    assertEquals(SLOWNESS_MEASUREMENT_SPEC.getFeatureMeasurementType(), spec.getFeatureMeasurementType());
    assertEquals(SLOWNESS_MEASUREMENT_SPEC.getMeasuredValueExtractor().isPresent(), spec.getMeasuredValueExtractor().isPresent());
    assertEquals(SLOWNESS_MEASUREMENT_SPEC.getUnits(), spec.getUnits());
  }

  @Test
  void test_visitAmplitude() {

    MeasurementValueSpecVisitor<AmplitudeMeasurementValue> converterVisitor = MeasurementValueSpecVisitor.create();
    Stream<MeasurementValueSpec<AmplitudeMeasurementValue>> specStream = converterVisitor.visit(ampSpec,
      FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, ARRIVAL_1, Optional.of(AMPLITUDE_DAO_1));
    MeasurementValueSpec<AmplitudeMeasurementValue> spec = specStream.findFirst().orElseThrow();
    assertEquals(AMPLITUDE_DAO_1, spec.getAmplitudeDao().get());
    assertEquals(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2, spec.getFeatureMeasurementType());
  }
}
package gms.shared.signaldetection.coi.detection;

import com.google.common.collect.ImmutableSet;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.waveform.testfixture.WaveformTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.MEASUREMENT_LIST;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.PHASE_FEATURE_MEASUREMENT;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SignalDetectionHypothesis} factory creation
 */
class SignalDetectionHypothesisTests {

  private static final UUID id = UUID.randomUUID();
  private static final UUID id2 = UUID.randomUUID();
  private static final UUID id4 = UUID.randomUUID();
  private static final UUID id5 = UUID.randomUUID();

  private static final boolean NOT_REJECTED = false;
  private static final FeatureMeasurement<ArrivalTimeMeasurementValue> arrivalMeasurement =
    ARRIVAL_TIME_FEATURE_MEASUREMENT;
  private static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement =
    PHASE_FEATURE_MEASUREMENT;
  private static final List<FeatureMeasurement<?>> featureMeasurements = MEASUREMENT_LIST;

  private static final String monitoringOrganization = "CTBTO";

  private SignalDetectionHypothesis signalDetectionHypothesis;

  @BeforeEach
  void createSignalDetectionHypothesis() {
    signalDetectionHypothesis = SignalDetectionHypothesis.from(
      SignalDetectionHypothesisId.from(id, id5),
      Optional.of(
        SignalDetectionHypothesis.Data.builder()
          .setMonitoringOrganization(monitoringOrganization)
          .setStation(UtilsTestFixtures.STATION)
          .setRejected(false)
          .setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurements))
          .build()));
  }

  @Test
  void testSerialization() throws Exception {
    final SignalDetectionHypothesis hyp = SignalDetectionHypothesis.from(
      SignalDetectionHypothesisId.from(id, UUID.randomUUID()),
      Optional.of(SignalDetectionHypothesis.Data.builder()
        .setMonitoringOrganization(monitoringOrganization)
        .setStation(UtilsTestFixtures.STATION)
        .setParentSignalDetectionHypothesis(
          Optional.of(SignalDetectionHypothesis.createEntityReference(id, UUID.randomUUID())))
        .setRejected(NOT_REJECTED)
        .setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurements))
        .build()));
    TestUtilities.assertSerializes(hyp, SignalDetectionHypothesis.class);
  }

  @Test
  void testFrom() {
    final SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
      SignalDetectionHypothesisId.from(id2, id),
      Optional.of(SignalDetectionHypothesis.Data.builder()
        .setMonitoringOrganization(monitoringOrganization)
        .setStation(UtilsTestFixtures.STATION)
        .setParentSignalDetectionHypothesis(Optional.of(
          SignalDetectionHypothesis.create(SignalDetectionHypothesisId.from(id, UUID.randomUUID()), Optional.empty())))
        .setRejected(NOT_REJECTED)
        .setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurements))
        .build()));
    assertEquals(id2, signalDetectionHypothesis.getId().getSignalDetectionId());
    assertEquals(id, signalDetectionHypothesis.getId().getId());
    assertEquals(NOT_REJECTED, signalDetectionHypothesis.getData().orElseThrow().isRejected());
    assertArrayEquals(featureMeasurements.toArray(),
      signalDetectionHypothesis.getData().get().getFeatureMeasurements().toArray());
  }

  @Test
  void testNoArrivalTimeFeatureMeasurement() {
    List<FeatureMeasurement<?>> featureMeasurementList = List.of(phaseMeasurement);
    SignalDetectionHypothesis.Data.Builder dataBuilder = SignalDetectionHypothesis.Data.builder()
      .setMonitoringOrganization(monitoringOrganization)
      .setStation(UtilsTestFixtures.STATION)
      .setParentSignalDetectionHypothesis(Optional.of(SignalDetectionHypothesis.createEntityReference(id, id4)))
      .setRejected(NOT_REJECTED)
      .setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurementList));
    assertThrows(IllegalArgumentException.class,
      dataBuilder::build);
  }

  @Test
  void testNoPhaseFeatureMeasurement() {
    List<FeatureMeasurement<?>> featureMeasurementList = List.of(arrivalMeasurement);
    SignalDetectionHypothesis.Data.Builder dataBuilder = SignalDetectionHypothesis.Data.builder()
      .setMonitoringOrganization(monitoringOrganization)
      .setStation(UtilsTestFixtures.STATION)
      .setParentSignalDetectionHypothesis(Optional.of(SignalDetectionHypothesis.createEntityReference(id, id4)))
      .setRejected(NOT_REJECTED)
      .setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurementList));
    assertThrows(IllegalArgumentException.class,
      dataBuilder::build);
  }

  @Test
  void testGetFeatureMeasurementByType() {
    Optional<FeatureMeasurement<ArrivalTimeMeasurementValue>> arrivalTime =
      signalDetectionHypothesis.getData().orElseThrow().getFeatureMeasurement(
        FeatureMeasurementTypes.ARRIVAL_TIME);
    assertNotNull(arrivalTime);
    assertTrue(arrivalTime.isPresent());
    assertEquals(arrivalMeasurement, arrivalTime.get());
    // get phase measurement
    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> phase =
      signalDetectionHypothesis.getData().orElseThrow().getFeatureMeasurement(
        FeatureMeasurementTypes.PHASE);
    assertNotNull(phase);
    assertTrue(phase.isPresent());
    assertEquals(phaseMeasurement, phase.get());
    // get non-existent measurement
    Optional<FeatureMeasurement<NumericMeasurementValue>> emergenceAngle =
      signalDetectionHypothesis.getData().orElseThrow().getFeatureMeasurement(
        FeatureMeasurementTypes.EMERGENCE_ANGLE);
    assertEquals(Optional.empty(), emergenceAngle);
  }

  @Test
  void testWithMeasurementsWrongChannelBuild() {

    FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement = FeatureMeasurement.from(
      UtilsTestFixtures.CHANNEL_TWO_FACET,
      WaveformTestFixtures.singleStationEpochStart100RandomSamples(),
      FeatureMeasurementTypes.PHASE,
      PhaseTypeMeasurementValue.fromFeaturePrediction(PhaseType.P, Optional.of(1.0)),
      Optional.of(DoubleValue.from(1.0, Optional.empty(), Units.DECIBELS)));

    List<FeatureMeasurement<?>> measurements = List.of(arrivalMeasurement, phaseMeasurement);

    SignalDetectionHypothesis.Data.Builder data = SignalDetectionHypothesis.Data.builder()
      .setMonitoringOrganization(monitoringOrganization)
      .setStation(UtilsTestFixtures.STATION)
      .setRejected(NOT_REJECTED)
      .setFeatureMeasurements(ImmutableSet.copyOf(measurements));

    assertThrows(IllegalStateException.class, data::build);
  }

  @Test
  void testCreateFacetedSignalDetectionHypothesis() {
    assertDoesNotThrow(() -> SignalDetectionHypothesis.createEntityReference(id, id2));

    SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.createEntityReference(id, id2);
    assertEquals(Optional.empty(), signalDetectionHypothesis.getData());
    assertEquals(id, signalDetectionHypothesis.getId().getSignalDetectionId());
    assertEquals(id2, signalDetectionHypothesis.getId().getId());
  }

  @Test
  void testToFacetedSignalDetection() {
    assertDoesNotThrow(SIGNAL_DETECTION_HYPOTHESIS::toEntityReference);

    SignalDetectionHypothesis signalDetectionHypothesis = SIGNAL_DETECTION_HYPOTHESIS.toEntityReference();
    assertEquals(Optional.empty(), signalDetectionHypothesis.getData());
    assertEquals(SIGNAL_DETECTION_HYPOTHESIS.getId().getSignalDetectionId(),
      signalDetectionHypothesis.getId().getSignalDetectionId());
    assertEquals(SIGNAL_DETECTION_HYPOTHESIS.getId().getId(), signalDetectionHypothesis.getId().getId());
  }
}

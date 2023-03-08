package gms.shared.frameworks.osd.coi.signaldetection;

import com.google.common.collect.ImmutableMap;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.channel.ChannelProcessingMetadataType;
import gms.shared.frameworks.osd.coi.channel.Orientation;
import gms.shared.frameworks.osd.coi.event.EventTestFixtures;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SignalDetectionHypothesis} factory creation
 */
class SignalDetectionHypothesisTests {

  private static final UUID id = UUID.randomUUID();
  private static final UUID id1 = UUID.randomUUID();
  private static final UUID id2 = UUID.randomUUID();
  private static final UUID id3 = UUID.randomUUID();
  private static final boolean rejected = false;
  private static final FeatureMeasurement<InstantValue> arrivalMeasurement =
    EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT;
  private static final FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement =
    EventTestFixtures.PHASE_FEATURE_MEASUREMENT;
  private static final List<FeatureMeasurement<?>> featureMeasurements = List
    .of(arrivalMeasurement, phaseMeasurement);

  private static final String monitoringOrganization = "CTBTO";
  private static final String stationName = UtilsTestFixtures.STATION.getName();

  private SignalDetection signalDetection;

  private SignalDetectionHypothesis signalDetectionHypothesis;

  @BeforeEach
  public void createSignalDetectionHypothesis() {
    signalDetection = SignalDetection
      .from(id, monitoringOrganization, stationName,
        Collections.emptyList());
    signalDetection.addSignalDetectionHypothesis(featureMeasurements);
    signalDetectionHypothesis = signalDetection.getSignalDetectionHypotheses().get(0);
  }

  @Test
  void testSerialization() throws Exception {
    final SignalDetectionHypothesis hyp = SignalDetectionHypothesis.from(
      id, UUID.randomUUID(), monitoringOrganization, stationName, UUID.randomUUID(), rejected,
      featureMeasurements);
    TestUtilities.testSerialization(hyp, SignalDetectionHypothesis.class);
  }

  @Test
  void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullableArguments(SignalDetectionHypothesis.class,
      "from",
      List.of(4),
      id, signalDetection.getId(), monitoringOrganization, stationName, UUID.randomUUID(),
      rejected,
      featureMeasurements);
  }

  @Test
  void testFrom() {
    final SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
      id, signalDetection.getId(), monitoringOrganization, stationName, id2,
      rejected, featureMeasurements);
    assertEquals(signalDetection.getId(), signalDetectionHypothesis.getParentSignalDetectionId());
    assertEquals(id, signalDetectionHypothesis.getId());
    assertEquals(rejected, signalDetectionHypothesis.isRejected());
    assertArrayEquals(featureMeasurements.toArray(),
      signalDetectionHypothesis.getFeatureMeasurements().toArray());
  }

  @Test
  void testNoArrivalTimeFeatureMeasurement() {
    List<FeatureMeasurement<?>> phaseMeasurementList = List.of(phaseMeasurement);
    assertThrows(IllegalArgumentException.class,
      () -> SignalDetectionHypothesis.from(id1, id2,
        monitoringOrganization, stationName, id3,
        rejected, phaseMeasurementList));
  }

  @Test
  void testNoPhaseFeatureMeasurement() {
    List<FeatureMeasurement<?>> arrivalMeasurementList = List.of(arrivalMeasurement);
    assertThrows(IllegalArgumentException.class,
      () -> SignalDetectionHypothesis.from(id, id2,
        monitoringOrganization, stationName, id3,
        rejected, arrivalMeasurementList));
  }

  @Test
  void testDuplicateFeatureMeasurementTypes() {
    List<FeatureMeasurement<?>> mixedList = List.of(arrivalMeasurement, phaseMeasurement, phaseMeasurement);
    assertThrows(IllegalArgumentException.class,
      () -> SignalDetectionHypothesis.from(id, id1,
        monitoringOrganization, stationName, id2, rejected,
        mixedList));
  }

  @Test
  void testGetFeatureMeasurementByType() {
    Optional<FeatureMeasurement<InstantValue>> arrivalTime =
      signalDetectionHypothesis.getFeatureMeasurement(
        FeatureMeasurementTypes.ARRIVAL_TIME);
    assertNotNull(arrivalTime);
    assertTrue(arrivalTime.isPresent());
    assertEquals(arrivalMeasurement, arrivalTime.get());
    // get phase measurement
    Optional<FeatureMeasurement<PhaseTypeMeasurementValue>> phase =
      signalDetectionHypothesis.getFeatureMeasurement(
        FeatureMeasurementTypes.PHASE);
    assertNotNull(phase);
    assertTrue(phase.isPresent());
    assertEquals(phaseMeasurement, phase.get());
    // get non-existent measurement
    Optional<FeatureMeasurement<NumericMeasurementValue>> emergenceAngle =
      signalDetectionHypothesis.getFeatureMeasurement(
        FeatureMeasurementTypes.EMERGENCE_ANGLE);
    assertEquals(Optional.empty(), emergenceAngle);
  }

  @Test
  void testGetFeatureMeasurementByTypeNull() {
    assertThrows(NullPointerException.class,
      () -> signalDetectionHypothesis.getFeatureMeasurement(null));
  }

  @Test
  void testWithMeasurementsBuilder() {
    SignalDetectionHypothesis actual = signalDetectionHypothesis.withMeasurements(List.of(
      FeatureMeasurementTypes.ARRIVAL_TIME)).build();
    assertTrue(actual.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).isPresent());
    assertFalse(actual.getFeatureMeasurement(FeatureMeasurementTypes.PHASE).isPresent());
  }

  @Test
  void testWithoutMeasurementsBuilder() {
    SignalDetectionHypothesis actual = signalDetectionHypothesis.withoutMeasurements(List.of(
      FeatureMeasurementTypes.ARRIVAL_TIME)).build();
    assertFalse(actual.getFeatureMeasurement(FeatureMeasurementTypes.ARRIVAL_TIME).isPresent());
    assertTrue(actual.getFeatureMeasurement(FeatureMeasurementTypes.PHASE).isPresent());
  }

  @Test
  void testBuilderSetMeasurementsThenAddMeasurement() {
    assertDoesNotThrow(() -> {
      SignalDetectionHypothesis.builder(id,
          id1, monitoringOrganization, stationName, id2, false)
        .setFeatureMeasurementsByType(ImmutableMap.of(FeatureMeasurementTypes.ARRIVAL_TIME,
          EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT))
        .addMeasurement(EventTestFixtures.PHASE_FEATURE_MEASUREMENT)
        .build();
    });
  }

  @Test
  void testWithMeasurementsWrongChannelBuild() {
    Channel channel = Channel.from(
      "Real Channel Name", "Canonical Name",
      "Example description",
      "Bad Station",
      ChannelDataType.DIAGNOSTIC_SOH,
      ChannelBandType.BROADBAND,
      ChannelInstrumentType.HIGH_GAIN_SEISMOMETER,
      ChannelOrientationType.VERTICAL,
      'Z',
      Units.COUNTS_PER_NANOMETER,
      65.0,
      Location.from(35.0,
        -125.0,
        100.0,
        5500.0),
      Orientation.from(
        65.0,
        135.0
      ),
      List.of("inputChan"),
      Map.of(),
      Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, "GROUP"));

    FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement = FeatureMeasurement.from(
      channel,
      MeasuredChannelSegmentDescriptor.builder()
        .setChannelName(channel.getName())
        .setMeasuredChannelSegmentStartTime(Instant.EPOCH)
        .setMeasuredChannelSegmentEndTime(Instant.EPOCH.plusSeconds(60))
        .setMeasuredChannelSegmentCreationTime(Instant.now())
        .build(),
      FeatureMeasurementTypes.PHASE,
      PhaseTypeMeasurementValue.from(PhaseType.P, 1.0));

    SignalDetectionHypothesis.Builder builder = SignalDetectionHypothesis.builder(id,
        signalDetection.getId(),
        monitoringOrganization,
        stationName,
        null,
        false)
      .addMeasurement(arrivalMeasurement)
      .addMeasurement(phaseMeasurement);

    assertThrows(IllegalStateException.class, () -> builder.build());
  }
}

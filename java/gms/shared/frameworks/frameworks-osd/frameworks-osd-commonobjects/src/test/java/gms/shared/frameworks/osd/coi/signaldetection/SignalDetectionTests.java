package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.event.EventTestFixtures;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SignalDetection} factory creation
 */
class SignalDetectionTests {

  private final UUID id = UUID.randomUUID();
  private final UUID id1 = UUID.randomUUID();
  private String monitoringOrganization = "CTBTO";
  private String stationName = UtilsTestFixtures.STATION.getName();

  private List<FeatureMeasurement<?>> featureMeasurements =
    List.of(EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT, EventTestFixtures.PHASE_FEATURE_MEASUREMENT);

  @Test
  void testSerialization() throws Exception {
    final SignalDetection signalDetection = SignalDetection.from(
      id, "CTBTO", stationName, Collections.emptyList());
    signalDetection.addSignalDetectionHypothesis(featureMeasurements);
    TestUtilities.testSerialization(signalDetection, SignalDetection.class);
  }

  @Test
  void testFromNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetection.class, "from",
      id, monitoringOrganization, stationName, Collections.emptyList());
  }

  @Test
  void testCreateNullParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(SignalDetection.class, "create",
      monitoringOrganization, stationName, featureMeasurements);
  }

  @Test
  void testFrom() {
    SignalDetection signalDetection = SignalDetection.from(
      id, monitoringOrganization, stationName, Collections.emptyList());

    signalDetection.addSignalDetectionHypothesis(featureMeasurements);

    assertEquals(id, signalDetection.getId());
    assertEquals(monitoringOrganization, signalDetection.getMonitoringOrganization());
    assertEquals(stationName, signalDetection.getStationName());
    assertEquals(1, signalDetection.getSignalDetectionHypotheses().size());
  }

  @Test
  void testCreate() {
    SignalDetection signalDetection = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);

    assertEquals(monitoringOrganization, signalDetection.getMonitoringOrganization());
    assertEquals(stationName, signalDetection.getStationName());
    assertEquals(1, signalDetection.getSignalDetectionHypotheses().size());
    assertArrayEquals(featureMeasurements.toArray(),
      signalDetection.getSignalDetectionHypotheses().get(0).getFeatureMeasurements().toArray());
  }

  @Test
  void testRejectNullId() {
    SignalDetection signalDetection = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);
    assertThrows(NullPointerException.class, () -> signalDetection.reject(null)
    );
  }

  @Test
  void testRejectInvalidId() {
    SignalDetection signalDetection = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);
    assertThrows(IllegalArgumentException.class, () -> {
      signalDetection.reject(id1);
    });
  }

  @Test
  void testReject() {
    SignalDetection signalDetection = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);
    SignalDetectionHypothesis signalDetectionHypothesis = signalDetection
      .getSignalDetectionHypotheses().get(0);

    signalDetection.reject(signalDetectionHypothesis.getId());

    assertEquals(2, signalDetection.getSignalDetectionHypotheses().size());
    assertEquals(true, signalDetection.getSignalDetectionHypotheses().get(1).isRejected());
  }

  @Test
  void testOnlyContainsRejected() {
    SignalDetectionHypothesis signalDetectionHypothesis = SignalDetectionHypothesis.from(
      id, id, monitoringOrganization, stationName, UUID.randomUUID(), true, featureMeasurements);
    List<SignalDetectionHypothesis> signalDetectionHypothesisList = Arrays.asList(signalDetectionHypothesis);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
      SignalDetection.from(id, monitoringOrganization, stationName, signalDetectionHypothesisList)
    );
    assertTrue(ex.getMessage().contains("Cannot create a SignalDetection containing only rejected SignalDetectionHypotheses"));
  }

  @Test
  void testEqualsHashCode() {

    final SignalDetection sd1 = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);

    final SignalDetection sd2 = SignalDetection.from(
      sd1.getId(), sd1.getMonitoringOrganization(), sd1.getStationName(), sd1.getSignalDetectionHypotheses());

    assertEquals(sd1, sd2);
    assertEquals(sd2, sd1);
    assertEquals(sd1.hashCode(), sd2.hashCode());
  }

  @Test
  void testEqualsExpectInequality() {

    final SignalDetection sd1 = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);
    SignalDetection sd2 = SignalDetection.create(
      monitoringOrganization, stationName, featureMeasurements);

    // Different id
    assertNotEquals(sd1, sd2);

    // Different monitoring org
    sd2 = SignalDetection.from(sd1.getId(), "diffMonitoringOrg",
      sd1.getStationName(), sd1.getSignalDetectionHypotheses());
    assertNotEquals(sd1, sd2);

    // Different station id
    sd2 = SignalDetection.from(sd1.getId(), sd1.getMonitoringOrganization(),
      "test 3", sd1.getSignalDetectionHypotheses());
    assertNotEquals(sd1, sd2);

    // Different signal hypotheses
    sd2 = SignalDetection.from(sd1.getId(), sd1.getMonitoringOrganization(),
      sd1.getStationName(), Collections.emptyList());
    assertNotEquals(sd1, sd2);
  }

  @Test
  void testAddHypothesisParentNotFound() {
    SignalDetection signalDetection = SignalDetection.from(
      id, monitoringOrganization, stationName, Collections.emptyList());

    signalDetection.addSignalDetectionHypothesis(featureMeasurements);
    assertThrows(IllegalStateException.class,
      () -> signalDetection.addSignalDetectionHypothesis(id1, featureMeasurements));
  }

  @Test
  void testFromMulitpleNoParentHypotheses() {
    List<SignalDetectionHypothesis> hypotheses = List.of(SignalDetectionHypothesis.from(
        UUID.randomUUID(),
        id,
        monitoringOrganization,
        stationName,
        null,
        false,
        featureMeasurements),
      SignalDetectionHypothesis.from(UUID.randomUUID(),
        id,
        monitoringOrganization,
        stationName,
        null,
        false,
        featureMeasurements));

    assertThrows(IllegalStateException.class, () -> SignalDetection.from(id, monitoringOrganization, stationName, hypotheses));

  }

  @Test
  void testFromParentNotFound() {
    List<SignalDetectionHypothesis> hypotheses = List.of(SignalDetectionHypothesis.from(
        UUID.randomUUID(),
        id,
        monitoringOrganization,
        stationName,
        null,
        false,
        featureMeasurements),
      SignalDetectionHypothesis.from(UUID.randomUUID(),
        id,
        monitoringOrganization,
        stationName,
        UUID.randomUUID(),
        false,
        featureMeasurements));

    assertThrows(IllegalStateException.class, () -> SignalDetection.from(id, monitoringOrganization, stationName, hypotheses));
  }

  @Test
  void testFromParentAfterChild() {
    UUID firstId = UUID.randomUUID();
    UUID secondId = UUID.randomUUID();
    UUID thirdId = UUID.randomUUID();
    List<SignalDetectionHypothesis> hypotheses = List.of(SignalDetectionHypothesis.from(
        firstId,
        id,
        monitoringOrganization,
        stationName,
        null,
        false,
        featureMeasurements),
      SignalDetectionHypothesis.from(secondId,
        id,
        monitoringOrganization,
        stationName,
        thirdId,
        false,
        featureMeasurements),
      SignalDetectionHypothesis.from(thirdId,
        id,
        monitoringOrganization,
        stationName,
        firstId,
        false,
        featureMeasurements
      ));

    assertThrows(IllegalStateException.class, () -> SignalDetection.from(id, monitoringOrganization, stationName, hypotheses));
  }
}

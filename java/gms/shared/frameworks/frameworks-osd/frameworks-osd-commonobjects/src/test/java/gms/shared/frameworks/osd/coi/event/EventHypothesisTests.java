package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventHypothesisTests {

  private UUID id;
  private UUID eventId;
  private UUID parentEventHypothesis;
  private Set<UUID> parentEventHypotheses;
  private boolean isRejected;
  private LocationSolution locationSolution;
  private Set<LocationSolution> locationSoutions;
  private PreferredLocationSolution preferredLocationSolution;
  private SignalDetectionEventAssociation signalDetectionEventAssociation;
  private Set<SignalDetectionEventAssociation> signalDetectionEventAssociations;
  private EventHypothesis eventHypothesis;

  @BeforeEach
  public void setup() {
    id = UUID.fromString("407c377a-b6a4-478f-b3cd-5c934ee6b876");
    eventId = UUID.fromString("5432a77a-b6a4-478f-b3cd-5c934ee6b000");
    parentEventHypothesis = UUID
      .fromString("cccaa77a-b6a4-478f-b3cd-5c934ee6b999");
    parentEventHypotheses = Set.of(parentEventHypothesis);
    isRejected = false;
    locationSolution = EventTestFixtures.LOCATION_SOLUTION;
    locationSoutions = Set.of(locationSolution);
    preferredLocationSolution = PreferredLocationSolution
      .from(locationSolution);
    signalDetectionEventAssociation = SignalDetectionEventAssociation.create(id, UUID.randomUUID());
    signalDetectionEventAssociations = Set
      .of(signalDetectionEventAssociation);

    eventHypothesis = EventHypothesis.from(id, eventId,
      parentEventHypotheses, isRejected, locationSoutions, preferredLocationSolution,
      signalDetectionEventAssociations);
  }

  @Test
  void testSerialization() throws IOException {
    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    assertEquals(eventHypothesis,
      jsonObjectMapper.readValue(jsonObjectMapper.writeValueAsString(eventHypothesis),
        EventHypothesis.class));
  }

  @Test
  void testFrom() {
    final EventHypothesis evtHyp = EventHypothesis.from(id, eventId,
      parentEventHypotheses, isRejected, locationSoutions, preferredLocationSolution,
      signalDetectionEventAssociations);
    assertNotNull(evtHyp);
    assertEquals(eventId, evtHyp.getEventId());
    assertEquals(Set.of(parentEventHypothesis), evtHyp.getParentEventHypotheses());
    assertEquals(isRejected, evtHyp.isRejected());
    assertEquals(Set.of(locationSolution), evtHyp.getLocationSolutions());
    assertEquals(Optional.of(preferredLocationSolution), evtHyp.getPreferredLocationSolution());
    assertEquals(Set.of(signalDetectionEventAssociation), evtHyp.getAssociations());
  }

  /**
   * Tests that if isRejected is True, then locationSolutions should be empty. If not empty, throws
   * an IllegalArgumentException.
   */
  @Test
  void testIsRejectedTrueAndLocationSolutionsNotEmpty() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> EventHypothesis.from(id, eventId, parentEventHypotheses, true, locationSoutions, null,
        signalDetectionEventAssociations));
    assertTrue(exception.getMessage()
      .contains("Expected locationSolutions to be empty when isRejected=true"));
  }

  /**
   * Tests that if isRejected is True, then preferredLocationSolution should be null. If not null,
   * throws an IllegalArgumentException.
   */
  @Test
  void testIsRejectedTrueAndPreferredLocationSolutionNotNull() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> EventHypothesis
        .from(id, eventId, parentEventHypotheses, true, Collections.EMPTY_SET, preferredLocationSolution,
          signalDetectionEventAssociations));
    assertTrue(exception.getMessage()
      .contains("Expected preferredLocationSolution to be empty when isRejected=true"));
  }

  /**
   * Tests that if isRejected is False, then preferredLocationSolution should not be null. If null,
   * throws a NullPointerException.
   */
  @Test
  void testIsRejectedFalseAndPreferredLocationSolutionNull() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> EventHypothesis
        .from(id, eventId, parentEventHypotheses, false, locationSoutions, null,
          signalDetectionEventAssociations));
    assertTrue(exception.getMessage().contains(
      "Expected non-empty preferredLocationSolution when EventHypothesis is not rejected"));
  }

  /**
   * Tests that if isRejected is False, then locationSolutions should contain a
   * preferredLocationSolution. If not, throws an IllegalArgumentException.
   */
  @Test
  void testIsRejectedFalseAndLocationSolutionHasPreferredLocationSolution() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> EventHypothesis
        .from(id, eventId, parentEventHypotheses, false, Collections.EMPTY_SET, preferredLocationSolution,
          signalDetectionEventAssociations));
    assertTrue(exception.getMessage()
      .contains("Expected locationSolutions to contain preferredLocationSolution"));
  }

  @Test
  void testReturnedParentEventHypothesisImmutable() {
    Set<UUID> eventHypothesis1 = eventHypothesis.getParentEventHypotheses();
    UUID uuid = UUID.randomUUID();
    assertThrows(UnsupportedOperationException.class,
      () -> eventHypothesis1.add(uuid));
  }

  @Test
  void testReturnedLocationSolutionsImmutable() {
    Set<LocationSolution> locationSet = eventHypothesis.getLocationSolutions();
    assertThrows(UnsupportedOperationException.class,
      () -> locationSet.add(locationSolution));
  }
}

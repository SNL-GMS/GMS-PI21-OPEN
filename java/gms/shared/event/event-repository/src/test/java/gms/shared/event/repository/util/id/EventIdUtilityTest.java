package gms.shared.event.repository.util.id;

import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.ignite.IgniteCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static gms.shared.event.repository.util.id.EventIdUtility.EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID;
import static gms.shared.event.repository.util.id.EventIdUtility.EVENT_ID_EVENT_RECORD_ID;
import static gms.shared.event.repository.util.id.EventIdUtility.EVENT_RECORD_ID_EVENT_ID;
import static gms.shared.event.repository.util.id.EventIdUtility.ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class EventIdUtilityTest {

  private static EventIdUtility eventIdUtility;

  @Mock
  EventBridgeDefinition mockEventBridgeDefinition;

  @Mock
  IgniteCache<UUID, Long> eventIdtoEvid;
  @Mock
  IgniteCache<Long, UUID> evidToEventId;
  @Mock
  IgniteCache<UUID, OriginUniqueIdentifier> eventHypothesisIdToOriginUniqueIdentifier;
  @Mock
  IgniteCache<OriginUniqueIdentifier, UUID> originUniqueIdentifierToEventHypothesisId;

  @BeforeEach
  void setUp() {
    eventIdUtility = new EventIdUtility(mockEventBridgeDefinition, eventIdtoEvid, evidToEventId, eventHypothesisIdToOriginUniqueIdentifier,
      originUniqueIdentifierToEventHypothesisId);
  }

  @Test
  void testCreate() {
    try (MockedStatic<IgniteConnectionManager> managerMockedStatic = mockStatic(IgniteConnectionManager.class)) {

      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(EVENT_ID_EVENT_RECORD_ID))
        .thenReturn(eventIdtoEvid);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(EVENT_RECORD_ID_EVENT_ID))
        .thenReturn(evidToEventId);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID))
        .thenReturn(eventHypothesisIdToOriginUniqueIdentifier);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID))
        .thenReturn(originUniqueIdentifierToEventHypothesisId);

      assertNotNull(new EventIdUtility(mockEventBridgeDefinition));
    }
  }

  @Test
  void getOrCreateEventId() {
    var evid = 1L;

    given(evidToEventId.get(evid)).willReturn(null);
    var eventId = eventIdUtility.getOrCreateEventId(evid);
    assertNotNull(eventId);

    given(evidToEventId.get(evid)).willReturn(eventId);
    assertTrue(eventIdUtility.getEventId(evid).isPresent());
    assertTrue(eventIdUtility.getEventId(evid).isPresent());
  }

  @Test
  void testGetOrCreateEventHypothesisId() {
    var orid = 1L;
    var stage = "stage";
    var originUniqueIdentifier = OriginUniqueIdentifier.create(orid, stage);

    given(originUniqueIdentifierToEventHypothesisId.get(originUniqueIdentifier)).willReturn(null);
    var eventHypothesisId = eventIdUtility.getOrCreateEventHypothesisId(originUniqueIdentifier);
    assertNotNull(eventHypothesisId);

    given(originUniqueIdentifierToEventHypothesisId.get(originUniqueIdentifier)).willReturn(eventHypothesisId);
    given(eventHypothesisIdToOriginUniqueIdentifier.get(eventHypothesisId)).willReturn(originUniqueIdentifier);
    assertEquals(eventHypothesisId, eventIdUtility.getOrCreateEventHypothesisId(originUniqueIdentifier));
    assertTrue(eventIdUtility.getEventHypothesisId(originUniqueIdentifier).isPresent());
    assertTrue(eventIdUtility.getOriginUniqueIdentifier(eventHypothesisId).isPresent());
  }

  @Test
  void testGetEventHypothesisId() {
    var orid = 1L;
    var stage = "stage";
    assertThat(eventIdUtility.getEventHypothesisId(orid, stage)).isEmpty();

    given(originUniqueIdentifierToEventHypothesisId.get(OriginUniqueIdentifier.create(orid, stage)))
      .willReturn(null);
    var eventHypothesisId = eventIdUtility.getOrCreateEventHypothesisId(orid, stage);
    given(originUniqueIdentifierToEventHypothesisId.get(OriginUniqueIdentifier.create(orid, stage)))
      .willReturn(eventHypothesisId);
    assertThat(eventIdUtility.getEventHypothesisId(orid, stage)).containsSame(eventHypothesisId);
  }

  @Test
  void testGenerateEventHypothesisIdNullOriginDao() {
    assertThrows(NullPointerException.class, () -> eventIdUtility.getOrCreateEventHypothesisId(null));
  }

  @Test
  void testAddEventIds() {
    var eventId = UUID.randomUUID();
    var evid = 1L;

    assertDoesNotThrow(() -> eventIdUtility.addEventIdToEvid(eventId, evid));
  }

  @Test
  void testRetrieveMissingEventIds() {
    var eventIdOpt = eventIdUtility.getEventId(-10);
    var evidOpt = eventIdUtility.getEvid(UUID.randomUUID());

    assertTrue(eventIdOpt.isEmpty());
    assertTrue(evidOpt.isEmpty());
  }

  @Test
  void testRetrieveEventIdsNullInput() {
    assertThrows(NullPointerException.class, () -> eventIdUtility.getEvid(null));
  }

  @Test
  void testAddEventHypothesisIds() {
    var orid = 1L;
    var stage = "stage";
    var originUniqueIdentifier = OriginUniqueIdentifier.create(orid, stage);

    var eventHypothesisId = UUID.randomUUID();

    assertDoesNotThrow(() -> eventIdUtility
      .addEventHypothesisIdToOriginUniqueIdentifier(eventHypothesisId, originUniqueIdentifier));
  }

  @Test
  void testRetrieveMissingEventHypothesisIds() {
    var originUniqueIdentifier = OriginUniqueIdentifier.create(1L, "stage");

    var eventHypothesisIdOpt = eventIdUtility.getEventHypothesisId(originUniqueIdentifier);
    var originUniqueIdentifierOpt = eventIdUtility.getOriginUniqueIdentifier(UUID.randomUUID());

    assertTrue(eventHypothesisIdOpt.isEmpty());
    assertTrue(originUniqueIdentifierOpt.isEmpty());
  }

  @Test
  void testRetrieveEventHypothesisIdsNullInput() {
    assertThrows(NullPointerException.class, () -> eventIdUtility.getEventHypothesisId(null));
    assertThrows(NullPointerException.class, () -> eventIdUtility.getOriginUniqueIdentifier(null));
  }

  @Test
  void testOriginUniqueIdentifierToUUID() {
    var stage = "al1";
    var originUniqueIdentifier = OriginUniqueIdentifier.create(1L, stage);
    var dbUrlMap = Map.of(WorkflowDefinitionId.from(stage), "test");

    given(mockEventBridgeDefinition.getDatabaseUrlByStage()).willReturn(dbUrlMap);
    var expectedSeed = String.valueOf(1L) + "test";
    var expectedUUID = UUID.nameUUIDFromBytes(expectedSeed.getBytes(StandardCharsets.UTF_8));

    assertEquals(expectedUUID, eventIdUtility.originUniqueIdentifierToUUID(originUniqueIdentifier));
  }

}

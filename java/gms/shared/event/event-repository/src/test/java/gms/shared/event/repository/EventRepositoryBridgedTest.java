package gms.shared.event.repository;

import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.MagnitudeType;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.GaTagDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.event.repository.connector.ArInfoDatabaseConnector;
import gms.shared.event.repository.connector.AssocDatabaseConnector;
import gms.shared.event.repository.connector.EventBridgeDatabaseConnectors;
import gms.shared.event.repository.connector.EventControlDatabaseConnector;
import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.GaTagDatabaseConnector;
import gms.shared.event.repository.connector.NetMagDatabaseConnector;
import gms.shared.event.repository.connector.OriginDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.StaMagDatabaseConnector;
import gms.shared.event.repository.converter.EventConverter;
import gms.shared.event.repository.util.id.EventIdUtility;
import gms.shared.event.repository.util.id.OriginUniqueIdentifier;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisAssocIdComponents;
import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.AR_INFO_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.ASSOC_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.EVENT_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.EVENT_CONTROL_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.GA_TAG_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.NETMAG_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.ORIGERR_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.ORIGIN_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.STAMAG_CONNECTOR_TYPE;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventRepositoryBridgedTest {

  //  public static final String GA_TAG_OBJECT_TYPE_REJECTED = "o";
  public static final String GA_TAG_ANALYST_REJECTED = "analyst_rejected";

  public static final long TRUTH_EVID = 31415926535L;
  public static final UUID EVENT_UUID = UUID.randomUUID();

  @Mock
  EventDatabaseConnector eventDatabaseConnector;

  @Mock
  EventControlDatabaseConnector eventControlDatabaseConnector;

  @Mock
  OriginDatabaseConnector originDatabaseConnector;

  @Mock
  OriginErrDatabaseConnector originErrDatabaseConnector;

  @Mock
  AssocDatabaseConnector assocDatabaseConnector;

  @Mock
  GaTagDatabaseConnector gaTagDatabaseConnector;

  @Mock
  NetMagDatabaseConnector netMagDatabaseConnector;

  @Mock
  StaMagDatabaseConnector staMagDatabaseConnector;

  @Mock
  EventIdUtility eventIdUtility;

  @Mock
  EventConverter eventConverter;

  @Mock
  EventBridgeDatabaseConnectors eventBridgeDatabaseConnectors;

  @Mock
  SignalDetectionLegacyAccessor signalDetectionLegacyAccessor;

  @Mock
  ArInfoDatabaseConnector arInfoDatabaseConnector;

  EventRepositoryBridged eventRepositoryBridged;

  WorkflowDefinitionId stageOneId;
  WorkflowDefinitionId stageTwoId;
  WorkflowDefinitionId stageThreeId;
  String stageOneName;
  String stageTwoName;
  String stageThreeName;

  @BeforeEach
  void init() {

    stageOneId = WorkflowDefinitionId.from("STAGE_ONE");
    stageOneName = stageOneId.getName();
    var stageOneAccount = "stage_one_account";
    stageTwoId = WorkflowDefinitionId.from("STAGE_TWO");
    stageTwoName = stageTwoId.getName();
    var stageTwoAccount = "stage_two_account";
    stageThreeId = WorkflowDefinitionId.from("STAGE_Three");
    stageThreeName = stageThreeId.getName();
    var stageThreeAccount = "stage_three_account";

    var eventBridgeDefiniton = EventBridgeDefinition.builder()
      .setMonitoringOrganization("MonitoringOrganization")
      .setOrderedStages(List.of(stageOneId, stageTwoId, stageThreeId))
      .setDatabaseUrlByStage(
        Map.of(
          stageOneId, stageOneAccount,
          stageTwoId, stageTwoAccount,
          stageThreeId, stageThreeAccount
        )
      )
      .setPreviousDatabaseUrlByStage(
        Map.of(
          stageTwoId, stageOneAccount,
          stageThreeId, stageTwoAccount
        )
      )
      .build();

    var eventStages = new EventStages(eventBridgeDefiniton);

    initConnectorMocks();

    this.eventRepositoryBridged = new EventRepositoryBridged(
      eventBridgeDatabaseConnectors,
      signalDetectionLegacyAccessor,
      eventIdUtility,
      eventConverter,
      eventBridgeDefiniton,
      eventStages
    );

  }

  @Test
  void testFindByIdNulls() {
    assertThrows(NullPointerException.class, () -> eventRepositoryBridged.findByIds(null, null));
  }

  @Test
  void testFindByIdNoEventCollection() {
    var eventCollection = new ArrayList<UUID>();
    assertThrows(NullPointerException.class, () -> eventRepositoryBridged.findByIds(eventCollection, null));
  }

  @Test
  void testFindByIdInvalidEvent() {

    var eventCollection = new ArrayList<UUID>();
    eventCollection.add(EVENT_UUID);

    doReturn(Optional.empty())
      .when(eventIdUtility).getEvid(EVENT_UUID);

    assertTrue(eventRepositoryBridged.findByIds(eventCollection, stageOneId).isEmpty());
  }

  @Test
  void testFindByIdNoEventDao() {
    var eventCollection = new ArrayList<UUID>();
    eventCollection.add(EVENT_UUID);

    doReturn(Optional.of(TRUTH_EVID))
      .when(eventIdUtility).getEvid(EVENT_UUID);

    doReturn(Optional.<Event>empty())
      .when(eventDatabaseConnector).findEventById(TRUTH_EVID);

    assertTrue(eventRepositoryBridged.findByIds(eventCollection, stageOneId).isEmpty());
  }

  @Test
  void testFindById_InvalidStageId() {
    assertThat(assertDoesNotThrow(() -> eventRepositoryBridged.findByIds(List.of(EVENT_UUID),
      WorkflowDefinitionId.from("NOT_A_STAGE")))).isEmpty();
  }

  @Test
  void testFindById_SingleStage() {
    var currentStage = stageOneId;
    var eventCollection = new ArrayList<UUID>();
    eventCollection.add(EVENT_UUID);

    doReturn(Optional.of(TRUTH_EVID))
      .when(eventIdUtility).getEvid(EVENT_UUID);

    var expectedEventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(TRUTH_EVID)
      .build();
    doReturn(Optional.of(expectedEventDao))
      .when(eventDatabaseConnector).findEventById(TRUTH_EVID);

    var originDaos = List.of(EventTestFixtures.DEFAULT_ORIGIN_DAO);
    doReturn(originDaos)
      .when(originDatabaseConnector).findByEventIds(List.of(expectedEventDao.getEventId()));

    var gaTagDaos = List.of(new GaTagDao());
    doReturn(gaTagDaos)
      .when(gaTagDatabaseConnector)
      .findGaTagByObjectTypeProcessStateAndEvid(EventRepositoryBridged.OBJECT_TYPE_O, GA_TAG_ANALYST_REJECTED, TRUTH_EVID);

    var expectedEvent = Event.createEntityReference(EVENT_UUID);
    doReturn(expectedEvent)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(expectedEventDao, originDaos, gaTagDaos, currentStage);

    var eventSet = eventRepositoryBridged.findByIds(eventCollection, currentStage);
    assertEquals(Set.of(expectedEvent), eventSet);
  }

  @Test
  void testFindById_MultiStage_MatchingEvid() {
    var currentStage = stageTwoId;
    var previousStage = stageOneId;

    var eventCollection = new ArrayList<UUID>();
    eventCollection.add(EVENT_UUID);

    doReturn(Optional.of(TRUTH_EVID))
      .when(eventIdUtility).getEvid(EVENT_UUID);

    var expectedEventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(TRUTH_EVID)
      .build();
    doReturn(Optional.of(expectedEventDao))
      .when(eventDatabaseConnector).findEventById(TRUTH_EVID);

    var originDaos = List.of(EventTestFixtures.DEFAULT_ORIGIN_DAO);
    doReturn(originDaos)
      .when(originDatabaseConnector).findByEventIds(List.of(expectedEventDao.getEventId()));

    var gaTagDaos = List.of(new GaTagDao());
    doReturn(gaTagDaos)
      .when(gaTagDatabaseConnector)
      .findGaTagByObjectTypeProcessStateAndEvid(EventRepositoryBridged.OBJECT_TYPE_O, GA_TAG_ANALYST_REJECTED, TRUTH_EVID);

    var expectedEventCurr = EventTestFixtures.generateDummyEvent(EVENT_UUID, currentStage, "UnitTest", "ANALYST1",
      Instant.EPOCH, 1.1, MagnitudeType.MB);
    var expectedEventPrev = EventTestFixtures.generateDummyEvent(EVENT_UUID, previousStage, "UnitTest", "ANALYST1",
      Instant.EPOCH, 1.1, MagnitudeType.MB);

    doReturn(expectedEventCurr)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(expectedEventDao, originDaos, gaTagDaos, currentStage);

    doReturn(expectedEventPrev)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(expectedEventDao, originDaos, gaTagDaos, previousStage);

    var eventSet = eventRepositoryBridged.findByIds(eventCollection, currentStage);
    assertEquals(Set.of(expectedEventCurr, expectedEventPrev), eventSet);
  }

  @Test
  void testFindById_MultiStage_OneReferencePerStage() {
    var currentStage = stageTwoId;
    var previousStage = stageOneId;

    var TRUTH_EVIDCurr = 31415926535L;
    var EVENT_UUIDCurr = UUID.randomUUID();

    var TRUTH_EVIDPrev = 31415926536L;
    var EVENT_UUIDPrev = UUID.randomUUID();

    var eventCollection = List.of(EVENT_UUIDCurr, EVENT_UUIDPrev);

    doReturn(Optional.of(TRUTH_EVIDCurr))
      .when(eventIdUtility).getEvid(EVENT_UUIDCurr);

    var expectedEventDaoCurr = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(TRUTH_EVIDCurr)
      .build();
    doReturn(Optional.of(expectedEventDaoCurr))
      .when(eventDatabaseConnector).findEventById(TRUTH_EVIDCurr);

    var originDaos = List.of(EventTestFixtures.DEFAULT_ORIGIN_DAO);
    doReturn(originDaos)
      .when(originDatabaseConnector).findByEventIds(List.of(expectedEventDaoCurr.getEventId()));

    var gaTagDaos = List.of(new GaTagDao());
    doReturn(gaTagDaos)
      .when(gaTagDatabaseConnector)
      .findGaTagByObjectTypeProcessStateAndEvid(EventRepositoryBridged.OBJECT_TYPE_O, GA_TAG_ANALYST_REJECTED, TRUTH_EVIDCurr);

    var expectedEventCurr = EventTestFixtures.generateDummyEvent(EVENT_UUID, currentStage, "UnitTest", "ANALYST1",
      Instant.EPOCH, 1.1, MagnitudeType.MB);
    doReturn(expectedEventCurr)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(expectedEventDaoCurr, originDaos, gaTagDaos, currentStage);

    doReturn(Optional.of(TRUTH_EVIDPrev))
      .when(eventIdUtility).getEvid(EVENT_UUIDPrev);

    var expectedEventDaoPrev = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(TRUTH_EVIDPrev)
      .build();
    doReturn(Optional.of(expectedEventDaoPrev))
      .when(eventDatabaseConnector).findEventById(TRUTH_EVIDPrev);

    doReturn(originDaos)
      .when(originDatabaseConnector).findByEventIds(List.of(expectedEventDaoPrev.getEventId()));

    doReturn(gaTagDaos)
      .when(gaTagDatabaseConnector)
      .findGaTagByObjectTypeProcessStateAndEvid(EventRepositoryBridged.OBJECT_TYPE_O, GA_TAG_ANALYST_REJECTED, TRUTH_EVIDPrev);

    var expectedEventPrev = EventTestFixtures.generateDummyEvent(EVENT_UUIDPrev, previousStage, "UnitTest", "ANALYST1",
      Instant.EPOCH, 1.1, MagnitudeType.MB);
    doReturn(expectedEventPrev)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(expectedEventDaoPrev, originDaos, gaTagDaos, currentStage);

    var eventSet = eventRepositoryBridged.findByIds(eventCollection, currentStage);
    assertEquals(Set.of(expectedEventCurr, expectedEventPrev), eventSet);
  }

  @Test
  void testFindByTime_SingleStage() {
    var currentStage = stageOneId;

    var startTime = Instant.EPOCH;
    var endTime = Instant.ofEpochSecond(1);

    var eventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(TRUTH_EVID)
      .build();

    doReturn(List.of(eventDao))
      .when(eventDatabaseConnector).findEventsByTime(startTime, endTime);

    var originDao = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withEventId(TRUTH_EVID)
      .build();
    var originDaos = List.of(originDao);
    doReturn(originDaos)
      .when(originDatabaseConnector).findByEventIds(List.of(eventDao.getEventId()));

    var gaTagDao = new GaTagDao();
    gaTagDao.setRejectedArrivalOriginEvid(TRUTH_EVID);
    var gaTagDaos = List.of(gaTagDao);
    doReturn(gaTagDaos)
      .when(gaTagDatabaseConnector)
      .findGaTagsByObjectTypesProcessStatesAndEvids(any(), eq(List.of(GA_TAG_ANALYST_REJECTED)), eq(List.of(TRUTH_EVID)));

    var event = Event.createEntityReference(EVENT_UUID);
    doReturn(event)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(eventDao, originDaos, gaTagDaos, currentStage);

    var events = eventRepositoryBridged.findByTime(startTime, endTime, currentStage);
    assertEquals(Set.of(event), events);
  }

  @Test
  void testFindByTime_MultiStage() {
    var currentStage = stageTwoId;
    var previousStage = stageOneId;

    var startTime = Instant.EPOCH;
    var endTime = Instant.ofEpochSecond(1);

    var eventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(TRUTH_EVID)
      .build();
    var EVENT_UUIDPrevious = UUID.randomUUID();

    doReturn(List.of(eventDao))
      .when(eventDatabaseConnector).findEventsByTime(startTime, endTime);

    var originDao = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withEventId(TRUTH_EVID)
      .build();
    var originDaos = List.of(originDao);
    doReturn(originDaos)
      .when(originDatabaseConnector).findByEventIds(List.of(eventDao.getEventId()));

    var gaTagDao = new GaTagDao();
    gaTagDao.setRejectedArrivalOriginEvid(TRUTH_EVID);
    var gaTagDaos = List.of(gaTagDao);
    doReturn(gaTagDaos)
      .when(gaTagDatabaseConnector)
      .findGaTagsByObjectTypesProcessStatesAndEvids(any(), eq(List.of(GA_TAG_ANALYST_REJECTED)), eq(List.of(TRUTH_EVID)));

    var mockEvid = 1L;
    var prevEventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withEventId(mockEvid)
      .build();
    doReturn(Optional.of(prevEventDao))
      .when(eventDatabaseConnector).findEventById(mockEvid);
    var event = EventTestFixtures.generateDummyEvent(EVENT_UUID, currentStage, "UnitTest", "ANALYST1",
      Instant.EPOCH, 1.1, MagnitudeType.MB);
    var eventPrevStage = EventTestFixtures.generateDummyEvent(EVENT_UUIDPrevious, previousStage, "UnitTest", "ANALYST1",
      Instant.EPOCH, 1.1, MagnitudeType.MB);

    var prevOriginDao1 = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withOriginId(2222L)
      .withEventId(mockEvid)
      .build();
    var prevOriginDao2 = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withOriginId(3333L)
      .withEventId(mockEvid)
      .build();
    var prevOriginDaoSet = List.of(prevOriginDao1, prevOriginDao2);
    var eh1UUID = UUID.randomUUID();
    var eh2UUID = UUID.randomUUID();
    var prevStage_EH1 = EventHypothesis.createEntityReference(EventHypothesis.Id.from(event.getId(), eh1UUID));
    var prevStage_EH2 = EventHypothesis.createEntityReference(EventHypothesis.Id.from(event.getId(), eh2UUID));
    doReturn(eh1UUID)
      .doReturn(eh2UUID)
      .when(eventIdUtility).getOrCreateEventHypothesisId(any());

    var updatedData = event.getData().orElseThrow(() -> new IllegalStateException("No data for unit test"))
      .toBuilder().addAllEventHypotheses(Set.of(prevStage_EH1, prevStage_EH2)).build();
    var resultEvent = event.toBuilder().setData(updatedData).build();

    doReturn(Optional.of(mockEvid))
      .when(eventIdUtility).getEvid(event.getId());

    doReturn(prevOriginDaoSet)
      .when(originDatabaseConnector).findByEventIds(List.of(1L));

    doReturn(prevOriginDaoSet)
      .when(originDatabaseConnector).findByTime(any(), any());

    doReturn(event)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(eventDao, originDaos, gaTagDaos, currentStage);

    doReturn(eventPrevStage)
      .when(eventConverter).fromLegacyToDefaultFacetedEvent(prevEventDao, Set.of(prevOriginDao1, prevOriginDao2), Set.of(), previousStage);

    var actualEventSet = eventRepositoryBridged.findByTime(startTime, endTime, currentStage);
    assertEquals(Set.of(resultEvent, eventPrevStage), actualEventSet);

  }

  @Test
  void testFindByTime_InvalidStageId() {
    assertThat(assertDoesNotThrow(() -> eventRepositoryBridged.findByTime(Instant.EPOCH,
      Instant.MAX, WorkflowDefinitionId.from("NOT_A_STAGE")))).isEmpty();
  }

  @Test
  void testFindByTime_NullArgs() {
    var stageId = WorkflowDefinitionId.from("StageName");
    var startTime = Instant.EPOCH;
    var endTime = Instant.ofEpochSecond(1);

    assertThrows(NullPointerException.class, () -> eventRepositoryBridged.findByTime(null, endTime, stageId));
    assertThrows(NullPointerException.class, () -> eventRepositoryBridged.findByTime(startTime, null, stageId));
    assertThrows(NullPointerException.class, () -> eventRepositoryBridged.findByTime(startTime, endTime, null));
  }

  @Test
  void testFindByTime_TimingArgs() {

    var stageId = WorkflowDefinitionId.from("StageName");
    var startTime = Instant.EPOCH;
    var endTime = Instant.ofEpochSecond(startTime.getEpochSecond(), -1L);

    assertThrows(IllegalArgumentException.class,
      () -> eventRepositoryBridged.findByTime(startTime, startTime, stageId));
    assertThrows(IllegalArgumentException.class, () -> eventRepositoryBridged.findByTime(startTime, endTime, stageId));
  }

  @Test
  void testFindHypothesesByIds() {
    var stageId = stageTwoId;

    // parentEventHypothesis's eventId needs to match DEFAULT_BRIDGED_EH_INFORMATION's evid
    var defaultBridgedEhInfoEvid = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION.getOriginDao().getEventId();
    var defaultBridgedEhInfoEventId = UUID.nameUUIDFromBytes(Long.toString(defaultBridgedEhInfoEvid).getBytes(StandardCharsets.UTF_8));
    var parentEventHypothesisId = EventHypothesis.Id.from(defaultBridgedEhInfoEventId, UUID.randomUUID());

    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION.toBuilder()
      .setParentEventHypotheses(Set.of(parentEventHypothesisId))
      .build();
    var sdhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;
    var eventHypothesis = mock(EventHypothesis.class);

    var ehId = EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID());
    var evid = ehInfo.getOriginDao().getEventId();
    var orid = ehInfo.getOriginDao().getOriginId();

    var previousStageEventDao = mock(EventDao.class);

    given(eventIdUtility.getOriginUniqueIdentifier(ehId.getHypothesisId()))
      .willReturn(Optional.of(OriginUniqueIdentifier.create(orid, stageId.getName())));
    given(eventIdUtility.getEvid(ehId.getEventId())).willReturn(Optional.of(evid));
    given(eventIdUtility.getOrCreateEventId(evid)).willReturn(defaultBridgedEhInfoEventId);
    given(eventIdUtility.getOrCreateEventHypothesisId(previousStageEventDao.getPreferredOrigin(), stageId.getName())).willReturn(parentEventHypothesisId.getHypothesisId());

    given(originDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOriginDao()));
    given(originErrDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOrigerrDao()));
    given(eventControlDatabaseConnector.findByEventIdOriginId(evid, orid)).willReturn(ehInfo.getEventControlDao());
    given(assocDatabaseConnector.findAssocsByOrids(List.of(orid))).willReturn(List.of(sdhInfo.getAssocDao()));
    given(netMagDatabaseConnector.findNetMagByOrid(orid)).willReturn(new ArrayList<>(ehInfo.getNetMagDaos()));
    given(eventDatabaseConnector.findEventById(evid)).willReturn(Optional.of(previousStageEventDao));
    given(signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(any(WorkflowDefinitionId.class), any(Long.class), any(Long.class)))
      .willReturn(sdhInfo.getSignalDetectionHypothesis());

    given(eventConverter.fromLegacyToDefaultFacetedEventHypothesis(eq(stageId), eq(ehInfo), any()))
      .willReturn(List.of(eventHypothesis));

    var actualEventHypotheses = eventRepositoryBridged.findHypothesesByIds(singleton(ehId));

    assertThat(actualEventHypotheses).containsExactly(eventHypothesis);
  }

  /**
   * Verifies the use-case where the current stage is the initial stage with no prior stage available
   */
  @Test
  void testFindHypothesesByIdsParentHypotheses_InitialStage() {
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var sdhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;
    var eventHypothesis = mock(EventHypothesis.class);

    var ehId = EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID());
    var evid = ehInfo.getOriginDao().getEventId();
    var orid = ehInfo.getOriginDao().getOriginId();
    var arid = sdhInfo.getAssocDao().getId().getArrivalId();

    var aridOridKey = new AridOridKey();
    aridOridKey.setArrivalId(arid);
    aridOridKey.setOriginId(orid);

    given(eventIdUtility.getOriginUniqueIdentifier(ehId.getHypothesisId()))
      .willReturn(Optional.of(OriginUniqueIdentifier.create(orid, stageOneId.getName())));
    given(eventIdUtility.getEvid(ehId.getEventId())).willReturn(Optional.of(evid));

    given(originDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOriginDao()));
    given(originErrDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOrigerrDao()));
    given(eventControlDatabaseConnector.findByEventIdOriginId(evid, orid)).willReturn(ehInfo.getEventControlDao());
    given(netMagDatabaseConnector.findNetMagByOrid(orid)).willReturn(new ArrayList<>(ehInfo.getNetMagDaos()));
    given(assocDatabaseConnector.findAssocsByOrids(List.of(orid))).willReturn(List.of(sdhInfo.getAssocDao()));
    given(signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageOneId, arid, orid)).willReturn(sdhInfo.getSignalDetectionHypothesis());
    given(arInfoDatabaseConnector.findArInfosByAssocs(List.of(sdhInfo.getAssocDao()))).willReturn(Map.of(aridOridKey, sdhInfo.getArInfoDao().orElseThrow()));
    given(staMagDatabaseConnector.findStaMagDaosByAssocs(List.of(sdhInfo.getAssocDao()))).willReturn(new ArrayList<>(sdhInfo.getStaMagDaos()));
    given(eventConverter.fromLegacyToDefaultFacetedEventHypothesis(stageOneId, ehInfo, singleton(sdhInfo)))
      .willReturn(List.of(eventHypothesis));

    var actualEventHypotheses = eventRepositoryBridged.findHypothesesByIds(singleton(ehId));

    assertThat(actualEventHypotheses).containsExactly(eventHypothesis);
  }

  @Test
  void testFindHypothesesByIdsMissingAssocs() {
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var eventHypothesis = mock(EventHypothesis.class);

    var ehId = EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID());
    var evid = ehInfo.getOriginDao().getEventId();
    var orid = ehInfo.getOriginDao().getOriginId();

    given(eventIdUtility.getOriginUniqueIdentifier(ehId.getHypothesisId()))
      .willReturn(Optional.of(OriginUniqueIdentifier.create(orid, stageOneId.getName())));
    given(eventIdUtility.getEvid(ehId.getEventId())).willReturn(Optional.of(evid));

    given(originDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOriginDao()));
    given(originErrDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOrigerrDao()));
    given(eventControlDatabaseConnector.findByEventIdOriginId(evid, orid)).willReturn(ehInfo.getEventControlDao());
    given(assocDatabaseConnector.findAssocsByOrids(List.of(orid))).willReturn(Collections.emptyList());
    given(netMagDatabaseConnector.findNetMagByOrid(orid)).willReturn(new ArrayList<>(ehInfo.getNetMagDaos()));

    given(eventConverter.fromLegacyToDefaultFacetedEventHypothesis(stageOneId, ehInfo, emptySet()))
      .willReturn(List.of(eventHypothesis));

    var actualEventHypotheses = eventRepositoryBridged.findHypothesesByIds(List.of(ehId));
    assertThat(actualEventHypotheses).containsExactly(eventHypothesis);
  }

  @Test
  void testFindHypothesesByIdsMissingOriginDao() {
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var sdhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;

    var ehId = EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID());
    var evid = ehInfo.getOriginDao().getEventId();
    var orid = ehInfo.getOriginDao().getOriginId();

    given(eventIdUtility.getOriginUniqueIdentifier(ehId.getHypothesisId()))
      .willReturn(Optional.of(OriginUniqueIdentifier.create(orid, stageOneId.getName())));
    given(eventIdUtility.getEvid(ehId.getEventId())).willReturn(Optional.of(evid));

    given(originDatabaseConnector.findById(orid)).willReturn(Optional.empty());
    given(originErrDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOrigerrDao()));
    given(eventControlDatabaseConnector.findByEventIdOriginId(evid, orid)).willReturn(ehInfo.getEventControlDao());
    given(assocDatabaseConnector.findAssocsByOrids(List.of(orid))).willReturn(List.of(sdhInfo.getAssocDao()));

    var actualEventHypotheses = eventRepositoryBridged.findHypothesesByIds(List.of(ehId));
    assertThat(actualEventHypotheses).isEmpty();
    verify(eventConverter, never()).fromLegacyToDefaultFacetedEventHypothesis(any(), any(), any());
  }

  @Test
  void testFindHypothesesByIdsMissingOriginMapping() {
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;

    var ehId = EventHypothesis.Id.from(UUID.randomUUID(), UUID.randomUUID());
    var evid = ehInfo.getOriginDao().getEventId();

    given(eventIdUtility.getOriginUniqueIdentifier(ehId.getHypothesisId())).willReturn(Optional.empty());
    given(eventIdUtility.getEvid(ehId.getEventId())).willReturn(Optional.of(evid));

    var actualEventHypotheses = eventRepositoryBridged.findHypothesesByIds(singleton(ehId));
    assertThat(actualEventHypotheses).isEmpty();
    verify(eventConverter, never()).fromLegacyToDefaultFacetedEventHypothesis(any(), any(), any());
  }

  @Test
  void testFindHypothesesByIdsNoEvidMapping() {
    var eventId = UUID.randomUUID();
    var eventHypothesisId = EventHypothesis.Id.from(eventId, UUID.randomUUID());

    given(eventIdUtility.getOriginUniqueIdentifier(eventHypothesisId.getHypothesisId()))
      .willReturn(Optional.of(OriginUniqueIdentifier.create(1111L, stageOneId.getName())));
    given(eventIdUtility.getEvid(eventId)).willReturn(Optional.empty());

    var actualEventHypotheses = eventRepositoryBridged.findHypothesesByIds(List.of(eventHypothesisId));
    assertThat(actualEventHypotheses).isEmpty();
    verify(eventConverter, never()).fromLegacyToDefaultFacetedEventHypothesis(any(), any(), any());
  }

  @Test
  void testFindByAssociatedDetectionHypotheses() {
    var sdh = SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;
    var arid = 1L;
    var orid = 1L;
    var legacyDatabaseId = "al1";
    var evid = 1L;
    var eventId = UUID.randomUUID();
    var eventDao = EventTestFixtures.DEFAULT_EVENT_DAO;
    var originDao = EventTestFixtures.DEFAULT_ORIGIN_DAO;
    var gaTagDao = EventTestFixtures.DEFAULT_GATAG_DAO;

    var signalDetectionHypotheses = List.of(sdh);
    var stageId = WorkflowDefinitionId.from("STAGE_ONE");
    var databaseConnector = eventDatabaseConnector;
    var event = EventTestFixtures.generateDummyEvent(eventId, stageId, "test",
      "test",
      Instant.EPOCH,
      1D,
      MagnitudeType.MB);

    // findByAssociatedSignalDetectionHypothethes mocks
    given(signalDetectionLegacyAccessor.getSignalDetectionHypothesesAssocIdComponents(List.of(sdh)))
      .willReturn(Set.of(SignalDetectionHypothesisAssocIdComponents.create(legacyDatabaseId, orid, arid)));

    given(eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(any(), eq(EVENT_CONNECTOR_TYPE)))
      .willReturn(eventDatabaseConnector);
    given(eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(any(), eq(ORIGIN_CONNECTOR_TYPE)))
      .willReturn(originDatabaseConnector);
    given(eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(any(), eq(GA_TAG_CONNECTOR_TYPE)))
      .willReturn(gaTagDatabaseConnector);
    given(eventIdUtility.getEvid(any())).willReturn(Optional.of(evid));
    given(eventDatabaseConnector.findEventById(evid)).willReturn(Optional.of(eventDao));
    given(originDatabaseConnector.findByEventIds(List.of(eventDao.getEventId()))).willReturn(List.of(originDao));
    given(gaTagDatabaseConnector.findGaTagByObjectTypeProcessStateAndEvid(any(), any(), eq(eventDao.getEventId())))
      .willReturn(List.of(gaTagDao));
    given(originDatabaseConnector.findById(orid))
      .willReturn(Optional.of(originDao));
    given(eventConverter.fromLegacyToDefaultFacetedEvent(eventDao, List.of(originDao), List.of(gaTagDao), stageId))
      .willReturn(event);
    // --

    // findHypothesesByIds mocks
    var ehInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
    var sdhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;
    var aridOridKey = new AridOridKey();
    aridOridKey.setArrivalId(arid);
    aridOridKey.setOriginId(orid);
    given(eventIdUtility.getOriginUniqueIdentifier(event.getData().orElseThrow().getEventHypotheses().iterator().next().getId().getHypothesisId()))
      .willReturn(Optional.of(OriginUniqueIdentifier.create(orid, stageOneId.getName())));
    given(eventIdUtility.getEvid(event.getData().orElseThrow().getEventHypotheses().iterator().next().getId().getEventId())).willReturn(Optional.of(evid));

    given(originDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOriginDao()));
    given(originErrDatabaseConnector.findById(orid)).willReturn(Optional.of(ehInfo.getOrigerrDao()));
    given(eventControlDatabaseConnector.findByEventIdOriginId(evid, orid)).willReturn(ehInfo.getEventControlDao());
    given(netMagDatabaseConnector.findNetMagByOrid(orid)).willReturn(new ArrayList<>(ehInfo.getNetMagDaos()));
    given(assocDatabaseConnector.findAssocsByOrids(List.of(orid))).willReturn(List.of(sdhInfo.getAssocDao()));
    given(signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageOneId, sdhInfo.getArInfoDao().orElseThrow().getArrivalId(), ehInfo.getOriginDao().getOriginId())).willReturn(sdhInfo.getSignalDetectionHypothesis());
    given(arInfoDatabaseConnector.findArInfosByAssocs(List.of(sdhInfo.getAssocDao()))).willReturn(Map.of(aridOridKey, sdhInfo.getArInfoDao().orElseThrow()));
    given(staMagDatabaseConnector.findStaMagDaosByAssocs(List.of(sdhInfo.getAssocDao()))).willReturn(new ArrayList<>(sdhInfo.getStaMagDaos()));
    given(eventConverter.fromLegacyToDefaultFacetedEventHypothesis(eq(stageOneId), any(), any()))
      .willReturn(event.getData().orElseThrow().getEventHypotheses());
    // --

    given(databaseConnector.findEventIdsByArids(List.of(1L))).willReturn(List.of(evid));

    var returnedEvent = eventRepositoryBridged.findByAssociatedDetectionHypotheses(signalDetectionHypotheses, stageId);

    assertEquals(Set.of(event), returnedEvent);

  }

  @Test
  void testFindByAssociatedDetectionHypotheses_EmptySDH() {
    var stageId = WorkflowDefinitionId.from("test");
    assertEquals(Set.of(), eventRepositoryBridged.findByAssociatedDetectionHypotheses(List.of(), stageId));
  }

  private void initConnectorMocks() {

    // Marking these as lenient stubbings to avoid having to set up individual sets of mock connectors for each test, because
    // not all tests use the same connectors.

    // Current stage connector mocks
    lenient().doReturn(eventDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(EVENT_CONNECTOR_TYPE));
    lenient().doReturn(eventControlDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(EVENT_CONTROL_CONNECTOR_TYPE));
    lenient().doReturn(originDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(ORIGIN_CONNECTOR_TYPE));
    lenient().doReturn(originErrDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(ORIGERR_CONNECTOR_TYPE));
    lenient().doReturn(assocDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(ASSOC_CONNECTOR_TYPE));
    lenient().doReturn(gaTagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(GA_TAG_CONNECTOR_TYPE));
    lenient().doReturn(netMagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(NETMAG_CONNECTOR_TYPE));
    lenient().doReturn(arInfoDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(AR_INFO_CONNECTOR_TYPE));
    lenient().doReturn(staMagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(any(), eq(STAMAG_CONNECTOR_TYPE));

    // Previous stage connector mocks
    lenient().doReturn(eventDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, EVENT_CONNECTOR_TYPE);
    lenient().doReturn(eventControlDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, EVENT_CONTROL_CONNECTOR_TYPE);
    lenient().doReturn(originDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, ORIGIN_CONNECTOR_TYPE);
    lenient().doReturn(originErrDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, ORIGERR_CONNECTOR_TYPE);
    lenient().doReturn(assocDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, ASSOC_CONNECTOR_TYPE);
    lenient().doReturn(staMagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, GA_TAG_CONNECTOR_TYPE);
    lenient().doReturn(arInfoDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, AR_INFO_CONNECTOR_TYPE);
    lenient().doReturn(gaTagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, GA_TAG_CONNECTOR_TYPE);
    lenient().doReturn(netMagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageTwoName, NETMAG_CONNECTOR_TYPE);
    lenient().doReturn(eventDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, EVENT_CONNECTOR_TYPE);
    lenient().doReturn(eventControlDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, EVENT_CONTROL_CONNECTOR_TYPE);
    lenient().doReturn(originDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, ORIGIN_CONNECTOR_TYPE);
    lenient().doReturn(originErrDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, ORIGERR_CONNECTOR_TYPE);
    lenient().doReturn(assocDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, ASSOC_CONNECTOR_TYPE);
    lenient().doReturn(gaTagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, GA_TAG_CONNECTOR_TYPE);
    lenient().doReturn(netMagDatabaseConnector)
      .when(eventBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(stageThreeName, NETMAG_CONNECTOR_TYPE);

    lenient().doReturn(true)
      .when(eventBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(stageTwoName), any());
    lenient().doReturn(true)
      .when(eventBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(stageThreeName), any());
  }

}

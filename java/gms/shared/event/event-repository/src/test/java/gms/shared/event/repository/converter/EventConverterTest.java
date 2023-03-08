package gms.shared.event.repository.converter;

import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.PreferredEventHypothesis;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.GaTagDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.repository.BridgeTestFixtures;
import gms.shared.event.repository.BridgedEhInformation;
import gms.shared.event.repository.BridgedSdhInformation;
import gms.shared.event.repository.EventStages;
import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.event.repository.util.id.EventIdUtility;
import gms.shared.event.repository.util.id.OriginUniqueIdentifier;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.ignite.IgniteCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventConverterTest {

  static final BridgedEhInformation defaultEhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION;
  static final BridgedSdhInformation defaultSdhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;

  @Mock
  EventBridgeDefinition mockedEventBridgeDefinition;

  @Mock
  SignalDetectionIdUtility mockedSignalDetectionIdUtility;

  @Mock
  IgniteCache<UUID, Long> eventIdtoEvid;

  @Mock
  IgniteCache<Long, UUID> evidToEventId;

  @Mock
  IgniteCache<UUID, OriginUniqueIdentifier> eventHypothesisIdToOriginUniqueIdentifier;

  @Mock
  IgniteCache<OriginUniqueIdentifier, UUID> originUniqueIdentifierToEventHypothesisId;

  @Mock
  EventStages mockedEventStages;

  EventIdUtility eventIdUtility;
  EventConverter eventConverter;

  private static Stream<Arguments> eventPrecondition() {

    return Stream.of(
      arguments(null, List.of(mock(OriginDao.class)), List.of(mock(GaTagDao.class)), mock(WorkflowDefinitionId.class), NullPointerException.class),
      arguments(mock(EventDao.class), null, List.of(mock(GaTagDao.class)), mock(WorkflowDefinitionId.class), NullPointerException.class),
      arguments(mock(EventDao.class), List.of(mock(OriginDao.class)), null, mock(WorkflowDefinitionId.class), NullPointerException.class),
      arguments(mock(EventDao.class), List.of(mock(OriginDao.class)), List.of(mock(GaTagDao.class)), null, NullPointerException.class)
    );
  }

  @BeforeEach
  void setUp() {
    eventIdUtility = new EventIdUtility(mockedEventBridgeDefinition, eventIdtoEvid, evidToEventId, eventHypothesisIdToOriginUniqueIdentifier,
      originUniqueIdentifierToEventHypothesisId);
    eventConverter = new EventConverter(eventIdUtility, mockedSignalDetectionIdUtility, mockedEventBridgeDefinition, mockedEventStages);
  }

  private static Stream<Arguments> eventHypothesisPrecondition() {
    var stageId = WorkflowDefinitionId.from("test");

    return Stream.of(
      arguments(null, defaultEhInfo, singleton(defaultSdhInfo), NullPointerException.class),
      arguments(stageId, null, singleton(defaultSdhInfo), NullPointerException.class),
      arguments(stageId, defaultEhInfo, null, NullPointerException.class)
    );
  }

  @Test
  void testFromLegacyToDefaultFacetedEventHypothesis() {
    var stageId = WorkflowDefinitionId.from("test");
    var eventHypothesis = eventConverter.fromLegacyToDefaultFacetedEventHypothesis(stageId,
      defaultEhInfo, singleton(defaultSdhInfo));

    assertTrue(!eventHypothesis.isEmpty());
    assertEquals(1, eventHypothesis.size());
    assertTrue(eventHypothesis.iterator().next().getData().isPresent());
    var ehElementData = eventHypothesis.iterator().next().getData().get();
    assertFalse(ehElementData.isRejected());
    assertFalse(ehElementData.getAssociatedSignalDetectionHypotheses().isEmpty());
    assertFalse(ehElementData.getLocationSolutions().isEmpty());
    assertFalse(ehElementData.getPreferredLocationSolution().isEmpty());
    assertEquals(1, ehElementData.getLocationSolutions().size());
    assertTrue(ehElementData.getLocationSolutions().iterator().next().getData().isPresent());
  }

  @Test
  void testFromLegacyToRejectedEventHypothesis() {
    var bridgeEh = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION.toBuilder()
      .setGaTagDao(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .build();
    var bridgeSdh = BridgeTestFixtures.DEFAULT_BRIDGED_SDH_INFORMATION;
    var eventStages = bridgeEh.getEventStages()
      .orElseThrow(() -> new IllegalArgumentException("No EventStage data available"));
    var currentStage = eventStages.getOrderedStages().get(0);
    var nextStage = eventStages.getOrderedStages().get(1);

    var eventEHParentId = OriginUniqueIdentifier.create(bridgeEh.getOriginDao().getOriginId(), currentStage.getName());
    var ehUUIDRejectedParent = eventIdUtility.getOrCreateEventHypothesisId(eventEHParentId);

    var rejectedEhOrid = OriginUniqueIdentifier.create(bridgeEh.getGaTagDao().orElseThrow().getId(), nextStage.getName());
    var rejectedEhUUId = eventIdUtility.getOrCreateEventHypothesisId(rejectedEhOrid);

    doReturn(ehUUIDRejectedParent)
      .when(originUniqueIdentifierToEventHypothesisId).get(eventEHParentId);

    var rejectedEventUUID = eventIdUtility.getOrCreateEventId(bridgeEh.getGaTagDao().get().getRejectedArrivalOriginEvid());

    var expectedRejectedEh = EventHypothesis.createRejectedEventHypothesis(rejectedEventUUID, rejectedEhUUId, ehUUIDRejectedParent);

    var actualEh = eventConverter.fromLegacyToDefaultFacetedEventHypothesis(currentStage, bridgeEh, List.of(bridgeSdh));

    Assertions.assertEquals(2, actualEh.size());
    Assertions.assertTrue(actualEh.contains(expectedRejectedEh), "fromLegacyToDefaultFacetedEventHypothesis did not create and return a rejected EH when one was expected");
  }

  @ParameterizedTest
  @MethodSource("eventHypothesisPrecondition")
  void TestFromLegacyToDefaultFacetedEventHypothesisPreconditions(WorkflowDefinitionId stageId,
    BridgedEhInformation ehInfo, Collection<BridgedSdhInformation> sdhInfo, Class<Throwable> expectedExceptionClass
  ) {
    //test preconditions
    assertThrows(expectedExceptionClass, () ->
      eventConverter.fromLegacyToDefaultFacetedEventHypothesis(stageId, ehInfo, sdhInfo));
  }

  @Test
  void testFromLegacyToDefaultFacetedEventHypothesis_EmptySdh() {

    var sdhInfoWithEmptySdh = BridgedSdhInformation.builder()
      .setArInfoDao(defaultSdhInfo.getArInfoDao().orElseThrow())
      .setAssocDao(defaultSdhInfo.getAssocDao())
      .setStaMagDaos(defaultSdhInfo.getStaMagDaos())
      .build();

    var stageId = WorkflowDefinitionId.from("test");
    var eventHypothesis = eventConverter.fromLegacyToDefaultFacetedEventHypothesis(stageId,
      defaultEhInfo, Set.of(defaultSdhInfo, sdhInfoWithEmptySdh));

    assertTrue(!eventHypothesis.isEmpty());
    assertEquals(1, eventHypothesis.size());
    assertTrue(eventHypothesis.iterator().next().getData().isPresent());
    var ehElementData = eventHypothesis.iterator().next().getData().get();
    assertEquals(
      Set.of(defaultSdhInfo.getSignalDetectionHypothesis().orElseThrow().toEntityReference()),
      ehElementData.getAssociatedSignalDetectionHypotheses()
    );
  }

  @Test
  void testFromLegacyToDefaultFacetedEvent() {
    var workflowDefinitionId = WorkflowDefinitionId.from("AL1");
    var signalDetectionUUID = UUID.randomUUID();
    var evid = 238382843822L;
    var gaTagArid = 12345L;
    final var the_dude = "The Dude";

    var localEventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withAuthor(the_dude)
      .withEventId(evid)
      .withPreferredOrigin(55553432288L)
      .build();

    var localOriginDao = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withOriginId(23423432288L)
      .build();

    var localPreferredOrigin = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withOriginId(55553432288L)
      .build();

    var localGaTagDao = GaTagDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_GATAG_DAO)
      .withObjectType("a")
      .withProcessState("analyst_rejected")
      .withId(gaTagArid)
      .withRejectedArrivalOriginEvid(evid)
      .build();

    when(mockedEventBridgeDefinition.getMonitoringOrganization()).thenReturn("ORG");
    when(mockedSignalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(gaTagArid))
      .thenReturn(signalDetectionUUID);
//    Mockito.when(mockedEventStages.getOrderedStages()).thenReturn(List.of(WorkflowDefinitionId.from("Auto Network"), WorkflowDefinitionId.from("AL1")));

    var event = eventConverter.fromLegacyToDefaultFacetedEvent(
      localEventDao,
      List.of(localOriginDao, localPreferredOrigin),
      List.of(localGaTagDao),
      workflowDefinitionId);

    var eventData = assertDoesNotThrow(() -> event.getData().orElseThrow());

    assertEquals(1, eventData.getRejectedSignalDetectionAssociations().size());
    assertEquals(signalDetectionUUID, eventData.getRejectedSignalDetectionAssociations()
      .iterator().next().getId());

    //this test will change once configuration is completed
    assertEquals("ORG", eventData.getMonitoringOrganization());

    var preferredByStage = eventData.preferredEventHypothesisByStage()
      .filter(preferredEventHypothesis -> preferredEventHypothesis.getStage().equals(workflowDefinitionId))
      .collect(Collectors.toUnmodifiableSet());
    assertNotNull(preferredByStage);
    assertEquals(1, preferredByStage.size());
    assertEquals(the_dude, preferredByStage.iterator().next().getPreferredBy());
    assertTrue(eventData.getOverallPreferred().isPresent());
    assertEquals(0, eventData.getFinalEventHypothesisHistory().size());
    assertEquals(2, eventData.getEventHypotheses().size());
  }

  @Test
  void testFromLegacyToDefaultFacetedEvent_EventHypothesisRejectedInPreviousStage() {

    var orid = 1L;
    var evid = 1L;
    var autoNetwork = WorkflowDefinitionId.from("Auto Network");
    var al1 = WorkflowDefinitionId.from("AL1");
    var stages = List.of(autoNetwork, al1);
    var rejectedPreferredEventHypothesisId = EventHypothesis.Id.from(
      UUID.randomUUID(),
      eventIdUtility.originUniqueIdentifierToUUID(OriginUniqueIdentifier.create(orid, al1.getName()))
    );
    var rejectedPreferredEventHypothesis = PreferredEventHypothesis.from(
      al1,
      "Author",
      EventHypothesis.createEntityReference(rejectedPreferredEventHypothesisId)
    );

    var eventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withAuthor("Author")
      .withEventId(evid)
      .withPreferredOrigin(orid)
      .build();

    var originDao = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withOriginId(orid)
      .build();

    var gaTagDao = GaTagDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .withId(orid)
      .withRejectedArrivalOriginEvid(evid)
      .build();

    when(mockedEventBridgeDefinition.getMonitoringOrganization()).thenReturn("Monitoring Org");
    when(mockedEventBridgeDefinition.getOrderedStages()).thenReturn(stages);
    when(mockedEventStages.getNextStage(autoNetwork)).thenReturn(Optional.of(al1));
    when(originUniqueIdentifierToEventHypothesisId.get(OriginUniqueIdentifier.create(orid, autoNetwork.getName())))
      .thenReturn(UUID.randomUUID());
    when(evidToEventId.get(evid)).thenReturn(rejectedPreferredEventHypothesisId.getEventId());

    // Test event hypothesis being rejected in the stage after the requested stage
    var convertedEventAutoNetwork = eventConverter.fromLegacyToDefaultFacetedEvent(eventDao, Set.of(originDao), Set.of(gaTagDao), autoNetwork);
    assertEquals(2, convertedEventAutoNetwork.getData().orElseThrow().getEventHypotheses().size(), "Two Event Hypothesis expected in this rejected EH case");
    assertTrue(convertedEventAutoNetwork.getData().orElseThrow().getEventHypotheses()
        .contains(convertedEventAutoNetwork.getData().orElseThrow().getOverallPreferred().get()),
      "Overall Preferred must exist in EventHypotheses list");
    assertTrue(convertedEventAutoNetwork.getData().orElseThrow().getPreferredEventHypothesisByStage()
      .contains(rejectedPreferredEventHypothesis), "PreferredEventHypothesisByStage did not contain rejected EventHypothesis ");
  }

  @Test
  void testFromLegacyToDefaultFacetedEvent_EventHypothesisRejectedInCurrentStage() {

    var orid = 1L;
    var evid = 1L;
    var autoNetwork = WorkflowDefinitionId.from("Auto Network");
    var al1 = WorkflowDefinitionId.from("AL1");
    var al2 = WorkflowDefinitionId.from("AL2");
    var stages = List.of(autoNetwork, al1, al2);
    var rejectedPreferredEventHypothesisId = EventHypothesis.Id.from(
      UUID.randomUUID(),
      eventIdUtility.originUniqueIdentifierToUUID(OriginUniqueIdentifier.create(orid, al2.getName()))
    );
    var rejectedPreferredEventHypothesis = PreferredEventHypothesis.from(
      al2,
      "Author",
      EventHypothesis.createEntityReference(rejectedPreferredEventHypothesisId)
    );

    var eventDao = EventDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_DAO)
      .withAuthor("Author")
      .withEventId(evid)
      .withPreferredOrigin(orid)
      .build();

    var originDao = OriginDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .withOriginId(orid)
      .build();

    var gaTagDao = GaTagDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .withId(orid)
      .withRejectedArrivalOriginEvid(evid)
      .build();

    when(mockedEventBridgeDefinition.getMonitoringOrganization()).thenReturn("Monitoring Org");
    when(mockedEventBridgeDefinition.getOrderedStages()).thenReturn(stages);
    when(mockedEventStages.getPreviousStage(al1)).thenReturn(Optional.of(autoNetwork));
    when(mockedEventStages.getNextStage(al1)).thenReturn(Optional.of(al2));
    when(originUniqueIdentifierToEventHypothesisId.get(OriginUniqueIdentifier.create(orid, autoNetwork.getName())))
      .thenReturn(UUID.randomUUID());
    when(originUniqueIdentifierToEventHypothesisId.get(OriginUniqueIdentifier.create(orid, al1.getName())))
      .thenReturn(UUID.randomUUID());//null or with uuid
    when(evidToEventId.get(evid)).thenReturn(rejectedPreferredEventHypothesisId.getEventId());

    // Test event hypothesis being rejected in the requested stage
    var convertedEventAl1 = eventConverter.fromLegacyToDefaultFacetedEvent(eventDao, Set.of(originDao), Set.of(gaTagDao), al1);
    assertEquals(2, convertedEventAl1.getData().orElseThrow().getEventHypotheses().size(), "Two Event Hypothesis expected in this rejected EH case");
    assertTrue(convertedEventAl1.getData().orElseThrow().getEventHypotheses()
        .contains(convertedEventAl1.getData().orElseThrow().getOverallPreferred().get()),
      "Overall Preferred must exist in EventHypotheses list");
    System.out.println("getPreferredEventHypothesisByStage:" + convertedEventAl1.getData().orElseThrow().getPreferredEventHypothesisByStage() +
      "rejectedPreferredEventHypothesis" + rejectedPreferredEventHypothesis);
    assertTrue(convertedEventAl1.getData().orElseThrow().getPreferredEventHypothesisByStage()
      .contains(rejectedPreferredEventHypothesis), "PreferredEventHypothesisByStage did not contain rejected EventHypothesis " + rejectedPreferredEventHypothesis);
  }

  @ParameterizedTest
  @MethodSource("eventPrecondition")
  void TestFromLegacyToDefaultFacetedEventPreconditions(EventDao eventDao, Collection<OriginDao> originDaos,
    Collection<GaTagDao> arrivalGaTagDaos, WorkflowDefinitionId workflowDefinitionId,
    Class<Throwable> expectedExceptionClass) {
    //test preconditions
    assertThrows(expectedExceptionClass, () ->
      eventConverter.fromLegacyToDefaultFacetedEvent(eventDao, originDaos, arrivalGaTagDaos, workflowDefinitionId));
  }
}

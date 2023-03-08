package gms.shared.signaldetection.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisConverterId;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.signaldetection.converter.detection.SignalDetectionConverter;
import gms.shared.signaldetection.converter.detection.SignalDetectionHypothesisConverter;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.signaldetection.database.connector.AssocDatabaseConnector;
import gms.shared.signaldetection.database.connector.SignalDetectionBridgeDatabaseConnectors;
import gms.shared.signaldetection.database.connector.config.SignalDetectionBridgeDefinition;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisArrivalIdComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisAssocIdComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import gms.shared.stationdefinition.repository.BridgedChannelRepository;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.IgniteCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.AMPLITUDE_CONNECTOR_TYPE;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.ARRIVAL_CONNECTOR_TYPE;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.ASSOC_CONNECTOR_TYPE;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_TEST_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_TEST_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_TEST_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_TEST_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.WFTAG_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.WFTAG_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.MEASURED_WAVEFORM_LAG_DURATION;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.MEASURED_WAVEFORM_LEAD_DURATION;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.MONITORING_ORG;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_0;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ID;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ID_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ID_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ID_TEST_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ID_TEST_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_ID;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_ID_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.WORKFLOW_DEFINITION_ID1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.WORKFLOW_DEFINITION_ID2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.WORKFLOW_DEFINITION_ID3;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_3;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.WFID_3;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.getSiteForStation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedSignalDetectionRepositoryTest {

  @Mock
  AmplitudeDatabaseConnector amplitudeDatabaseConnector;

  @Mock
  ArrivalDatabaseConnector currArrivalDatabaseConnector;

  @Mock
  ArrivalDatabaseConnector prevArrivalDatabaseConnector;

  @Mock
  AssocDatabaseConnector currAssocDatabaseConnector;

  @Mock
  AssocDatabaseConnector prevAssocDatabaseConnector;

  @Mock
  private SignalDetectionBridgeDatabaseConnectors signalDetectionBridgeDatabaseConnectors;

  @Mock
  SiteDatabaseConnector siteDatabaseConnector;

  @Mock
  WfdiscDatabaseConnector wfdiscDatabaseConnector;

  @Mock
  WftagDatabaseConnector wftagDatabaseConnector;

  @Mock
  SignalDetectionBridgeDefinition signalDetectionBridgeDefinition;

  @Mock
  private BridgedChannelRepository bridgedChannelRepository;

  @Mock
  private SignalDetectionIdUtility signalDetectionIdUtility;

  @Mock
  private SignalDetectionHypothesisConverter signalDetectionHypothesisConverter;

  @Mock
  private SignalDetectionConverter signalDetectionConverter;

  @Mock
  private IgniteCache<ChannelSegmentDescriptor, Long> channelSegmentDescriptorWfidCache;

  private static final String WORKFLOW_DEFINITION_ID1_NAME = WORKFLOW_DEFINITION_ID1.getName();
  private static final String WORKFLOW_DEFINITION_ID2_NAME = WORKFLOW_DEFINITION_ID2.getName();
  private static final String WORKFLOW_DEFINITION_ID3_NAME = WORKFLOW_DEFINITION_ID3.getName();

  private static final ImmutableMap<WorkflowDefinitionId, String> dbAccountStageMap = ImmutableMap.of(
    WORKFLOW_DEFINITION_ID1, WORKFLOW_DEFINITION_ID1_NAME,
    WORKFLOW_DEFINITION_ID2, WORKFLOW_DEFINITION_ID2_NAME);

  private static final ImmutableMap<WorkflowDefinitionId, String> dbAccountMissingStageMap = ImmutableMap.of(
    WORKFLOW_DEFINITION_ID1, WORKFLOW_DEFINITION_ID1_NAME);

  private BridgedSignalDetectionRepository repository;

  private static final List<UUID> SIGNAL_DETECTION_IDS = List.of(SIGNAL_DETECTION.getId(),
    SIGNAL_DETECTION_3.getId());

  private static final List<SignalDetectionHypothesisId> SIGNAL_DETECTION_HYPOTHESIS_IDS = List.of(
    SIGNAL_DETECTION_HYPOTHESIS_ID, SIGNAL_DETECTION_HYPOTHESIS_ID_3, SIGNAL_DETECTION_HYPOTHESIS_ID_2);

  private static final List<SignalDetectionHypothesisId> SIGNAL_DETECTION_HYPOTHESIS_IDS_2 = List.of(
    SIGNAL_DETECTION_HYPOTHESIS_ID_TEST_1, SIGNAL_DETECTION_HYPOTHESIS_ID_TEST_3);

  private static final Instant START_TIME = Instant.EPOCH;
  private static final Instant END_TIME = Instant.EPOCH.plusSeconds(300);

  @BeforeEach
  void setUp() {

    repository = new BridgedSignalDetectionRepository(
      signalDetectionBridgeDatabaseConnectors,
      siteDatabaseConnector,
      wfdiscDatabaseConnector,
      wftagDatabaseConnector,
      signalDetectionBridgeDefinition,
      bridgedChannelRepository,
      signalDetectionIdUtility,
      signalDetectionHypothesisConverter,
      signalDetectionConverter,
      channelSegmentDescriptorWfidCache);
  }

  @Test
  void testFindByIds_nullIds() {
    assertThrows(NullPointerException.class, () -> repository.findByIds(null, WORKFLOW_DEFINITION_ID1));
  }

  @Test
  void testFindByIds_nullStageId() {
    assertThrows(NullPointerException.class, () -> repository.findByIds(SIGNAL_DETECTION_IDS, null));
  }

  @Test
  void testFindByIds_emptyStage() {
    var ids = List.of(SIGNAL_DETECTION_ID);

    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of());

    assertTrue(repository.findByIds(ids, WORKFLOW_DEFINITION_ID2).isEmpty());
  }

  @ParameterizedTest
  @MethodSource("getFindByIdsArguments")
  void testFindByIds(List<SignalDetection> expectedValues,
    Consumer<SignalDetectionIdUtility> setupMocks,
    Consumer<SignalDetectionIdUtility> verifyMocks) {

    // initialize the db connector mocks
    initConnectorMocks();

    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of(WORKFLOW_DEFINITION_ID1, WORKFLOW_DEFINITION_ID2));

    //current arrivals
    var currentArrivals = List.of(ARRIVAL_1);
    doReturn(currentArrivals)
      .when(currArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_1.getId(), ARRIVAL_3.getId()));

    // current assocs
    var currentAssocs = List.of(ASSOC_DAO_3);
    doReturn(currentAssocs)
      .when(currAssocDatabaseConnector).findAssocsByArids(List.of(ARRIVAL_1.getId()));

    //prev arrivals
    var prevArrivals = List.of(ARRIVAL_1, ARRIVAL_3);
    doReturn(prevArrivals)
      .when(prevArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_1.getId(), ARRIVAL_3.getId()));

    //prev assocs
    var prevAssocs = List.of(ASSOC_DAO_1);
    doReturn(prevAssocs)
      .when(prevAssocDatabaseConnector).findAssocsByArids(List.of(ARRIVAL_1.getId(), ARRIVAL_3.getId()));

    when(signalDetectionBridgeDefinition.getMonitoringOrganization())
      .thenReturn(MONITORING_ORG);
    when(signalDetectionConverter.convert(any()))
      .thenReturn(Optional.of(SIGNAL_DETECTION), Optional.of(SIGNAL_DETECTION_3));

    setupMocks.accept(signalDetectionIdUtility);
    List<SignalDetection> signalDetections = repository.findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID2);
    assertTrue(signalDetections.size() > 0);
    verifyMocks.accept(signalDetectionIdUtility);

    signalDetections.forEach(sd -> assertTrue(expectedValues.contains(sd)));
    verify(signalDetectionBridgeDefinition, times(6)).getOrderedStages();
    verify(signalDetectionBridgeDefinition, times(3)).getMonitoringOrganization();
    verify(signalDetectionConverter, times(3)).convert(any());
    verifyNoMoreInteractions(signalDetectionIdUtility,
      signalDetectionBridgeDefinition,
      signalDetectionConverter,
      channelSegmentDescriptorWfidCache);
  }

  @ParameterizedTest
  @MethodSource("getFindByIdsArguments")
  void testFindByIds_nullPrevious(List<SignalDetection> expectedValues,
    Consumer<SignalDetectionIdUtility> setupMocks,
    Consumer<SignalDetectionIdUtility> verifyMocks) {

    // initialize the db connector mocks
    initCurrentConnectorMocks();

    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of(WORKFLOW_DEFINITION_ID1));

    //current arrivals
    var currentArrivals = List.of(ARRIVAL_1, ARRIVAL_3);
    doReturn(currentArrivals)
      .when(currArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_1.getId(), ARRIVAL_3.getId()));

    // current assocs
    var currentAssocs = List.of(ASSOC_DAO_1);
    doReturn(currentAssocs)
      .when(currAssocDatabaseConnector).findAssocsByArids(List.of(ARRIVAL_1.getId(), ARRIVAL_3.getId()));

    when(signalDetectionBridgeDefinition.getMonitoringOrganization())
      .thenReturn(MONITORING_ORG);
    when(signalDetectionConverter.convert(any()))
      .thenReturn(Optional.of(SIGNAL_DETECTION), Optional.of(SIGNAL_DETECTION_3));

    setupMocks.accept(signalDetectionIdUtility);
    List<SignalDetection> signalDetections = repository.findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID1);
    assertTrue(signalDetections.size() > 0);
    signalDetections.forEach(sd -> assertTrue(expectedValues.contains(sd)));
    verifyMocks.accept(signalDetectionIdUtility);

    verifyNoMoreInteractions(signalDetectionIdUtility,
      signalDetectionBridgeDefinition,
      signalDetectionConverter,
      channelSegmentDescriptorWfidCache);
  }

  @Test
  void testFindHypothesesByIds_ArrivalsNoAssocs() {
    // initialize current and previous stage connectors
    initMultiStageConnectorMocks();

    List<SignalDetectionHypothesis> expectedValues = List.of(SIGNAL_DETECTION_HYPOTHESIS_0, SIGNAL_DETECTION_HYPOTHESIS);

    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of(WORKFLOW_DEFINITION_ID1, WORKFLOW_DEFINITION_ID2, WORKFLOW_DEFINITION_ID3));
    when(signalDetectionBridgeDefinition.getDatabaseAccountByStage())
      .thenReturn(dbAccountStageMap);

    var idComponents1 = SignalDetectionHypothesisArrivalIdComponents.create(
      WORKFLOW_DEFINITION_ID1_NAME,
      ARRIVAL_1.getId());
    doReturn(idComponents1)
      .when(signalDetectionIdUtility)
      .getArrivalIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID.getId());

    var idComponents2 = SignalDetectionHypothesisArrivalIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME,
      ARRIVAL_3.getId());
    doReturn(idComponents2)
      .when(signalDetectionIdUtility)
      .getArrivalIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId());

    //create arrival current and previous stage connector mock returns
    List<ArrivalDao> currArrivals1 = List.of(ARRIVAL_1);
    doReturn(currArrivals1)
      .when(currArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_1.getId()));
    List<ArrivalDao> currArrivals2 = List.of(ARRIVAL_3);
    doReturn(currArrivals2)
      .when(currArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_3.getId()));
    List<ArrivalDao> prevArrivals = List.of(ARRIVAL_3);
    doReturn(prevArrivals)
      .when(prevArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_3.getId()));

    //create assoc current and previous stage connector mock returns
    doReturn(List.of())
      .when(currAssocDatabaseConnector).findAssocsByArids(List.of(ARRIVAL_1.getId()));

    when(signalDetectionIdUtility
      .getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(ARRIVAL_3.getId(), WORKFLOW_DEFINITION_ID1_NAME))
      .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_3.getId().getId());

    // find current stage wftags and wfdiscs
    when(wftagDatabaseConnector.findWftagsByTagIds(List.of(ARRIVAL_1.getId())))
      .thenReturn(List.of(WFTAG_1));
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(anyCollection()))
      .thenReturn(List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_3));

    // find previous stage wftags and wfdiscs
    when(wftagDatabaseConnector.findWftagsByTagIds(List.of(ARRIVAL_3.getId())))
      .thenReturn(List.of(WFTAG_3));
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(anyCollection()))
      .thenReturn(List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_3));

    // find curr/prev stage amplitudes
    doReturn(List.of(AMPLITUDE_DAO_1))
      .when(amplitudeDatabaseConnector).findAmplitudesByArids(anyCollection());

    when(signalDetectionBridgeDefinition.getMonitoringOrganization())
      .thenReturn(MONITORING_ORG);

    doReturn(SIGNAL_DETECTION_HYPOTHESIS_ID_3.getSignalDetectionId())
      .when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(ARRIVAL_3.getId());

    doReturn(SIGNAL_DETECTION_HYPOTHESIS_ID.getSignalDetectionId())
      .when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(ARRIVAL_1.getId());

    doReturn(CHANNEL)
      .when(bridgedChannelRepository).loadChannelFromWfdisc(List.of((long) WFID_3),
        Optional.of(TagName.ARID),
        Optional.of(ARRIVAL_3.getId()),
        Optional.empty(),
        WFDISC_TEST_DAO_3.getTime(),
        WFDISC_TEST_DAO_3.getEndTime());

    doReturn(CHANNEL)
      .when(bridgedChannelRepository).loadChannelFromWfdisc(List.of((long) WFID_1),
        Optional.of(TagName.ARID),
        Optional.of(ARRIVAL_1.getId()),
        Optional.empty(),
        WFDISC_TEST_DAO_3.getTime(),
        WFDISC_TEST_DAO_3.getEndTime());

    doReturn(Optional.of(SIGNAL_DETECTION_HYPOTHESIS))
      .when(signalDetectionHypothesisConverter).convert(eq(SignalDetectionHypothesisConverterId.from(WORKFLOW_DEFINITION_ID2_NAME,
          SIGNAL_DETECTION_HYPOTHESIS_ID_3.getSignalDetectionId(), Optional.of(SIGNAL_DETECTION_HYPOTHESIS_3.getId().getId()))),
        eq(ARRIVAL_3),
        eq(Optional.empty()),
        any(),
        eq(MONITORING_ORG),
        any(),
        any(),
        any());

    doReturn(Optional.of(SIGNAL_DETECTION_HYPOTHESIS_0))
      .when(signalDetectionHypothesisConverter).convert(eq(SignalDetectionHypothesisConverterId.from(WORKFLOW_DEFINITION_ID1_NAME,
          SIGNAL_DETECTION_HYPOTHESIS_ID.getSignalDetectionId(), Optional.empty())),
        eq(ARRIVAL_1),
        eq(Optional.empty()),
        any(),
        eq(MONITORING_ORG),
        any(),
        any(),
        any());

    List<SignalDetectionHypothesis> signalDetectionHypotheses =
      repository.findHypothesesByIds(List.of(SIGNAL_DETECTION_HYPOTHESIS_ID, SIGNAL_DETECTION_HYPOTHESIS_ID_2));

    assertEquals(2, signalDetectionHypotheses.size());
    signalDetectionHypotheses.forEach(sdh -> assertTrue(expectedValues.contains(sdh)));

    verify(signalDetectionBridgeDefinition, times(5)).getOrderedStages();

    verify(signalDetectionBridgeDefinition, times(2)).getMonitoringOrganization();
    verify(signalDetectionIdUtility)
      .getOrCreateSignalDetectionIdfromArid(ARRIVAL_1.getId());
    verify(signalDetectionIdUtility)
      .getOrCreateSignalDetectionIdfromArid(ARRIVAL_3.getId());

    verify(bridgedChannelRepository).loadChannelFromWfdisc(List.of((long) WFID_3),
      Optional.of(TagName.ARID),
      Optional.of(ARRIVAL_3.getId()),
      Optional.empty(),
      WFDISC_TEST_DAO_3.getTime(),
      WFDISC_TEST_DAO_3.getEndTime());
    verify(channelSegmentDescriptorWfidCache, times(2))
      .put(any(ChannelSegmentDescriptor.class), any(Long.class));
    verify(signalDetectionHypothesisConverter).convert(any(SignalDetectionHypothesisConverterId.class),
      eq(ARRIVAL_3),
      any(),
      any(),
      eq(MONITORING_ORG),
      any(),
      any(),
      any());
    verify(signalDetectionIdUtility, times(2))
      .getAssocIdComponentsFromSignalDetectionHypothesisId(any());

    verifyNoMoreInteractions(signalDetectionBridgeDefinition,
      signalDetectionIdUtility,
      bridgedChannelRepository,
      channelSegmentDescriptorWfidCache);
  }

  @Test
  void testFindHypothesesByIds_arrivalsAndAssocs() {
    initStageTwoConnectorMocks();

    List<SignalDetectionHypothesis> expectedValues = List.of(SIGNAL_DETECTION_HYPOTHESIS_0, SIGNAL_DETECTION_HYPOTHESIS,
      SIGNAL_DETECTION_HYPOTHESIS_2);

    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of(WORKFLOW_DEFINITION_ID1, WORKFLOW_DEFINITION_ID2, WORKFLOW_DEFINITION_ID3));

    when(signalDetectionBridgeDefinition.getDatabaseAccountByStage())
      .thenReturn(dbAccountStageMap);

    var idComponents1 = SignalDetectionHypothesisArrivalIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME,
      ARRIVAL_TEST_1.getId());
    doReturn(idComponents1)
      .when(signalDetectionIdUtility)
      .getArrivalIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID.getId());

    var idComponents2 = SignalDetectionHypothesisArrivalIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME,
      ARRIVAL_TEST_3.getId());
    doReturn(idComponents2)
      .when(signalDetectionIdUtility)
      .getArrivalIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId());

    var assocIdComponents1 = SignalDetectionHypothesisAssocIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME, ASSOC_TEST_1.getId().getArrivalId(), ASSOC_TEST_1.getId().getOriginId());
    doReturn(assocIdComponents1)
      .when(signalDetectionIdUtility)
      .getAssocIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID.getId());

    var assocIdComponents2 = SignalDetectionHypothesisAssocIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME, ASSOC_TEST_3.getId().getArrivalId(), ASSOC_TEST_3.getId().getOriginId());
    doReturn(assocIdComponents2)
      .when(signalDetectionIdUtility)
      .getAssocIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId());

    // ---------------------------------------------------------
    //create arrival current and previous stage connector mock returns
    List<ArrivalDao> currArrivals = List.of(ARRIVAL_TEST_1, ARRIVAL_TEST_3);
    doReturn(currArrivals)
      .when(currArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_TEST_1.getId(), ARRIVAL_TEST_3.getId()));
    List<ArrivalDao> prevArrivals = List.of(ARRIVAL_TEST_1);
    doReturn(prevArrivals)
      .when(prevArrivalDatabaseConnector).findArrivalsByArids(List.of(ARRIVAL_TEST_1.getId(), ARRIVAL_TEST_3.getId()));

    //create assoc current and previous stage connector mock returns
    doReturn(List.of(ASSOC_TEST_1, ASSOC_TEST_3))
      .when(currAssocDatabaseConnector).findAssocsByArids(List.of(ARRIVAL_TEST_1.getId(), ARRIVAL_TEST_3.getId()));

    // find current stage wftags and wfdiscs
    when(wftagDatabaseConnector.findWftagsByTagIds(List.of(ARRIVAL_TEST_1.getId(), ARRIVAL_TEST_3.getId())))
      .thenReturn(List.of());
    when(wfdiscDatabaseConnector.findWfDiscVersionAfterEffectiveTime(anyCollection()))
      .thenReturn(List.of(WFDISC_TEST_DAO_1, WFDISC_TEST_DAO_3));

    // find curr/prev stage amplitudes
    doReturn(List.of(AMPLITUDE_DAO_1))
      .when(amplitudeDatabaseConnector).findAmplitudesByArids(List.of(ARRIVAL_TEST_1.getId(), ARRIVAL_TEST_3.getId()));

    when(currAssocDatabaseConnector
      .findAssocsByAridsAndOrids(List.of(Pair.of(ASSOC_TEST_1.getId().getArrivalId(), ASSOC_TEST_1.getId().getOriginId()),
        Pair.of(ASSOC_TEST_3.getId().getArrivalId(), ASSOC_TEST_3.getId().getOriginId()))))
      .thenReturn(List.of(ASSOC_TEST_1, ASSOC_TEST_3));
    when(prevAssocDatabaseConnector
      .findAssocsByAridsAndOrids(List.of(Pair.of(ASSOC_TEST_1.getId().getArrivalId(), ASSOC_TEST_1.getId().getOriginId()),
        Pair.of(ASSOC_TEST_3.getId().getArrivalId(), ASSOC_TEST_3.getId().getOriginId()))))
      .thenReturn(List.of(ASSOC_TEST_3));
    // ---------------------------------------------------------

    when(signalDetectionBridgeDefinition.getMonitoringOrganization())
      .thenReturn(MONITORING_ORG);

    doReturn(SIGNAL_DETECTION_HYPOTHESIS_ID_3.getSignalDetectionId())
      .when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(ARRIVAL_TEST_3.getId());

    doReturn(SIGNAL_DETECTION_HYPOTHESIS_ID.getSignalDetectionId())
      .when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(ARRIVAL_TEST_1.getId());

    when(signalDetectionIdUtility
      .getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(ARRIVAL_TEST_1.getId(), WORKFLOW_DEFINITION_ID1_NAME))
      .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_ID.getId());

    when(signalDetectionIdUtility
      .getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(ASSOC_TEST_3.getId().getArrivalId(),
        ASSOC_TEST_3.getId().getOriginId(), WORKFLOW_DEFINITION_ID1_NAME))
      .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId());

    doReturn(CHANNEL)
      .when(bridgedChannelRepository).loadChannelFromWfdisc(List.of((long) WFID_3),
        Optional.of(TagName.ARID),
        Optional.of(ARRIVAL_TEST_3.getId()),
        Optional.empty(),
        WFDISC_TEST_DAO_3.getTime(),
        WFDISC_TEST_DAO_3.getEndTime());

    doReturn(CHANNEL_TWO)
      .when(bridgedChannelRepository).loadChannelFromWfdisc(List.of((long) WFID_1),
        Optional.of(TagName.ARID),
        Optional.of(ARRIVAL_TEST_1.getId()),
        Optional.empty(),
        WFDISC_TEST_DAO_3.getTime(),
        WFDISC_TEST_DAO_3.getEndTime());

    doReturn(Optional.of(SIGNAL_DETECTION_HYPOTHESIS_0))
      .when(signalDetectionHypothesisConverter).convert(eq(SignalDetectionHypothesisConverterId.from(
          WORKFLOW_DEFINITION_ID2_NAME,
          SIGNAL_DETECTION_HYPOTHESIS_ID_3.getSignalDetectionId(), Optional.of(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId()))),
        eq(ARRIVAL_TEST_3),
        eq(Optional.of(ASSOC_TEST_3)),
        any(),
        eq(MONITORING_ORG),
        any(),
        any(),
        any());

    doReturn(Optional.of(SIGNAL_DETECTION_HYPOTHESIS))
      .when(signalDetectionHypothesisConverter).convert(eq(SignalDetectionHypothesisConverterId.from(
          WORKFLOW_DEFINITION_ID2_NAME,
          SIGNAL_DETECTION_HYPOTHESIS_ID_3.getSignalDetectionId(), Optional.empty())),
        eq(ARRIVAL_TEST_3),
        eq(Optional.empty()),
        any(),
        eq(MONITORING_ORG),
        any(),
        any(),
        any());

    doReturn(Optional.of(SIGNAL_DETECTION_HYPOTHESIS_2))
      .when(signalDetectionHypothesisConverter).convert(eq(SignalDetectionHypothesisConverterId.from(
          WORKFLOW_DEFINITION_ID2_NAME,
          SIGNAL_DETECTION_HYPOTHESIS_ID.getSignalDetectionId(), Optional.of(SIGNAL_DETECTION_HYPOTHESIS_ID.getId()))),
        eq(ARRIVAL_TEST_1),
        eq(Optional.of(ASSOC_TEST_1)),
        any(),
        eq(MONITORING_ORG),
        any(),
        any(),
        any());

    List<SignalDetectionHypothesis> signalDetectionHypotheses = repository.findHypothesesByIds(List.of(
      SIGNAL_DETECTION_HYPOTHESIS_ID, SIGNAL_DETECTION_HYPOTHESIS_ID_2));

    assertEquals(3, signalDetectionHypotheses.size());
    signalDetectionHypotheses.forEach(sdh -> assertTrue(expectedValues.contains(sdh)));

    verify(bridgedChannelRepository, times(2)).loadChannelFromWfdisc(List.of((long) WFID_3),
      Optional.of(TagName.ARID),
      Optional.of(ARRIVAL_TEST_3.getId()),
      Optional.empty(),
      WFDISC_TEST_DAO_3.getTime(),
      WFDISC_TEST_DAO_3.getEndTime());

    verify(bridgedChannelRepository).loadChannelFromWfdisc(List.of((long) WFID_1),
      Optional.of(TagName.ARID),
      Optional.of(ARRIVAL_TEST_1.getId()),
      Optional.empty(),
      WFDISC_TEST_DAO_3.getTime(),
      WFDISC_TEST_DAO_3.getEndTime());
  }

  @Test
  void testFindHypothesesByIds_arrivalsAndAssocs_missingStageAccount() {

    when(signalDetectionBridgeDefinition.getDatabaseAccountByStage())
      .thenReturn(dbAccountMissingStageMap);

    var idComponents1 = SignalDetectionHypothesisArrivalIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME,
      ARRIVAL_TEST_1.getId());
    doReturn(idComponents1)
      .when(signalDetectionIdUtility)
      .getArrivalIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID.getId());

    var idComponents2 = SignalDetectionHypothesisArrivalIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME,
      ARRIVAL_TEST_3.getId());
    doReturn(idComponents2)
      .when(signalDetectionIdUtility)
      .getArrivalIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId());

    var assocIdComponents1 = SignalDetectionHypothesisAssocIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME, ASSOC_TEST_1.getId().getArrivalId(), ASSOC_TEST_1.getId().getOriginId());
    doReturn(assocIdComponents1)
      .when(signalDetectionIdUtility)
      .getAssocIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID.getId());

    var assocIdComponents2 = SignalDetectionHypothesisAssocIdComponents.create(
      WORKFLOW_DEFINITION_ID2_NAME, ASSOC_TEST_3.getId().getArrivalId(), ASSOC_TEST_3.getId().getOriginId());
    doReturn(assocIdComponents2)
      .when(signalDetectionIdUtility)
      .getAssocIdComponentsFromSignalDetectionHypothesisId(SIGNAL_DETECTION_HYPOTHESIS_ID_2.getId());

    List<SignalDetectionHypothesis> signalDetectionHypotheses = repository.findHypothesesByIds(List.of(
      SIGNAL_DETECTION_HYPOTHESIS_ID, SIGNAL_DETECTION_HYPOTHESIS_ID_2));

    assertEquals(0, signalDetectionHypotheses.size());
  }

  @Test
  void testFindByStationsAndTime_missingStage() {
    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of(WORKFLOW_DEFINITION_ID1));

    assertEquals(0, repository.findByStationsAndTime(List.of(STATION),
      START_TIME, END_TIME, WORKFLOW_DEFINITION_ID2, List.of(SIGNAL_DETECTION)).size());

  }

  @ParameterizedTest
  @MethodSource("getFindByStationsAndTime")
  void testFindByStationsAndTime(List<SignalDetection> expectedValues,
    Consumer<SignalDetectionIdUtility> setupMocks,
    Consumer<SignalDetectionIdUtility> verifyMocks) {

    // current stage connector mocks
    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        AMPLITUDE_CONNECTOR_TYPE);
    doReturn(currAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ASSOC_CONNECTOR_TYPE);

    // previous stage connectors exist
    lenient().doReturn(true)
      .when(signalDetectionBridgeDatabaseConnectors).connectorExistsForPreviousStage(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);

    // initialize the arrival db connectors (others aren't used)
    doReturn(currArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);
    doReturn(prevArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);

    when(signalDetectionBridgeDefinition.getOrderedStages())
      .thenReturn(ImmutableList.of(WORKFLOW_DEFINITION_ID1, WORKFLOW_DEFINITION_ID2));

    when(prevArrivalDatabaseConnector.findArrivalsByArids(List.of(ARRIVAL_1.getId(), ARRIVAL_3.getId())))
      .thenReturn(List.of());
    when(currArrivalDatabaseConnector.findArrivals(List.of(STATION.getName()), List.of(ARRIVAL_1.getId()),
      START_TIME, END_TIME, MEASURED_WAVEFORM_LEAD_DURATION, MEASURED_WAVEFORM_LAG_DURATION))
      .thenReturn(List.of(ARRIVAL_1, ARRIVAL_3));

    when(signalDetectionBridgeDefinition.getMonitoringOrganization())
      .thenReturn(MONITORING_ORG);
    when(signalDetectionBridgeDefinition.getMeasuredWaveformLeadDuration())
      .thenReturn(MEASURED_WAVEFORM_LEAD_DURATION);
    when(signalDetectionBridgeDefinition.getMeasuredWaveformLagDuration())
      .thenReturn(MEASURED_WAVEFORM_LAG_DURATION);

    when(signalDetectionConverter.convert(any()))
      .thenReturn(Optional.of(SIGNAL_DETECTION_3));
    when(siteDatabaseConnector.findSitesByReferenceStationAndTimeRange(any(), any(), any())).thenReturn(List.of(getSiteForStation()));

    setupMocks.accept(signalDetectionIdUtility);
    List<SignalDetection> signalDetections = repository.findByStationsAndTime(List.of(STATION),
      START_TIME, END_TIME, WORKFLOW_DEFINITION_ID2, List.of(SIGNAL_DETECTION));
    assertTrue(signalDetections.size() > 0);
    signalDetections.forEach(sd -> assertTrue(expectedValues.contains(sd)));
    verifyMocks.accept(signalDetectionIdUtility);
  }

  /**
   * Initialize current stage db connectors for arrival and assoc
   */
  private void initCurrentConnectorMocks() {
    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        AMPLITUDE_CONNECTOR_TYPE);
    doReturn(currArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        ARRIVAL_CONNECTOR_TYPE);
    doReturn(currAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        ASSOC_CONNECTOR_TYPE);
  }

  /**
   * Initialize individual stage db connectors in the main signal detection db connectors
   */
  private void initConnectorMocks() {

    // current stage connector mocks
    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        AMPLITUDE_CONNECTOR_TYPE);
    doReturn(currArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);
    doReturn(currAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ASSOC_CONNECTOR_TYPE);

    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        AMPLITUDE_CONNECTOR_TYPE);

    // previous stage connector mocks
    doReturn(prevArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);
    doReturn(prevAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ASSOC_CONNECTOR_TYPE);

    // previous stage connectors exist
    lenient().doReturn(true)
      .when(signalDetectionBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(WORKFLOW_DEFINITION_ID2_NAME), any());
  }

  /**
   * Initialize multi-stage db connectors in the main signal detection db connectors
   */
  private void initMultiStageConnectorMocks() {

    // current stage connector mocks
    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        AMPLITUDE_CONNECTOR_TYPE);
    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        AMPLITUDE_CONNECTOR_TYPE);

    doReturn(currArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        ARRIVAL_CONNECTOR_TYPE);
    doReturn(currArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);

    doReturn(currAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID1_NAME,
        ASSOC_CONNECTOR_TYPE);
    doReturn(currAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ASSOC_CONNECTOR_TYPE);

    // previous stage connector mocks
    doReturn(prevArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);

    // previous stage connectors exist
    lenient().doReturn(true)
      .when(signalDetectionBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(WORKFLOW_DEFINITION_ID2_NAME), any());
    lenient().doReturn(true)
      .when(signalDetectionBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(WORKFLOW_DEFINITION_ID3_NAME), any());
  }

  /**
   * Initialize two-stage db connectors in the main signal detection db connectors
   */
  private void initStageTwoConnectorMocks() {

    // current stage connector mocks
    doReturn(amplitudeDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        AMPLITUDE_CONNECTOR_TYPE);

    doReturn(currArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);

    doReturn(currAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForCurrentStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ASSOC_CONNECTOR_TYPE);

    // previous stage connector mocks
    doReturn(prevArrivalDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ARRIVAL_CONNECTOR_TYPE);

    doReturn(prevAssocDatabaseConnector)
      .when(signalDetectionBridgeDatabaseConnectors).getConnectorForPreviousStageOrThrow(WORKFLOW_DEFINITION_ID2_NAME,
        ASSOC_CONNECTOR_TYPE);

    // previous stage connectors exist
    lenient().doReturn(true)
      .when(signalDetectionBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(WORKFLOW_DEFINITION_ID2_NAME), any());
    lenient().doReturn(true)
      .when(signalDetectionBridgeDatabaseConnectors).connectorExistsForPreviousStage(eq(WORKFLOW_DEFINITION_ID3_NAME), any());
  }

  // Create find signal detection by ids arguments
  static Stream<Arguments> getFindByIdsArguments() {
    List<SignalDetection> expectedValues = List.of(SIGNAL_DETECTION, SIGNAL_DETECTION_3);
    Consumer<SignalDetectionIdUtility> twoAridSetup = sdUtil -> {
      when(sdUtil.getAridForSignalDetectionUUID(SIGNAL_DETECTION_ID))
        .thenReturn(ARRIVAL_1.getId());
      when(sdUtil.getAridForSignalDetectionUUID(SIGNAL_DETECTION_ID_3))
        .thenReturn(ARRIVAL_3.getId());
    };
    Consumer<SignalDetectionIdUtility> twoAridVerification = sdUtil -> {
      verify(sdUtil).getAridForSignalDetectionUUID(SIGNAL_DETECTION_ID);
      verify(sdUtil).getAridForSignalDetectionUUID(SIGNAL_DETECTION_ID_3);
      verifyNoMoreInteractions(sdUtil);
    };

    return Stream.of(
      arguments(expectedValues, twoAridSetup, twoAridVerification)
    );
  }

  // Create find signal detections by stations and time
  static Stream<Arguments> getFindByStationsAndTime() {
    List<SignalDetection> expectedValues = List.of(SIGNAL_DETECTION_3);
    Consumer<SignalDetectionIdUtility> aridSetup = sdUtil -> {
      when(sdUtil.getAridForSignalDetectionUUID(SIGNAL_DETECTION_ID))
        .thenReturn(ARRIVAL_1.getId());
    };
    Consumer<SignalDetectionIdUtility> aridVerification = sdUtil -> {
      verify(sdUtil).getAridForSignalDetectionUUID(SIGNAL_DETECTION_ID);
      verifyNoMoreInteractions(sdUtil);
    };

    return Stream.of(
      arguments(expectedValues, aridSetup, aridVerification)
    );
  }
}

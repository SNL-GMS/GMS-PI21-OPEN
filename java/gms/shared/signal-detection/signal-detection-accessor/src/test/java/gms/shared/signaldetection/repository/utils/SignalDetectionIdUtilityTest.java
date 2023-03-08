package gms.shared.signaldetection.repository.utils;

import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypesChecking;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility.ARID_SIGNAL_DETECTION_ID_CACHE;
import static gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility.ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID;
import static gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility.ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID;
import static gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility.SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID;
import static gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility.SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID;
import static gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility.SIGNAL_DETECTION_ID_ARID_CACHE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SignalDetectionIdUtilityTest {

  @Mock
  IgniteCache<Long, UUID> aridSignalDetectionMap;
  @Mock
  IgniteCache<UUID, Long> signalDetectionAridMap;
  @Mock
  IgniteCache<SignalDetectionHypothesisArrivalIdComponents, UUID> arrivalIdComponentsSignalDetectionHypothesisIdMap;
  @Mock
  IgniteCache<UUID, SignalDetectionHypothesisArrivalIdComponents> signalDetectionHypothesisIdArrivalIdComponentsMap;
  @Mock
  IgniteCache<SignalDetectionHypothesisAssocIdComponents, UUID> assocIdComponentsSignalDetectionHypothesisIdMap;
  @Mock
  IgniteCache<UUID, SignalDetectionHypothesisAssocIdComponents> signalDetectionHypothesisIdAssocIdComponentsMap;
  @Mock
  IgniteCache<AmplitudeIdComponents, FeatureMeasurementIdComponents> amplitudeIdComponentsFeatureMeasurementIdComponentsMap;
  @Mock
  IgniteCache<FeatureMeasurementIdComponents, AmplitudeIdComponents> featureMeasurementIdComponentsAmplitudeIdComponentsMap;

  @Mock
  SystemConfig systemConfig;

  private static SignalDetectionIdUtility signalDetectionIdUtility;

  private static final long ARID_1 = 45L;
  private static final long ARID_2 = 932L;
  private static final long ORID_1 = 1L;

  @BeforeEach
  void setup() {
    signalDetectionIdUtility = new SignalDetectionIdUtility(aridSignalDetectionMap, signalDetectionAridMap,
      arrivalIdComponentsSignalDetectionHypothesisIdMap, signalDetectionHypothesisIdArrivalIdComponentsMap,
      assocIdComponentsSignalDetectionHypothesisIdMap, signalDetectionHypothesisIdAssocIdComponentsMap,
      amplitudeIdComponentsFeatureMeasurementIdComponentsMap, featureMeasurementIdComponentsAmplitudeIdComponentsMap);
  }

  @Test
  void testCreate() {


    CacheInfo signalDetectionIdAridCache =
      new CacheInfo("signal-detection-id-arid-cache",
        CacheMode.LOCAL, CacheAtomicityMode.ATOMIC, true, Optional.empty());
    CacheInfo arrivalIdSignalDetectionHypothesisId =
      new CacheInfo("arrival-id-signal-detection-hypothesis-id", CacheMode.LOCAL,
        CacheAtomicityMode.ATOMIC, true, Optional.empty());
    CacheInfo signalDetectionHypothesisIdArrivalId =
      new CacheInfo("signal-detection-hypothesis-id-arrival-id-cache", CacheMode.LOCAL,
        CacheAtomicityMode.ATOMIC, true, Optional.empty());
    CacheInfo assocIdSignalDetectionHypothesisId =
      new CacheInfo("assoc-id-signal-detection-hypothesis-id", CacheMode.LOCAL,
        CacheAtomicityMode.ATOMIC, true, Optional.empty());
    CacheInfo signalDetectionHypothesisIdAssocId =
      new CacheInfo("signal-detection-hypothesis-id-assoc-id-cache", CacheMode.LOCAL,
        CacheAtomicityMode.ATOMIC, true, Optional.empty());

    try (MockedStatic<IgniteConnectionManager> managerMockedStatic = mockStatic(IgniteConnectionManager.class)) {

      MockedStatic<SystemConfig> systemConfigMockedStatic = mockStatic(SystemConfig.class);

      systemConfigMockedStatic.when(() -> SystemConfig.create(any())).thenReturn(systemConfig);

      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(ARID_SIGNAL_DETECTION_ID_CACHE))
        .thenReturn(aridSignalDetectionMap);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(SIGNAL_DETECTION_ID_ARID_CACHE))
        .thenReturn(signalDetectionAridMap);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(ARRIVAL_ID_SIGNAL_DETECTION_HYPOTHESIS_ID))
        .thenReturn(arrivalIdComponentsSignalDetectionHypothesisIdMap);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(SIGNAL_DETECTION_HYPOTHESIS_ID_ARRIVAL_ID))
        .thenReturn(signalDetectionHypothesisIdArrivalIdComponentsMap);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(ASSOC_ID_SIGNAL_DETECTION_HYPOTHESIS_ID))
        .thenReturn(assocIdComponentsSignalDetectionHypothesisIdMap);
      managerMockedStatic.when(() -> IgniteConnectionManager.getOrCreateCache(SIGNAL_DETECTION_HYPOTHESIS_ID_ASSOC_ID))
        .thenReturn(signalDetectionHypothesisIdAssocIdComponentsMap);

      assertNotNull(new SignalDetectionIdUtility());
    }
  }

  @Test
  void getSignalDetectionId() {


    given(aridSignalDetectionMap.get(ARID_1)).willReturn(null);
    assertNull(signalDetectionIdUtility.getSignalDetectionForArid(ARID_1));
    assertNotNull(signalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(ARID_1));

    UUID uuid = UUID.randomUUID();
    given(aridSignalDetectionMap.get(ARID_2)).willReturn(uuid);
    given(signalDetectionAridMap.get(uuid)).willReturn(ARID_2);
    assertEquals(ARID_2, signalDetectionIdUtility.getAridForSignalDetectionUUID(uuid));
    assertEquals(uuid, signalDetectionIdUtility.getSignalDetectionForArid(ARID_2));
    assertEquals(uuid, signalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(ARID_2));
  }

  @ParameterizedTest
  @MethodSource("addAridAndStageIdForSignalDetectionHypothesisUUIDArguments")
  void testCreateValidation(Class<? extends Exception> exception, long arid, WorkflowDefinitionId stageId, UUID uuid) {
    assertThrows(exception, () -> signalDetectionIdUtility.addAridAndStageIdForSignalDetectionHypothesisUUID(arid,
      stageId.getName(), uuid));
  }

  static Stream<Arguments> addAridAndStageIdForSignalDetectionHypothesisUUIDArguments() {
    final UUID uuid = UUID.randomUUID();

    return Stream.of(
      arguments(NullPointerException.class, 1L, null, uuid),
      arguments(NullPointerException.class, 1L, WorkflowDefinitionId.from("test"), null)
    );
  }

  @Test
  void getSignalDetectionIdWithAridStageId() {

    var workFlowId = WorkflowDefinitionId.from("workflow id");
    var id = SignalDetectionHypothesisArrivalIdComponents.create(
      workFlowId.getName(), ARID_1);
    var uuid = UUID.nameUUIDFromBytes((Long.toString(ARID_1) + workFlowId).getBytes());

    given(signalDetectionHypothesisIdArrivalIdComponentsMap.get(uuid)).willReturn(null);
    assertNull(signalDetectionIdUtility.getArrivalIdComponentsFromSignalDetectionHypothesisId(uuid));
    assertNotNull(signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(ARID_1,
      workFlowId.getName()));

    given(arrivalIdComponentsSignalDetectionHypothesisIdMap.get(id)).willReturn(uuid);
    assertEquals(uuid, signalDetectionIdUtility.getSignalDetectionHypothesisIdForAridAndStageId(ARID_1,
      workFlowId.getName()));
    assertEquals(uuid, signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(ARID_1,
      workFlowId.getName()));
  }

  @Test
  void getSignalDetectionIdWithAridOridStageId() {

    var workFlowId = WorkflowDefinitionId.from("workflow id");
    var id = SignalDetectionHypothesisAssocIdComponents.create(
      workFlowId.getName(), ARID_1, ORID_1);
    var uuid = UUID.nameUUIDFromBytes((Long.toString(ARID_1) + ORID_1 + workFlowId).getBytes());

    given(signalDetectionHypothesisIdAssocIdComponentsMap.get(uuid)).willReturn(null);
    assertNull(signalDetectionIdUtility.getAssocIdComponentsFromSignalDetectionHypothesisId(uuid));
    assertNotNull(signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(ARID_1, ORID_1,
      workFlowId.getName()));

    given(assocIdComponentsSignalDetectionHypothesisIdMap.get(id)).willReturn(uuid);
    assertEquals(uuid, signalDetectionIdUtility.getSignalDetectionHypothesisIdForAridOridAndStageId(ARID_1, ORID_1,
      workFlowId.getName()));
    assertEquals(uuid, signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(ARID_1,
      ORID_1, workFlowId.getName()));
  }

  @Test
  void testaddAridForSignalDetectionUUID() {

    var uuid = UUID.nameUUIDFromBytes(Long.toString(ARID_1).getBytes());

    assertDoesNotThrow(() -> signalDetectionIdUtility.addAridForSignalDetectionUUID(ARID_1, uuid));
  }

  @Test
  void testaddAridAndStageIdForSignalDetectionHypothesisUUID() {

    var workFlowId = WorkflowDefinitionId.from("workflow id");
    var uuid = UUID.nameUUIDFromBytes((Long.toString(ARID_1) + workFlowId).getBytes());

    assertDoesNotThrow(() -> signalDetectionIdUtility.addAridAndStageIdForSignalDetectionHypothesisUUID(ARID_1,
      workFlowId.getName(), uuid));
  }

  @Test
  void testaddAridAndOridAndStageIdForSignalDetectionHypothesisUUID() {

    var workFlowId = WorkflowDefinitionId.from("workflow id");
    var uuid = UUID.nameUUIDFromBytes((Long.toString(ARID_1) + ORID_1 + workFlowId).getBytes());

    assertDoesNotThrow(() -> signalDetectionIdUtility.addAridAndOridAndStageIdForSignalDetectionHypothesisUUID(ARID_1,
      ORID_1, workFlowId.getName(), uuid));
  }

  @Test
  void testAddAmpidandStageIdForSignalDetectionHypothesisUUIDAndFeatureMeasurementType() {

    long ampid = 1L;
    var workflowId = WorkflowDefinitionId.from("test id");
    var uuid = UUID.randomUUID();
    var featureMeasurementType = FeatureMeasurementTypesChecking
      .featureMeasurementTypeFromMeasurementTypeString("ARRIVAL_TIME");

    assertDoesNotThrow(() -> signalDetectionIdUtility
      .addAmpidAndStageIdForSignalDetectionHypothesisUUIDAndFeatureMeasurementType(ampid, workflowId.getName(), uuid, featureMeasurementType));

    assertDoesNotThrow(() -> signalDetectionIdUtility
      .getAmplitudeIdComponentsFromSignalDetectionHypothesisIdAndFeatureMeasurementType(uuid, featureMeasurementType));

    assertDoesNotThrow(() -> signalDetectionIdUtility
      .getFeatureMeasurementIdComponentsFromAmpidAndStageId(ampid, workflowId.getName()));
  }

  @ParameterizedTest
  @MethodSource("addAridAndOridAndStageIdForSignalDetectionHypothesisUUIDArguments")
  void testAddValidation(Class<? extends Exception> exception, long arid, long orid, WorkflowDefinitionId stageId,
    UUID uuid) {
    assertThrows(exception, () -> signalDetectionIdUtility.addAridAndOridAndStageIdForSignalDetectionHypothesisUUID(
      arid, orid, stageId.getName(), uuid));
  }

  static Stream<Arguments> addAridAndOridAndStageIdForSignalDetectionHypothesisUUIDArguments() {
    final UUID uuid = UUID.randomUUID();

    return Stream.of(
      arguments(NullPointerException.class, 1L, 1L, null, uuid),
      arguments(NullPointerException.class, 1L, 1L, WorkflowDefinitionId.from("test"), null)
    );
  }

}

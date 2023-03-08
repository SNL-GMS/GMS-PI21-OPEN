package gms.shared.event.repository;

import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisAssocIdComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SignalDetectionLegacyAccessorTest {

  @Mock
  private SignalDetectionIdUtility signalDetectionIdUtility;

  @Mock
  private SignalDetectionAccessorInterface signalDetectionAccessor;

  private SignalDetectionLegacyAccessor signalDetectionLegacyAccessor;

  @BeforeEach
  void init() {
    signalDetectionLegacyAccessor = new SignalDetectionLegacyAccessor(signalDetectionIdUtility, signalDetectionAccessor);
  }

  @Test
  void testFindHypothesisByStageIdAndArid() {

    var stageId = WorkflowDefinitionId.from("Auto Network");
    var arid = 0L;
    var orid = 0L;
    var signalDetectionHypothesisId = SignalDetectionHypothesisId.from(UUID.randomUUID(), UUID.randomUUID());

    var signalDetectionHypothesis = mock(SignalDetectionHypothesis.class);
    doReturn(signalDetectionHypothesisId).when(signalDetectionHypothesis).getId();

    doReturn(signalDetectionHypothesisId.getSignalDetectionId()).when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(arid);
    doReturn(signalDetectionHypothesisId.getId()).when(signalDetectionIdUtility).getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, "soccpro");

    doReturn(List.of(signalDetectionHypothesis)).when(signalDetectionAccessor).findHypothesesByIds(List.of(signalDetectionHypothesisId));

    var retrievedSignalDetectionHypothesis = signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageId, arid, orid);

    Assertions.assertEquals(Optional.of(signalDetectionHypothesis), retrievedSignalDetectionHypothesis);
  }

  @Test
  void testFindHypothesisByStageIdAndArid_NoHypothesesFound() {

    var arid = 0;
    var orid = 0;
    var stageId = WorkflowDefinitionId.from("AL1");

    doReturn(UUID.randomUUID()).when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(arid);
    doReturn(UUID.randomUUID()).when(signalDetectionIdUtility).getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, "al1");

    doReturn(
      List.of()
    ).when(signalDetectionAccessor).findHypothesesByIds(any());

    assertEquals(Optional.empty(), signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageId, arid, orid));
  }

  @Test
  void testFindHypothesisByStageIdAndArid_MultipleHypothesesFound() {

    var arid = 0;
    var orid = 0;
    var stageId = WorkflowDefinitionId.from("AL2");
    
    var signalDetectionHypothesisId = SignalDetectionHypothesisId.from(UUID.randomUUID(), UUID.randomUUID());
    var signalDetectionHypothesis = mock(SignalDetectionHypothesis.class);
    doReturn(signalDetectionHypothesisId).when(signalDetectionHypothesis).getId();

    doReturn(signalDetectionHypothesisId.getSignalDetectionId()).when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(arid);
    doReturn(signalDetectionHypothesisId.getId()).when(signalDetectionIdUtility).getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, "al2");

    doReturn(
      List.of(signalDetectionHypothesis, signalDetectionHypothesis)
    ).when(signalDetectionAccessor).findHypothesesByIds(any());

    assertThrows(IllegalStateException.class, () ->
      signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageId, arid, orid));
  }

  @Test
  void testFindHypothesisByStageIdAndArid_NullArgs() {

    assertThrows(NullPointerException.class, () -> new SignalDetectionLegacyAccessor(null, signalDetectionAccessor));
    assertThrows(NullPointerException.class, () -> new SignalDetectionLegacyAccessor(signalDetectionIdUtility, null));
    assertThrows(NullPointerException.class, () -> signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(null, 0L, 0L));
  }

  @Test
  void testFindHypothesisByStageIdAndArid_AccessorThrowsException() {

    var arid = 0;
    var orid = 0;
    var stageId = WorkflowDefinitionId.from("AL1");

    doReturn(UUID.randomUUID()).when(signalDetectionIdUtility).getOrCreateSignalDetectionIdfromArid(arid);
    doReturn(UUID.randomUUID()).when(signalDetectionIdUtility).getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, "al1");

    doThrow(IllegalStateException.class).when(signalDetectionAccessor).findHypothesesByIds(any());
    var sdhOpt = signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageId, arid, orid);
    assertTrue(sdhOpt.isEmpty());
  }

  @Test
  void testGetSignalDetectionHypothesesAssocIdComponents() {
    var sdh = SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;
    var sdhId = sdh.getId().getId();

    var legacyDatabaseId = "AL1";
    var arid = 1L;
    var orid = 1L;
    var components = SignalDetectionHypothesisAssocIdComponents
      .create(legacyDatabaseId, orid, arid);

    doReturn(components).when(signalDetectionIdUtility).getAssocIdComponentsFromSignalDetectionHypothesisId(sdhId);

    var result = signalDetectionLegacyAccessor
      .getSignalDetectionHypothesesAssocIdComponents(List.of(sdh));

    assertEquals(Set.of(components), result);
  }

  @Test
  void testGetSignalDetectionHypothesesAssocIdComponents_Empty() {
    var sdh = SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;

    var result = signalDetectionLegacyAccessor
      .getSignalDetectionHypothesesAssocIdComponents(List.of(sdh));

    assertEquals(Set.of(), result);
  }

  @Test
  void testLegacyDatabaseAccountToStageId() {
    assertEquals(WorkflowDefinitionId.from("Auto Network"), SignalDetectionLegacyAccessor.legacyDatabaseAccountToStageId("soccpro"));
    assertEquals(WorkflowDefinitionId.from("AL1"), SignalDetectionLegacyAccessor.legacyDatabaseAccountToStageId("al1"));
    assertEquals(WorkflowDefinitionId.from("AL2"), SignalDetectionLegacyAccessor.legacyDatabaseAccountToStageId("al2"));
  }

}
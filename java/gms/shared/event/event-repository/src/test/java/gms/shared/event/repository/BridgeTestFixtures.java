package gms.shared.event.repository;

import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.OriginIdArrivalIdKey;
import gms.shared.event.dao.StaMagDao;
import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class BridgeTestFixtures {

  private static EventBridgeDefinition eventBridgeDefinition = EventBridgeDefinition.builder()
    .setDatabaseUrlByStage(Map.of())
    .setPreviousDatabaseUrlByStage(Map.of())
    .setMonitoringOrganization("Monitoring Org")
    .setOrderedStages(List.of(WorkflowDefinitionId.from("STAGE_ONE"), WorkflowDefinitionId.from("STAGE_TWO"), WorkflowDefinitionId.from("STAGE_Three")))
    .build();

  public static BridgedEhInformation DEFAULT_BRIDGED_EH_INFORMATION = BridgedEhInformation.builder()
    .setEventStages(new EventStages(eventBridgeDefinition))
    .setOriginDao(EventTestFixtures.DEFAULT_ORIGIN_DAO)
    .setOrigerrDao(EventTestFixtures.DEFAULT_ORIGERR_DAO)
    .setEventControlDao(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO)
    .setNetMagDaos(Set.of(EventTestFixtures.DEFAULT_NET_MAG_DAO))
    .setParentEventHypotheses(Set.of())
    .build();

  public static BridgedSdhInformation DEFAULT_BRIDGED_SDH_INFORMATION = BridgedSdhInformation.builder()
    .setSignalDetectionHypothesis(SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_WITH_AMPLITUDE)
    .setAssocDao(EventTestFixtures.DEFAULT_ASSOC_DAO)
    .setArInfoDao(EventTestFixtures.DEFAULT_AR_INFO_DAO)
    .setStaMagDaos(Set.of(EventTestFixtures.DEFAULT_STA_MAG_DAO))
    .build();

  public static BridgedSdhInformation withIds(BridgedSdhInformation sdhInfo, long arrivalId, long originId) {
    var assocDao = sdhInfo.getAssocDao();
    var arInfoDao = sdhInfo.getArInfoDao().get();
    var staMagDaos = sdhInfo.getStaMagDaos();

    return sdhInfo.toBuilder()
      .setAssocDao(AssocDao.Builder.initializeFromInstance(assocDao)
        .withId(new AridOridKey.Builder()
          .withArrivalId(arrivalId)
          .withOriginId(originId)
          .build())
        .build())
      .setArInfoDao(ArInfoDao.Builder.initializeFromInstance(arInfoDao)
        .withOriginIdArrivalIdKey(new OriginIdArrivalIdKey.Builder()
          .withArrivalId(arrivalId)
          .withOriginId(originId)
          .build())
        .build())
      .setStaMagDaos(staMagDaos.stream().map(staMagDao -> StaMagDao.Builder.initializeFromInstance(staMagDao)
          .withArrivalId(arrivalId)
          .withOriginId(originId)
          .build())
        .collect(toSet()))
      .build();
  }
}

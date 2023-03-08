package gms.shared.event.repository;

import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.EventIdOriginIdKey;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

class BridgedEhInformationTest {


  private EventBridgeDefinition eventBridgeDefinition = EventBridgeDefinition.builder()
    .setDatabaseUrlByStage(Map.of())
    .setPreviousDatabaseUrlByStage(Map.of())
    .setMonitoringOrganization("Monitoring Org")
    .setOrderedStages(List.of(WorkflowDefinitionId.from("STAGE_ONE"), WorkflowDefinitionId.from("STAGE_TWO"), WorkflowDefinitionId.from("STAGE_Three")))
    .build();

  @Test
  void testBridgedEhInfoBuilder() {

    Assertions.assertDoesNotThrow(() -> BridgedEhInformation.builder()
      .setEventStages(new EventStages(eventBridgeDefinition))
      .setOriginDao(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .setOrigerrDao(EventTestFixtures.DEFAULT_ORIGERR_DAO)
      .setEventControlDao(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO)
      .setGaTagDao(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .setNetMagDaos(Set.of(EventTestFixtures.DEFAULT_NET_MAG_DAO))
      .setParentEventHypotheses(Set.of())
      .build());
  }

  @Test
  void testBridgedEhInfoBuilder_WrongNetMag() {

    var wrongNetMag = NetMagDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_NET_MAG_DAO)
      .withOriginId(EventTestFixtures.DEFAULT_NET_MAG_DAO.getOriginId() + 1)
      .build();
    var bridgeBuilder = BridgedEhInformation.builder()
      .setEventStages(new EventStages(eventBridgeDefinition))
      .setOriginDao(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .setOrigerrDao(EventTestFixtures.DEFAULT_ORIGERR_DAO)
      .setEventControlDao(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO)
      .setGaTagDao(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .setNetMagDaos(Set.of(wrongNetMag))
      .setParentEventHypotheses(Set.of());
    Assertions.assertThrows(IllegalStateException.class, bridgeBuilder::build);
  }

  @Test
  void testBridgedEhInfoBuilder_WrongNEventControlData() {

    var wrongEventControlDao = EventControlDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO)
      .withEventIdOriginIdKey(
        new EventIdOriginIdKey.Builder()
          .withOriginId(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO.getOriginId() + 1)
          .withEventId(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO.getEventId())
          .build()
      )
      .build();
    var bridgeBuilder = BridgedEhInformation.builder()
      .setEventStages(new EventStages(eventBridgeDefinition))
      .setOriginDao(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .setOrigerrDao(EventTestFixtures.DEFAULT_ORIGERR_DAO)
      .setEventControlDao(wrongEventControlDao)
      .setGaTagDao(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .setNetMagDaos(Set.of(EventTestFixtures.DEFAULT_NET_MAG_DAO))
      .setParentEventHypotheses(Set.of());
    Assertions.assertThrows(IllegalStateException.class, bridgeBuilder::build);
  }

  @Test
  void testBridgedEhInfoBuilder_WrongOrigErr() {

    var wrongOrigErr = OrigerrDao.Builder.initializeFromInstance(EventTestFixtures.DEFAULT_ORIGERR_DAO)
      .withOriginId(EventTestFixtures.DEFAULT_ORIGIN_DAO.getOriginId() + 1)
      .build();
    var bridgeBuilder = BridgedEhInformation.builder()
      .setEventStages(new EventStages(eventBridgeDefinition))
      .setOriginDao(EventTestFixtures.DEFAULT_ORIGIN_DAO)
      .setOrigerrDao(wrongOrigErr)
      .setEventControlDao(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO)
      .setGaTagDao(EventTestFixtures.DEFAULT_REJECTED_GATAG_DAO)
      .setNetMagDaos(Set.of(EventTestFixtures.DEFAULT_NET_MAG_DAO))
      .setParentEventHypotheses(Set.of());
    Assertions.assertThrows(IllegalStateException.class, bridgeBuilder::build);
  }
}

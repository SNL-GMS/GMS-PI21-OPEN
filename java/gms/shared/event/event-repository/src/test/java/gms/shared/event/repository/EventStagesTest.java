package gms.shared.event.repository;

import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventStagesTest {

  private final WorkflowDefinitionId stage1 = WorkflowDefinitionId.from("TestStage1");
  private final WorkflowDefinitionId stage2 = WorkflowDefinitionId.from("TestStage2");
  private final WorkflowDefinitionId stage3 = WorkflowDefinitionId.from("TestStage3");
  private final List<WorkflowDefinitionId> stageList = List.of(stage1, stage2, stage3);

  private EventBridgeDefinition eventBridgeDefinitionNoStages;
  private EventBridgeDefinition eventBridgeDefinition;

  @BeforeEach
  void init() {

    var eventBridgeDefinitionBuilder = EventBridgeDefinition.builder()
      .setDatabaseUrlByStage(Map.of())
      .setPreviousDatabaseUrlByStage(Map.of())
      .setMonitoringOrganization("Monitoring Org");

    this.eventBridgeDefinitionNoStages = eventBridgeDefinitionBuilder.setOrderedStages(new ArrayList<>()).build();
    this.eventBridgeDefinition = eventBridgeDefinitionBuilder.setOrderedStages(stageList).build();
  }

  @Test
  void testEventStageNullStage() {
    WorkflowDefinitionId stage = WorkflowDefinitionId.from("TestStage1");
    var eventStages = new EventStages(eventBridgeDefinitionNoStages);
    assertFalse(eventStages.containsStage(stage));
    assertThrows(NullPointerException.class, () -> eventStages.getPreviousStage(null));
    assertThrows(NullPointerException.class, () -> eventStages.getNextStage(null));
  }

  @Test
  void testEventStageNoStage() {
    WorkflowDefinitionId stage = WorkflowDefinitionId.from("TestStage1");
    var eventStages = new EventStages(eventBridgeDefinitionNoStages);
    assertFalse(eventStages.containsStage(stage));
    assertEquals(Optional.empty(), eventStages.getPreviousStage(stage));
    assertEquals(Optional.empty(), eventStages.getNextStage(stage));
  }

  @Test
  void testEventStageSingleStage() {
    WorkflowDefinitionId stage = WorkflowDefinitionId.from("TestStage1");
    var eventBridgeDefinitionSingleStage = EventBridgeDefinition.builder()
      .setDatabaseUrlByStage(Map.of())
      .setPreviousDatabaseUrlByStage(Map.of())
      .setMonitoringOrganization("Monitoring Org")
      .setOrderedStages(List.of(stage))
      .build();
    var eventStages = new EventStages(eventBridgeDefinitionSingleStage);
    assertTrue(eventStages.containsStage(stage));
    assertEquals(Optional.empty(), eventStages.getPreviousStage(stage));
    assertEquals(Optional.empty(), eventStages.getNextStage(stage));
  }

  @Test
  void testEventStageMultiStage() {
    var eventStages = new EventStages(eventBridgeDefinition);
    assertEquals(stageList, eventStages.getOrderedStages());
    assertTrue(eventStages.containsStage(stage1));
    assertEquals(Optional.empty(), eventStages.getPreviousStage(stage1));
    assertEquals(Optional.of(stage2), eventStages.getNextStage(stage1));
    assertEquals(Optional.of(stage3), eventStages.getNextStage(stage2));
    assertEquals(Optional.of(stage3), eventStages.getNextStage(stage2));
    assertEquals(Optional.empty(), eventStages.getNextStage(stage3));
  }

  @Test
  void testEventStageEquals() {
    var eventStages = new EventStages(eventBridgeDefinition);
    var eventStages2 = new EventStages(eventBridgeDefinition);
    assertEquals(eventStages, eventStages2);
  }
}
package gms.shared.workflow.configuration;

import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.workflow.coi.AutomaticProcessingStage;
import gms.shared.workflow.coi.InteractiveAnalysisStage;
import gms.shared.workflow.coi.Stage;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.coi.WorkflowDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WorkflowConfigurationTest {

  @Mock
  ConfigurationConsumerUtility mockConfigUtil;

  WorkflowConfiguration workflowConfiguration;

  @BeforeEach
  void setUp() {
    workflowConfiguration = new WorkflowConfiguration(mockConfigUtil);
  }

  @Test
  void testResolveWorkflowDefinition() {
    String workflowName = "TEST_WORKFLOW";
    Map<String, Stage> expectedStagesByName = Stream.of("AUTOMATIC_STAGE1", "INTERACTIVE_STAGE2", "AUTOMATIC_STAGE3")
      .collect(toMap(identity(), stageName ->
        stageName.startsWith("AUTOMATIC")
          ? AutomaticProcessingStage.from(stageName, Duration.ZERO, List.of())
          : InteractiveAnalysisStage.from(stageName, Duration.ZERO, List.of())));

    given(mockConfigUtil.resolve(WorkflowConfiguration.WORKFLOW_DEFINITION_CONFIG, List.of(), WorkflowDefinition.class))
      .willReturn(WorkflowDefinition.from(workflowName, expectedStagesByName.keySet()));

    expectedStagesByName.forEach((name, stage) -> given(mockConfigUtil.resolve(
      WorkflowConfiguration.STAGE_DEFINITION_CONFIG,
      List.of(Selector.from(WorkflowConfiguration.NAME_SELECTOR, name)),
      Stage.class))
      .willReturn(stage));

    Workflow actualWorkflow = workflowConfiguration.resolveWorkflowDefinition();

    assertEquals(workflowName, actualWorkflow.getName());
    List<Stage> actualStages = actualWorkflow.getStages();
    assertEquals(expectedStagesByName.size(), actualStages.size());
    actualStages.forEach(stage -> {
      assertTrue(expectedStagesByName.containsKey(stage.getName()));
      assertEquals(expectedStagesByName.get(stage.getName()), stage);
    });
  }
}

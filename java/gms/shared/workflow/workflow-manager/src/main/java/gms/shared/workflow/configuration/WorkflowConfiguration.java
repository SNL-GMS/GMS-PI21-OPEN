package gms.shared.workflow.configuration;

import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.workflow.coi.Stage;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.coi.WorkflowDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Manages Workflow Processing Configuration
 */
@Component
public class WorkflowConfiguration {

  static final String WORKFLOW_DEFINITION_CONFIG = "workflow-manager.workflow-definition";
  static final String STAGE_DEFINITION_CONFIG = "workflow-manager.stage-definition";
  static final String NAME_SELECTOR = "name";

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  @Autowired
  public WorkflowConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;
  }

  /**
   * Creates and returns a {@link Workflow} from a configured {@link WorkflowDefinition} and {@link Stage}s
   *
   * @return A Workflow generated from configuration
   */
  public Workflow resolveWorkflowDefinition() {
    var workflowDefinition = configurationConsumerUtility.resolve(WORKFLOW_DEFINITION_CONFIG,
      List.of(), WorkflowDefinition.class);
    List<Stage> stages = workflowDefinition.getStageNames().stream()
      .map(stageName -> configurationConsumerUtility.resolve(STAGE_DEFINITION_CONFIG,
        List.of(Selector.from(NAME_SELECTOR, stageName)), Stage.class))
      .collect(toList());

    return Workflow.from(workflowDefinition.getName(), stages);
  }
}

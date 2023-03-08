package gms.shared.signalenhancementconfiguration.coi.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import gms.shared.workflow.coi.WorkflowDefinitionId;

@AutoValue
@JsonPropertyOrder(alphabetic = true)
public abstract class WorkflowDefinitionIdStringPair {

  public abstract WorkflowDefinitionId getWorkflowDefinitionId();

  public abstract String getName();

  @JsonCreator
  public static WorkflowDefinitionIdStringPair create(
    @JsonProperty("workflowDefinitionId") WorkflowDefinitionId workflowDefinitionId,
    @JsonProperty("name") String name) {

    return new AutoValue_WorkflowDefinitionIdStringPair(workflowDefinitionId, name);
  }
}

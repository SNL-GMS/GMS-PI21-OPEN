package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

/**
 * A processing configuration class containing a {@link gms.shared.workflow.coi.Workflow} name and its
 * {@link gms.shared.workflow.coi.Stage} names
 */
@AutoValue
public abstract class WorkflowDefinition {

  /**
   * Gets the {@link gms.shared.workflow.coi.Workflow} name
   *
   * @return The {@link gms.shared.workflow.coi.Workflow} name
   */
  public abstract String getName();

  /**
   * Gets the {@link gms.shared.workflow.coi.Stage} names contained in the {@link gms.shared.workflow.coi.Workflow}
   *
   * @return The {@link gms.shared.workflow.coi.Stage} names contained in the {@link gms.shared.workflow.coi.Workflow}
   */
  public abstract List<String> getStageNames();

  /**
   * Creates and returns a new {@link WorkflowDefinition}
   *
   * @param name The name of the {@link gms.shared.workflow.coi.Workflow}
   * @param stageNames The names of the {@link gms.shared.workflow.coi.Stage}s in the Workflow
   * @return A new WorkflowDefinition
   */
  @JsonCreator
  public static WorkflowDefinition from(
    @JsonProperty("name") String name,
    @JsonProperty("stageNames") Collection<String> stageNames
  ) {
    return new AutoValue_WorkflowDefinition(name, ImmutableList.copyOf(stageNames));
  }
}

package gms.shared.event.repository;

import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides utilities for moving between {@link WorkflowDefinitionId} stages
 */
@Component
public class EventStages {
  private final List<WorkflowDefinitionId> orderedStages;

  @Autowired
  public EventStages(EventBridgeDefinition eventBridgeDefinition) {
    this.orderedStages = Objects.requireNonNullElseGet(eventBridgeDefinition.getOrderedStages(), Collections::emptyList);
  }

  /**
   * Retrieves the previous stage to stageId if one exists
   *
   * @param stageId reference {@link WorkflowDefinitionId} stage
   * @return {@link WorkflowDefinitionId} of the previous stage or empty {@link Optional} if not present.
   */
  public Optional<WorkflowDefinitionId> getPreviousStage(WorkflowDefinitionId stageId) {
    checkNotNull(stageId);
    var currStageIndex = orderedStages.indexOf(stageId);
    if (currStageIndex <= 0) {
      return Optional.empty();
    }
    return Optional.of(orderedStages.get(currStageIndex - 1));
  }

  /**
   * Retrieves the next stage to stageId if one exists
   *
   * @param stageId reference {@link WorkflowDefinitionId} stage
   * @return {@link WorkflowDefinitionId} of the next stage or empty {@link Optional} if not present.
   */
  public Optional<WorkflowDefinitionId> getNextStage(WorkflowDefinitionId stageId) {
    checkNotNull(stageId);
    var currStageIndex = orderedStages.indexOf(stageId);
    var nextStageIndex = currStageIndex + 1;
    if (nextStageIndex <= 0 || nextStageIndex >= orderedStages.size()) {
      return Optional.empty();
    }
    return Optional.of(orderedStages.get(nextStageIndex));
  }

  /**
   * Validate a stage is in the {@link List}
   *
   * @param stage stage to validate existence
   * @return true is valid stage, false otherwise
   */
  public boolean containsStage(WorkflowDefinitionId stage) {
    return orderedStages.contains(stage);
  }

  /**
   * Get all {@link WorkflowDefinitionId} stages  associated with Events as an ordered {@link List}
   *
   * @return Ordered {@link List} of {@link WorkflowDefinitionId} associated with Events
   */
  public List<WorkflowDefinitionId> getOrderedStages() {
    return orderedStages;
  }

  @Override
  public String toString() {
    return "EventStages{" +
      "orderedStages=" + orderedStages +
      '}';
  }


  @Override
  public int hashCode() {
    return Objects.hash(orderedStages);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EventStages that = (EventStages) o;
    return Objects.equals(orderedStages, that.orderedStages);
  }
}
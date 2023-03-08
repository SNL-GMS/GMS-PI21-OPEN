package gms.shared.workflow.coi;

import java.time.Instant;

public interface Interval {

  String getName();

  IntervalStatus getStatus();

  Instant getStartTime();

  Instant getEndTime();

  Instant getProcessingStartTime();

  Instant getProcessingEndTime();

  Instant getStorageTime();

  Instant getModificationTime();

  double getPercentAvailable();

  String getComment();

  default IntervalId getIntervalId() {
    return IntervalId.from(getStartTime(), getWorkflowDefinitionId());
  }

  default WorkflowDefinitionId getWorkflowDefinitionId() {
    return WorkflowDefinitionId.from(getName());
  }


}

package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;

@AutoValue
public abstract class IntervalId {

  public abstract Instant getStartTime();

  public abstract WorkflowDefinitionId getDefinitionId();

  @JsonCreator
  public static IntervalId from(
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("definitionId") WorkflowDefinitionId definitionId) {
    return new AutoValue_IntervalId(startTime, definitionId);
  }


}

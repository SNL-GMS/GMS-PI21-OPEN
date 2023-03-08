package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonSerialize(as = WorkflowDefinitionId.class)
public abstract class WorkflowDefinitionId {

  public abstract String getName();

  @JsonCreator
  public static WorkflowDefinitionId from(@JsonProperty("name") String name) {
    return new AutoValue_WorkflowDefinitionId(name);
  }


}

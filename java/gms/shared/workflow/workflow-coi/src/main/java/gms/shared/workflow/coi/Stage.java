package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.time.Duration;

@JsonTypeInfo(
  use = Id.NAME,
  include = As.EXISTING_PROPERTY,
  property = "mode",
  visible = true
)
@JsonSubTypes({
  @Type(value = AutomaticProcessingStage.class, name = "AUTOMATIC"),
  @Type(value = InteractiveAnalysisStage.class, name = "INTERACTIVE")
})
public interface Stage {

  String getName();

  Duration getDuration();

  StageMode getMode();

  @JsonIgnore
  default WorkflowDefinitionId getStageId() {
    return WorkflowDefinitionId.from(getName());
  }
}

package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import java.util.Optional;

@JsonTypeInfo(
  use = Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "stageMode",
  visible = true
)
@JsonSubTypes({
  @Type(value = AutomaticProcessingStageInterval.class, name = "AUTOMATIC"),
  @Type(value = InteractiveAnalysisStageInterval.class, name = "INTERACTIVE")
})
public interface StageInterval extends Interval {

  Optional<StageMetrics> getStageMetrics();

  StageMode getStageMode();

}

package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.stream.Stream;

@AutoValue
public abstract class Workflow {

  public abstract String getName();

  public abstract List<Stage> getStages();

  public Stream<Stage> stages() {
    return getStages().stream();
  }

  public Stream<WorkflowDefinitionId> stageIds() {
    return stages().map(Stage::getStageId);
  }

  @JsonCreator
  public static Workflow from(
    @JsonProperty("name") String name,
    @JsonProperty("stages") List<Stage> stages) {
    return new AutoValue_Workflow(name, stages);
  }
}

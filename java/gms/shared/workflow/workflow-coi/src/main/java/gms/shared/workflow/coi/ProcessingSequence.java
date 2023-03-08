package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ProcessingSequence {

  public abstract String getName();

  public abstract List<ProcessingStep> getSteps();

  @JsonCreator
  public static ProcessingSequence from(
    @JsonProperty("name") String name,
    @JsonProperty("steps") List<ProcessingStep> steps) {

    return new AutoValue_ProcessingSequence(name, steps);
  }
}

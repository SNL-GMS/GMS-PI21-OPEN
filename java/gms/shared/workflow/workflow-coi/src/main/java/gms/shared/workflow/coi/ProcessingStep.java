package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ProcessingStep {

  public abstract String getName();

  @JsonCreator
  public static ProcessingStep from(@JsonProperty("name") String name) {
    return new AutoValue_ProcessingStep(name);
  }
}

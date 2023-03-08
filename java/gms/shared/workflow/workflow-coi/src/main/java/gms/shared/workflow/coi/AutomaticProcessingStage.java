package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Duration;
import java.util.List;

@AutoValue
public abstract class AutomaticProcessingStage implements Stage {

  public abstract List<ProcessingSequence> getSequences();

  @Override
  public StageMode getMode() {
    return StageMode.AUTOMATIC;
  }

  @JsonCreator
  public static AutomaticProcessingStage from(
    @JsonProperty("name") String name,
    @JsonProperty("duration") Duration duration,
    @JsonProperty("sequences") List<ProcessingSequence> sequences) {

    return new AutoValue_AutomaticProcessingStage(name, duration, sequences);
  }
}

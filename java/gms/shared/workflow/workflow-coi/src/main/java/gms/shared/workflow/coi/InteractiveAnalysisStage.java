package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Duration;
import java.util.List;

@AutoValue
public abstract class InteractiveAnalysisStage implements Stage {

  public abstract List<Activity> getActivities();

  @Override
  public StageMode getMode() {
    return StageMode.INTERACTIVE;
  }

  @JsonCreator
  public static InteractiveAnalysisStage from(
    @JsonProperty("name") String name,
    @JsonProperty("duration") Duration duration,
    @JsonProperty("activities") List<Activity> activities) {

    return new AutoValue_InteractiveAnalysisStage(name, duration, activities);
  }
}

package gms.shared.workflow.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@AutoValue
@JsonSerialize(as = InteractiveAnalysisStageInterval.class)
@JsonDeserialize(builder = AutoValue_InteractiveAnalysisStageInterval.Builder.class)
public abstract class InteractiveAnalysisStageInterval implements StageInterval {

  public abstract List<ActivityInterval> getActivityIntervals();

  public Stream<ActivityInterval> activityIntervals() {
    return getActivityIntervals().stream();
  }

  @Override
  public StageMode getStageMode() {
    return StageMode.INTERACTIVE;
  }

  public static InteractiveAnalysisStageInterval.Builder builder() {
    return new AutoValue_InteractiveAnalysisStageInterval.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setName(String name);

    Builder setStatus(IntervalStatus intervalStatus);

    Builder setStartTime(Instant startTime);

    Builder setEndTime(Instant endTime);

    Builder setProcessingStartTime(Instant processingStartTime);

    Builder setProcessingEndTime(Instant processingEndTime);

    Builder setStorageTime(Instant storageTime);

    Builder setModificationTime(Instant modificationTime);

    Builder setPercentAvailable(double percentAvailable);

    Builder setComment(String comment);

    Builder setStageMetrics(StageMetrics stageMetrics);

    Builder setActivityIntervals(
      List<ActivityInterval> activityIntervals);

    InteractiveAnalysisStageInterval build();
  }
}

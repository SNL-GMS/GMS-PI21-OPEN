package gms.shared.workflow.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.List;

@AutoValue
@JsonSerialize(as = AutomaticProcessingStageInterval.class)
@JsonDeserialize(builder = AutoValue_AutomaticProcessingStageInterval.Builder.class)
public abstract class AutomaticProcessingStageInterval implements StageInterval {

  public abstract List<ProcessingSequenceInterval> getSequenceIntervals();

  @Override
  public StageMode getStageMode() {
    return StageMode.AUTOMATIC;
  }

  public static AutomaticProcessingStageInterval.Builder builder() {
    return new AutoValue_AutomaticProcessingStageInterval.Builder();
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

    Builder setSequenceIntervals(
      List<ProcessingSequenceInterval> processingSequenceIntervals);

    AutomaticProcessingStageInterval build();
  }
}

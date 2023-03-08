package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;

import java.time.Instant;

@AutoValue
@JsonTypeName("ProcessingSequenceInterval")
@JsonDeserialize(builder = AutoValue_ProcessingSequenceInterval.Builder.class)
public abstract class ProcessingSequenceInterval implements Interval {

  public abstract String getStageName();

  public abstract double getPercentComplete();

  public abstract String getLastExecutedStepName();

  public static ProcessingSequenceInterval.Builder builder() {
    return new AutoValue_ProcessingSequenceInterval.Builder();
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

    Builder setStageName(String stageName);

    Builder setPercentComplete(double percentComplete);

    Builder setLastExecutedStepName(String lastExecutedStepName);

    ProcessingSequenceInterval build();
  }
}

package gms.shared.workflow.coi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

@AutoValue
@JsonSerialize(as = ActivityInterval.class)
@JsonDeserialize(builder = AutoValue_ActivityInterval.Builder.class)
public abstract class ActivityInterval implements Interval {

  public abstract String getStageName();

  public abstract List<String> getActiveAnalysts();

  public abstract Builder toBuilder();

  public static ActivityInterval.Builder builder() {
    return new AutoValue_ActivityInterval.Builder();
  }

  public Stream<String> activeAnalysts() {
    return getActiveAnalysts().stream();
  }

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

    Builder setActiveAnalysts(List<String> activeAnalysts);

    ActivityInterval build();
  }

}

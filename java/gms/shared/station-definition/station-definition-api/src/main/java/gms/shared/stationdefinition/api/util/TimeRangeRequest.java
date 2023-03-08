package gms.shared.stationdefinition.api.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = TimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_TimeRangeRequest.Builder.class)
public abstract class TimeRangeRequest {

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  public static TimeRangeRequest.Builder builder() {
    return new AutoValue_TimeRangeRequest.Builder();
  }

  public abstract TimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    TimeRangeRequest.Builder setStartTime(Instant startTime);

    Instant getStartTime();

    TimeRangeRequest.Builder setEndTime(Instant endTime);

    Instant getEndTime();

    TimeRangeRequest autoBuild();

    default TimeRangeRequest build() {
      final TimeRangeRequest timeRangeRequest = autoBuild();
      Preconditions
        .checkArgument(!timeRangeRequest.getStartTime().isAfter(timeRangeRequest.getEndTime()),
          "Start time cannot be after end time");
      return timeRangeRequest;
    }
  }
}

package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;

@AutoValue
public abstract class TimeRangeRequest {
  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  @JsonCreator
  public static TimeRangeRequest create(
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime) {
    return new AutoValue_TimeRangeRequest(startTime, endTime);
  }
}

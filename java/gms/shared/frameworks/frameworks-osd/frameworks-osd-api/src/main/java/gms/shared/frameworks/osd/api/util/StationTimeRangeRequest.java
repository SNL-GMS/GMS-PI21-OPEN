package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = StationTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_StationTimeRangeRequest.Builder.class)
public abstract class StationTimeRangeRequest {

  public abstract String getStationName();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static StationTimeRangeRequest create(
    String stationName,
    Instant startTime,
    Instant endTime) {
    return builder().setStationName(stationName)
      .setTimeRange(TimeRangeRequest.create(startTime, endTime))
      .build();
  }

  public static Builder builder() {
    return new AutoValue_StationTimeRangeRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setStationName(String stationName);

    @JsonUnwrapped
    public abstract Builder setTimeRange(TimeRangeRequest timeRange);

    public abstract StationTimeRangeRequest build();

  }
}

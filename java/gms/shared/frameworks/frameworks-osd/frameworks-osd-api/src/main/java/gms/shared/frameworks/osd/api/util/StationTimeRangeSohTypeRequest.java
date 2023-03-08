package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = StationTimeRangeSohTypeRequest.class)
@JsonDeserialize(builder = AutoValue_StationTimeRangeSohTypeRequest.Builder.class)
public abstract class StationTimeRangeSohTypeRequest {

  public abstract String getStationName();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public abstract AcquiredChannelEnvironmentIssueType getType();

  public static StationTimeRangeSohTypeRequest create(
    String stationName,
    Instant startTime,
    Instant endTime,
    AcquiredChannelEnvironmentIssueType type) {

    return builder()
      .setStationName(stationName)
      .setTimeRange(TimeRangeRequest.create(startTime, endTime))
      .setType(type)
      .build();
  }

  public static StationTimeRangeSohTypeRequest.Builder builder() {
    return new AutoValue_StationTimeRangeSohTypeRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationTimeRangeSohTypeRequest.Builder setStationName(String stationName);

    @JsonUnwrapped
    StationTimeRangeSohTypeRequest.Builder setTimeRange(TimeRangeRequest timeRange);

    StationTimeRangeSohTypeRequest.Builder setType(AcquiredChannelEnvironmentIssueType type);

    StationTimeRangeSohTypeRequest build();

  }

}

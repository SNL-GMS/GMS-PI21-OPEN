package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = ChannelTimeRangeSohTypeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelTimeRangeSohTypeRequest.Builder.class)
public abstract class ChannelTimeRangeSohTypeRequest {

  public abstract String getChannelName();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public abstract AcquiredChannelEnvironmentIssueType getType();

  public static ChannelTimeRangeSohTypeRequest create(
    String channelName,
    Instant startTime,
    Instant endTime,
    AcquiredChannelEnvironmentIssueType type) {

    return builder()
      .setChannelName(channelName)
      .setTimeRange(TimeRangeRequest.create(startTime, endTime))
      .setType(type)
      .build();
  }

  public static Builder builder() {
    return new AutoValue_ChannelTimeRangeSohTypeRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setChannelName(String channelName);

    @JsonUnwrapped
    Builder setTimeRange(TimeRangeRequest timeRange);

    Builder setType(AcquiredChannelEnvironmentIssueType type);

    ChannelTimeRangeSohTypeRequest build();

  }

}

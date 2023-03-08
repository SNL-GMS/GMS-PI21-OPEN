package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@AutoValue
@JsonSerialize(as = ChannelsTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelsTimeRangeRequest.Builder.class)
public abstract class ChannelsTimeRangeRequest {

  public abstract ImmutableList<String> getChannelNames();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static ChannelsTimeRangeRequest.Builder builder() {
    return new AutoValue_ChannelsTimeRangeRequest.Builder();
  }

  public static ChannelsTimeRangeRequest create(
    List<String> channelNames,
    Instant startTime,
    Instant endTime) {
    Preconditions.checkState(!channelNames.isEmpty());
    return builder().setChannelNames(channelNames)
      .setTimeRange(TimeRangeRequest.create(startTime, endTime))
      .build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract Builder setChannelNames(ImmutableList<String> channelNames);

    public Builder setChannelNames(Collection<String> channelNames) {
      return setChannelNames(ImmutableList.copyOf(channelNames));
    }

    abstract ImmutableList.Builder<String> channelNamesBuilder();

    public Builder addChannelName(String channelName) {
      channelNamesBuilder().add(channelName);
      return this;
    }

    @JsonUnwrapped
    public abstract Builder setTimeRange(TimeRangeRequest timeRange);

    public abstract ChannelsTimeRangeRequest build();
  }
}

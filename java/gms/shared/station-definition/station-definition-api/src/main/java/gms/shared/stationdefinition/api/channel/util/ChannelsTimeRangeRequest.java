package gms.shared.stationdefinition.api.channel.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;

import java.util.Collection;

@AutoValue
@JsonSerialize(as = ChannelsTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelsTimeRangeRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChannelsTimeRangeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getChannelNames();
  }

  public abstract ImmutableList<String> getChannelNames();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static Builder builder() {
    return new AutoValue_ChannelsTimeRangeRequest.Builder();
  }

  public abstract ChannelsTimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelsTimeRangeRequest.Builder setChannelNames(ImmutableList<String> channelNames);

    default ChannelsTimeRangeRequest.Builder setChannelNames(Collection<String> channelNames) {
      return setChannelNames(ImmutableList.copyOf(channelNames));
    }

    ImmutableList.Builder<String> channelNamesBuilder();

    default ChannelsTimeRangeRequest.Builder addChannelName(String channelName) {
      channelNamesBuilder().add(channelName);
      return this;
    }

    @JsonUnwrapped
    ChannelsTimeRangeRequest.Builder setTimeRange(TimeRangeRequest timeRange);

    ChannelsTimeRangeRequest build();
  }
}

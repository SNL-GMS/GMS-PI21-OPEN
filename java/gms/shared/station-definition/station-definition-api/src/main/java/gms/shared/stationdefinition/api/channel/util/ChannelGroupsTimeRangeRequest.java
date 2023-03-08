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
@JsonSerialize(as = ChannelGroupsTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelGroupsTimeRangeRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChannelGroupsTimeRangeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getChannelGroupNames();
  }

  public abstract ImmutableList<String> getChannelGroupNames();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static Builder builder() {
    return new AutoValue_ChannelGroupsTimeRangeRequest.Builder();
  }

  public abstract ChannelGroupsTimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelGroupsTimeRangeRequest.Builder setChannelGroupNames(ImmutableList<String> channelGroupNames);

    default ChannelGroupsTimeRangeRequest.Builder setChannelGroupNames(Collection<String> channelGroupNames) {
      return setChannelGroupNames(ImmutableList.copyOf(channelGroupNames));
    }

    ImmutableList.Builder<String> channelGroupNamesBuilder();

    default ChannelGroupsTimeRangeRequest.Builder addChannelGroupName(String channelGroupName) {
      channelGroupNamesBuilder().add(channelGroupName);
      return this;
    }

    @JsonUnwrapped
    ChannelGroupsTimeRangeRequest.Builder setTimeRange(TimeRangeRequest timeRange);

    ChannelGroupsTimeRangeRequest build();
  }
}

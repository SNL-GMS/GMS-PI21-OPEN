package gms.shared.stationdefinition.api.channel.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;

import java.time.Instant;
import java.util.Collection;

@AutoValue
@JsonSerialize(as = ChannelGroupsTimeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelGroupsTimeRequest.Builder.class)
public abstract class ChannelGroupsTimeRequest implements Request {
  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getChannelGroupNames();
  }

  public abstract ImmutableList<String> getChannelGroupNames();

  public abstract Instant getEffectiveTime();

  public static ChannelGroupsTimeRequest.Builder builder() {
    return new AutoValue_ChannelGroupsTimeRequest.Builder();
  }

  public abstract ChannelGroupsTimeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelGroupsTimeRequest.Builder setChannelGroupNames(ImmutableList<String> channelGroupNames);

    default ChannelGroupsTimeRequest.Builder setChannelGroupNames(Collection<String> channelGroupNames) {
      return setChannelGroupNames(ImmutableList.copyOf(channelGroupNames));
    }

    ImmutableList.Builder<String> channelGroupNamesBuilder();

    default ChannelGroupsTimeRequest.Builder addChannelGroupName(String channelGroupName) {
      channelGroupNamesBuilder().add(channelGroupName);
      return this;
    }

    ChannelGroupsTimeRequest.Builder setEffectiveTime(Instant effectiveTime);

    ChannelGroupsTimeRequest build();
  }
}

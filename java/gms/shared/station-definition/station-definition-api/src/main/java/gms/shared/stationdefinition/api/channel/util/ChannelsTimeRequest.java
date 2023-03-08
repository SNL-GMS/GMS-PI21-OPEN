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
@JsonSerialize(as = ChannelsTimeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelsTimeRequest.Builder.class)
public abstract class ChannelsTimeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getChannelNames();
  }

  public abstract ImmutableList<String> getChannelNames();

  public abstract Instant getEffectiveTime();

  public static ChannelsTimeRequest.Builder builder() {
    return new AutoValue_ChannelsTimeRequest.Builder();
  }

  public abstract ChannelsTimeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ChannelsTimeRequest.Builder setChannelNames(ImmutableList<String> channelNames);

    default ChannelsTimeRequest.Builder setChannelNames(Collection<String> channelNames) {
      return setChannelNames(ImmutableList.copyOf(channelNames));
    }

    ImmutableList.Builder<String> channelNamesBuilder();

    default ChannelsTimeRequest.Builder addChannelName(String channelName) {
      channelNamesBuilder().add(channelName);
      return this;
    }

    ChannelsTimeRequest.Builder setEffectiveTime(Instant effectiveTime);

    ChannelsTimeRequest build();
  }
}

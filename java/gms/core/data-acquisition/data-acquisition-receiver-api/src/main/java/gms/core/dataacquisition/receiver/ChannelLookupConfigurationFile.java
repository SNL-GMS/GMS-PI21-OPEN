package gms.core.dataacquisition.receiver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

@AutoValue
public abstract class ChannelLookupConfigurationFile {

  public abstract ImmutableMap<String, String> getChannelIdsByPacketName();

  @JsonCreator
  public static ChannelLookupConfigurationFile from(
    @JsonProperty("channelIdsByPacketName") ImmutableMap<String, String> channelIdsByPacketName) {
    return new AutoValue_ChannelLookupConfigurationFile(channelIdsByPacketName);
  }

  public static ChannelLookupConfigurationFile from(Map<String, String> channelIdsByPacketName) {
    return from(ImmutableMap.copyOf(channelIdsByPacketName));
  }
}

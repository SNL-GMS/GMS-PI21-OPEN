package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Set;


//TODO: update description with a meaningful definition of "used" - how is this configuration used?

/**
 * Used to specify a Set of {@link gms.shared.frameworks.osd.coi.channel.Channel}s to be associated
 * with a {@link gms.shared.frameworks.osd.coi.soh.SohMonitorType} for {@link
 * ChannelsByMonitorType}. If ChannelsMode is USE_LISTED, only those Channels
 * specified in the Set of Channels are used.  Otherwise, all Channels for the associated Station
 * are used.
 */
@AutoValue
public abstract class ChannelsForMonitorTypeConfigurationOption {

  public abstract Set<String> getChannels();

  public abstract ChannelsMode getChannelsMode();

  @JsonCreator
  public static ChannelsForMonitorTypeConfigurationOption create(
    @JsonProperty("channels") Set<String> channels,
    @JsonProperty("channelsMode") ChannelsMode channelsMode) {

    return new AutoValue_ChannelsForMonitorTypeConfigurationOption(channels, channelsMode);
  }

  public enum ChannelsMode {
    USE_ALL,
    USE_LISTED
  }
}

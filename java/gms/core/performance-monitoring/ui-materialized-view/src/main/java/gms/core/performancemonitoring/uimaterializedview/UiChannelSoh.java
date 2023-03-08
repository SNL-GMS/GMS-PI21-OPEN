package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

import java.util.Set;

/**
 * Represents state-of-health of a single channel.
 */
@AutoValue
public abstract class UiChannelSoh {

  /**
   * @return the channel with this state-of-health
   */
  public abstract String getChannelName();

  /**
   * @return Rollup object representing the worst of the SOH statuses
   */
  public abstract SohStatus getChannelSohStatus();

  /**
   * @return Types, values, and status of the monitored SOH of the channel.
   */
  public abstract Set<UiSohMonitorValueAndStatus> getAllSohMonitorValueAndStatuses();

  /**
   * Creates a Channel SOH object
   *
   * @param channelName channel that has this state-of-health
   * @param channelSohStatus Rolled-up status of the channel (see getChannelSohStatus)
   * @param allSohMonitorValueAndStatuses All of the status that were monitored for this channel.
   * @return new ChannelSoh object
   */
  @JsonCreator
  public static UiChannelSoh from(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("channelSohStatus") SohStatus channelSohStatus,
    @JsonProperty("allSohMonitorValueAndStatuses") Set<UiSohMonitorValueAndStatus> allSohMonitorValueAndStatuses
  ) {
    return new AutoValue_UiChannelSoh(
      channelName,
      channelSohStatus,
      allSohMonitorValueAndStatuses
    );
  }
}

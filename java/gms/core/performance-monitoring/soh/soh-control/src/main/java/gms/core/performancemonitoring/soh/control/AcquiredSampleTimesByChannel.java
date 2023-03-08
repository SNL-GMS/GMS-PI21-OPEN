package gms.core.performancemonitoring.soh.control;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class provides a container with helper methods for the channel to latest endTime mappings for the TIMELINESS
 * calculations
 */
public class AcquiredSampleTimesByChannel {

  //Channel to latest endTime map
  private final Map<String, Instant> latestChannelToEndTime = new ConcurrentHashMap<>();

  public void setLatestChannelToEndTime(
    Map<String, Instant> latestChannelToEndTime) {
    this.latestChannelToEndTime.clear();
    this.latestChannelToEndTime.putAll(latestChannelToEndTime);
  }

  /**
   * Update the cache for a particular channel with a endTime. Note this method will only update if a.) there is no
   * endTime already for a channel or b.) the endTime is greater than what is already stored
   *
   * @param channel
   * @param endTime
   */
  public void update(String channel, Instant endTime) {

    // If the channel exists, check if the instant that is stored is before the endTime
    // If it is update the map. Also, update the map if no channel exists in the map.
    if (!latestChannelToEndTime.containsKey(channel) ||
      latestChannelToEndTime.get(channel).isBefore(endTime)) {
      latestChannelToEndTime.put(channel, endTime);
    }
  }

  /**
   * Method will return the latest endTime for a given channel if it exists in the cache
   *
   * @param channelName
   * @return
   */
  public Optional<Instant> getLatestEndTime(String channelName) {

    return latestChannelToEndTime.containsKey(channelName) ?
      Optional.of(latestChannelToEndTime.get(channelName)) : Optional.empty();
  }

  public boolean isEmpty() {
    return latestChannelToEndTime.isEmpty();
  }
}

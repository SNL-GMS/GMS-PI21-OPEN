package gms.shared.stationdefinition.api.channel;

import gms.shared.stationdefinition.coi.channel.ChannelGroup;

import java.time.Instant;
import java.util.List;

public interface ChannelGroupRepositoryInterface {

  /**
   * Finds {@link ChannelGroup}s having one of the provided names that were active at the effective time
   *
   * @param channelGroupNames The names of the channel groups to find
   * @param effectiveAt The effective time at which the channel groups must be active
   * @return A list of {@link ChannelGroup}s with provided names and effective time
   */
  List<ChannelGroup> findChannelGroupsByNameAndTime(List<String> channelGroupNames, Instant effectiveAt);

  /**
   * Finds {@link ChannelGroup}s having one of the provided names that were active at the effective time
   *
   * @param channelGroupNames The names of the channel groups to find
   * @param startTime The earliest allowable effective time of the channels
   * @param endTime The latest allowable effective time of the channels
   * @return A list of {@link ChannelGroup}s with the provided names and active between the provided times
   */
  List<ChannelGroup> findChannelGroupsByNameAndTimeRange(List<String> channelGroupNames, Instant startTime,
    Instant endTime);

  /**
   * Stores the provided channel groups
   *
   * @param channelGroups the channel groups to store
   */
  default void storeChannelGroups(List<ChannelGroup> channelGroups) {
    // no op
  }

}

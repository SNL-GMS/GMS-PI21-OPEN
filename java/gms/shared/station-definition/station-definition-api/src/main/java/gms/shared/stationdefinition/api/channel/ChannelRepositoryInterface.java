package gms.shared.stationdefinition.api.channel;


import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.dao.css.enums.TagName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChannelRepositoryInterface {

  /**
   * Finds {@link Channel}s having one of the provided names that were active at the provided time
   *
   * @param channelNames The names of the channels to find
   * @param effectiveAt The time at which the channels must be active
   * @return A list of {@link Channel}s with the provided names and effective times
   */
  List<Channel> findChannelsByNameAndTime(List<String> channelNames, Instant effectiveAt);

  /**
   * Finds {@link Channel}s having one of the provided names that were active between the provided start and end times.
   *
   * @param channelNames The names of the channels to find
   * @param startTime The earliest allowable effective time of the channels
   * @param endTime The latest allowable effective time of the channels
   * @return A list of {@link Channel}s with the provided names and active between the provided times
   */
  List<Channel> findChannelsByNameAndTimeRange(List<String> channelNames, Instant startTime, Instant endTime);

  /**
   * Finds a {@link Channel} using the provided wfdisc record identifier (i.e. wfid) to bridge the RAW or DERIVED
   * Channel associated with a particular ChannelSegment&lt;Waveform&gt;
   *
   * @param wfids List of wfdisc record identifiers
   * @param associatedRecordType record type, e.g. arrival or origin
   * @param associatedRecordId associated record id, arid or orid
   * @param filterId parameter used for filtering operations
   * @param channelEffectiveTime the time at which the channel is effective
   * @param channelEndTime the time at which the channel is no longer effective
   * @return a {@link Channel} meeting or derived from the provided criteria
   */
  Channel loadChannelFromWfdisc(List<Long> wfids, Optional<TagName> associatedRecordType,
    Optional<Long> associatedRecordId, Optional<Long> filterId,
    Instant channelEffectiveTime, Instant channelEndTime);

  /**
   * Stores the provided list of {@link Channel}s
   *
   * @param channels the {@link Channel}
   */
  default void storeChannels(List<Channel> channels) {
    // no op
  }

}

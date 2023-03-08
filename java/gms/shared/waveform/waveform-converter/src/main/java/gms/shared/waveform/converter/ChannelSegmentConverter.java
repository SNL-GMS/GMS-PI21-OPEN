package gms.shared.waveform.converter;

import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.waveform.coi.Waveform;

import java.time.Instant;
import java.util.List;

public interface ChannelSegmentConverter {

  /**
   * Converts a channel, list of {@link WfdiscDao} and a list of files for those wfdisc daos into a ChannelSegment
   *
   * @param channel The channel that captured the waveforms
   * @param wfDiscDaos A list of WfdisDao and corresponding File pairs
   * @return a {@link ChannelSegment} containing {@link Waveform}s for the provided data
   */
  ChannelSegment<Waveform> convert(Channel channel, List<WfdiscDao> wfDiscDaos, Instant startTime, Instant endTime);

  /**
   * Converts a {@link ChannelSegmentDescriptor}, list of {@link WfdiscDao} and a list of files for those wfdisc daos
   * into a ChannelSegment
   *
   * @param channelSegmentDescriptor The channel segment descriptor corresponding to the channel segment
   * @param wfDiscDaos A list of WfdisDao and corresponding File pairs
   * @return a {@link ChannelSegment} containing {@link Waveform}s for the provided data
   */
  ChannelSegment<Waveform> convert(ChannelSegmentDescriptor channelSegmentDescriptor, List<WfdiscDao> wfDiscDaos);
}

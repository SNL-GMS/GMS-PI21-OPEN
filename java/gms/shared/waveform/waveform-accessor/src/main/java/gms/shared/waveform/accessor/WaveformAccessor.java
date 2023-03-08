package gms.shared.waveform.accessor;

import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.waveform.api.WaveformAccessorInterface;
import gms.shared.waveform.api.WaveformRepositoryInterface;
import gms.shared.waveform.api.facet.WaveformFacetingUtility;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.waveform.coi.Waveform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Accessor for retrieving Waveforms from the backing store.  This contains a cache of previously retrieved channels,
 * and a class to a retrieve any channels not in the cache from the backing store.
 */
@Service
public class WaveformAccessor implements WaveformAccessorInterface {
  private static final Logger logger = LoggerFactory.getLogger(WaveformAccessor.class);
  private final WaveformRepositoryInterface waveformRepositoryImpl;
  private final StationDefinitionAccessorInterface stationDefinitionAccessorImpl;

  public static final String NULL_CHANNELS = "Channel list cannot be null";
  public static final String NULL_CHANNEL_SEGMENT_DESCRIPTORS = "Channel segment descriptors list cannot be null";
  public static final String EMPTY_CHANNELS_MESSAGE = "Channels cannot be empty";
  public static final String EMPTY_CHANNEL_SEGMENT_DESCRIPTORS_MESSAGE = "Channel segment descriptors cannot be empty";
  public static final String START_END_TIME_ERR = "Start Time cannot be after end time";
  public static final String START_FUTURE_ERR = "Start Time cannot be in the future";
  public static final String NULL_FACETING_DEFINITION_MESSAGE = "Faceting definition cannot be null";

  @Autowired
  public WaveformAccessor(
    WaveformRepositoryInterface waveformRepositoryImpl,
    @Qualifier("bridgedAccessor") StationDefinitionAccessorInterface stationDefinitionAccessorImpl) {
    this.waveformRepositoryImpl = waveformRepositoryImpl;
    this.stationDefinitionAccessorImpl = stationDefinitionAccessorImpl;
  }

  @Override
  public Collection<ChannelSegment<Waveform>> findByChannelsAndTimeRange(
    Set<Channel> channels, Instant startTime, Instant endTime) {

    Preconditions.checkNotNull(channels, NULL_CHANNELS);
    Preconditions.checkState(!channels.isEmpty(), EMPTY_CHANNELS_MESSAGE);
    Preconditions.checkState(startTime.isBefore(endTime), START_END_TIME_ERR);

    return waveformRepositoryImpl.findByChannelsAndTimeRange(
      channels, startTime, endTime);
  }

  @Override
  public Collection<ChannelSegment<Waveform>> findByChannelsAndTimeRange(
    Set<Channel> channels, Instant startTime, Instant endTime, FacetingDefinition facetingDefinition) {

    Preconditions.checkNotNull(channels, NULL_CHANNELS);
    Preconditions.checkState(!channels.isEmpty(), EMPTY_CHANNELS_MESSAGE);
    Preconditions.checkState(startTime.isBefore(endTime), START_END_TIME_ERR);
    Preconditions.checkNotNull(facetingDefinition, NULL_FACETING_DEFINITION_MESSAGE);

    Collection<ChannelSegment<Waveform>> channelSegments = waveformRepositoryImpl.findByChannelsAndTimeRange(
      channels, startTime, endTime);

    var facetingUtil = new WaveformFacetingUtility(
      this, stationDefinitionAccessorImpl);

    return channelSegments.stream().map(channelSeg ->
      (ChannelSegment<Waveform>) facetingUtil.populateFacets(channelSeg, facetingDefinition)
    ).collect(Collectors.toList());
  }

  @Override
  public Collection<ChannelSegment<Waveform>> findByChannelSegmentDescriptors(
    Collection<ChannelSegmentDescriptor> channelSegmentDescriptors) {

    Preconditions.checkNotNull(channelSegmentDescriptors, NULL_CHANNEL_SEGMENT_DESCRIPTORS);
    Preconditions.checkState(!channelSegmentDescriptors.isEmpty(), EMPTY_CHANNEL_SEGMENT_DESCRIPTORS_MESSAGE);

    logger.info("Retrieving waveforms for {} channel segment descriptors", channelSegmentDescriptors.size());
    return waveformRepositoryImpl.findByChannelSegmentDescriptors(
      channelSegmentDescriptors);
  }

  @Override
  public Collection<ChannelSegment<Waveform>> findByChannelNamesAndSegmentDescriptor(
    Collection<ChannelSegmentDescriptor> channelSegmentDescriptors, FacetingDefinition facetingDefinition) {

    Preconditions.checkNotNull(channelSegmentDescriptors, NULL_CHANNEL_SEGMENT_DESCRIPTORS);
    Preconditions.checkState(!channelSegmentDescriptors.isEmpty(), EMPTY_CHANNEL_SEGMENT_DESCRIPTORS_MESSAGE);
    Preconditions.checkNotNull(facetingDefinition, NULL_FACETING_DEFINITION_MESSAGE);

    logger.info("Retrieving waveforms for {} channel segment descriptors", channelSegmentDescriptors.size());

    Collection<ChannelSegment<Waveform>> channelSegments = waveformRepositoryImpl.findByChannelSegmentDescriptors(
      channelSegmentDescriptors);

    var facetingUtil = new WaveformFacetingUtility(
      this, stationDefinitionAccessorImpl);

    return channelSegments.stream().map(channelSeg ->
      (ChannelSegment<Waveform>) facetingUtil.populateFacets(channelSeg, facetingDefinition)
    ).collect(Collectors.toList());
  }
}

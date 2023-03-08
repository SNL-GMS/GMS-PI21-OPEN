package gms.shared.waveform.manager;

import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.waveform.api.WaveformAccessorInterface;
import gms.shared.waveform.api.util.ChannelSegmentDescriptorRequest;
import gms.shared.waveform.api.util.ChannelTimeRangeRequest;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Waveform;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;

@RestController
@RequestMapping(value = "/waveform",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
public class WaveformManager {

  private final WaveformAccessorInterface waveformAccessorImpl;

  @Autowired
  public WaveformManager(WaveformAccessorInterface waveformAccessorImpl) {
    this.waveformAccessorImpl = waveformAccessorImpl;
  }

  /**
   * Returns a collection of {@link ChannelSegment}s for each Channel entity provided in the query parameters
   * (since Channel is faceted, the provided objects may be fully populated or contain only references).
   * <p>
   * The response has a collection of ChannelSegments for each Channel entity since a ChannelSegment
   * is associated to a single Channel object but there may be multiple versions of each Channel entity
   * within the queried time interval.
   * <p>
   * Each ChannelSegment may contain multiple Waveforms to account for gaps in available waveform samples
   * or changes in sample rate, but each Waveform is as long as possible.
   * This operation always returns calibrated waveform samples.
   *
   * @param channelTimeRangeRequest List of channels to and time ranges to query over.
   * @return list of all {@link ChannelSegment} objects for each Channel entity within the queried time interval
   */
  @PostMapping(value = "/channel-segment/query/channel-timerange")
  @Operation(summary = "Loads and returns ChannelSegment<Waveform> based on channel and time range")
  public Collection<ChannelSegment<Waveform>> findWaveformsByChannelsAndTimeRange(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of channels and time range used to query ChannelSegment<Waveform>")
    @RequestBody ChannelTimeRangeRequest channelTimeRangeRequest) {

    Optional<FacetingDefinition> facetingDefinition = channelTimeRangeRequest.getFacetingDefinition();
    if (facetingDefinition.isPresent()) {
      return waveformAccessorImpl.findByChannelsAndTimeRange(channelTimeRangeRequest.getChannels(),
        channelTimeRangeRequest.getStartTime(), channelTimeRangeRequest.getEndTime(),
        facetingDefinition.get());
    }
    return waveformAccessorImpl.findByChannelsAndTimeRange(channelTimeRangeRequest.getChannels(),
      channelTimeRangeRequest.getStartTime(), channelTimeRangeRequest.getEndTime());
  }

  /**
   * Returns a collection of {@link ChannelSegment}s as it existed at the creation time listed in
   * ChannelSegmentDescriptor, even if newer data samples have since been stored in this WaveformRepository.
   * (since Channel is faceted, the provided objects may be fully populated or contain only references).
   * <p>
   * All of the samples returned for a ChannelSegmentDescriptor must be for the exact Channel version provided
   * in that ChannelSegmentDescriptor. Each returned ChannelSegment may contain multiple Waveforms to account
   * for gaps in available waveform samples or changes in sample rate, but each Waveform is as long as possible.
   *
   * @param channelSegmentDescriptorRequest ChannelName, time ranges, and creation time to query over.
   * @return list of all {@link ChannelSegment} objects for each Channel entity within the queried time interval
   */
  @PostMapping(value = "/channel-segment/query/channel-segment-descriptors")
  @Operation(summary = "Loads and returns ChannelSegment<Waveform> based on channel name and segment start, end, and creation times")
  public Collection<ChannelSegment<Waveform>> findWaveformsByChannelSegmentDescriptors(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Channel name and segment start, end, and creation times used to query ChannelSegment<Waveform>")
    @RequestBody ChannelSegmentDescriptorRequest channelSegmentDescriptorRequest) {

    Optional<FacetingDefinition> facetingDefinition = channelSegmentDescriptorRequest.getFacetingDefinition();
    if (facetingDefinition.isPresent()) {
      return waveformAccessorImpl.findByChannelNamesAndSegmentDescriptor(
        channelSegmentDescriptorRequest.getChannelSegmentDescriptors(),
        facetingDefinition.get());
    }
    return waveformAccessorImpl.findByChannelSegmentDescriptors(
      channelSegmentDescriptorRequest.getChannelSegmentDescriptors());
  }
}

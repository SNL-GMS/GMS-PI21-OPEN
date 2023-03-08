package gms.shared.frameworks.osd.api.channel;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.channel.util.ChannelSegmentsIdRequest;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.ChannelsTimeRangeRequest;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import gms.shared.frameworks.osd.coi.waveforms.FkSpectra;
import gms.shared.frameworks.osd.coi.waveforms.Timeseries;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

public interface ChannelSegmentsRepositoryInterface {

  /**
   * Retrieve {@link ChannelSegment}s, regardless of timeseries type, by their ids.
   *
   * @param request The collection of channel segment ids and a boolean indicating whether or not to
   * include the timeseries values
   * @return The list of {@link ChannelSegment}s corresponding to the provided ids.
   */
  @Path("/channel-segments/segment-ids")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.MSGPACK_NAME)
  @Operation(summary = "Retrieves channel segments by their ids")
  Collection<ChannelSegment<? extends Timeseries>> retrieveChannelSegmentsByIds(
    @RequestBody(description = "Collection of channel segment ids and a boolean to include the " +
      "timeseries")
    ChannelSegmentsIdRequest request);

  /**
   * Retrieve {@link ChannelSegment}s, regardless of timeseries type, by their channel names and a
   * time range
   *
   * @param request The collection of channel names and the start and end times that will bound the
   * {@link ChannelSegment}s
   * @return The list of {@link ChannelSegment}s corresponding to the provided channels names
   * and times.
   */
  @Path("/channel-segments/channels-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.MSGPACK_NAME)
  @Operation(summary = "Retrieves channel segments by their channel name and time range")
  Collection<ChannelSegment<Waveform>> retrieveChannelSegmentsByChannelNames(
    @RequestBody(description = "Collection of channel names and a time range bounding the " +
      "channel" +
      " segment")
    ChannelsTimeRangeRequest request);

  /**
   * Retrieve {@link ChannelSegment}s, regardless of timeseries type, by their channel names and a
   * time range
   *
   * @param channelTimeRangeRequests The collection of channel names and the start and end times for
   * each channel that will bound the {@link ChannelSegment}s
   * @return The list of {@link ChannelSegment}s corresponding to the provided channels names
   * and times.
   */
  @Path("/channel-segments/channels-time-multiple")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.MSGPACK_NAME)
  @Operation(summary = "Retrieves channel segments for the provided list of channel name and time" +
    " range for that channel")
  Collection<ChannelSegment<Waveform>> retrieveChannelSegmentsByChannelsAndTimeRanges(
    @RequestBody(description = "Collection of channel names and time ranges for each channel")
    Collection<ChannelTimeRangeRequest> channelTimeRangeRequests);

  /**
   * Store a collection of {@link ChannelSegment}s
   *
   * @param segments waveform channel segments to store
   */
  @Path("/channel-segments/new")
  @POST
  @Consumes(ContentType.MSGPACK_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Stores a collection of channel segments")
  void storeChannelSegments(
    @RequestBody(description = "The Channel Segments to store")
    Collection<ChannelSegment<Waveform>> segments);

  /**
   * Retrieves {@link ChannelSegment}s of {@link FkSpectra} for the provided channel names and time
   * ranges
   *
   * @param channelTimeRangeRequests The collection of {@link ChannelTimeRangeRequest} defining the
   * channel segments to retrieve, not null
   * @return the Collection of {@link ChannelSegment}s corresponding to the provided channel names
   * and times.
   */
  @Path("/channel-segments/fk/channels-time-multiple")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.MSGPACK_NAME)
  @Operation(summary = "Retrieves FK channel segments for the provided list of channel names and " +
    "time ranges")
  List<ChannelSegment<FkSpectra>> retrieveFkChannelSegmentsByChannelsAndTime(
    @RequestBody(description = "Collection of channel names and a time range for each channel")
    Collection<ChannelTimeRangeRequest> channelTimeRangeRequests);

  /**
   * Store a collection of {@link ChannelSegment}s containing {@link FkSpectra}
   *
   * @param segments the fk channel segments to store
   */
  @Path("/channel-segments/fk/new")
  @POST
  @Consumes(ContentType.MSGPACK_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Stores a collection of FK channel segments")
  void storeFkChannelSegments(
    @RequestBody(description = "The FK Channel Segments to store")
    Collection<ChannelSegment<FkSpectra>> segments);

}

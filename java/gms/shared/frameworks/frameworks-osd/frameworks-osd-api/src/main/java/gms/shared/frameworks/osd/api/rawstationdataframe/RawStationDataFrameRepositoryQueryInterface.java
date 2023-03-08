package gms.shared.frameworks.osd.api.rawstationdataframe;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface RawStationDataFrameRepositoryQueryInterface {

  /**
   * Retrieves all {@link RawStationDataFrameMetadata}, for a station, that have data in the
   * specified time range.
   *
   * @param request The {@link StationTimeRangeRequest}
   * @return
   */
  @Path("/raw-station-data-frame/query/metadata-by-station-and-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieves Raw Station Data Frame Metadata between the provided times")
  List<RawStationDataFrameMetadata> retrieveRawStationDataFrameMetadataByStationAndTime(
    @RequestBody(description = "Station and time range for raw station data frame metadata to retrieve " +
      "from the database")
    StationTimeRangeRequest request);

  /**
   * Retrieves the latest sample times for the provided channels.
   *
   * @param channelNames The {@link List} of channel names to get latest sample times
   * @return A {@link Map} of the channel time to it's latest sample time
   */
  @Path("/raw-station-data-frame")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieves the latest sample time received for each channel")
  Map<String, Instant> retrieveLatestSampleTimeByChannel(
    @RequestBody(description = "The channel names to receive sample data for")
    List<String> channelNames);

}

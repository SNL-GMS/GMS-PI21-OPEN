package gms.shared.frameworks.osd.api.rawstationdataframe;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

/**
 * A repository interface for storing and retrieving raw station data frames.
 */
public interface RawStationDataFrameRepositoryInterface {

  /**
   * Retrieves all raw station data frames that have any data in the specified time range for the
   * given stationName.
   *
   * @param request {@link StationTimeRangeRequest} instance representing a time range and station
   * to retrieve raw station data frames for
   * @return list of frames that start in that time range with the given station name; may be empty
   */
  @Path("/raw-station-data-frames/station-and-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all raw station data frames that have any data in the specified "
    + "time range for the given station name")
  List<RawStationDataFrame> retrieveRawStationDataFramesByStationAndTime(
    @RequestBody(description = "Time range and station name for raw station data frames to "
      + "retrieve from the database")
    StationTimeRangeRequest request);

  /**
   * Retrieves all raw station data frames that have any data in the specified time range.
   *
   * @param request {@link TimeRangeRequest} instance representing a time range to retrieve raw
   * station data frames for
   * @return list of frames that start in that time range; may be empty
   */
  @Path("/raw-station-data-frames/time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all raw station data frames that have any data in the specified "
    + "time range")
  List<RawStationDataFrame> retrieveRawStationDataFramesByTime(
    @RequestBody(description = "Time range for raw station data frames to retrieve from the database")
    TimeRangeRequest request);

  /**
   * Stores {@link RawStationDataFrame}s
   *
   * @param frames Collection of {@link RawStationDataFrame}s to store
   */
  @Path("/raw-station-data-frames/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Store Raw Station Data Frames")
  void storeRawStationDataFrames(
    @RequestBody(description = "Collection of Raw Station Data Frames to store")
    Collection<RawStationDataFrame> frames);

}

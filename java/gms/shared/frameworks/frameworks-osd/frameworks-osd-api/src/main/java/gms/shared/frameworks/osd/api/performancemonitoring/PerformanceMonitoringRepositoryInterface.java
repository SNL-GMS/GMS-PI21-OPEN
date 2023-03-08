package gms.shared.frameworks.osd.api.performancemonitoring;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.util.HistoricalStationSohRequest;
import gms.shared.frameworks.osd.api.util.StationsTimeRangeRequest;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * The PerformanceMonitoringRepository provides storage and retrieval of objects related to
 * Performance Monitoring.
 */

public interface PerformanceMonitoringRepositoryInterface {

  /**
   * Loads and returns the {@link StationSoh} with the given station group ids. Returns
   * an empty collection if no matching {@link StationSoh} are found in the repository.
   *
   * @param stationNames the list of station names  to find the related {@link StationSoh}
   * @return {@link StationSoh}s with the provided station name
   */
  @Path("/coi/performance-monitoring/station-soh/query/current-by-station-ids")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Loads and returns current StationSohStatuses with the given station ids")
  List<StationSoh> retrieveByStationId(
    @RequestBody(description = "list of station names to retrieve state of health status from")
    List<String> stationNames);

  /**
   * Loads and returns the StationSoh(es) that fall within the provided time range
   * (inclusive). Returns an empty collection if no StationSoh(es) are available for the
   * given time range.
   *
   * @param stationsTimeRangeRequest {@link StationsTimeRangeRequest} range to find StationSoh for
   * @return {@link List} of {@link StationSoh} that falls within range.
   */
  @Path("/coi/performance-monitoring/station-soh/query/by-station-ids-and-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary =
    "Loads and returns the StationSoh(es) that fall within the provided time range "
      + "(inclusive) and have the provided station id (name).")
  List<StationSoh> retrieveByStationsAndTimeRange(
    @RequestBody(description =
      "Object that holds the station names, start time and end time of the range to look for"
        + "StationSoh(es) for")
    StationsTimeRangeRequest stationsTimeRangeRequest);

  /**
   * Stores the provided {@link StationSoh} object(s) and returns a claim check of UUID[]
   * of stored {@link StationSoh}
   *
   * @param stationSohs the {@link StationSoh}s to store
   * @return the {@link UUID}s of the {@link StationSoh}s that were stored
   */
  @Path("/coi/performance-monitoring/station-soh")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Stores the provided StationGroupSohStatus object(s)",
    description =
      "Stores the provided StationSoh object(s) and returns a UUID[] of stored or"
        + "updated StationSoh(s)")
  List<UUID> storeStationSoh(
    @RequestBody(description = "collection of StationSohs to store")
    Collection<StationSoh> stationSohs);

  /**
   * Retrieves a HistoricalStationSoh DTO object corresponding to the provided Station ID and
   * collection of SohMonitorTypes provided in the request body.
   * <p>
   * The returned HistoricalStationSoh object contains SOH monitor values from StationSoh objects
   * with calculation time attributes in the time range provided (both start and end times are
   * inclusive), and aggregates the HistoricalSohMonitorValue objects by value and all associations
   * to Station and Channel are by identifier.
   *
   * @return A {@link HistoricalStationSoh} object that conforms to the provided parameters
   */
  @Path("/coi/performance-monitoring/station-soh/query/historical-by-station-id-time-and-soh-monitor-types")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieves a HistoricalStationSoh DTO object based on a Station ID (i.e. "
    + "Station name),  a time range, and a collection of SohMonitorTypes.")
  HistoricalStationSoh retrieveHistoricalStationSoh(
    @RequestBody(description = "Request containing the Station ID (i.e. Station name),  a time "
      + "range, and a collection of SohMonitorTypes")
    HistoricalStationSohRequest request);

}

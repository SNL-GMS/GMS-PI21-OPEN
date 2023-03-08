package gms.shared.frameworks.osd.api.rawstationdataframe;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.util.StationTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.dto.soh.HistoricalAcquiredChannelEnvironmentalIssues;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

public interface AcquiredChannelEnvironmentIssueRepositoryQueryInterface {
  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssue} objects for the provided station,
   * {@link AcquiredChannelEnvironmentIssueType},
   * created within the provided time range.
   *
   * @param request The station name, type, and time range that will bound the
   * {@link AcquiredChannelEnvironmentIssue}s retrieved.
   * @return All {@link AcquiredChannelEnvironmentIssue} objects, mapped to the provided station's
   * raw channels and sorted by start time, that meet the query criteria.
   */
  @Path("/coi/acquired-channel-environment-issues/query/station-id-time-and-type")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieves AcquiredChannelEnvironmentIssue<?> COI objects based on a Station " +
    "ID (i.e. Station name),  a time range, and an AcquiredChannelEnvironmentIssueType.")
  List<HistoricalAcquiredChannelEnvironmentalIssues> retrieveAcquiredChannelEnvironmentIssuesByStationTimeRangeAndType(
    @RequestBody(description = "Station name, type, and time range bounding the acquired station " +
      "soh data retrieved")
    StationTimeRangeSohTypeRequest request);
}

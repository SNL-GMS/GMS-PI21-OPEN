package gms.shared.frameworks.osd.api.performancemonitoring;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

public interface CapabilitySohRollupRepositoryInterface {

  /**
   * Retrieves capababilty soh rollup based on station group
   * if list is empty retrieves all capability rollups from osd
   *
   * @param stationGroups
   */
  @Path("/soh/capability-rollup/by-station-group")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve Capability Rollups from the database for the specified station groups")
  List<CapabilitySohRollup> retrieveCapabilitySohRollupByStationGroup(
    @RequestBody(description = "List of Station Group names")
    Collection<String> stationGroups);

  /**
   * Save Capability Soh Rolllup to the database
   *
   * @param capabilitySohRollups
   */
  @Path("/soh/capability-rollup/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Save a collection of Capability Soh Rollups to the database")
  void storeCapabilitySohRollup(
    @RequestBody(description = "Contains collection of Capability Soh Rollups")
    Collection<CapabilitySohRollup> capabilitySohRollups);

  @Path("/soh/capability-rollup/latest-by-station-group")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve the latest Capability Rollups from the databasse with specified station groups")
  List<CapabilitySohRollup> retrieveLatestCapabilitySohRollupByStationGroup(
    @RequestBody(description = "List of Station Groups")
    Collection<String> stationGroupNames);
}

package gms.shared.frameworks.osd.api.station;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroupDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

public interface StationGroupRepositoryInterface {

  /**
   * retrieves all {@link StationGroup} objects specified by a list of station group names. If the
   * list is empty, the server will return an error.
   *
   * @param stationGroupNames non-empty collection of station group names
   * @return list of {@link StationGroup} objects
   */
  @Path("/station-groups")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieves all station groups specified by a list of station group names")
  List<StationGroup> retrieveStationGroups(
    @RequestBody(description = "list of station group names", required = true)
    Collection<String> stationGroupNames);

  /**
   * Stores new collection of station groups into database. Will return an error if any station
   * group already exists.
   *
   * @param stationGroups non-empty collection of {@link StationGroup} objects to store
   */
  @Path("/station-groups/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "store a set of new station groups")
  void storeStationGroups(
    @RequestBody(description = "list of station groups to store", required = true)
    Collection<StationGroup> stationGroups);

  /**
   * Associates existing Stations with a collection of either new or existing StationGroups. Will
   * overwrite a StationGroup if one with the same name already exists. Will return an error if a
   * provided station name does not correspond to an existing Station.
   *
   * @param stationGroupDefinitions list of {@link StationGroupDefinition}s for updating {@link
   * StationGroup}s
   * @throws IllegalArgumentException if a provided station name in a definition does not correspond
   * to an existing station
   */
  @Path("/station-groups/update")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "store new or update existing station groups with provided definitions")
  void updateStationGroups(
    @RequestBody(description = "list of station group definitions for generating/updating station groups", required = true)
    Collection<StationGroupDefinition> stationGroupDefinitions);
}

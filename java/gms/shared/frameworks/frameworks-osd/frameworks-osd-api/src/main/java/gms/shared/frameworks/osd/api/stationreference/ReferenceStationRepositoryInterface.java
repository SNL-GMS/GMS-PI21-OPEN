package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.stationreference.util.ReferenceStationMembershipRequest;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReferenceStationRepositoryInterface {

  /**
   * Retrieves all {@link ReferenceStation} objects corresponding to given list of entity ids. If
   * Empty List provided, all reference stations will be retrieved.
   *
   * @return all stations
   */
  @Path("/station-reference/reference-stations/entity-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieves the given collection of reference stations that correspond to "
    + " the given input set of entity ids.")
  List<ReferenceStation> retrieveStations(
    @RequestBody(description = "entity ids of reference stations to retrieve")
    List<UUID> entityIds);

  /**
   * Retrieve all {@link ReferenceStation} objects by version ids.
   *
   * @param stationVersionIds the version ids
   * @return all station versions with those version ids
   */
  @Path("/station-reference/reference-stations/version-ids")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieves the given collection of reference stations that correspond to "
    + " the given input set of version ids.")
  List<ReferenceStation> retrieveStationsByVersionIds(
    @RequestBody(description = "version ids of reference stations to retrieve")
    Collection<UUID> stationVersionIds);

  /**
   * Retrieve all {@link ReferenceStation} objects corresponding to the given collection of names.
   *
   * @param names the name
   * @return all station versions with that name
   */
  @Path("/station-reference/reference-stations/names")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all reference stations corresponding to the given collection of names.")
  List<ReferenceStation> retrieveStationsByName(
    @RequestBody(description = "names corresponding to reference stations to retrieve.")
    List<String> names);

  /**
   * Stores the list of {@link ReferenceStation} objects.
   *
   * @param stations list of {@link ReferenceStation} objects to store.
   */
  @Path("/station-reference/reference-stations/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Stores the list of reference station objects.")
  void storeReferenceStation(
    @RequestBody(description = "list of reference station objects to store")
    Collection<ReferenceStation> stations);

  /**
   * Retrieves all station memberships
   *
   * @param ids The ids of the {@link ReferenceStationMembership}s to retrieve
   * @return the memberships
   */
  @Path("/station-reference/reference-station-memberships/id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve all reference station memberships for the given list of ids")
  Map<UUID, List<ReferenceStationMembership>> retrieveStationMemberships(
    @RequestBody(description = "list of ids for the reference stations to retrieve. Can be empty")
    List<UUID> ids);

  /**
   * Retrieves station memberships corresponding to the given set of  station ids passed in.
   *
   * @param stationIds list of station Ids for which we return the corresponding set of {@link
   * ReferenceStationMembership} objects.
   * @return the memberships
   */
  @Path("/station-reference/reference-station-memberships/station-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve all reference station memberships for the given list of ids")
  Map<UUID, List<ReferenceStationMembership>> retrieveStationMembershipsByStationId(
    @RequestBody(description = "list of station Ids for which we return the corresponding "
      + "set of reference station membership object")
    List<UUID> stationIds);

  /**
   * Retrieves station memberships with the given site entity id.
   *
   * @param siteIds The {@link UUID}s defining the sites corresponding to the desired memberships
   * @return the memberships
   */
  @Path("/station-reference/reference-station-memberships/site-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve all reference station memberships for the given list of ids")
  Map<UUID, List<ReferenceStationMembership>> retrieveStationMembershipsBySiteId(
    @RequestBody(description = "list of site Ids for which we return the corresponding set of "
      + "reference station membership object")
    List<UUID> siteIds);


  /**
   * Retrieves station memberships with the given station entity id and site entity id.
   *
   * @param request The {@link ReferenceStationMembershipRequest} describing the station and site
   * ids for the {@link ReferenceStationMembership}s to find.
   * @return the memberships
   */
  @Path("/station-reference/reference-station-memberships/site-and-station-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve station memberships for a given station and site pair")
  List<ReferenceStationMembership> retrieveStationMembershipsByStationAndSiteId(
    @RequestBody(description = "request holding the station/site pair")
    ReferenceStationMembershipRequest request);


  /**
   * Stores station memberships
   *
   * @param memberships the memberships
   */
  @Path("/station-reference/reference-station-memberships/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "stores the input list of station memberships")
  void storeStationMemberships(
    @RequestBody(description = "list of station memberships to store")
    Collection<ReferenceStationMembership> memberships);

}

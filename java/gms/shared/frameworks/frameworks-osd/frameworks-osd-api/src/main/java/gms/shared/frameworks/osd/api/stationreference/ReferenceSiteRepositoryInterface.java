package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.stationreference.util.ReferenceSiteMembershipRequest;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
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

public interface ReferenceSiteRepositoryInterface {

  /**
   * Retrieve all {@link ReferenceSite} objects corresponding to the given list of entityIds. If an
   * empty list is passed into the method, this method will return all reference sites.
   *
   * @param entityIds list of entityIds to retrieve {@link ReferenceSite} objects.
   * @return all site
   */
  @Path("/station-reference/reference-sites/entity-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve all reference site objects corresponding to the given list of entity ids. "
    + "If passed an empty list is passed, all reference sites are returned.")
  List<ReferenceSite> retrieveSites(
    @RequestBody(description = "list of entity ids to retrieve corresponding reference sites")
    List<UUID> entityIds);

  /**
   * Retrieve all {@link ReferenceSite} objects corresponding to the given list of names.
   *
   * @param names the names
   * @return all site versions with that names
   */
  @Path("/station-reference/reference-sites/name")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve all reference sites corresponding to the list of names")
  List<ReferenceSite> retrieveSitesByName(
    @RequestBody(description = "list of names to retrieve corresponding reference sites")
    List<String> names);

  /**
   * Stores a list of {@link ReferenceSite} objects.
   *
   * @param sites list of {@link ReferenceSite} objects to store.
   */
  @Path("/station-reference/reference-sites/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "store the list of reference sites")
  void storeReferenceSites(
    @RequestBody(description = "list of reference sites to store")
    Collection<ReferenceSite> sites);

  /**
   * Retrieves {@link ReferenceSiteMembership} objects corresponding to each .
   *
   * @return the memberships
   */
  @Path("/station-reference/reference-site-memberships/site-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Given a list of reference site Ids, "
    + "retrieves the list of reference site memberships associated with each "
    + "individual reference site")
  Map<UUID, List<ReferenceSiteMembership>> retrieveSiteMembershipsBySiteId(
    @RequestBody(description = "list of site ids whose site memberships we want to retrieve.")
    List<UUID> siteIds);

  /**
   * Retrieves site memberships with the given channel entity id.
   *
   * @param channelNames
   * @return the memberships
   */
  @Path("/station-reference/reference-site-memberships/channel-name")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Given a list of channels, "
    + "retrieves the list of reference site memberships associated with each "
    + "individual channel")
  Map<String, List<ReferenceSiteMembership>> retrieveSiteMembershipsByChannelNames(
    @RequestBody(description = "list of channel ids whose site memberships we want to retrieve.")
    List<String> channelNames);

  /**
   * Retrieves site memberships with the given site entity id and channel entity id.
   *
   * @return the memberships
   */
  @Path("/station-reference/reference-site-memberships/site-and-channel")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieves the list of reference site memberships associated with the given "
    + "site and channel")
  List<ReferenceSiteMembership> retrieveSiteMembershipsBySiteIdAndChannelName(
    @RequestBody(description = "reference site membership request containing siteId and channelId "
      + "to retrieve site memberships for.")
    ReferenceSiteMembershipRequest request);


  /**
   * Stores site memberships
   *
   * @param memberships the memberships
   */
  @Path("/station-reference/reference-site-memberships/store")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "stores a collection of reference site memberships into the data store.")
  void storeSiteMemberships(
    @RequestBody(description = "collection of reference site memberships to store")
    Collection<ReferenceSiteMembership> memberships);
}

package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.stationreference.util.NetworkMembershipRequest;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
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

public interface ReferenceNetworkRepositoryInterface {

  /**
   * Retrieve all networks.
   *
   * @param networkIds Collection of Entity Ids representing networks to retrieve. If empty, it will retrieve all networks.
   * @return all networks
   */
  @Path("/station-reference/reference-networks/entity-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve all the provided networks")
  List<ReferenceNetwork> retrieveNetworks(
    @RequestBody(description = "collection of networkIds representing the reference networks to retrieve")
    Collection<UUID> networkIds);

  /**
   * Retrieve all networks for the given list of the name.
   *
   * @param names List of names for all network needs to retrieve. Returns an empty list if nothing is
   * provided.
   * @return all network versions with that name
   */
  @Path("/station-reference/reference-networks/name")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieves the reference networks for the given list of names")
  List<ReferenceNetwork> retrieveNetworksByName(
    @RequestBody(description = "list of names representing the reference networks")
    List<String> names);

  /**
   * Stores a ReferenceNetwork.
   *
   * @param network the network
   */
  @Path("/station-reference/reference-networks/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "stores the given list of reference networks")
  void storeReferenceNetwork(
    @RequestBody(description = "list of networks to store")
    Collection<ReferenceNetwork> network);

  /**
   * Retrieves network memberships  with the given network entity id.
   *
   * @param networkIds the collection of {@link UUID}s identifying the {@link ReferenceNetwork}s
   * to retrieve
   * @return the memberships
   */
  @Path("/station-reference/reference-network-memberships/network-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve a collection of network memberships for the given set of input of "
    + "network ids")
  Map<UUID, List<ReferenceNetworkMembership>> retrieveNetworkMembershipsByNetworkId(
    @RequestBody(description = "collection of networkIds that we want the list of network memberships for")
    Collection<UUID> networkIds);

  /**
   * Retrieves network memberships with the given station entity id
   *
   * @param referenceStationIds
   * @return the memberships
   */
  @Path("/station-reference/reference-network-memberships/station-name")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve a collection of network memberships for the given set of input of "
    + "station names")
  Map<UUID, List<ReferenceNetworkMembership>> retrieveNetworkMembershipsByStationId(
    @RequestBody(description = "collection of station names that we want the list of network memberships for")
    Collection<UUID> referenceStationIds);

  /**
   * Retrieves network memberships with the given network entity id and station entity id.
   *
   * @return the memberships
   */
  @Path("/station-reference/reference-network-memberships/network-and-station-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve a collection of network memberships for the given network ID and "
    + "station names")
  List<ReferenceNetworkMembership> retrieveNetworkMembershipsByNetworkAndStationId(
    @RequestBody(description = "request containing the given networkId and stationName for which "
      + "we want the list of network memberships")
    NetworkMembershipRequest request);

  /**
   * Stores network memberships
   *
   * @param memberships the memberships
   */
  @Path("/station-reference/reference-network-memberships/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "stores the given collection of reference network memberships")
  void storeNetworkMemberships(
    @RequestBody(description = "collection of reference network memberships to store")
    Collection<ReferenceNetworkMembership> memberships);

}

package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.util.ReferenceChannelRequest;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

public interface ReferenceChannelRepositoryInterface {

  /**
   * Retrieves {@link ReferenceChannel}s for specified ReferenceChannel names
   *
   * @param referenceChannelRequest names/entityids/versionids of ReferenceChannels to retrieve
   * @return ReferenceChannels for specified reference channel names
   */
  @Path("/station-reference/reference-channels")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve Reference Channels")
  List<ReferenceChannel> retrieveReferenceChannels(
    @RequestBody(description = "Channel names or entityids or versionids for ReferenceChannels "
      + "to retrieve")
    ReferenceChannelRequest referenceChannelRequest);

  /**
   * Stores {@link ReferenceChannel}s
   *
   * @param channels the {@link ReferenceChannel}s to store
   */
  @Path("/station-reference/reference-channel/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Store Reference Channels")
  void storeReferenceChannels(
    @RequestBody(description = "Collection of ReferenceChannels to store")
    Collection<ReferenceChannel> channels);
}

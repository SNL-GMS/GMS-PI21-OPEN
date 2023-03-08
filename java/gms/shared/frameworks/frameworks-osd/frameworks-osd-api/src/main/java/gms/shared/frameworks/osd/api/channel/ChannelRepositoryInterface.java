package gms.shared.frameworks.osd.api.channel;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.channel.Channel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ChannelRepositoryInterface {

  /**
   * Returns a list of {@link Channel} objects that corresponds to the list of channelIds passed in. If
   * no ids are passed into this interface, all channels from all stations are retrieved.
   *
   * @param channelIds List of channelIds to return the list of Channel Objects for. This list can be empty.
   * @return list of all {@link Channel} objects for the given set of channel ids (or all channels if no filtered list
   * of channel ids are passed).
   */
  @Path("/channels")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieves All Channels")
  List<Channel> retrieveChannels(
    @RequestBody(description = "Collection of Channel IDs to retrieve from the database")
    Collection<String> channelIds);

  /**
   * Batch {@link Channel} store operation. This will, given a non-empty collection of channels, will store
   * given set of channels in the database.
   *
   * @param channels non-empty collection of {@link Channel} objects
   * @return list of the channel names that were stored into the database.
   */
  @Path("/channels/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  Set<String> storeChannels(Collection<Channel> channels);
}

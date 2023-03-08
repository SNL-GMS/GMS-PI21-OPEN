package gms.shared.stationdefinition.api.channel;

import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeFacetRequest;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;

import java.time.Instant;
import java.util.List;

public interface ChannelGroupAccessorInterface extends ChannelGroupRepositoryInterface {

  /**
   * Retrieves all {@link ChannelGroup} objects specified by a list of channel group names,
   * effective time instant and faceting definition.
   * If the list is empty, the server will return an empty list of channel groups.
   *
   * @param channelGroupNames - list of channel group names
   * @param effectiveTime - effective time for query
   * @param facetingDefinition - FacetingDefintion from the {@link ChannelGroupsTimeFacetRequest}
   * @return list of {@link ChannelGroup} objects
   */
  List<ChannelGroup> findChannelGroupsByNameAndTime(List<String> channelGroupNames,
    Instant effectiveTime, FacetingDefinition facetingDefinition);

}

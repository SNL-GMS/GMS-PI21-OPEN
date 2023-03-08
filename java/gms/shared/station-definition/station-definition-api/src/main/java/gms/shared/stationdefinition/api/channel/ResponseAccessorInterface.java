package gms.shared.stationdefinition.api.channel;

import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ResponseAccessorInterface extends ResponseRepositoryInterface {

  /**
   * Retrieves {@link Response}s having the provided responseIds, effectiveTime
   * and applies the provided faceting definition to the retrieved {@link Response}s
   *
   * @param responseIds the collection of UUIDs
   * @param effectiveTime instant effective time
   * @param facetingDefinition faceting definition defining how responses should be populated
   * @return list of {@link Response}s
   */
  List<Response> findResponsesById(Collection<UUID> responseIds, Instant effectiveTime,
    FacetingDefinition facetingDefinition);
}

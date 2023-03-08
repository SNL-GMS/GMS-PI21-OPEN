package gms.shared.stationdefinition.api.station;

import gms.shared.stationdefinition.api.station.util.StationGroupsTimeFacetRequest;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.StationGroup;

import java.time.Instant;
import java.util.List;

public interface StationGroupAccessorInterface extends StationGroupRepositoryInterface {

  /**
   * Retrieves all {@link StationGroup} objects specified by a list of station group names,
   * effective time instant and faceting definition.
   * If the list is empty, the server will return an empty list of station groups.
   *
   * @param stationGroupNames - list of station group names
   * @param effectiveTime - effective time for query
   * @param facetingDefinition - FacetingDefintion from the {@link StationGroupsTimeFacetRequest}
   * @return list of {@link StationGroup} objects
   */
  List<StationGroup> findStationGroupsByNameAndTime(List<String> stationGroupNames,
    Instant effectiveTime, FacetingDefinition facetingDefinition);

}

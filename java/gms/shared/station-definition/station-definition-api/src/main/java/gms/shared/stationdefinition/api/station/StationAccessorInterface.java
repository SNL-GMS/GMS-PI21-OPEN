package gms.shared.stationdefinition.api.station;

import gms.shared.stationdefinition.api.station.util.StationsTimeFacetRequest;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;

import java.time.Instant;
import java.util.List;

public interface StationAccessorInterface extends StationRepositoryInterface {
  /**
   * Given a list of stationNames, effectiveTime and facetingDefinition this method
   * will return those list of Stations.
   * If an empty list is provided, this method will return an empty list of stations.
   *
   * @param stationNames - list of station names
   * @param effectiveTime - effective time for query
   * @param facetingDefinition - FacetingDefintion from the {@link StationsTimeFacetRequest}
   * @return List of Station objects
   */
  List<Station> findStationsByNameAndTime(List<String> stationNames,
    Instant effectiveTime, FacetingDefinition facetingDefinition);

  /**
   * Retrieves the times at which the provided {@link Station} and any of its aggregate objects changed, within the
   * provided time range.
   *
   * @param station the {@link Station} to retrieve all change times for
   * @param startTime the beginning of the change time range to retrieve
   * @param endTime the end of the change time range to retrieve
   * @return a collection of {@link Instant}s representing times when new versions of the {@link Station} and/or any
   * of its aggregate objects became effective
   */
  List<Instant> determineStationChangeTimes(Station station,
    Instant startTime,
    Instant endTime);
}

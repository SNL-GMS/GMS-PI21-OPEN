package gms.shared.stationdefinition.api.station;

import gms.shared.stationdefinition.coi.station.Station;

import java.time.Instant;
import java.util.List;

public interface StationRepositoryInterface {

  /**
   * Given a list of stationNames, effectiveTime and facetingDefinition this method
   * will return those list of Stations.
   * If an empty list is provided, this method will return an empty list of stations.
   *
   * @param stationNames - List of station names,
   * @param effectiveTime The effective time of the stations
   * @return List of Station objects
   */
  List<Station> findStationsByNameAndTime(List<String> stationNames, Instant effectiveTime);

  /**
   * Retrieves all {@link Station} objects specified by a list of station names and
   * time range bounding the station.
   * If the list is empty, the server will return an empty list of stations.
   *
   * @param stationNames The list of station names
   * @param startTime The earliest allowable effective time of the stations
   * @param endTime The latest allowable effective time of the stations
   * @return list of {@link Station} objects
   */
  List<Station> findStationsByNameAndTimeRange(List<String> stationNames, Instant startTime, Instant endTime);

  /**
   * Store the provided stations
   *
   * @param stations the stations to store
   */
  default void storeStations(List<Station> stations) {
    // no op
  }

}

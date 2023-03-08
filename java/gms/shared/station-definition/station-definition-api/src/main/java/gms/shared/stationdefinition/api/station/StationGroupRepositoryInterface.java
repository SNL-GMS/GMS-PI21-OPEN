package gms.shared.stationdefinition.api.station;

import gms.shared.stationdefinition.coi.station.StationGroup;

import java.time.Instant;
import java.util.List;

public interface StationGroupRepositoryInterface {

  /**
   * Finds {@link StationGroup}s having one of the provided names that were active at the effective time
   *
   * @param stationGroupNames The names of the station groups to find
   * @param effectiveAt The effective time at which the station groups must be active
   * @return A list of {@link StationGroup}s with provided names and effective time
   */
  List<StationGroup> findStationGroupsByNameAndTime(List<String> stationGroupNames, Instant effectiveAt);

  /**
   * Finds {@link StationGroup}s having one of the provided names that were active at the effective time
   *
   * @param stationGroupNames The names of the station groups to find
   * @param startTime The earliest allowable effective time of the station groups
   * @param endTime The latest allowable effective time of the station groups
   * @return A list of {@link StationGroup}s with the provided names and active between the provided times
   */
  List<StationGroup> findStationGroupsByNameAndTimeRange(List<String> stationGroupNames, Instant startTime,
    Instant endTime);


  /**
   * Stores the provided station groups
   *
   * @param stationGroups the station groups to store
   */
  default void storeStationGroups(List<StationGroup> stationGroups) {
    // no op
  }

}

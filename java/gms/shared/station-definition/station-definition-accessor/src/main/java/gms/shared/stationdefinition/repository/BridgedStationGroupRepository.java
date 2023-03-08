package gms.shared.stationdefinition.repository;


import com.google.common.collect.Range;
import gms.shared.stationdefinition.api.station.StationGroupRepositoryInterface;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.converter.util.assemblers.StationGroupAssembler;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.NetworkStationTimeKey;
import gms.shared.stationdefinition.database.connector.AffiliationDatabaseConnector;
import gms.shared.stationdefinition.database.connector.NetworkDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link StationGroupRepositoryInterface} implementation that uses a bridged database to provide {@link StationGroup}
 * instances
 */
@Component("bridgedStationGroupRepository")
public class BridgedStationGroupRepository implements StationGroupRepositoryInterface {

  private final BridgedStationRepository bridgedStationRepository;
  private final NetworkDatabaseConnector networkDatabaseConnector;
  private final AffiliationDatabaseConnector affiliationDatabaseConnector;
  private final SiteDatabaseConnector siteDatabaseConnector;
  private final StationGroupAssembler stationGroupAssembler;

  public BridgedStationGroupRepository(BridgedStationRepository bridgedStationRepository,
    NetworkDatabaseConnector networkDatabaseConnector,
    AffiliationDatabaseConnector affiliationDatabaseConnector,
    SiteDatabaseConnector siteDatabaseConnector,
    StationGroupAssembler stationGroupAssembler) {
    this.bridgedStationRepository = bridgedStationRepository;
    this.networkDatabaseConnector = networkDatabaseConnector;
    this.affiliationDatabaseConnector = affiliationDatabaseConnector;
    this.siteDatabaseConnector = siteDatabaseConnector;
    this.stationGroupAssembler = stationGroupAssembler;
  }

  @Override
  public List<StationGroup> findStationGroupsByNameAndTime(List<String> stationGroupNames, Instant effectiveTime) {
    List<NetworkDao> networkDaos = getNetworksByName(stationGroupNames);

    // Query all AffiliationDaos associated with NetworkDao Ids and Request EffectiveTime
    List<String> networkNames = networkDaos.stream()
      .map(NetworkDao::getNet)
      .collect(Collectors.toList());

    List<AffiliationDao> affiliationDaos = affiliationDatabaseConnector.findAffiliationsByNameAndTime(
      networkNames, effectiveTime);

    List<AffiliationDao> nextAffiliationDaos = affiliationDatabaseConnector.findNextAffiliationByNameAfterTime(
      networkNames, effectiveTime);

    List<String> referenceStations = affiliationDaos.stream()
      .map(AffiliationDao::getNetworkStationTimeKey)
      .map(NetworkStationTimeKey::getStation)
      .distinct()
      .collect(Collectors.toList());

    List<Station> stations = bridgedStationRepository.findStationsByNameAndTime(referenceStations, effectiveTime);

    return stationGroupAssembler.buildAllForTime(effectiveTime, networkDaos, affiliationDaos,
      nextAffiliationDaos, stations);

  }

  @Override
  public List<StationGroup> findStationGroupsByNameAndTimeRange(List<String> stationGroupNames,
    Instant startTime,
    Instant endTime) {

    List<NetworkDao> networkDaos = getNetworksByName(stationGroupNames);
    List<String> networkNames = networkDaos.stream()
      .map(NetworkDao::getNet)
      .collect(Collectors.toList());

    List<AffiliationDao> affiliationDaos = affiliationDatabaseConnector.findAffiliationsByNameAndTimeRange(networkNames,
      startTime, endTime);
    List<String> referenceStations = affiliationDaos.stream()
      .map(AffiliationDao::getNetworkStationTimeKey)
      .map(NetworkStationTimeKey::getStation)
      .distinct()
      .collect(Collectors.toList());

    List<AffiliationDao> nextAffiliationDaos = affiliationDatabaseConnector.findNextAffiliationByNameAfterTime(
      networkNames, endTime);
    List<Station> stations = bridgedStationRepository.findStationsByNameAndTimeRange(referenceStations, startTime, endTime);
    return stationGroupAssembler.buildAllForTimeRange(Range.closed(startTime, endTime),
      networkDaos, affiliationDaos, nextAffiliationDaos, stations);
  }

  /**
   * Query for {@link NetworkDao}s using list of network names
   *
   * @param networkNames - list of names
   * @return list of NetworkDaos
   */
  private List<NetworkDao> getNetworksByName(Collection<String> networkNames) {
    return networkDatabaseConnector.findNetworks(networkNames);
  }
}

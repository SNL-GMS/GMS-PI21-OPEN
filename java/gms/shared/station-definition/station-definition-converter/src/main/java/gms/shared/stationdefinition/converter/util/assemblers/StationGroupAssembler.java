package gms.shared.stationdefinition.converter.util.assemblers;


import com.google.common.base.Functions;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.coi.utils.comparator.StationGroupComparator;
import gms.shared.stationdefinition.converter.interfaces.StationGroupConverter;
import gms.shared.stationdefinition.converter.util.TemporalMap;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.NetworkStationTimeKey;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Assemble CSS DAOS into COI StationGroups
 */
@Component
public class StationGroupAssembler {

  private static final Logger logger = LoggerFactory.getLogger(StationGroupAssembler.class);

  BiPredicate<NetworkDao, NetworkDao> changeOccuredForSite = (NetworkDao prev, NetworkDao curr) -> {
    if (Objects.isNull(prev) || Objects.isNull(curr)) {
      return true;
    }
    return !prev.getDescription().equals(curr.getDescription());
  };

  private final StationGroupConverter stationGroupConverter;

  public StationGroupAssembler(StationGroupConverter stationGroupConverter) {
    this.stationGroupConverter = stationGroupConverter;
  }

  public List<StationGroup> buildAllForTime(Instant effectiveAt,
    List<NetworkDao> networkDaos,
    List<AffiliationDao> affiliationDaos,
    List<AffiliationDao> nextAffiliationDaos,
    List<Station> stations) {

    Objects.requireNonNull(effectiveAt, "Cannot build station groups from null effective time");
    Objects.requireNonNull(networkDaos, "Cannot build station groups from null networks");
    Objects.requireNonNull(affiliationDaos, "Cannot build station groups from null affiliations");
    Objects.requireNonNull(stations, "Cannot build with null stations");

    UnaryOperator<Station> stationFacetOperator = station -> Station.createVersionReference(station.getName(),
      station.getEffectiveAt().orElseThrow());

    List<StationGroup> stationGroups =
      createStationGroupTablesAndMaps(Pair.of(effectiveAt, effectiveAt), networkDaos, affiliationDaos, nextAffiliationDaos, stations, stationFacetOperator);
    Map<String, List<StationGroup>> stationGroupMap = stationGroups.stream().collect(groupingBy(StationGroup::getName));

    //we need to remove the prev/next versions since they were only used to set the start/end time
    //we also return the version that is effectiveAt the request time or the one previous to it if none exist
    return stationGroupMap.values().stream()
      .map(list -> list.stream()
        .filter(station -> !station.getEffectiveAt().get().isAfter(effectiveAt))
        .sorted(new StationGroupComparator())
        .sorted(Comparator.reverseOrder())
        .findFirst())
      .filter(Optional::isPresent)
      .map(Optional::get)
      .sorted(new StationGroupComparator())
      .collect(Collectors.toList());
  }

  public List<StationGroup> buildAllForTimeRange(Range<Instant> timeRange, List<NetworkDao> networkDaos,
    List<AffiliationDao> affiliationDaos, List<AffiliationDao> nextAffiliationDaos, List<Station> stations) {

    Objects.requireNonNull(timeRange);
    Objects.requireNonNull(networkDaos);
    Objects.requireNonNull(affiliationDaos);
    Objects.requireNonNull(stations);

    UnaryOperator<Station> stationFacetOperator = Station::toEntityReference;

    return createStationGroupTablesAndMaps(Pair.of(timeRange.lowerEndpoint(), timeRange.upperEndpoint()), networkDaos,
      affiliationDaos, nextAffiliationDaos, stations, stationFacetOperator).stream()
      .filter(stationGroup -> !stationGroup.getEffectiveUntil().isPresent() ||
        !stationGroup.getEffectiveUntil().get().isBefore(timeRange.lowerEndpoint()))
      .filter(stationGroup -> !stationGroup.getEffectiveAt().orElseThrow().isAfter(timeRange.upperEndpoint()))
      .sorted(new StationGroupComparator())
      .collect(Collectors.toList());
  }

  private List<StationGroup> createStationGroupTablesAndMaps(Pair<Instant, Instant> startEndTime,
    List<NetworkDao> networkDaos,
    List<AffiliationDao> affiliationDaos,
    List<AffiliationDao> nextAffiliationDaos,
    List<Station> stations,
    UnaryOperator<Station> stationFacetOperator) {

    List<AffiliationDao> allAffiliationDaos = new ArrayList<>();
    allAffiliationDaos.addAll(affiliationDaos);
    allAffiliationDaos.addAll(nextAffiliationDaos);

    TemporalMap<String, NetworkDao> netToNetworkMap = networkDaos.stream()
      .collect(TemporalMap.collector(NetworkDao::getNet, this::getNetworkDaoOnDate));

    Table<String, String, NavigableMap<Instant, AffiliationDao>> netToAffiliationMap =
      AssemblerUtils.buildVersionTable(Functions.compose(NetworkStationTimeKey::getNetwork, AffiliationDao::getNetworkStationTimeKey),
        Functions.compose(NetworkStationTimeKey::getStation, AffiliationDao::getNetworkStationTimeKey),
        Functions.compose(NetworkStationTimeKey::getTime, AffiliationDao::getNetworkStationTimeKey),
        allAffiliationDaos);

    TemporalMap<String, Station> stationTemporalMap = stations.stream()
      .collect(TemporalMap.collector(Station::getName, Functions.compose(Optional::get, Station::getEffectiveAt)));

    return netToNetworkMap.keySet().stream()
      .flatMap(netString ->
        processStationGroups(
          startEndTime,
          netToNetworkMap.getVersionMap(netString),
          netToAffiliationMap.row(netString),
          stationTemporalMap,
          stationFacetOperator
        ).stream())
      .filter(Objects::nonNull)
      .sorted()
      .collect(Collectors.toList());
  }

  private List<StationGroup> processStationGroups(
    Pair<Instant, Instant> startEndTime,
    NavigableMap<Instant, NetworkDao> netToNetworkMap,
    Map<String, NavigableMap<Instant, AffiliationDao>> affiliationDaos,
    TemporalMap<String, Station> stationTemporalMap,
    UnaryOperator<Station> stationFacetOperator) {

    //determine if range or single point in time
    boolean isRange = startEndTime.getLeft().isBefore(startEndTime.getRight());
    Range<Instant> stationGroupRange = Range.closed(startEndTime.getLeft(), startEndTime.getRight());

    var netWorksInRange =
      AssemblerUtils.getDaosInRange(stationGroupRange, netToNetworkMap, this::getNetworkDaoOnDate, this::getNetworkDaoOffDate);

    var affiliationsInRange = AssemblerUtils.getDaosWithinRange(stationGroupRange, affiliationDaos,
      Functions.compose(NetworkStationTimeKey::getTime, AffiliationDao::getNetworkStationTimeKey), AffiliationDao::getEndTime);

    NavigableSet<Instant> possibleVersionTimes = getChangeTimes(netWorksInRange, affiliationsInRange);

    SortedSet<Instant> validTimes = AssemblerUtils.getValidTimes(startEndTime, possibleVersionTimes, isRange);
    return processPossibleVersionTimes(new ArrayList<>(validTimes), netWorksInRange, affiliationsInRange, stationTemporalMap, stationFacetOperator);
  }

  private List<StationGroup> processPossibleVersionTimes(
    List<Instant> possibleVersionTimes,
    NavigableMap<Instant, NetworkDao> networkDaos,
    Map<String, NavigableMap<Instant, AffiliationDao>> affiliationDaos,
    TemporalMap<String, Station> stationTemporalMap,
    UnaryOperator<Station> stationFacetOperator) {

    List<StationGroup> versionedStationGroups = new ArrayList<>();
    for (var i = 0; i < possibleVersionTimes.size() - 1; i++) {
      Range<Instant> versionRange = Range.open(possibleVersionTimes.get(i), possibleVersionTimes.get(i + 1));
      var currTime = versionRange.lowerEndpoint();

      Optional<NetworkDao> optNetworkDao = AssemblerUtils.getObjectsForVersionTime(currTime,
        networkDaos, this::getNetworkDaoOffDate);

      List<String> staCodes = affiliationDaos.entrySet().stream()
        .map(entry -> {
          var currAffiliation = entry.getValue().floorEntry(currTime);

          if(currAffiliation != null && currAffiliation.getValue().getEndTime().isAfter(currTime)){
            return entry.getKey();
          }
          return null;
        }).filter(Objects::nonNull)
        .collect(Collectors.toList());

      List<Station> stations = staCodes.stream()
        .map(sta -> getNearestInTimeStation(stationTemporalMap, currTime, sta))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(station -> station.getEffectiveUntil().orElse(Instant.MAX).isAfter(currTime))
        .map(stationFacetOperator::apply)
        .collect(Collectors.toList());

      if (stations.isEmpty() || optNetworkDao.isEmpty()) {
        logger.info("No Active Stations or Network Dao for time range: " + versionRange);
        continue;
      }

      var endtime = AssemblerUtils.getImmediatelyBeforeInstant(versionRange.upperEndpoint());
      var endTimeOpt = Optional.of(endtime);
      if(endtime.equals(Instant.MAX)){
        endTimeOpt = Optional.empty();
      }

      try {
        var stationGroup = stationGroupConverter.convert(currTime, endTimeOpt, optNetworkDao.get(), stations);
        versionedStationGroups.add(stationGroup);
      } catch (Exception ex) {
        logger.warn(ex.getMessage());
      }
    }

    return versionedStationGroups;
  }

  private Optional<Station> getNearestInTimeStation(TemporalMap<String, Station> stationTemporalMap, Instant currTime, String sta){

    Optional<Station> currStation = stationTemporalMap.getVersionFloor(sta, currTime);

    if(currStation.isEmpty()){
      currStation = stationTemporalMap.getVersionCeiling(sta, currTime);
    }

    return currStation;
  }

  private NavigableSet<Instant> getChangeTimes(NavigableMap<Instant, NetworkDao> networkDaos,
    Map<String, NavigableMap<Instant, AffiliationDao>> affiliationDaos) {

    NavigableSet<Instant> changeTimes = getChangeTimesForNetworkDaos(networkDaos);
    changeTimes.addAll(AssemblerUtils.getTimesForObjectChanges(affiliationDaos,
      Functions.compose(NetworkStationTimeKey::getTime, AffiliationDao::getNetworkStationTimeKey), AffiliationDao::getEndTime,
      Functions.compose(NetworkStationTimeKey::getStation, AffiliationDao::getNetworkStationTimeKey)));

    return changeTimes;
  }

  private NavigableSet<Instant> getChangeTimesForNetworkDaos(NavigableMap<Instant, NetworkDao> networkDaos) {

    var changeTimes = new TreeSet<Instant>();

    NetworkDao prev = null;
    NetworkDao curr;

    for (var entry : networkDaos.entrySet()) {

      curr = entry.getValue();
      if (prev == null) {
        changeTimes.add(getNetworkDaoOnDate(curr));
      } else if (!AssemblerUtils.fullTimePrecisionObjectAdjacent(getNetworkDaoOffDate(prev), getNetworkDaoOnDate(curr))) {
        changeTimes.add(AssemblerUtils.getImmediatelyAfterInstant(getNetworkDaoOffDate(prev)));
        changeTimes.add(getNetworkDaoOnDate(curr));
      } else if (changeOccuredForSite.test(prev, curr)) {
        changeTimes.add(getNetworkDaoOnDate(curr));
      }
      prev = curr;
    }

    if (prev != null) {
      changeTimes.add(AssemblerUtils.getImmediatelyAfterInstant(getNetworkDaoOffDate(prev)));
    }
    return changeTimes;
  }

  private Instant getNetworkDaoOnDate(NetworkDao networkDao) {
    return Optional.ofNullable(networkDao.getOnDate())
      .orElse(Instant.EPOCH);
  }

  private Instant getNetworkDaoOffDate(NetworkDao networkDao) {
    return Optional.ofNullable(networkDao.getOffDate())
      .orElse(Instant.MAX);
  }
}
package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.coi.utils.StationDefinitionObject;
import gms.shared.stationdefinition.converter.util.TemporalMap;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class AssemblerUtils {

  private AssemblerUtils() {
  }

  public static final Duration MAX_GAP_ALLOWED = Duration.ofSeconds(1);

  public static final UnaryOperator<Instant> effectiveAtStartOfDayOffset = effectiveUntil -> {
    if (!effectiveUntil.equals(Instant.MAX)) {
      effectiveUntil = effectiveUntil.atZone(ZoneOffset.UTC).withHour(00).withMinute(00).withSecond(00).withNano(0).toInstant();
    }
    return effectiveUntil;
  };

  public static final UnaryOperator<Instant> effectiveUntilNoonOffset = effectiveUntil -> {
    if (!effectiveUntil.equals(Instant.MAX)) {
      effectiveUntil = effectiveUntil.atZone(ZoneOffset.UTC).withHour(11).withMinute(59).withSecond(59).withNano(999000000).toInstant();
    }
    return effectiveUntil;
  };

  public static final UnaryOperator<Instant> effectiveUntilEndOfDay = effectiveUntil -> {
    if (!effectiveUntil.equals(Instant.MAX)) {
      effectiveUntil = effectiveUntil.atZone(ZoneOffset.UTC).withHour(23).withMinute(59).withSecond(59).withNano(999000000).toInstant();
    }
    return effectiveUntil;
  };

  public static final UnaryOperator<Instant> effectiveAtNoonOffset = effectiveUntil -> {
    if (!effectiveUntil.equals(Instant.MAX)) {
      effectiveUntil = effectiveUntil.atZone(ZoneOffset.UTC).withHour(12).withMinute(00).withSecond(00).withNano(000000000).toInstant();
    }
    return effectiveUntil;
  };

  public static boolean changeTimeExistsForDay(Instant time, NavigableSet<Instant> changeTimes) {

    time = time.truncatedTo(ChronoUnit.DAYS);
    var correspondingTime = changeTimes.ceiling(time);
    return correspondingTime != null && correspondingTime.truncatedTo(ChronoUnit.DAYS).equals(time);
  }

  public static boolean siteOrSiteChansAdjacent(Instant prevTime, Instant startTime) {

    //1 or less day difference between times
    return Duration.between(prevTime, startTime).compareTo(Duration.ofDays(1)) <= 0;
  }

  public static boolean onSameDay(Instant prev, Instant curr) {
    return prev.truncatedTo(ChronoUnit.DAYS).equals(curr.truncatedTo(ChronoUnit.DAYS));
  }

  public static boolean instantGreaterOrEqual(Instant i1, Instant i2) {
    return i1.isAfter(i2) || i1.equals(i2);
  }

  public static boolean fullTimePrecisionObjectAdjacent(Instant prev, Instant curr) {

    return Duration.between(prev, curr).abs().compareTo(MAX_GAP_ALLOWED) <= 0;
  }

  /**
   * Builds a table of version for a provided  collection of objects
   *
   * @param rowKeyExtractor A function that extracts the first key (row key) from the object to be versioned
   * @param columnKeyExtractor A function that extracts the second key (column key) from the object to be versioned
   * @param versionKeyExtractor A function that extracts the key that defines the version of the object
   * @param versions The objects to be versioned
   * @param <R> The type of the row key
   * @param <C> The type of the column key
   * @param <V> The type of object to be versioned
   * @return A table of versions of object, based on row and column keys
   */
  public static <R, C, V> Table<R, C, NavigableMap<Instant, V>> buildVersionTable(Function<V, R> rowKeyExtractor,
    Function<V, C> columnKeyExtractor,
    Function<V, Instant> versionKeyExtractor,
    Collection<V> versions) {

    if (versions == null) {
      return HashBasedTable.create();
    }

    return versions.stream()
      .collect(Collector.of(HashBasedTable::create,
        (table, version) -> {
          var rowKey = rowKeyExtractor.apply(version);
          var columnKey = columnKeyExtractor.apply(version);
          if (!table.contains(rowKey, columnKey)) {
            table.put(rowKey, columnKey, new TreeMap<>());
          }

          table.get(rowKey, columnKey).put(versionKeyExtractor.apply(version), version);
        },
        (table1, table2) -> {
          table2.cellSet().forEach(cell -> {
            if (!table1.contains(cell.getRowKey(), cell.getColumnKey())) {
              table1.put(cell.getRowKey(), cell.getColumnKey(), cell.getValue());
            } else {
              table1.get(cell.getRowKey(), cell.getColumnKey()).putAll(cell.getValue());
            }
          });

          return table1;
        }));
  }

  /*
   * Check for name changes of sub-objects that trigger top object change
   *
   */
  public static <T extends StationDefinitionObject> NavigableSet<Instant> getTimesForObjectChanges(
    Map<String, NavigableMap<Instant, T>> sdoByStaChan) {

    return getTimesForObjectChanges(sdoByStaChan, sdo -> sdo.getEffectiveAt().orElseThrow(),
      sdo -> sdo.getEffectiveUntil().orElse(Instant.MAX), StationDefinitionObject::getName);
  }

  public static <T> NavigableSet<Instant> getTimesForObjectChanges(
    Map<String, NavigableMap<Instant, T>> map,
    Function<T, Instant> startTimeExtractor,
    Function<T, Instant> endTimeExtractor,
    Function<T, String> nameExtractor) {

    var versionChanges = new TreeSet<Instant>();
    var possibleVersionTimes = getPossibleChangeTimesForObjects(map, startTimeExtractor, endTimeExtractor);

    Iterator<Instant> versionTimeIterator = possibleVersionTimes.iterator();
    Set<String> currNames;
    Set<String> prevNames = Collections.emptySet();

    if (versionTimeIterator.hasNext()) {

      var currInstant = versionTimeIterator.next();
      versionChanges.add(currInstant);

      prevNames = map.values().stream()
        .map(naviMap -> naviMap.floorEntry(currInstant))
        .filter(Objects::nonNull)
        .map(Map.Entry::getValue)
        .filter(sdo -> !endTimeExtractor.apply(sdo).isBefore(currInstant))
        .map(nameExtractor::apply)
        .collect(Collectors.toSet());
    }

    while (versionTimeIterator.hasNext()) {

      var currInstant = versionTimeIterator.next();

      currNames = map.values().stream()
        .map(naviMap -> naviMap.floorEntry(currInstant))
        .filter(Objects::nonNull)
        .map(Map.Entry::getValue)
        .filter(sdo -> !endTimeExtractor.apply(sdo).isBefore(currInstant))
        .map(nameExtractor::apply)
        .collect(Collectors.toSet());

      if (!prevNames.equals(currNames)) {
        versionChanges.add(currInstant);
      }
      prevNames = currNames;
    }
    return versionChanges;

  }

  public static <T> NavigableSet<Instant> getPossibleChangeTimesForObjects(Map<String, NavigableMap<Instant, T>> map,
    Function<T, Instant> startTimeExtractor,
    Function<T, Instant> endTimeExtractor) {

    var possibleVersionTimes = new TreeSet<Instant>();

    for (NavigableMap<Instant, T> sdoMap : map.values()) {
      Instant prevTime = null;
      for (Map.Entry<Instant, T> sdo : sdoMap.entrySet()) {
        Instant effectiveAt = startTimeExtractor.apply(sdo.getValue());

        possibleVersionTimes.add(effectiveAt);

        if (prevTime != null && !fullTimePrecisionObjectAdjacent(prevTime, effectiveAt)) {
          possibleVersionTimes.add(getImmediatelyAfterInstant(prevTime));
        }

        prevTime = endTimeExtractor.apply(sdo.getValue());
      }

      if (prevTime != null) {
        possibleVersionTimes.add(getImmediatelyAfterInstant(prevTime));
      }
    }
    return possibleVersionTimes;

  }

  /**
   * Returns a list of objects in range
   *
   * @param versionTime the instant to get objects for
   * @param navMap a map of navigableMaps of the object
   * @param endTimeExtractor a function to get the end time of the object
   * @param <V> The type of the object
   * @return A list of objects
   */
  public static <V> List<V> getObjectsForVersionTime(Instant versionTime,
    Map<String, NavigableMap<Instant, V>> navMap,
    Function<V, Instant> endTimeExtractor) {
    
    if(navMap == null){
      return Collections.emptyList();
    }
    
    return navMap.values().stream()
      .map(naviMap -> naviMap.floorEntry(versionTime))
      .filter(Objects::nonNull)
      .map(Map.Entry::getValue)
      .filter(sdo -> endTimeExtractor.apply(sdo).isAfter(versionTime))
      .collect(Collectors.toList());
  }

  /**
   * Returns a list of objects in range
   *
   * @param versionTime the Instant of time to get objects for
   * @param navMap a map of navigableMaps of the object
   * @param endTimeExtractor a function to get the end time of the object
   * @param <V> The type of the object
   * @return An optional object
   */
  public static <V> Optional<V> getObjectsForVersionTime(Instant versionTime,
    NavigableMap<Instant, V> navMap,
    Function<V, Instant> endTimeExtractor) {
    
    if(navMap == null){
      return Optional.empty();
    }

    var objAtTime = navMap.floorEntry(versionTime);

    if (Objects.nonNull(objAtTime) &&
      endTimeExtractor.apply(objAtTime.getValue()).isAfter(versionTime)) {
      return Optional.of(objAtTime.getValue());
    }

    return Optional.empty();
  }

  /**
   * Returns a Map of sta or sta+chan code to a navigable map of daos within range
   *
   * @param range the
   * @param daosByStationCode a temporal map of daos to a sta code string
   * @param stationCodes a set of sta codes
   * @param startTimeExtractor a function to get the start time of the dao
   * @param endTimeExtractor a function to get the end time of the dao
   * @param <V> The type of the dao
   * @return A map of navigable maps
   */
  public static <V> Map<String, NavigableMap<Instant, V>> getDaosWithinRange(
    Range<Instant> range,
    TemporalMap<String, V> daosByStationCode,
    Set<String> stationCodes,
    Function<V, Instant> startTimeExtractor,
    Function<V, Instant> endTimeExtractor) {

    var daoStream = stationCodes.stream()
      .map(stationCode -> Pair.of(stationCode, daosByStationCode.getVersionMap(stationCode)));

    return getSubMap(range, daoStream, startTimeExtractor, endTimeExtractor);
  }

  /**
   * Returns a Map of chan code to a navigable map of daos within range
   *
   * @param range the
   * @param daosByChanCode a temporal map of daos to a chan code string
   * @param startTimeExtractor a function to get the start time of the dao
   * @param endTimeExtractor a function to get the end time of the dao
   * @param <V> The type of the dao
   * @return A map of navigable maps
   */
  public static <V> Map<String, NavigableMap<Instant, V>> getDaosWithinRange(
    Range<Instant> range,
    Map<String, NavigableMap<Instant, V>> daosByChanCode,
    Function<V, Instant> startTimeExtractor,
    Function<V, Instant> endTimeExtractor) {

    var daoStream = daosByChanCode.entrySet().stream()
      .map(entry -> Pair.of(entry.getKey(), entry.getValue()));

    return getSubMap(range, daoStream, startTimeExtractor, endTimeExtractor);
  }

  /**
   * Returns a Map of sta or sta+chan code to a navigable map of daos within range
   *
   * @param range the Instant range
   * @param daosByStationAndChannel a table of navigable map of daos to a sta code and chan code
   * @param stationCodes a set of sta codes
   * @param startTimeExtractor a function to get the start time of the dao
   * @param endTimeExtractor a function to get the end time of the dao
   * @param <V> The type of the dao
   * @return A map of navigable maps
   */
  public static <V> Map<String, NavigableMap<Instant, V>> getDaosWithinRange(
    Range<Instant> range,
    Table<String, String, NavigableMap<Instant, V>> daosByStationAndChannel,
    Set<String> stationCodes,
    Function<V, Instant> startTimeExtractor,
    Function<V, Instant> endTimeExtractor) {

    var daoStream = stationCodes.stream()
      .map(stationCode -> Pair.of(stationCode, daosByStationAndChannel.row(stationCode).entrySet()))
      .flatMap(pair ->
        pair.getRight().stream()
          .map(entry -> Pair.of(pair.getLeft() + "." + entry.getKey(), entry.getValue())));

    return getSubMap(range, daoStream, startTimeExtractor, endTimeExtractor);
  }

  private static <V> Map<String, NavigableMap<Instant, V>> getSubMap(
    Range<Instant> range,
    Stream<Pair<String, NavigableMap<Instant, V>>> daosByStationAndChannel,
    Function<V, Instant> startTimeExtractor,
    Function<V, Instant> endTimeExtractor) {

    return daosByStationAndChannel
      .map(navMapPair -> Pair.of(navMapPair.getLeft(),
        getDaosInRange(range, navMapPair.getValue(), startTimeExtractor, endTimeExtractor)))
      .collect(toMap(Pair::getKey, Pair::getValue));
  }

  public static <V> NavigableMap<Instant, V> getDaosInRange(
    Range<Instant> range,
    NavigableMap<Instant, V> daosNavMap,
    Function<V, Instant> startTimeExtractor,
    Function<V, Instant> endTimeExtractor) {

    if (daosNavMap == null) {
      return new TreeMap<>();
    }

    var daosInRange = daosNavMap.subMap(range.lowerEndpoint(), true, range.upperEndpoint(), true);
    var possibleDaoEndingInRange =
      daosNavMap.floorEntry(range.lowerEndpoint().minusNanos(1));
    if (possibleDaoEndingInRange != null &&
      AssemblerUtils.instantGreaterOrEqual(endTimeExtractor.apply(possibleDaoEndingInRange.getValue()), range.lowerEndpoint())) {

      var treeMap = new TreeMap<Instant, V>();
      treeMap.putAll(daosInRange);
      treeMap.put(startTimeExtractor.apply(possibleDaoEndingInRange.getValue()), possibleDaoEndingInRange.getValue());
      daosInRange = treeMap;
    }

    return daosInRange;
  }

  public static <T> void addChangeTimesToListForDaosWithDayAccuracy(
    NavigableSet<Instant> changeTimes,
    Map<String, NavigableMap<Instant, T>> daosForVersion,
    BiPredicate<T, T> changeOccured,
    Function<T, Instant> startTimeExtractor,
    Function<T, Instant> endTimeExtractor) {

    for (var daoEntry : daosForVersion.entrySet()) {

      addChangeTimesToListForDaosWithDayAccuracy(changeTimes, daoEntry.getValue(), changeOccured, startTimeExtractor, endTimeExtractor);
    }
  }

  public static <T> void addChangeTimesToListForDaosWithDayAccuracy(
    NavigableSet<Instant> changeTimes,
    NavigableMap<Instant, T> daosForVersion,
    BiPredicate<T, T> changeOccured,
    Function<T, Instant> startTimeExtractor,
    Function<T, Instant> endTimeExtractor) {

    T prevDao = null;
    Instant prevEndTime = null;

    //looking for site chans changes that could cause station type to change
    //this happens when a site chans channel type changes.
    for (var entry : daosForVersion.entrySet()) {

      var startTime = startTimeExtractor.apply(entry.getValue());
      var endTime = endTimeExtractor.apply(entry.getValue());
      var currDao = entry.getValue();


      if (prevEndTime != null && !AssemblerUtils.siteOrSiteChansAdjacent(prevEndTime, startTime)
        && !AssemblerUtils.changeTimeExistsForDay(prevEndTime, changeTimes)) {

        changeTimes.add(getImmediatelyAfterInstant(prevEndTime));
      }

      if (changeOccured(prevEndTime, startTime, changeOccured, changeTimes, prevDao, currDao)) {

        if (prevEndTime != null && AssemblerUtils.onSameDay(prevEndTime, startTime)) {
          changeTimes.add(AssemblerUtils.effectiveAtNoonOffset.apply(startTime));
        } else {
          changeTimes.add(startTime);
        }
      }
      prevEndTime = endTime;
      prevDao = currDao;
    }

    if (prevEndTime != null && !AssemblerUtils.changeTimeExistsForDay(prevEndTime, changeTimes)) {

      changeTimes.add(getImmediatelyAfterInstant(prevEndTime));
    }
  }

  private static <T> boolean changeOccured(Instant prevTime, Instant startTime, BiPredicate<T, T> changeOccured,
    NavigableSet<Instant> changeTimes, T prevDao, T currDao){

    var timeExistsInList = AssemblerUtils.changeTimeExistsForDay(startTime, changeTimes);
    var noGapOccured = (prevTime != null) && AssemblerUtils.siteOrSiteChansAdjacent(prevTime, startTime);
    var changed = changeOccured.test(prevDao, currDao);

    return !timeExistsInList && (!noGapOccured || changed);
  }

  public static SortedSet<Instant> getValidTimes(Pair<Instant, Instant> startEndTime, NavigableSet<Instant> possibleVersionTimes,
    boolean isRange){

    //if effectiveTime == a time in possibleVersionTimes, only 1 validTime will be found, we want to find the nextValidTime as well
    Instant possibleVersionsEndTime;
    if (!isRange) {
      possibleVersionsEndTime = startEndTime.getRight().plus(1, ChronoUnit.DAYS);
    }else{
      possibleVersionsEndTime = getImmediatelyAfterInstant(startEndTime.getRight());
    }

    Optional<Instant> floor = Optional.ofNullable(possibleVersionTimes.floor(startEndTime.getLeft()));
    Optional<Instant> ceiling = Optional.ofNullable(possibleVersionTimes.ceiling(possibleVersionsEndTime));
    return possibleVersionTimes.subSet(
      floor.orElse(startEndTime.getLeft()), true, ceiling.orElse(possibleVersionsEndTime), true);
  }
  

  public static Instant getImmediatelyBeforeInstant(Instant instant) {

    if (instant == Instant.MIN || instant == Instant.MAX) {
      return instant;
    }
    return instant.minusMillis(1);
  }

  public static Instant getImmediatelyAfterInstant(Instant instant) {

    if (instant == Instant.MIN || instant == Instant.MAX) {
      return instant;
    }
    return instant.plusMillis(1);
  }
}


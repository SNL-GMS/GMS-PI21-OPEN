package gms.shared.stationdefinition.converter.util.assemblers;

import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class StationDefinitionVersionUtility {

  private static final String COI_ID_STRING_DELIMITER = ".";

  private StationDefinitionVersionUtility() {
  }

  public static List<WfdiscDao> getWfDiscsAsSingleVersion(Collection<WfdiscDao> wfdiscDaos) {
    return StationDefinitionVersionUtility.getWfDiscsWithVersionEndTime(wfdiscDaos).values().stream()
      .map(wfdiscPair -> {
        var newDao = new WfdiscDao(wfdiscPair.getLeft());
        newDao.setEndTime(wfdiscPair.getRight().getEndTime());
        return newDao;
      }).collect(Collectors.toList());
  }

  public static List<WfdiscDao> getWfDiscsWithNextVersion(Collection<WfdiscDao> wfdiscDaos) {
    return StationDefinitionVersionUtility.getWfDiscsWithVersionEndTime(wfdiscDaos).values().stream()
      .map(wfdiscPair -> {
        var newDao = new WfdiscDao(wfdiscPair.getLeft());
        if (wfdiscPair.getLeft().getVersionAttributeHash() == wfdiscPair.getRight().getVersionAttributeHash()) {
          newDao.setEndTime(wfdiscPair.getRight().getEndTime());
        } else {
          newDao.setEndTime(wfdiscPair.getRight().getTime());
        }
        return newDao;
      }).collect(Collectors.toList());
  }

  /**
   * Group wfdisc into correct sta.chan bins and return the first wfDisc
   * in the list with it's effectiveUntil set to the last wfdisc endTime.
   * <p>
   * This will be used in setting response versions
   *
   * @param wfdiscDaos list of possibly many wfdisc per sta.chan
   * @return List of 1 wfDisc per sta.chan with wfdisc effectiveUntil set to end of version
   */
  private static LinkedHashMap<String, Pair<WfdiscDao, WfdiscDao>> getWfDiscsWithVersionEndTime(
    Collection<WfdiscDao> wfdiscDaos) {
    Validate.notNull(wfdiscDaos);

    //bin wfDisc into sta.chan with a Pair of first/last wfdisc in list (will be used to create a version)
    return wfdiscDaos.stream()
      .collect(groupingBy(wfdisc -> createStationChannelCode(
          wfdisc.getStationCode(), wfdisc.getChannelCode()),
        LinkedHashMap::new,
        Collectors.collectingAndThen(Collectors.toList(),
          list -> Pair.of(
            Collections.min(list, Comparator.comparing(WfdiscDao::getTime)),
            Collections.max(list, Comparator.comparing(WfdiscDao::getEndTime))))));
  }

  /**
   * Group sensor into correct sta.chan bins and return the first sensor
   * in the list with it's effectiveUntil set to the last sensor endTime.
   * <p>
   * This will be used in setting response versions
   *
   * @param sensorDaos list of possibly many sensor per sta.chan
   * @return List of 1 sensor per sta.chan with sensor effectiveUntil set to end of version
   */
  public static List<SensorDao> getSensorsWithVersionEndTime(Collection<SensorDao> sensorDaos) {
    Validate.notNull(sensorDaos);

    //bin sensor into sta.chan with a Pair of first/last wfdisc in list (will be used to create a version)
    LinkedHashMap<String, Pair<SensorDao, SensorDao>> sensorTimes = sensorDaos.stream()
      .collect(groupingBy(sensor -> createStationChannelCode(
          sensor.getSensorKey().getStation(), sensor.getSensorKey().getChannel()),
        LinkedHashMap::new,
        Collectors.collectingAndThen(
          Collectors.toList(),
          list -> Pair.of(
            Collections.min(list, Comparator.comparing(x -> x.getSensorKey().getTime())),
            Collections.max(list, Comparator.comparing(x -> x.getSensorKey().getEndTime()))))));

    //set EndTime of Version
    return sensorTimes.values().stream()
      .map(x -> {
        var dao = x.getLeft();
        dao.getSensorKey().setEndTime(x.getRight().getSensorKey().getEndTime());
        return dao;
      }).collect(Collectors.toList());
  }

  /**
   * creates a map of a hash(version-props) to list of all elements that have the same hash.
   *
   * @param nonVersionsList - ordered list of all elements to be binned into versions
   * @param versionAttributeHashFunction function to compute hash for elements that create a version.  used to determine when there is a new version
   * @param versionTimeHashFunction function that returns time-sta-chan hash to act as unique key for version Map
   * @param <T> elements to bin by version
   * @return
   */
  public static <T, U> NavigableMap<U, List<T>> getVersionMapAsInt(
    Collection<T> nonVersionsList, ToIntFunction<T> versionAttributeHashFunction,
    Function<T, U> versionTimeHashFunction) {

    NavigableMap<U, List<T>> versionsByTimeMap = new TreeMap<>();
    T firstVersion = null;
    for (T version : nonVersionsList) {

      if (firstVersion == null ||
        versionAttributeHashFunction.applyAsInt(firstVersion) != versionAttributeHashFunction.applyAsInt(version)) {
        firstVersion = version;
      }
      versionsByTimeMap.putIfAbsent(versionTimeHashFunction.apply(firstVersion), new ArrayList<>());
      versionsByTimeMap.get(versionTimeHashFunction.apply(firstVersion)).add(version);
    }

    return versionsByTimeMap;
  }

  public static <T, U> NavigableMap<U, List<T>> getVersionMapAsDouble(
    Collection<T> nonVersionsList, Function<T, Optional<Double>> versionAttributeHashFunction,
    Function<T, U> versionTimeHashFunction) {

    NavigableMap<U, List<T>> versionsByTimeMap = new TreeMap<>();
    T firstVersion = null;
    for (T version : nonVersionsList) {
      var newVersion = false;

      if (firstVersion != null && versionAttributeHashFunction.apply(firstVersion).isPresent() &&
        versionAttributeHashFunction.apply(version).isPresent()) {
        //this won't throw due to the if check above...exists to appease sonarqube
        var firstVersionVal = versionAttributeHashFunction.apply(firstVersion).orElseThrow().doubleValue();
        var newVersionVal = versionAttributeHashFunction.apply(version).orElseThrow().doubleValue();
        if (firstVersionVal != newVersionVal) {
          newVersion = true;
        }
      }
      if (firstVersion == null || newVersion) {
        firstVersion = version;
      }
      versionsByTimeMap.putIfAbsent(versionTimeHashFunction.apply(firstVersion), new ArrayList<>());
      versionsByTimeMap.get(versionTimeHashFunction.apply(firstVersion)).add(version);
    }

    return versionsByTimeMap;
  }

  public static String createStationChannelCode(String stationCode, String channelCode) {
    return stationCode + COI_ID_STRING_DELIMITER + channelCode;
  }
}

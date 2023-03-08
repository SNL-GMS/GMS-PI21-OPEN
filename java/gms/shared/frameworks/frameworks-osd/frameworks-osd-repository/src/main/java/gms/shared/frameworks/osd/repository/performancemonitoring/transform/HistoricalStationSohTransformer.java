package gms.shared.frameworks.osd.repository.performancemonitoring.transform;

import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.dao.soh.HistoricalSohMonitorValue;
import gms.shared.frameworks.osd.dto.soh.DurationSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;
import gms.shared.frameworks.osd.dto.soh.PercentSohMonitorValues;
import gms.shared.frameworks.osd.dto.soh.SohMonitorValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transformation utility for converting {@link StationSoh} objects into other forms, be it UI DTOs
 * or otherwise.
 */
public class HistoricalStationSohTransformer {

  private static final Logger logger = LoggerFactory
    .getLogger(HistoricalStationSohTransformer.class);

  private HistoricalStationSohTransformer() {

  }

  /**
   * Converts Result set (which is an Object[]) into 2 associative maps of different
   * SohMonitorValueAndStatus types This is then later processed into HistoricalStationSoh
   *
   * @param stationName stationName that the data is relevant for
   * @param historicalSohMonitorValueDaos results of query to be post-processed
   * @return HistoricalStationSoh - result of Query post-processed to correct format
   */
  public static HistoricalStationSoh createHistoricalStationSoh(String stationName,
    List<HistoricalSohMonitorValue> historicalSohMonitorValueDaos) {

    Set<String> alreadyProcessedStationSoh = new LinkedHashSet<>();
    Set<Long> calcValues = new LinkedHashSet<>();
    Set<String> channelNames = new LinkedHashSet<>();
    Map<String, List<DurationSohMonitorValueAndStatus>> durationSmvsMap = new LinkedHashMap<>();
    Map<String, List<PercentSohMonitorValueAndStatus>> percentSmvsMap = new LinkedHashMap<>();

    for (HistoricalSohMonitorValue historicalSmvs : historicalSohMonitorValueDaos) {

      String channelName = historicalSmvs.getChannelName();
      long calcTime = historicalSmvs.getCreationTime().toEpochMilli();
      var sohMonitorType = historicalSmvs.getMonitorType();

      // if the calculation time, the channel and the monitor type have both been processed then this would
      // indicate duplicate data and should be ignored
      var processedStationSohKey = calcTime + channelName + sohMonitorType;
      if (alreadyProcessedStationSoh.contains(processedStationSohKey)) {
        logger.warn("Ignoring duplicate data detected while processing Historical Station SOH.");
        continue;
      } else {
        alreadyProcessedStationSoh.add(processedStationSohKey);
      }
      calcValues.add(calcTime);
      channelNames.add(channelName);

      durationSmvsMap.putIfAbsent(channelName, new ArrayList<>());
      percentSmvsMap.putIfAbsent(channelName, new ArrayList<>());

      if (sohMonitorType.getSohValueType() == SohMonitorType.SohValueType.DURATION) {
        Duration lag = null;
        if (historicalSmvs.getDuration() != null) {
          lag = Duration.ofSeconds(historicalSmvs.getDuration().longValue());
        }
        var smvs = DurationSohMonitorValueAndStatus.from(
          lag,
          historicalSmvs.getStatus(),
          sohMonitorType);
        durationSmvsMap.get(channelName).add(smvs);
      } else if (sohMonitorType.getSohValueType() == SohMonitorType.SohValueType.PERCENT) {
        // initialize percent to worst case for now
        double percent = 100;
        if (historicalSmvs.getPercent() != null) {
          percent = historicalSmvs.getPercent();
        }
        var smvs = PercentSohMonitorValueAndStatus.from(
          percent,
          historicalSmvs.getStatus(),
          sohMonitorType);
        percentSmvsMap.get(channelName).add(smvs);
      } else {
        throw new IllegalStateException(String.format("Invalid sohMonitorType:%s detected cannot continue!", sohMonitorType.getSohValueType()));
      }

    }
    logger.info("Query returned {} num Channels", channelNames.size());

    var calcTimes = calcValues.stream().mapToLong(l -> l).toArray();

    List<HistoricalSohMonitorValues> monitorValues =
      createHistoricalSohMonitorValues(channelNames, durationSmvsMap, percentSmvsMap);

    return HistoricalStationSoh.create(
      stationName,
      calcTimes,
      monitorValues);
  }

  /**
   * loop through SMVS maps to construct arrays containing values to be used to create
   * HistoricalSohMonitorValues This currently only works with Percent/Duration MonitorTypes
   *
   * @param durationSmvsMap map of duration SMVS
   * @param percentSmvsMap map of percent SMVS
   * @return - List of HistoricalSohMonitorValues
   */
  private static List<HistoricalSohMonitorValues> createHistoricalSohMonitorValues(
    Set<String> channelNames,
    Map<String, List<DurationSohMonitorValueAndStatus>> durationSmvsMap,
    Map<String, List<PercentSohMonitorValueAndStatus>> percentSmvsMap) {

    List<HistoricalSohMonitorValues> monitorValues = new ArrayList<>();

    for (String channelName : channelNames) {
      SohMonitorType durationMonitorType = null;
      SohMonitorType percentMonitorType = null;
      var durations = new long[durationSmvsMap.get(channelName).size()];
      var percents = new double[percentSmvsMap.get(channelName).size()];

      Map<SohMonitorType, SohMonitorValues> valueMap = new LinkedHashMap<>();
      var index = 0;

      for (DurationSohMonitorValueAndStatus smvs : durationSmvsMap.get(channelName)) {
        if (durationMonitorType == null) {
          durationMonitorType = smvs.getMonitorType();
        }
        durations[index++] = smvs.getValue().orElse(Duration.of(-1, ChronoUnit.MILLIS)).toMillis();
      }
      index = 0;
      for (PercentSohMonitorValueAndStatus smvs : percentSmvsMap.get(channelName)) {
        if (percentMonitorType == null) {
          percentMonitorType = smvs.getMonitorType();
        }
        percents[index++] = smvs.getValue().orElse(-1.0);
      }
      if (durations.length > 0) {
        valueMap.put(durationMonitorType, DurationSohMonitorValues.create(durations));
      }
      if (percents.length > 0) {
        valueMap.put(percentMonitorType, PercentSohMonitorValues.create(percents));
      }
      monitorValues.add(HistoricalSohMonitorValues.create(channelName, valueMap));
    }
    return monitorValues;
  }
}

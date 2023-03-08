package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationStationAggregate;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for computing station aggregates, which are "summaries" of state of health for
 * stations.
 */
public class StationAggregateCalculationUtility {

  private final Instant now;

  private final AcquiredSampleTimesByChannel acquiredSampleTimesByChannel;

  public StationAggregateCalculationUtility(
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel,
    Instant now
  ) {

    this.acquiredSampleTimesByChannel = acquiredSampleTimesByChannel;
    this.now = now;
  }

  /**
   * Build the set of StationAggregate objects for a particular station.
   *
   * @param waveformSummaryAndReceptionTimesMono The map of channel to set of WaveformSummaryAndReceptionTime, for
   * calculations that need waveform data
   * @param aceiBooleanMapMono The map of channel to set of ACEIs, for calculations that need environment data
   * @param stationSohDefinition the StationSohDefinition for the station
   * @return Set of StationAggregates, for LAG, TIMELINESS, MISSING, ENVIRONMENT_ISSUES
   */
  Mono<Set<StationAggregate<?>>> buildStationAggregateMono(
    Mono<Map<String, Set<WaveformSummaryAndReceptionTime>>> waveformSummaryAndReceptionTimesMono,
    Mono<Map<String, Set<AcquiredChannelEnvironmentIssueBoolean>>> aceiBooleanMapMono,
    Mono<Set<ChannelSoh>> channelSohSetMono,
    StationSohDefinition stationSohDefinition
  ) {

    Objects.requireNonNull(
      waveformSummaryAndReceptionTimesMono,
      "waveformSummaryAndReceptionTimesMono is null!"
    );

    Objects.requireNonNull(
      aceiBooleanMapMono,
      "aceiBooleanMapMono is null!"
    );

    Objects.requireNonNull(
      stationSohDefinition,
      "stationSohDefinition is null!"
    );

    var lagMono = filterWaveformSummaries(
      waveformSummaryAndReceptionTimesMono,
      stationSohDefinition,
      SohMonitorType.LAG
    )
      .flatMap(map -> Mono.just(map.values()))
      .map(collection -> collection.stream().flatMap(Collection::stream)
        .collect(Collectors.toSet()))
      .map(waveformSummaryAndReceptionTimes -> this.lag(
        waveformSummaryAndReceptionTimes,
        stationSohDefinition.getTimeWindowBySohMonitorType().get(SohMonitorType.LAG)
      ));

    var missingMono = channelSohSetMono
      .map(channelSohSet -> channelSohSet.stream()
        .filter(
          channelSoh -> stationSohDefinition.getChannelsBySohMonitorType()
            .get(SohMonitorType.MISSING)
            .contains(channelSoh.getChannelName()))
        .map(channelSoh -> channelSoh.getSohMonitorValueAndStatusMap()
          .get(SohMonitorType.MISSING))
        .map(PercentSohMonitorValueAndStatus.class::cast)
        .collect(Collectors.toList()))
      .map(this::missing)
      .map(Optional::of);

    var timelinessMono = filterWaveformSummaries(
      waveformSummaryAndReceptionTimesMono,
      stationSohDefinition,
      SohMonitorType.TIMELINESS
    ).map(map -> this.timeliness(map,
      stationSohDefinition.getChannelsBySohMonitorType().get(SohMonitorType.TIMELINESS)));

    var envIssuesMono = aceiBooleanMapMono
      .map(aceiBolleanMap -> filterAllAceisByTimeWindows(
          aceiBolleanMap,
          stationSohDefinition,
          now
        )
      )
      .map(this::environmentIssues)
      .map(Optional::of);

    return Flux.concat(
        lagMono,
        missingMono,
        timelinessMono,
        envIssuesMono
      ).filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }

  /**
   * Calculate the average lag of all channels of the station
   *
   * @param waveformSummaryAndReceptionTimes Set of waveform summaries, with reception times
   * @param timeWindowDefinition TimeWindowDefinition containing the calculation interval and back off duration
   * @return DurationStationAggregate of type LAG containing the average lag.
   */
  private Optional<DurationStationAggregate> lag(
    Collection<WaveformSummaryAndReceptionTime> waveformSummaryAndReceptionTimes,
    TimeWindowDefinition timeWindowDefinition
  ) {

    if (waveformSummaryAndReceptionTimes.isEmpty()) {
      return Optional.empty();
    }

    return ChannelSohCalculationUtility.lag(
      timeWindowDefinition,
      waveformSummaryAndReceptionTimes,
      Aggregator.getDurationAverager(),
      now
    ).map(
      averageDuration -> DurationStationAggregate.from(
        averageDuration, StationAggregateType.LAG
      )
    );
  }

  /**
   * Calculate the percentage of all environmental issues in the given collection that are true (that is, something is
   * wrong)
   *
   * @param acquiredChannelEnvironmentIssueBooleans environment booleans to calculate te average for
   * @return PercentStationAggregate that wraps the percentage value.
   */
  private PercentStationAggregate environmentIssues(
    Stream<AcquiredChannelEnvironmentIssueBoolean> acquiredChannelEnvironmentIssueBooleans
  ) {

    double average = 100.0 * acquiredChannelEnvironmentIssueBooleans
      .mapToInt(
        acquiredChannelEnvironmentIssueBoolean ->
          Boolean.TRUE.equals(acquiredChannelEnvironmentIssueBoolean.getStatus()) ? 1
            : 0
      ).average().orElse(Double.NaN);

    return PercentStationAggregate.from(
      Double.isNaN(average) ? null : average,
      StationAggregateType.ENVIRONMENTAL_ISSUES
    );
  }

  /**
   * Calculate the missing percentage using the PercentSohMonitorValueAndStatus for all channels of the station
   *
   * @param channelPercentSohMonitorValueAndStatus List of Channel channelPercentSohMonitorValueAndStatus for the given station
   * @return PercentStationAggregate of type MISSING.
   */
  private PercentStationAggregate missing(
    List<PercentSohMonitorValueAndStatus> channelPercentSohMonitorValueAndStatus
  ) {
    var missingPercentage = channelPercentSohMonitorValueAndStatus.stream()
      .mapToDouble(psmvas -> psmvas.getValue().orElse(0.0)).average().orElse(0.0);

    return PercentStationAggregate.from(missingPercentage, StationAggregateType.MISSING);
  }

  /**
   * Calculate the timeliness, which is how long in the past we recieved the most recent data
   *
   * @param waveformSummaryAndReceptionTimesMap Map of channel to waveform summary
   * @param channelNames list of channels to consider
   * @return DurationStationAggregate that wraps the timeliness value
   */
  private Optional<DurationStationAggregate> timeliness(
    Map<String, Set<WaveformSummaryAndReceptionTime>> waveformSummaryAndReceptionTimesMap,
    Set<String> channelNames
  ) {

    if (waveformSummaryAndReceptionTimesMap.isEmpty() && acquiredSampleTimesByChannel.isEmpty()) {
      return Optional.empty();
    }
    return channelNames.stream()
      .map(acquiredSampleTimesByChannel::getLatestEndTime)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .max(Comparator.naturalOrder())
      .map(
        latestEndTime -> Duration.between(latestEndTime, now)
      )
      .map(
        timelinessDuration -> DurationStationAggregate.from(
          timelinessDuration,
          StationAggregateType.TIMELINESS
        )
      );
  }

  private static Map<String, Set<WaveformSummaryAndReceptionTime>> filterWaveformSummaries(
    Map<String, Set<WaveformSummaryAndReceptionTime>> waveformSummaryAndReceptionTimeMap,
    StationSohDefinition definition,
    SohMonitorType monitorType
  ) {
    return waveformSummaryAndReceptionTimeMap.entrySet().stream().filter(
      stringSetEntry ->
        definition.getStationName().equals(
          stringSetEntry.getKey().substring(
            0, stringSetEntry.getKey().indexOf(".")
          )) &&
          definition.getChannelsBySohMonitorType().get(monitorType)
            .contains(stringSetEntry.getKey()
            )
    ).collect(
      Collectors.toMap(
        Entry::getKey, Entry::getValue
      )
    );
  }

  private static Mono<Map<String, Set<WaveformSummaryAndReceptionTime>>> filterWaveformSummaries(
    Mono<Map<String, Set<WaveformSummaryAndReceptionTime>>> waveformSummaryAndReceptionTimesMono,
    StationSohDefinition stationSohDefinition,
    SohMonitorType monitorType
  ) {

    return waveformSummaryAndReceptionTimesMono
      .map(
        waveformSummaryMap -> filterWaveformSummaries(
          waveformSummaryMap,
          stationSohDefinition,
          monitorType
        )
      );
  }

  private static Stream<AcquiredChannelEnvironmentIssueBoolean> filterAllAceisByTimeWindows(
    Map<String, Set<AcquiredChannelEnvironmentIssueBoolean>> channelAceiMap,
    StationSohDefinition stationSohDefinition,
    Instant now
  ) {
    return channelAceiMap.values().stream()
      .flatMap(Collection::stream)
      .filter(acei -> aceiInChannelsByMonitorType(acei, stationSohDefinition))
      .filter(acei -> aceiInSohMonitorTypesForStationRollup(acei, stationSohDefinition))
      .filter(acei -> {

        var timeWindowDefinition = stationSohDefinition.getTimeWindowBySohMonitorType()
          .get(acei.getType().getMatchingSohMonitorType());

        var calculationStopTime = now.minus(timeWindowDefinition.getBackOffDuration());
        var calculationStartTime = calculationStopTime.minus(
          timeWindowDefinition.getCalculationInterval());

        return acei.getStartTime().isBefore(calculationStopTime)
          && acei.getEndTime().isAfter(calculationStartTime);
      });
  }

  private static boolean aceiInChannelsByMonitorType(
    AcquiredChannelEnvironmentIssueBoolean acei,
    StationSohDefinition stationSohDefinition
  ) {
    var sohMonitorType = acei.getType().getMatchingSohMonitorType();
    var channelName = acei.getChannelName();

    return stationSohDefinition.getChannelsBySohMonitorType().containsKey(sohMonitorType) &&
      stationSohDefinition.getChannelsBySohMonitorType().get(sohMonitorType)
        .contains(channelName);
  }

  private static boolean aceiInSohMonitorTypesForStationRollup(
    AcquiredChannelEnvironmentIssueBoolean acei,
    StationSohDefinition stationSohDefinition
  ) {
    var sohMonitorType = acei.getType().getMatchingSohMonitorType();

    return stationSohDefinition.getSohMonitorTypesForRollup().contains(sohMonitorType);
  }
}

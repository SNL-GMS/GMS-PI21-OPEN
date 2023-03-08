package gms.core.performancemonitoring.soh.control;

import com.google.common.collect.Comparators;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Static utility class for calculating Station SOH averages, rollups, summaries, etc.
 */
public final class StationSohCalculationUtility {

  // We must keep this for logging TIMING point logs for legacy support
  private static final org.apache.logging.log4j.Logger legacyLogger = LogManager.getLogger(StationSohCalculationUtility.class);

  //This is for our timing point logging
  private static final Level TIMING_LEVEL = Level.getLevel("TIMING");

  private static final boolean TIMING_LEVEL_ENABLED = legacyLogger.isEnabled(TIMING_LEVEL);

  private static final Logger logger = LoggerFactory.getLogger(StationSohCalculationUtility.class);

  private StationSohCalculationUtility() {
  }

  /**
   * Given a set of {@code AcquiredStationSohExtract}s, computes a set of {@code StationSoh}.
   *
   * @param acquiredStationSohExtracts The state of health extracts, which must not be null.
   * @param stationSohDefinitions Defines which stations to compute state of health for. Must not be null.
   */
  static Flux<StationSoh> buildStationSohFlux(
    final Set<AcquiredStationSohExtract> acquiredStationSohExtracts,
    final Set<StationSohDefinition> stationSohDefinitions,
    final Instant stationSohTime,
    final AcquiredSampleTimesByChannel acquiredSampleTimesByChannel
  ) {
    return buildStationSohFlux(
      acquiredStationSohExtracts,
      stationSohDefinitions,
      stationSohTime,
      new ChannelSohCalculationUtility(stationSohTime, acquiredSampleTimesByChannel),
      new StationAggregateCalculationUtility(acquiredSampleTimesByChannel, stationSohTime),
      acquiredSampleTimesByChannel);
  }

  /**
   * Version of computeStationSohFlux that takes a ChannelSohCalculationUtility. Exists only for testing.
   */
  static Flux<StationSoh> buildStationSohFlux(
    final Set<AcquiredStationSohExtract> acquiredStationSohExtracts,
    final Set<StationSohDefinition> stationSohDefinitions,
    final Instant stationSohTime,
    final ChannelSohCalculationUtility channelSohCalculationUtility,
    final StationAggregateCalculationUtility stationAggregateCalculationUtility,
    final AcquiredSampleTimesByChannel acquiredSampleTimesByChannel) {

    Validate.notNull(acquiredStationSohExtracts,
      "acquiredStationSohExtracts is required");
    Validate.notNull(stationSohDefinitions,
      "stationSohDefinitions is required");
    Validate.notNull(stationSohTime,
      "stationSohTime is required");

    // Handle the trivial case.
    if (stationSohDefinitions.isEmpty()) {
      return Flux.empty();
    }

    // get earliest Instant of the extracts not included in the last call to
    // this method for each station.
    Map<String, Instant> earliestReceptionMap =
      getEarliestReceptionMap(acquiredStationSohExtracts);

    var waveformSummaryAndReceptionTimeSetMono =
      Mono.just(
        createWsRtMapAndPopulateSampleTimes(acquiredStationSohExtracts, acquiredSampleTimesByChannel)
      ).cache();

    var aceiStationMonoMap = Mono.just(createAceiBooleanStationChannelMap(
      acquiredStationSohExtracts
    ));

    return Flux.concat(Flux.fromIterable(stationSohDefinitions)
      .parallel()
      .runOn(Schedulers.boundedElastic())
      .map(stationSohDefinition ->
      {
        var aceisForStationMono = aceiStationMonoMap.map(
          stationChannelMap -> Optional.ofNullable(
            stationChannelMap.get(stationSohDefinition.getStationName())
          ).orElse(Map.of())
        ).cache();

        var channelSohsSetMono = channelSohCalculationUtility.buildChannelSohSetMono(
          waveformSummaryAndReceptionTimeSetMono,
          aceisForStationMono,
          stationSohDefinition,
          stationSohTime
        ).subscribeOn(Schedulers.boundedElastic());

        var stationAggregateMono = stationAggregateCalculationUtility
          .buildStationAggregateMono(waveformSummaryAndReceptionTimeSetMono,
            aceisForStationMono,
            channelSohsSetMono,
            stationSohDefinition).subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(stationAggregateMono, channelSohsSetMono).map(
            tuple -> {
              Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatusSet =
                getWorstStatusSet(
                  tuple.getT2(),
                  stationSohDefinition
                );

              return ChannelSohCalculationUtility.rollup(
                sohMonitorValueAndStatusSet,
                stationSohDefinition.getSohMonitorTypesForRollup()).map(
                rollupStationSohMapClosure(
                  stationSohDefinition,
                  sohMonitorValueAndStatusSet,
                  tuple.getT2(),
                  tuple.getT1(),
                  stationSohTime
                )
              );
            }).filter(Optional::isPresent)
          .map(Optional::get)
          //
          // Log timing to verify that incoming data is being processed within required timeframe
          //
          .doOnNext(stationSoh -> {
            if (TIMING_LEVEL_ENABLED &&
              earliestReceptionMap.containsKey(stationSoh.getStationName())) {

              legacyLogger.log(TIMING_LEVEL,
                String.format("Timing Point A: SOH object <%s> earliest RSDF <%s>",
                  stationSoh.getId(),
                  earliestReceptionMap.get(stationSoh.getStationName())));
            }
          })
          ;
      })).subscribeOn(Schedulers.boundedElastic());
  }

  /*
   * Set of RawStationDataFrameMetadata from the previous call to getEarliestReceptionMap()
   */
  static Set<RawStationDataFrameMetadata> lastSet = new HashSet<>();

  /**
   * Returns a map of station name to the earliest reception time among the RawStationDataFrameMetadata that were not
   * seen in the previous call to this method for that station.
   * <p>
   * Also, save the current RawStationDataFrameMetadata set for the next time this method is called.
   */
  private static Map<String, Instant> getEarliestReceptionMap(
    Set<AcquiredStationSohExtract> acquiredStationSohExtracts) {

    var startMs = System.currentTimeMillis();

    Set<RawStationDataFrameMetadata> currentSet = acquiredStationSohExtracts.stream()
      .flatMap(acquiredStationSohExtract ->
        acquiredStationSohExtract.getAcquisitionMetadata().stream())
      .collect(Collectors.toSet());

    Map<String, Instant> earliestInstantMap = new HashMap<>();

    currentSet.stream()
      // filtering out extracts that were seen from the previous call to this method
      .filter(rawStationDataFrameMetadata -> !lastSet.contains(rawStationDataFrameMetadata))
      .forEach(rawStationDataFrameMetadata -> {
        String stationName = rawStationDataFrameMetadata.getStationName();
        Instant receptionTime = rawStationDataFrameMetadata.getReceptionTime();
        if (!earliestInstantMap.containsKey(stationName) ||
          receptionTime.isBefore(earliestInstantMap.get(stationName))) {
          earliestInstantMap.put(stationName, receptionTime);
        }
      });

    lastSet = currentSet;

    if (logger.isDebugEnabled()) {
      //
      // This method is looping over the extracts, which reach six figures in size. Lets see
      // how long it takes to call this method.
      //
      logger.debug(
        "Gathering TIMING info took {} ms",
        System.currentTimeMillis() - startMs
      );
    }

    return earliestInstantMap;
  }

  /**
   * Extract boolean issues from a Flux of {@code AcquiredStationSohExtract} objects and returns them as
   * a map of channel name to collections of boolean issues for that channel.
   *
   * @param acquiredStationSohExtractSet the acquiredStationSohExtracts as a Flux
   * @return Mono of Map of channel name to AcquiredChannelEnvironmentIssueBoolean
   */
  // Only deprecating this for now. It is only used in tests now.
  //  TODO: Re-examine tests that use this in light of the new optimizations.
  @Deprecated
  static Map<String, Set<AcquiredChannelEnvironmentIssueBoolean>> createAceiBooleanMap(
    Set<AcquiredStationSohExtract> acquiredStationSohExtractSet
  ) {

    return acquiredStationSohExtractSet.stream()
      .flatMap(acquiredStationSohExtract -> acquiredStationSohExtract
        .getAcquiredChannelEnvironmentIssues().stream())
      .filter(acquiredChannelEnvironmentIssue ->
        acquiredChannelEnvironmentIssue instanceof AcquiredChannelEnvironmentIssueBoolean)
      .map(acquiredChannelEnvironmentIssue ->
        (AcquiredChannelEnvironmentIssueBoolean) acquiredChannelEnvironmentIssue)
      .collect(
        Collectors.groupingBy(
          AcquiredChannelEnvironmentIssue::getChannelName,
          Collectors.toSet()
        )
      );
  }

  /**
   * From A collection of AcquiredStationSohExtracts, create a Map of station -> Map of channel to
   * set of ACEIs.
   *
   * @param acquiredStationSohExtractSet The set to trans form
   * @return Map of a map: station -> channel -> set of ACEIs for the channel
   */
  static Map<String, Map<String, Set<AcquiredChannelEnvironmentIssueBoolean>>> createAceiBooleanStationChannelMap(
    Collection<AcquiredStationSohExtract> acquiredStationSohExtractSet
  ) {

    var acquiredChannelEnvironmentIssueBooleanStream = acquiredStationSohExtractSet.stream()
      .flatMap(acquiredStationSohExtract -> acquiredStationSohExtract
        .getAcquiredChannelEnvironmentIssues().stream())
      .filter(acquiredChannelEnvironmentIssue ->
        acquiredChannelEnvironmentIssue instanceof AcquiredChannelEnvironmentIssueBoolean)
      .map(acquiredChannelEnvironmentIssue ->
        (AcquiredChannelEnvironmentIssueBoolean) acquiredChannelEnvironmentIssue);

    var stationMap = acquiredChannelEnvironmentIssueBooleanStream
      .collect(Collectors.groupingBy(
        acquiredChannelEnvironmentIssueBoolean -> {
          var channelName = acquiredChannelEnvironmentIssueBoolean.getChannelName();

          return channelName.substring(
            0, channelName.indexOf(".")
          );
        }
      ));

    return stationMap.entrySet().stream()
      .map(entry -> {
          var channelMap = entry.getValue().stream()
            .collect(Collectors.groupingBy(
              AcquiredChannelEnvironmentIssue::getChannelName,
              Collectors.toSet()
            ));

          return Map.entry(
            entry.getKey(),
            channelMap
          );
        }
      ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Returns a closure that transforms an SohMonitorAndStatusRollup into a StationSoh, using the given definition, set
   * of StationSohMonitorAndValueStatuses, set of ChannelSohs, and receive time.
   */
  private static Function<SohStatus, StationSoh> rollupStationSohMapClosure(
    StationSohDefinition stationSohDefinition,
    Set<SohMonitorValueAndStatus<?>> valueAndStatuses,
    Set<ChannelSoh> channelSohs,
    Set<StationAggregate<?>> stationAggregates,
    Instant stationSohTime
  ) {

    return sohStatus ->
      StationSoh.create(
        stationSohTime,
        stationSohDefinition.getStationName(),
        valueAndStatuses,
        sohStatus,
        channelSohs,
        stationAggregates);
  }

  /**
   * Find the worst status for a given monitor type, over a set of ChannelSoh objects filtered by a list of channel
   * names.
   *
   * @param sohMonitorType The monitor type
   * @param channelSohs The set of channel soh objects
   * @param channelNames the set of names if channels we are interested in
   * @return An Optional containing the worst status, or empty if one could not be calculated.
   */
  private static Optional<SohMonitorValueAndStatus<?>> worstStatus(
    SohMonitorType sohMonitorType,
    Set<ChannelSoh> channelSohs,
    Set<String> channelNames
  ) {

    Map<SohStatus, List<SohMonitorValueAndStatus<?>>> partitionedStatuses =
      channelSohs.stream()
        //
        // Filter out channels that are not in channelNames
        //
        .filter(channelSoh -> channelNames.contains(channelSoh.getChannelName()))
        //
        // Get the set of statuses from each channel
        //
        .map(ChannelSoh::getSohMonitorValueAndStatusMap)

        //
        // Filter out those statues that are not for the passed in monitor type
        //
        .map(map -> map.get(sohMonitorType)).filter(Objects::nonNull)

        //
        // Group the SohMonitorValueAndStatuses by the SohStatus inside them
        //
        .collect(Collectors.groupingBy(SohMonitorValueAndStatus::getStatus));

    //
    // For each SohStatus, find the worst value associated with that status. Here we are
    // ordering Optional<T> by the natural order of T, and putting Optional.empty
    // before all possible values of T. Thus Optional.empty is the "lowest", or "best" value.
    // We want them to be the "lowest" so that any T value will override them.
    //
    // (For the moment, T is either Double (Percent) or Duration (Lag))
    //
    Map<SohStatus, Optional<SohMonitorValueAndStatus<?>>> worstValueByStatus =
      partitionedStatuses.entrySet().stream()
        .map(entry ->
          Map.entry(
            entry.getKey(),
            entry.getValue().stream().max(Comparator.comparing(
              SohMonitorValueAndStatus::getValue,
              Comparators.emptiesFirst(Comparator.naturalOrder())
            ))
          )
        )
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    //
    // worstValueByStatus will only have at most 3 keys, the three enum constants in SohStatus.
    // Those values are [BAD, MARGINAL, GOOD]. Thus as we are looping through this array, if the map
    // contains BAD, it is the worst. If it does not contain BAD but contains MARGINAL, MARGINAL
    // has to be the worst, because all that is left after it is GOOD.
    //
    for (SohStatus sohStatus : SohStatus.values()) {
      if (worstValueByStatus.containsKey(sohStatus)) {
        return Optional.of(
          worstValueByStatus.get(sohStatus).orElseThrow(
            () -> new IllegalArgumentException(
              "List of statuses for " + sohMonitorType + " for channels " + channelNames
                + "cannot be empty"
            )
          )
        );
      }
    }

    return Optional.empty();
  }

  private static Set<SohMonitorValueAndStatus<?>> getWorstStatusSet(
    Set<ChannelSoh> channelSohSet,
    StationSohDefinition definition
  ) {

    var stationStatusPartitionedSet = new HashSet<SohMonitorValueAndStatus<?>>();

    definition.getChannelsBySohMonitorType().forEach((k, v) -> {
        Optional<SohMonitorValueAndStatus<?>> opt = worstStatus(k, channelSohSet, v);
        opt.ifPresent(stationStatusPartitionedSet::add);
      }
    );

    return stationStatusPartitionedSet;
  }

  /**
   * Extract {@link WaveformSummaryAndReceptionTime}s from a set of {@link AcquiredStationSohExtract} and returns them
   * in a Flux of GroupFlux of channel name to {@link WaveformSummaryAndReceptionTime} objects
   * <p>
   * Also, while we are at it, lets populate our latest end times map. Doing that separately is
   * VERY costly.
   *
   * @param acquiredStationSohExtractSet the acquiredStationSohExtracts as a Mono of Set
   * @param acquiredSampleTimesByChannel AcquiredSampleTimesByChannel object to populate.
   * @return Mono of Map channel name to WaveformSummaryAndReceptionTime
   */
  static Map<String, Set<WaveformSummaryAndReceptionTime>> createWsRtMapAndPopulateSampleTimes(
    Set<AcquiredStationSohExtract> acquiredStationSohExtractSet,
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel
  ) {

    return acquiredStationSohExtractSet.stream()
      .flatMap(
        acquiredStationSohExtract -> acquiredStationSohExtract.getAcquisitionMetadata()
          .stream())
      .flatMap(rawStationDataFrameMetadata -> rawStationDataFrameMetadata
        .getWaveformSummaries().values().stream()
        .map(
          waveformSummary -> WaveformSummaryAndReceptionTime.create(
            waveformSummary, rawStationDataFrameMetadata.getReceptionTime()
          )
        )
      )
      .map(waveformSummaryAndReceptionTime -> {
        acquiredSampleTimesByChannel.update(
          waveformSummaryAndReceptionTime.getWaveformSummary().getChannelName(),
          waveformSummaryAndReceptionTime.getWaveformSummary().getEndTime()
        );
        return waveformSummaryAndReceptionTime;
      })
      .collect(
        Collectors.groupingBy(
          waveformSummaryAndReceptionTime -> waveformSummaryAndReceptionTime
            .getWaveformSummary()
            .getChannelName(),
          Collectors.toSet()
        )
      );
  }
}
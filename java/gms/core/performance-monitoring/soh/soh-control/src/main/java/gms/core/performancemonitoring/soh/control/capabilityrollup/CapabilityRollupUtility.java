package gms.core.performancemonitoring.soh.control.capabilityrollup;

import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupDefinition;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CapabilityRollupUtility {

  private static final Logger logger = LoggerFactory.getLogger(CapabilityRollupUtility.class);

  private static final boolean BEBUG_ENABLED = logger.isDebugEnabled();

  private static final boolean TIME_FLUX_BUILD = false;

  //
  // Package-private methods that are tested seperately need to have this instantiated, so
  // we are instantiating it here. But every call to calculateCapabilitySohRollupFlux will instantiate
  // it again.
  //
  private static Map<StationRollupDefinition, SohStatus> stationSohCache
    = new ConcurrentHashMap<>();

  private CapabilityRollupUtility() {

  }

  /**
   * Calculates a Flux of CapbilitySohRollups from a set of StationSohs. Capability rollups are
   * grouped by station group. Station groups are defined implicitly by the map of station to
   * StationRollupDefinition inside CapabilitySohRollupDefinition.
   *
   * @param definitions The set of CapabilityRollupDefinitions
   * @param stationSohFlux The set of all StationSoh across all station groups,
   * @return Flux of CapabilitySohRollups.
   */
  public static Flux<CapabilitySohRollup> buildCapabilitySohRollupFlux(
    Set<CapabilitySohRollupDefinition> definitions,
    Flux<StationSoh> stationSohFlux,
    Instant startTime
  ) {
    stationSohCache = new ConcurrentHashMap<>();

    var startMs = System.currentTimeMillis();

    // Map of station group name to unicast processor, which will emit StationSohs
    // for stations that belong to the station group.
    Map<String, UnicastProcessor<StationSoh>> processors = new HashMap<>();

    // Need to keep track of the sinks for the processors. Cant just repeatedly
    // call UnicastProcessor.sink because that has strange behavior. This maps
    // station group name to sink.
    Map<String, FluxSink<StationSoh>> stationSohSinkMap = new HashMap<>();

    //
    // Get a unicast processor for each station group, which will emit StationSohs for
    // stations in that station group.
    //
    definitions.forEach(definition -> {
        var processor = UnicastProcessor.<StationSoh>create();

        stationSohSinkMap.put(
          definition.getStationGroup(),
          processor.sink()
        );

        processors.put(definition.getStationGroup(), processor);

      }
    );

    // Count how many stations we've emitted in a station group, so we can
    // complete the flux when all have been emitted.
    Map<String, AtomicInteger> groupStationCounts = new HashMap<>();

    //
    // Subscribe a consumer to stationSohFlux that sinks the StationSoh
    // to the right UnicastProcessor, depending on station group.
    //
    stationSohFlux
      .doOnComplete(() -> stationSohSinkMap.values().forEach(FluxSink::complete))
      .subscribeOn(Schedulers.boundedElastic())
      .subscribe(stationSoh ->
        definitions.stream()
          .filter(
            capabilitySohRollupDefinition -> capabilitySohRollupDefinition
              .getStationRollupDefinitionsByStation()
              .containsKey(stationSoh.getStationName())
          )
          .forEach(
            capabilitySohRollupDefinition -> {
              var stationGroupName = capabilitySohRollupDefinition.getStationGroup();

              groupStationCounts.computeIfAbsent(
                stationGroupName,
                k -> new AtomicInteger()
              );

              var countReference = groupStationCounts.get(
                capabilitySohRollupDefinition.getStationGroup()
              );

              // Sink the StationSoh to the sink associated with this station group
              stationSohSinkMap.get(stationGroupName).next(stationSoh);

              countReference.incrementAndGet();

              // Complete the sink if we have seen all of the stations for the station group,
              // so that the UnicastProcessor will complete and the capability calculation can
              // proceed.
              if (countReference.get() == capabilitySohRollupDefinition
                .getStationRollupDefinitionsByStation().size()) {

                stationSohSinkMap.get(stationGroupName).complete();
              }
            }
          )
      );

    //
    // Get a publisher for each station group and bring them all together.
    //
    var flux = Flux.concat(
      definitions.stream().map(
        definition -> buildCapabilitySohRollupMono(
          definition,
          processors.get(definition.getStationGroup()),
          startTime
        )
      ).collect(Collectors.toList())
    );

    if (TIME_FLUX_BUILD && BEBUG_ENABLED) {
      logger.debug(
        "CAPABILITY SOH TIMING: calculateCapabilitySohRollupFlux built the flux in {} ms",
        System.currentTimeMillis() - startMs
      );
    }

    return flux;

  }

  /**
   * Calculates the rollup from a set of SohMonitorValueAndStatus
   *
   * @param definition ChannelRollupDefinition definition to use
   * @param sohMonitorValueAndStatusMap Map of SohMonitorType toSohMonitorValueAndStatus
   * @return SohStatus that results from running the operator on the set of SohStatuses in
   * sohMonitorValueAndStatusSet.
   */
  static SohStatus calculateSohMvasSetRollup(
    ChannelRollupDefinition definition,
    Map<SohMonitorType, SohMonitorValueAndStatus<?>> sohMonitorValueAndStatusMap
  ) {
    var startMs = System.currentTimeMillis();

    var sohStatus = RollupEvaluator.create(definition.getSohMonitorsToChannelRollupOperator(),
      RollupOperator::getSohMonitorTypeOperands,
      sohMonitorType -> Optional.ofNullable(sohMonitorValueAndStatusMap.get(sohMonitorType))
        .map(SohMonitorValueAndStatus::getStatus)
        .orElse(SohStatus.MARGINAL)
    ).evaluate();

    if (!TIME_FLUX_BUILD && BEBUG_ENABLED) {
      logger.debug(
        "CAPABILITY SOH TIMING: calculateSohMvasSetRollup took {} ms. Monitor set size: {}",
        System.currentTimeMillis() - startMs,
        sohMonitorValueAndStatusMap.size()
      );
    }

    return sohStatus;
  }

  /**
   * Calculates the rollup from a set of ChannelSoh objects.
   *
   * @param definition StationRollupDefinition definition to use
   * @param channelSohSet Set of ChannelSoh objects have channel operands.
   * @return SohStatus for set of channels resulting from using the given operator.
   */
  static SohStatus calculateChannelSohSetRollup(
    StationRollupDefinition definition,
    Set<ChannelSoh> channelSohSet
  ) {
    var startMs = System.currentTimeMillis();

    //
    // Get the status from the cache if it is there, otherwise compute it.
    //
    // Two definitions A and B are equal if:
    //   A and B have the same rollup operator (Operation and operands are the same)
    //   A and B have the same mappings of channel name to ChannelRollupDefinition
    //
    // For station X in station groups G and H, if the rollup behavior is the same for X for both
    // G and H, the definitions will be equal. Thus if the SohStatus for X is calculated first for G,
    // it will be cached and reused for H.
    //
    // If the behavior for G and H is different, then there will be two different definitions (say,
    // X_g and X_h) thus the rollup will be recomputed for G and H as expected.
    //
    var sohStatus = stationSohCache.computeIfAbsent(
      definition,
      stationRollupDefinition -> RollupEvaluator
        .create(stationRollupDefinition.getChannelsToStationRollupOperator(),
          RollupOperator::getChannelOperands,
          channelName ->
            calculateSohMvasSetRollup(
              stationRollupDefinition.getChannelRollupDefinitionsByChannel()
                .get(channelName),
              channelSohSet.stream()
                .filter(
                  channelSoh -> channelSoh.getChannelName().equals(channelName))
                .findFirst()
                .map(ChannelSoh::getSohMonitorValueAndStatusMap)

                //
                // calculateSohMvasSetRollup will return MARGINAL of this set is empty
                //
                .orElse(Map.of())
            )).evaluate()
    );

    if (!TIME_FLUX_BUILD && BEBUG_ENABLED) {
      logger.debug(
        "CAPABILITY SOH TIMING: calculateChannelSohSetRollup took {} ms. ChannelSoh set size: {}",
        System.currentTimeMillis() - startMs,
        channelSohSet.size()
      );
    }

    return sohStatus;
  }

  /**
   * Calculates the rollup from a group of StationSoh objects.
   *
   * @param definition CapabilitySohRollupDefinition definition to use
   * @param stationSohFlux Set (Group) of StationSoh objects station operands. This is a list of
   * entire Station objects (not just station name), so that we can pass the
   * list of channels for each station.
   * @return Soh for a group of stations resulting from using the given operator.
   */
  static Mono<CapabilitySohRollup> buildCapabilitySohRollupMono(
    CapabilitySohRollupDefinition definition,
    Flux<StationSoh> stationSohFlux,
    Instant startTime
  ) {

    Map<String, SohStatus> stationRollupMap = new HashMap<>();

    Set<UUID> stationSohUUIDs = new HashSet<>();

    return stationSohFlux
      .doFirst(() ->
        logger.info("Starting capability flux for {}", definition.getStationGroup())
      )
      .doOnNext(
        stationSoh -> stationSohUUIDs.add(stationSoh.getId())
      )
      .map(
        stationSoh -> Map.entry(
          stationSoh.getStationName(),
          calculateChannelSohSetRollup(
            definition.getStationRollupDefinitionsByStation()
              .get(stationSoh.getStationName()),
            stationSoh.getChannelSohs()
          )
        )
      ).collectMap(Entry::getKey, Entry::getValue)
      .map(
        stationStatusMap -> {
          var startMs = System.currentTimeMillis();

          var sohStatus = RollupEvaluator.create(definition.getStationsToGroupRollupOperator(),
            RollupOperator::getStationOperands,
            stationName -> {
              var status = Optional.ofNullable(stationStatusMap.get(stationName))
                .orElseGet(() -> {
                  logger.debug("No SohStatus for station {}, using MARGINAL", stationName);
                  return SohStatus.MARGINAL;
                });

              stationRollupMap.put(stationName, status);

              return status;
            }).evaluate();

          if (!TIME_FLUX_BUILD && BEBUG_ENABLED) {
            logger.debug(
              "CAPABILITY SOH TIMING: calculateCapabilitySohRollupMono mapper took {} ms.",
              System.currentTimeMillis() - startMs
            );
          }

          return sohStatus;
        }
      ).map(sohStatus -> CapabilitySohRollup.create(
          UUID.randomUUID(),
          startTime,
          sohStatus,
          definition.getStationGroup(),
          stationSohUUIDs,
          stationRollupMap
        )
      );

  }

}

package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.capabilityrollup.CapabilityRollupUtility;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that builds two fluxes: the StationSoh Flux (with "pure" station rollups) and the
 * CapabilitySohRollup Flux (with "capability" station group rollups). The Fluxes are built
 * using the "pure" and "capability" rollup utilities, respectively.
 */
class RollupFluxBuilder {

  private static final Logger logger = LoggerFactory.getLogger(RollupFluxBuilder.class);

  private final ConnectableFlux<CapabilitySohRollup> capabilitySohRollupFlux;

  private final ConnectableFlux<StationSoh> stationSohFlux;

  /**
   * Construct a new RollupFluxBuilder
   *
   * @param acquiredStationSohExtractSet The Flux of AcquiredStationSohExtract - the input SOH data
   * @param stationSohDefinitionSet Set of configurations specifying "pure" rollup behavior
   * @param capabilitySohRollupDefinitionSet Set of configurations specifying "capability" rollup
   * behavior
   * @param acquiredSampleTimesByChannel AcquiredSampleTimesByChannel object used for timeliness
   * calculations
   */
  RollupFluxBuilder(
    Set<AcquiredStationSohExtract> acquiredStationSohExtractSet,
    Set<StationSohDefinition> stationSohDefinitionSet,
    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitionSet,
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel) {

    var nowInstant = Instant.now();

    this.stationSohFlux = buildStationSohFlux(
      acquiredStationSohExtractSet,
      stationSohDefinitionSet,
      nowInstant,
      acquiredSampleTimesByChannel
    );

    this.capabilitySohRollupFlux = buildCapabilityRollupFlux(
      this.stationSohFlux,
      capabilitySohRollupDefinitionSet,
      nowInstant
    );

    //
    // Connect all of our fluxes to their source
    //
    this.stationSohFlux.connect();
    this.capabilitySohRollupFlux.connect();

  }

  /**
   * Construct a new RollupFluxBuilder
   *
   * @param acquiredStationSohExtractSet The Flux of AcquiredStationSohExtract - the input SOH data
   * @param stationSohDefinitionSet Set of configurations specifying "pure" rollup behavior
   * @param capabilitySohRollupDefinitionSet Set of configurations specifying "capability" rollup
   * behavior
   * @param rollupStationSohTimeTolerance Deprecated. Does nothing.
   * @param acquiredSampleTimesByChannel AcquiredSampleTimesByChannel object used for timeliness
   * calculations
   * @deprecated the parameter rollupStationSohTimeTolerance has never done anything, so this constructor
   * will be removed as a future refactor or robustness task. A good time to remove this constructor
   * is when rollupStationSohTimeTolerance is removed from config all together.
   */
  @Deprecated(forRemoval = true)
  RollupFluxBuilder(
    Set<AcquiredStationSohExtract> acquiredStationSohExtractSet,
    Set<StationSohDefinition> stationSohDefinitionSet,
    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitionSet,
    Duration rollupStationSohTimeTolerance,
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel) {

    this(
      acquiredStationSohExtractSet,
      stationSohDefinitionSet,
      capabilitySohRollupDefinitionSet,
      acquiredSampleTimesByChannel
    );
  }

  /**
   * @return Flux of CapabilitySohRollup
   */
  Flux<CapabilitySohRollup> getCapabilitySohRollupFlux() {

    return capabilitySohRollupFlux;
  }

  /**
   * @return Flux of StationSoh
   */
  Flux<StationSoh> getStationSohFlux() {

    return stationSohFlux;
  }

  /**
   * Build the Flux of CapabilitySohRollup
   *
   * @param stationSohFlux Flux of StationSoh used to build capability rollups
   * @param capabilitySohRollupDefinitionSet Set of configs specifying capability rollup behavior
   * @return Flux of CapabilitySohRollup
   */
  private static ConnectableFlux<CapabilitySohRollup> buildCapabilityRollupFlux(
    Flux<StationSoh> stationSohFlux,
    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitionSet,
    Instant now
  ) {

    return CapabilityRollupUtility.buildCapabilitySohRollupFlux(
        capabilitySohRollupDefinitionSet, stationSohFlux, now)
      //
      // Cache results so that they can be sent to multiple subscribers
      //
      .replay();
  }

  /**
   * Build the Flux of StationSoh
   *
   * @param acquiredStationSohExtractSet Flux of AcquiredStationSohExtract used to build the "pure"
   * rollups
   * @param stationSohDefinitionSet Set of configs specifying pure rollup behavior
   * @return Flux of StationSoh
   */
  private static ConnectableFlux<StationSoh> buildStationSohFlux(
    Set<AcquiredStationSohExtract> acquiredStationSohExtractSet,
    Set<StationSohDefinition> stationSohDefinitionSet,
    Instant now,
    AcquiredSampleTimesByChannel acquiredSampleTimesByChannel
  ) {

    var restartCount = new AtomicInteger(0);

    return StationSohCalculationUtility.buildStationSohFlux(
        acquiredStationSohExtractSet,
        stationSohDefinitionSet,
        now,
        acquiredSampleTimesByChannel)
      .doFirst(() ->
        logger.debug(
          "RollupFluxBuilder: stationSohFlux restarting for {}th time",
          restartCount.incrementAndGet()
        )
      )
      //
      // Cache results so that they can be sent to multiple subscribers
      //
      .replay();
  }
}

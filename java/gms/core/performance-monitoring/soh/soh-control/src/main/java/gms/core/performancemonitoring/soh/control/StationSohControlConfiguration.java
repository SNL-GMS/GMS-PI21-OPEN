package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohControlDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationGroupNamesConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohMonitoringDefinition;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.api.station.StationGroupRepositoryInterface;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains configuration values used by {@link StationSohControl}.  Includes {@link
 * StationSohMonitoringDefinition} that is required by {@link StationSohControl#monitor(Set)} to
 * call {@link StationSohCalculationUtility#buildStationSohFlux}
 */
public class StationSohControlConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(StationSohControlConfiguration.class);

  private static final boolean DEBUG_ENABLED = logger.isDebugEnabled();


  private static final String STATION_SOH_PREFIX = "soh-control";

  private static final String STATION_GROUP_NAME_SELECTOR_KEY = "StationGroupName";

  private static final String STATION_NAME_SELECTOR_KEY = "StationName";

  private static final String CHANNEL_NAME_SELECTOR_KEY = "ChannelName";

  private static final String NULL_CONFIGURATION_CONSUMER_UTILITY = "Null configurationConsumerUtility";

  private ConfigurationPair initialConfigurationPair;

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  private final CapabilityRollupConfigurationUtility capabilityRollupConfigurationUtility;

  private final Collection<StationGroup> stationGroups;

  private final Collection<Station> stations;

  public static class ConfigurationPair {

    private final StationSohMonitoringDefinition stationSohMonitoringDefinition;

    private final SohControlDefinition sohControlDefinition;

    public ConfigurationPair(
      StationSohMonitoringDefinition stationSohMonitoringDefinition,
      SohControlDefinition sohControlDefinition) {
      this.stationSohMonitoringDefinition = stationSohMonitoringDefinition;
      this.sohControlDefinition = sohControlDefinition;
    }

    public StationSohMonitoringDefinition getStationSohMonitoringDefinition() {
      return stationSohMonitoringDefinition;
    }

    public SohControlDefinition getSohControlDefinition() {
      return sohControlDefinition;
    }
  }

  /**
   * Instantiates and returns a new StationSohControlConfiguration.  The returned
   * StationSohControlConfiguration contains configuration loaded using the provided {@link
   * ConfigurationConsumerUtility} and {@link Station}s that are part of {@link StationGroup}s
   * loaded from the provided {@link StationGroupRepositoryInterface}.
   *
   * @param configurationRepository Used to retrieve configuration values from the OSDn.
   * @param stationGroupRepositoryInterface Used to retrieve StationGroups from the OSD.
   * @return New StationSohControlConfiguration.
   */
  public static StationSohControlConfiguration create(
    ConfigurationRepository configurationRepository,
    StationGroupRepositoryInterface stationGroupRepositoryInterface, RetryConfig retryConfig) {

    return new StationSohControlConfiguration(
      createConfigurationConsumerUtility(
        configurationRepository, retryConfig
      ),
      stationGroupRepositoryInterface
    );
  }

  public static StationSohControlConfiguration create(
    ConfigurationConsumerUtility configurationConsumerUtility,
    StationGroupRepositoryInterface stationGroupRepositoryInterface) {

    return new StationSohControlConfiguration(
      configurationConsumerUtility,
      stationGroupRepositoryInterface
    );
  }

  /**
   * Creates a new {@link ConfigurationConsumerUtility} for holding configurations for this class.
   *
   * @param configurationRepository Used to load configuration values from processing
   * configuration.
   * @return new {@link ConfigurationConsumerUtility}
   */
  private static ConfigurationConsumerUtility createConfigurationConsumerUtility(
    ConfigurationRepository configurationRepository, RetryConfig retryConfig) {
    Objects.requireNonNull(configurationRepository, "Null configurationRepository");
    return ConfigurationConsumerUtility.builder(configurationRepository)
      .configurationNamePrefixes(Set.of(STATION_SOH_PREFIX))
      .retryConfiguration(retryConfig)
      .build();
  }

  /**
   * Instantiates a new StationSohControlConfiguration that contains the provided {@link
   * StationSohMonitoringDefinition}.
   *
   * @param configurationConsumerUtility Used to obtain configuration parameters.
   * @param stationGroupRepositoryInterface Used to obtain {@link StationGroup}s from the OSD.
   */
  private StationSohControlConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    StationGroupRepositoryInterface stationGroupRepositoryInterface
  ) {

    // Start timing "CONFIG TIMING: Querying station groups"
    var startMs = System.currentTimeMillis();

    this.stationGroups = resolveStationGroups(
      configurationConsumerUtility,
      stationGroupRepositoryInterface
    );

    this.stations = stationGroups.stream()
      .flatMap(staGroup -> staGroup.getStations().stream()).collect(
        Collectors.toSet());

    if (DEBUG_ENABLED) {
      logger.debug(
        "CONFIG TIMING: Querying station groups took {} ms",
        System.currentTimeMillis() - startMs
      );
    }

    this.configurationConsumerUtility = configurationConsumerUtility;

    this.capabilityRollupConfigurationUtility = new CapabilityRollupConfigurationUtility(
      STATION_GROUP_NAME_SELECTOR_KEY,
      STATION_NAME_SELECTOR_KEY,
      CHANNEL_NAME_SELECTOR_KEY,
      STATION_SOH_PREFIX,
      configurationConsumerUtility,
      stationGroups
    );

    this.initialConfigurationPair = resolveConfigurationPair();
  }

  public boolean hasNonEmptyConfiguration() {
    return !this.initialConfigurationPair
      .getStationSohMonitoringDefinition()
      .getStationSohDefinitions()
      .isEmpty();
  }

  public ConfigurationPair getInitialConfigurationPair() {

    if (initialConfigurationPair != null) {

      var pair = initialConfigurationPair;
      initialConfigurationPair = null;
      return pair;

    } else {
      throw new IllegalStateException(
        "StationSohControlConfiguration: getInitialConfigurationPair should only be called once"
      );
    }
  }

  private ConfigurationPair resolveConfigurationPair() {

    // Start timing "CONFIG TIMING: Creating CapabilityRollupConfigurationUtility"
    var startMs = System.currentTimeMillis();

    if (DEBUG_ENABLED) {
      logger.debug(
        "CONFIG TIMING: Creating CapabilityRollupConfigurationUtility took {} ms",
        System.currentTimeMillis() - startMs
      );
    }

    // Start timing "CONFIG TIMING: Resolving StationSohDefinitions"
    startMs = System.currentTimeMillis();

    Set<StationSohDefinition> stationSohDefinitions =
      PerformanceMonitoringConfigurationUtility.resolveStationSohDefinitions(
        configurationConsumerUtility,
        stations
      );

    if (DEBUG_ENABLED) {
      logger.debug(
        "CONFIG TIMING: Resolving {} StationSohDefinitions took {} ms",
        stationSohDefinitions.size(),
        System.currentTimeMillis() - startMs
      );
    }

    // Start timing "CONFIG TIMING: Resolving CapabilityRollupDefinitions"
    startMs = System.currentTimeMillis();

    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitions =
      capabilityRollupConfigurationUtility.resolveCapabilitySohRollupDefinitions();

    if (DEBUG_ENABLED) {
      logger.debug(
        "CONFIG TIMING: Resolving {} CapabilityRollupDefinitions took {} ms",
        capabilitySohRollupDefinitions.size(),
        System.currentTimeMillis() - startMs
      );
    }

    var stationSohMonitoringDefinition = StationSohMonitoringDefinition.create(
      capabilityRollupConfigurationUtility.resolveRollupStationSohTimeTolerance(),
      stationGroups.stream().map(StationGroup::getName).collect(Collectors.toSet()),
      stationSohDefinitions,
      capabilitySohRollupDefinitions
    );

    // Start timing "CONFIG TIMING: Calculating the cache expiration"
    startMs = System.currentTimeMillis();

    if (DEBUG_ENABLED) {
      logger.debug(
        "CONFIG TIMING: Calculating the cache expiration took {} ms",
        System.currentTimeMillis() - startMs
      );
    }

    return new ConfigurationPair(
      stationSohMonitoringDefinition,
      SohControlDefinition.create(
        Duration.parse(String.valueOf(
          configurationConsumerUtility.resolve(
            STATION_SOH_PREFIX,
            List.of()
          ).get("reprocessingPeriod")))
      )
    );
  }

  /**
   * Resolves a {@link Set} of {@link Station}s for which to resolve {@link StationSohDefinition}s.
   * The returned Set contains all Stations in the {@link StationGroup}s whose names are loaded from
   * processing configuration.
   *
   * @return Set of Stations for which to resolve StationSohDefinitions.
   */
  private static List<StationGroup> resolveStationGroups(
    ConfigurationConsumerUtility configurationConsumerUtility,
    StationGroupRepositoryInterface stationGroupRepositoryInterface) {

    final var stationGroupNamesConfigurationOption =
      resolveStationGroupNamesConfigurationOption(configurationConsumerUtility);

    return stationGroupRepositoryInterface
      .retrieveStationGroups(stationGroupNamesConfigurationOption.getStationGroupNames());

  }


  /**
   * Resolves the default {@link StationGroupNamesConfigurationOption} from processing
   * configuration.
   *
   * @return The default StationGroupNamesConfigurationOption from processing configuration.
   */
  private static StationGroupNamesConfigurationOption resolveStationGroupNamesConfigurationOption(
    ConfigurationConsumerUtility configurationConsumerUtility) {

    Objects.requireNonNull(configurationConsumerUtility, NULL_CONFIGURATION_CONSUMER_UTILITY);

    final String stationGroupNamesConfigurationName =
      STATION_SOH_PREFIX + ".station-group-names";

    final List<Selector> selectors = new ArrayList<>();

    return configurationConsumerUtility.resolve(
      stationGroupNamesConfigurationName,
      selectors,
      StationGroupNamesConfigurationOption.class
    );
  }
}
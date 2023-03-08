package gms.core.performancemonitoring.ssam.control;

import gms.core.performancemonitoring.soh.control.PerformanceMonitoringConfigurationUtility;
import gms.core.performancemonitoring.soh.control.configuration.StationGroupNamesConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.ssam.control.api.StationSohAnalysisManager;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDisplayParameters;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration for implementation of {@link StationSohAnalysisManager}.
 */
public class StationSohAnalysisManagerConfiguration {

  private static final String STATION_SOH_PREFIX = "soh-control";
  private static final String UI_CONFIG_NAME = "ui.soh-settings";

  private final ConfigurationConsumerUtility configurationConsumerUtility;
  
  private final OsdRepositoryInterface osdRepositoryInterface;

  private final List<StationGroup> stationGroups;

  private StationSohAnalysisManagerConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    OsdRepositoryInterface osdRepositoryInterface) {

    this.configurationConsumerUtility = configurationConsumerUtility;
    this.osdRepositoryInterface = osdRepositoryInterface;

    final List<String> displayedStationGroups = configurationConsumerUtility
      .resolve(STATION_SOH_PREFIX + ".station-group-names",
        List.of(),
        StationGroupNamesConfigurationOption.class)
      .getStationGroupNames();

    this.stationGroups = osdRepositoryInterface
      .retrieveStationGroups(displayedStationGroups);
  }

  /**
   * Static factory method.
   *
   * @param configurationConsumerUtility
   * @param osdRepositoryInterface
   * @return
   */
  static StationSohAnalysisManagerConfiguration create(
    ConfigurationConsumerUtility configurationConsumerUtility,
    OsdRepositoryInterface osdRepositoryInterface) {

    Objects.requireNonNull(configurationConsumerUtility);
    Objects.requireNonNull(osdRepositoryInterface);

    return new StationSohAnalysisManagerConfiguration(
      configurationConsumerUtility,
      osdRepositoryInterface);
  }

  /**
   * Returns reprocessingPeriod from configuration
   *
   * @return reprocessingPeriod
   */
  public Duration reprocessingPeriod() {

    return Duration.parse(
      String.valueOf(configurationConsumerUtility
        .resolve(STATION_SOH_PREFIX, List.of())
        .get("reprocessingPeriod")));
  }

  /**
   * Returns a list of StationGroups
   *
   * @return
   */
  public List<StationGroup> stationGroups() {
    return stationGroups;
  }

  /**
   * Returns UI client parameters for all stations
   *
   * @return {@link StationSohMonitoringUiClientParameters}
   */
  public StationSohMonitoringUiClientParameters resolveDisplayParameters() {
    // Declare as LinkedHashSet to maintain order loaded
    List<String> displayedStationGroups = configurationConsumerUtility
      .resolve(STATION_SOH_PREFIX + ".station-group-names",
        List.of(),
        StationGroupNamesConfigurationOption.class)
      .getStationGroupNames();


    var rollupStationSohTimeTolerance = Duration.parse(String.valueOf(
      configurationConsumerUtility.resolve(
        STATION_SOH_PREFIX + ".rollup-stationsoh-time-tolerance",
        List.of()
      ).get("rollupStationSohTimeTolerance")));

    Set<Station> allStations = stationGroups.stream()
      .map(StationGroup::getStations)
      .flatMap(Set::stream)
      .collect(Collectors.toSet());

    Set<StationSohDefinition> stationSohDefinitions = PerformanceMonitoringConfigurationUtility
      .resolveStationSohDefinitions(configurationConsumerUtility, allStations);

    var stationSohControlConfiguration = StationSohMonitoringDefinition
      .from(reprocessingPeriod(), displayedStationGroups,
        rollupStationSohTimeTolerance, stationSohDefinitions);

    var stationSohMonitoringDisplayParameters =
      configurationConsumerUtility
        .resolve(UI_CONFIG_NAME, List.of(), StationSohMonitoringDisplayParameters.class);

    return StationSohMonitoringUiClientParameters
      .from(stationSohControlConfiguration, stationSohMonitoringDisplayParameters);
  }

  /**
   * Get the {@link OsdRepositoryInterface}.
   *
   * @return
   */
  public OsdRepositoryInterface getSohRepositoryInterface() {
    return osdRepositoryInterface;
  }

}

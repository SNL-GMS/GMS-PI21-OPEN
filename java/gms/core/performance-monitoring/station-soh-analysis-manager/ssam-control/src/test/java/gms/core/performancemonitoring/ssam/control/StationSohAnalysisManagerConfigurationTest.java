package gms.core.performancemonitoring.ssam.control;

import gms.core.performancemonitoring.soh.control.configuration.StationGroupNamesConfigurationOption;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringDisplayParameters;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;


class StationSohAnalysisManagerConfigurationTest {

  private static final String STATION_SOH_PREFIX = "soh-control";
  private static final String UI_CONFIG_NAME = "ui.soh-settings";


  @Test
  void testStationSohAnalysisManagerConfiguration() {

    var stationGroups = List.of("Group1", "Group2", "Group3", "Group4");
    var configurationConsumerUtility = Mockito.mock(ConfigurationConsumerUtility.class);
    var stationGroupNamesConfigurationOption = StationGroupNamesConfigurationOption
      .create(stationGroups);

    Mockito.when(configurationConsumerUtility
        .resolve(eq(STATION_SOH_PREFIX + ".station-group-names"), any(),
          eq(StationGroupNamesConfigurationOption.class)))
      .thenReturn(stationGroupNamesConfigurationOption);

    Mockito.when(configurationConsumerUtility
        .resolve(eq(STATION_SOH_PREFIX), any()))
      .thenReturn(Map.of("reprocessingPeriod", "PT20S"));

    Mockito.when(configurationConsumerUtility
        .resolve(eq(STATION_SOH_PREFIX + ".rollup-stationsoh-time-tolerance"), any()))
      .thenReturn(Map.of("rollupStationSohTimeTolerance", "PT30S"));

    var osdRepositoryInterface = Mockito.mock(OsdRepositoryInterface.class);
    var stationSohAnalysisManagerConfiguration = StationSohAnalysisManagerConfiguration
      .create(configurationConsumerUtility, osdRepositoryInterface);

    var stationSohMonitoringDisplayParameters = Mockito
      .mock(StationSohMonitoringDisplayParameters.class);

    Mockito.when(configurationConsumerUtility
        .resolve(eq(UI_CONFIG_NAME), any(),
          eq(StationSohMonitoringDisplayParameters.class)))
      .thenReturn(stationSohMonitoringDisplayParameters);

    Assertions.assertEquals(Duration.ofSeconds(20),
      stationSohAnalysisManagerConfiguration.reprocessingPeriod());

    Assertions.assertEquals(stationSohAnalysisManagerConfiguration.getSohRepositoryInterface(),
      osdRepositoryInterface);

    Assertions.assertEquals(stationGroups,
      stationSohAnalysisManagerConfiguration.resolveDisplayParameters()
        .getStationSohControlConfiguration().getDisplayedStationGroups());

    Assertions.assertEquals(Duration.ofSeconds(30),
      stationSohAnalysisManagerConfiguration.resolveDisplayParameters()
        .getStationSohControlConfiguration().getRollupStationSohTimeTolerance());

    Assertions.assertEquals(0, stationSohAnalysisManagerConfiguration.stationGroups().size());

  }

}

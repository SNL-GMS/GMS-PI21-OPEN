package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.STATION_SOH_PARAMETERS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class UiStationGroupGeneratorTest {
  @BeforeAll
  static void initializeContributingMap() {
    // Initialize the StationSohContributingUtility before creating a channel soh
    StationSohContributingUtility.getInstance().initialize(STATION_SOH_PARAMETERS);
  }

  @Test
  void testGeneratorStationGroups() {
    Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE);
    List<String> stationGroupNames =
      STATION_SOH_PARAMETERS.getStationSohControlConfiguration().getDisplayedStationGroups();
    List<UiStationGroupSoh> actual = assertDoesNotThrow(() ->
      UIStationGroupGenerator.buildSohStationGroups(
        Set.of(MARGINAL_STATION_GROUP_AND_STATION_CAPABILITY_ROLLUP),
        stationGroupNames,
        systemMessageSink
      ));
    Assertions.assertEquals(stationGroupNames.size(), actual.size());
    assertThat(actual
      .stream()
      .map(UiStationGroupSoh::getStationGroupName)
      .collect(Collectors.toSet())).containsAll(stationGroupNames);
  }
}

package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.STATION_SOH_PARAMETERS;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.CHANNEL;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.EXAMPLE_STATION;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationSohContributingUtilityTest {

  @BeforeAll
  static void initializeContributingMap() {
    // Initialize the StationSohContributingUtility before creating a channel soh
    StationSohContributingUtility.getInstance().initialize(STATION_SOH_PARAMETERS);
  }

  @Test
  void testContributingMapIsInitialized() {
    // Result of contributing by definition Channel Definition in materialized view
    boolean result = StationSohContributingUtility.getInstance().isChannelMonitorContributing(
      EXAMPLE_STATION,
      CHANNEL.getName(),
      SohMonitorType.ENV_GAP);

    // Should have found the contributing entry
    assertTrue(result);

    // Now for monitor type not contributing by definition Channel Definition in materialized view
    result = StationSohContributingUtility.getInstance().isChannelMonitorContributing(
      EXAMPLE_STATION,
      CHANNEL.getName(),
      SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED);

    // Should be false
    assert (result == false);
  }
}

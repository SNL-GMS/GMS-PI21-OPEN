package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.STATION_AND_STATION_GROUPS;

class UiStationAndStationGroupsTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(STATION_AND_STATION_GROUPS, UiStationAndStationGroups.class);
  }


}
package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.STATION_GROUP_SOH;

class UiStationGroupSohTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(STATION_GROUP_SOH, UiStationGroupSoh.class);
  }

}
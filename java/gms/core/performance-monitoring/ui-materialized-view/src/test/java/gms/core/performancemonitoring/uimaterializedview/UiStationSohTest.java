package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.UI_STATION_SOH;

class UiStationSohTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(UI_STATION_SOH, UiStationSoh.class);
  }

}
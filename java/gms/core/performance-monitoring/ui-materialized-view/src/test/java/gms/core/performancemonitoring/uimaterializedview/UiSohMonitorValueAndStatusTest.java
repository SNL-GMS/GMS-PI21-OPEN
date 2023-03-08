package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.MONITOR_VALUE_STATUS;

class UiSohMonitorValueAndStatusTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(MONITOR_VALUE_STATUS, UiSohMonitorValueAndStatus.class);
  }

}
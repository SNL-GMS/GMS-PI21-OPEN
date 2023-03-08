package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.CHANNEL_SOH;

class UiChannelSohTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(CHANNEL_SOH, UiChannelSoh.class);
  }
}
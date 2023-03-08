package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.ACK_CHANGE;

class AcknowledgedSohStatusChangeTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(ACK_CHANGE, AcknowledgedSohStatusChange.class);
  }

}
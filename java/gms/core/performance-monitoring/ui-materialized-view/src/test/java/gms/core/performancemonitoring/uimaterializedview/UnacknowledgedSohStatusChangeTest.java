package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.core.performancemonitoring.uimaterializedview.utils.MaterializedViewTestFixtures.UNACK_CHANGE_1;

class UnacknowledgedSohStatusChangeTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(UNACK_CHANGE_1, UnacknowledgedSohStatusChange.class);
  }

}
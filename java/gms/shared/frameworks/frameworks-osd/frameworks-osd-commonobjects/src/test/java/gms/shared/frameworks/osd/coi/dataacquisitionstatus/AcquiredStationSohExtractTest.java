package gms.shared.frameworks.osd.coi.dataacquisitionstatus;

import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

class AcquiredStationSohExtractTest {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(DataAcquisitionStatusTestFixtures.acquiredStationSohExtract,
      AcquiredStationSohExtract.class);
  }
}

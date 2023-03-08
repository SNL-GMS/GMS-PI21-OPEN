package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

class ReferenceNetworkTest {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.REFERENCE_NETWORK,
      ReferenceNetwork.class);
  }

}

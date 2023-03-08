package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

class ReferenceDigitizerTest {

  @Test
  void testSerialization() throws Exception {
    TestUtilities
      .testSerialization(StationReferenceTestFixtures.DIGITIZER, ReferenceDigitizer.class);
  }
}

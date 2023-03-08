package gms.shared.stationdefinition.coi.channel;

import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

class CalibrationTests {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.assertSerializes(UtilsTestFixtures.calibration,
      Calibration.class);
  }

}

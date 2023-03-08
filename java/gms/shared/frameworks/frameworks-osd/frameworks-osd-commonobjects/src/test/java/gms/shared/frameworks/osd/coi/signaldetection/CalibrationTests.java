package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

class CalibrationTests {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.calibration,
      Calibration.class);
  }

}

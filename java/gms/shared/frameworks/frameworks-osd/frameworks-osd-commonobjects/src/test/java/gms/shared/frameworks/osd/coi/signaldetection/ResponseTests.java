package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseTests {

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.RESPONSE,
      Response.class);
  }

  @Test
  void testResponseCreate() {
    Response response = Response.from(
      SignalDetectionTestFixtures.CHANNEL_NAME,
      SignalDetectionTestFixtures.calibration,
      SignalDetectionTestFixtures.FAP_RESPONSE);

    assertEquals(SignalDetectionTestFixtures.CHANNEL_NAME, response.getChannelName());
    assertEquals(SignalDetectionTestFixtures.calibration, response.getCalibration());
    assertEquals(SignalDetectionTestFixtures.FAP_RESPONSE, response.getFapResponse().orElse(null));
  }

}

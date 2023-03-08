package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrequencyAmplitudePhaseTest {
  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.FAP_RESPONSE,
      FrequencyAmplitudePhase.class);
  }

  /**
   * Test getting a response contained in the map. This is the Base Case.
   */
  @Test
  void testResponseFromMap() {
    final FrequencyAmplitudePhase fap = SignalDetectionTestFixtures.RESPONSE_BY_FREQUENCY_2;
    double frequency = 0.001000;
    AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    assertNotNull(apr);
    assertEquals(SignalDetectionTestFixtures.amplitudePhaseResponse, apr);
  }

  /**
   * Test getting a response using linear Interpolation.
   */
  @Test
  void testResponseInterpolated() {
    final FrequencyAmplitudePhase fap = SignalDetectionTestFixtures.RESPONSE_BY_FREQUENCY_2;
    double frequency = 0.001005;         //this value is halfway between the two values...
    AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    DoubleValue amp = DoubleValue.from(0.0000144695, 0.0, Units.NANOMETERS_PER_COUNT);
    DoubleValue phase = DoubleValue.from(350.1047945000, 0.0, Units.DEGREES);
    AmplitudePhaseResponse testApr = AutoValue_AmplitudePhaseResponse.from(amp, phase);
    assertNotNull(apr);
    assertEquals(apr, testApr);
  }

  /**
   * Test an invalid frequency that is > range
   */
  @Test
  void testInvalidFrequency() {
    assertThrows(IllegalStateException.class, () -> {
      FrequencyAmplitudePhase fap = SignalDetectionTestFixtures.RESPONSE_BY_FREQUENCY_2;
      double frequency = 0.001100;
      AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    });
  }

  /**
   * Test an invalid frequency that is < range
   */
  @Test
  void testInvalidFrequency2() {
    assertThrows(IllegalStateException.class, () -> {
      FrequencyAmplitudePhase fap = SignalDetectionTestFixtures.RESPONSE_BY_FREQUENCY_2;
      double frequency = 0.000900;
      AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    });
  }
}

package gms.shared.stationdefinition.coi.channel;

import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrequencyAmplitudePhaseTest {
  private UUID testUUID = UUID.nameUUIDFromBytes("test".getBytes());

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(UtilsTestFixtures.fapResponse, FrequencyAmplitudePhase.class);
  }

  @Test
  void testSerialization_entityReference() {
    TestUtilities.assertSerializes(FrequencyAmplitudePhase.createEntityReference(testUUID), FrequencyAmplitudePhase.class);
  }

  @Test
  void testFrequencyAmplitudePhase_CreateEntityReference_present() {
    Assertions.assertTrue(UtilsTestFixtures.fapResponse.isPresent());
  }

  @Test
  void testFrequencyAmplitudePhase_CreateEntityReference_notPresent() {
    FrequencyAmplitudePhase frequencyAmplitudePhase = getFrequencyAmplitudePhaseWithOnlyId(testUUID);
    assertFalse(frequencyAmplitudePhase.isPresent());
  }

  /**
   * Test getting a response contained in the map. This is the Base Case.
   */
  @Test
  void testResponseFromMap() {
    final FrequencyAmplitudePhase fap = UtilsTestFixtures.responseByFrequency2;
    double frequency = 0.001000;
    AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    assertNotNull(apr);
    Assertions.assertEquals(UtilsTestFixtures.amplitudePhaseResponse, apr);
  }

  /**
   * Test getting a response using linear Interpolation.
   */
  @Test
  void testResponseInterpolated() {
    final FrequencyAmplitudePhase fap = UtilsTestFixtures.responseByFrequency2;
    double frequency = 0.001005;         //this value is halfway between the two values...
    AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    DoubleValue amp = DoubleValue.from(0.0000144695, Optional.of(0.0), Units.NANOMETERS_PER_COUNT);
    DoubleValue phase = DoubleValue.from(350.1047945000, Optional.of(0.0), Units.DEGREES);
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
      FrequencyAmplitudePhase fap = UtilsTestFixtures.responseByFrequency2;
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
      FrequencyAmplitudePhase fap = UtilsTestFixtures.responseByFrequency2;
      double frequency = 0.000900;
      AmplitudePhaseResponse apr = fap.getResponseAtFrequency(frequency);
    });
  }

  private FrequencyAmplitudePhase getFrequencyAmplitudePhaseWithOnlyId(UUID id) {
    return FrequencyAmplitudePhase.createEntityReference(id);
  }
}

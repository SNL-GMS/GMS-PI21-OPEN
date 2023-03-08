package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FkSpectraDefinitionTests {

  private final FkSpectraDefinition definition = SignalDetectionTestFixtures.FK_SPECTRA_DEFINITION;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(definition,
      FkSpectraDefinition.class);
  }

  @Test
  void testBuildZeroWindowLength() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder()
      .setWindowLength(Duration.ZERO);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires windowLength of Duration > 0", e.getMessage());
  }

  @Test
  void testBuildNegativeSampleRate() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setSampleRateHz(-2);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires sampleRate > 0.0", e.getMessage());
  }

  @Test
  void testBuildNegativeLowFrequency() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setLowFrequencyHz(-1);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires lowFrequency >= 0.0", e.getMessage());
  }

  @Test
  void testBuildHighFrequencyLessThanLowFrequency() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setLowFrequencyHz(1)
      .setHighFrequencyHz(.5);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires lowFrequency < highFrequency", e.getMessage());
  }

  @Test
  void testBuildNegativeSlowCountX() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setSlowCountX(-1);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires slowCountX > 0", e.getMessage());
  }

  @Test
  void testBuildNegativeSlowCountY() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setSlowCountY(-1);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires slowCountY > 0", e.getMessage());
  }

  @Test
  void testBuildNegativeWaveformSampleRateHz() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setWaveformSampleRateHz(-2);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires waveformSampleRateHz > 0.0", e.getMessage());
  }

  @Test
  void testBuildNegativeWaveformSampleRateToleranceHz() {
    FkSpectraDefinition.Builder fkBuilder = definition.toBuilder().setWaveformSampleRateToleranceHz(-2);
    IllegalStateException e = assertThrows(IllegalStateException.class,
      () -> fkBuilder.build());
    assertEquals("FkSpectraDefinition requires waveformSampleRateToleranceHz >= 0.0", e.getMessage());
  }
}

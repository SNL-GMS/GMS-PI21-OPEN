package gms.shared.frameworks.osd.coi.signaldetection;


import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;


class BeamDefinitionTests {

  private final BeamDefinition beamDefinition = SignalDetectionTestFixtures.BEAM_DEFINITION;

  @Test
  void testSerialization() throws Exception {
    TestUtilities
      .testSerialization(beamDefinition, BeamDefinition.class);
  }

  @Test
  void testAziumithValidation() {
    BeamDefinition.Builder beamDefBuilder = beamDefinition.toBuilder().setAzimuth(-1);
    assertThrows(IllegalStateException.class,
      () -> beamDefBuilder.build());

    beamDefBuilder.setAzimuth(361);
    assertThrows(IllegalStateException.class,
      () -> beamDefBuilder.build());
  }

  @Test
  void testMinimumWaveformsValidation() {
    BeamDefinition.Builder beamDefBuilder = beamDefinition.toBuilder().setMinimumWaveformsForBeam(0);
    assertThrows(IllegalStateException.class,
      () -> beamDefBuilder.build());
  }
}

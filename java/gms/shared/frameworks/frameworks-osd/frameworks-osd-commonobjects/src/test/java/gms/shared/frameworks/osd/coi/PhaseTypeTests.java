package gms.shared.frameworks.osd.coi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhaseTypeTests {

  @Test
  void testPorSCheckForSampleOfPhases() {
    assertEquals(PhaseType.P, PhaseType.pPdiff.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.pPKiKP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.pPKP.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.SKKSdf.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.SKP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.SKPab.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.SKPbc.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.PPP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.PPP_B.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.PPS_B.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.PS.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.PS_1.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.pSdiff.getFinalPhase());

  }

}

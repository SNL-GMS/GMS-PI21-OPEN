package gms.shared.signaldetection.coi.types;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhaseTypeTests {

  @Test
  void testPorSCheckForSampleOfPhases() {
    assertEquals(PhaseType.P, PhaseType.pPdiff.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.pPKiKP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.pPKP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.SKP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.PPP.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.PPP_B.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.sP.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.PPS.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.PPS_B.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.PS.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.pSdiff.getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.sS.getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.valueOfLabel("pP").getFinalPhase());
    assertEquals(PhaseType.P, PhaseType.valueOfLabel("sP").getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.valueOfLabel("pSdiff").getFinalPhase());
    assertEquals(PhaseType.S, PhaseType.valueOfLabel("sS").getFinalPhase());
  }

  @Test
  void testNewValueMap() {

    Arrays.stream(PhaseType.values())
      .filter(phaseType -> phaseType != PhaseType.UNKNOWN && phaseType != PhaseType.WILD_CARD)
      .forEach(phaseType -> assertEquals(phaseType.name(), phaseType.getLabel()));

    assertEquals( "*", PhaseType.WILD_CARD.getLabel());
    assertEquals( "Unknown", PhaseType.UNKNOWN.getLabel());
  }
}

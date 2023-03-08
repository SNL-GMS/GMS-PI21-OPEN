package gms.shared.frameworks.configuration.constraints;

import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.osd.coi.PhaseType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhaseConstraintTests {

  @Test
  void testPhaseIN() {
    final PhaseConstraint con =
      PhaseConstraint.from("phase",
        Operator.from(Type.IN, false),
        Set.of(PhaseType.P, PhaseType.Lg, PhaseType.S),
        1);

    assertAll(
      () -> assertTrue(con.test(PhaseType.P)),
      () -> assertTrue(con.test(PhaseType.Lg)),
      () -> assertTrue(con.test(PhaseType.S)),
      () -> assertFalse(con.test(PhaseType.I))
    );
  }

  @Test
  void testPhaseEQ() {
    final PhaseConstraint con =
      PhaseConstraint.from("phase",
        Operator.from(Type.EQ, false),
        Set.of(PhaseType.P),
        1);

    assertAll(
      () -> assertTrue(con.test(PhaseType.P)),
      () -> assertFalse(con.test(PhaseType.I))
    );
  }

  @Test
  void testPhaseNotEQ() {
    final PhaseConstraint con =
      PhaseConstraint.from("phase",
        Operator.from(Type.EQ, false),
        Set.of(PhaseType.P, PhaseType.Lg),
        1);

    assertFalse(con.test(PhaseType.P));
  }

  @Test
  void testIsSatisfiedValidatesParameter() {
    Operator inOp = Operator.from(Type.IN, false);
    Set<PhaseType> phaseTypes = Set.of(PhaseType.P, PhaseType.Lg, PhaseType.S);
    PhaseConstraint phaseConstraint = PhaseConstraint.from("phase", inOp, phaseTypes, 1);

    NullPointerException e = assertThrows(NullPointerException.class,
      () -> phaseConstraint.test(null));
    assertTrue(e.getMessage().contains("queryVal can't be null"));
  }
}

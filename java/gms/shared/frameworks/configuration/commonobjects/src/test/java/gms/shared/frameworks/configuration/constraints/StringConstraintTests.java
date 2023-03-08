package gms.shared.frameworks.configuration.constraints;

import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.Operator.Type;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringConstraintTests {

  @Test
  void testPhaseIN() {
    final StringConstraint con =
      StringConstraint.from("phase",
        Operator.from(Type.IN, false),
        Set.of("P", "Lg", "S"),
        1);

    assertAll(
      () -> assertTrue(con.test("P")),
      () -> assertTrue(con.test("Lg")),
      () -> assertTrue(con.test("S")),
      () -> assertFalse(con.test("I"))
    );
  }

  @Test
  void testPhaseEQ() {
    final StringConstraint con =
      StringConstraint.from("phase",
        Operator.from(Type.EQ, false),
        Set.of("P"),
        1);

    assertAll(
      () -> assertTrue(con.test("P")),
      () -> assertFalse(con.test("I"))
    );
  }

  @Test
  void testPhaseNotEQ() {
    final StringConstraint con =
      StringConstraint.from("phase",
        Operator.from(Type.EQ, false),
        Set.of("P", "Lg"),
        1);

    assertFalse(con.test("P"));
  }

  @Test
  void testIsSatisfiedValidatesParameter() {
    Operator inOp = Operator.from(Type.IN, false);
    Set<String> phaseSet = Set.of("P", "Lg", "S");
    StringConstraint stringConstraint = StringConstraint.from("phase", inOp, phaseSet, 1);

    NullPointerException e = assertThrows(NullPointerException.class,
      () -> stringConstraint
        .test(null));
    assertTrue(e.getMessage().contains("queryVal can't be null"));
  }
}

package gms.shared.frameworks.configuration;

import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.configuration.constraints.NumericScalarConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintEvaluatorTests {

  private NumericScalarConstraint constraint;

  @BeforeEach
  void setUp() throws Exception {
    constraint = NumericScalarConstraint
      .from("criterion", Operator.from(Type.EQ, false), 10.0, 10);
  }

  @Test
  void testEvaluateExpectTrue() {
    assertTrue(ConstraintEvaluator.evaluate(constraint, Selector.from("criterion", 10.0)));
  }

  @Test
  void testEvaluateDifferentCriterionExpectFalse() {
    assertFalse(
      ConstraintEvaluator.evaluate(constraint, Selector.from("different criterion", 10.0)));
  }

  @Test
  void testEvaluateDifferentValueExpectFalse() {
    assertFalse(
      ConstraintEvaluator.evaluate(constraint, Selector.from("criterion", -10.0)));
  }
}

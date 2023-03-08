package gms.shared.frameworks.configuration.constraints;

import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanConstraintTests {

  @Test
  void testFrom() {

    final String criterion = "criterion";
    final boolean value = true;
    final long priority = 100;

    final BooleanConstraint constraint = BooleanConstraint.from(criterion, value, priority);
    assertAll(
      () -> assertNotNull(constraint),
      () -> Assertions.assertEquals(criterion, constraint.getCriterion()),
      () -> Assertions.assertEquals(value, constraint.getValue()),
      () -> Assertions.assertEquals(priority, constraint.getPriority())
    );
  }

  @Test
  void testIsSatisfiedTrueConstraint() {
    final BooleanConstraint trueConstraint = BooleanConstraint.from("A", true, 1);
    assertAll(
      () -> assertTrue(trueConstraint.test(true)),
      () -> assertFalse(trueConstraint.test(false)),
      () -> assertFalse(trueConstraint.test(null))
    );
  }

  @Test
  void testIsSatisfiedFalseConstraint() {
    final BooleanConstraint falseConstraint = BooleanConstraint.from("A", false, 1);
    assertAll(
      () -> assertFalse(falseConstraint.test(true)),
      () -> assertTrue(falseConstraint.test(false)),
      () -> assertFalse(falseConstraint.test(null))
    );
  }

  @Test
  void testSerializationIgnoresParentProperties() {
    final Map<String, Object> fieldMap = FieldMapUtilities
      .toFieldMap(BooleanConstraint.from("A", true, 3));

    assertAll(
      () -> assertNotNull(fieldMap),
      () -> assertEquals(4, fieldMap.size()),
      () -> assertTrue(fieldMap.containsKey("constraintType")),
      () -> assertTrue(fieldMap.containsKey("criterion")),
      () -> assertTrue(fieldMap.containsKey("value")),
      () -> assertTrue(fieldMap.containsKey("priority"))
    );
  }
}

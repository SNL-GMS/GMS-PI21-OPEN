package gms.shared.frameworks.configuration.constraints;

import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WildcardConstraintTests {

  @Test
  void testFrom() {
    Assertions.assertEquals("FOO", WildcardConstraint.from("FOO").getCriterion());
  }

  @Test
  void testIsSatisfiedIsTrue() {
    final WildcardConstraint constraint = WildcardConstraint.from("FOO");

    assertAll(
      () -> assertTrue(constraint.test("")),
      () -> assertTrue(constraint.test(-1)),
      () -> assertTrue(constraint.test(100.0))
    );
  }

  @Test
  void testSerializationIgnoresParentProperties() {
    final Map<String, Object> fieldMap = FieldMapUtilities.toFieldMap(WildcardConstraint.from("A"));

    assertAll(
      () -> assertNotNull(fieldMap),
      () -> assertEquals(2, fieldMap.size()),
      () -> assertTrue(fieldMap.containsKey("constraintType")),
      () -> assertTrue(fieldMap.containsKey("criterion"))
    );
  }
}

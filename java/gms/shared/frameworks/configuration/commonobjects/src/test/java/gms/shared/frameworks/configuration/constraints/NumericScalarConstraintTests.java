package gms.shared.frameworks.configuration.constraints;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumericScalarConstraintTests {

  private ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Test
  void testSerialization() throws Exception {
    Operator operator = Operator.from(Type.EQ, false);
    final NumericScalarConstraint snrIs10 = NumericScalarConstraint
      .from("sta", operator, 5.0, 100);

    final String json = objectMapper.writeValueAsString(snrIs10);
    assertNotNull(json);

    final Constraint deserialized = objectMapper.readValue(json, Constraint.class);
    assertEquals(snrIs10, deserialized);
  }

  @Test
  void testFromValidatesArguments() {
    Operator inOp = Operator.from(Type.IN, false);

    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
      () -> NumericScalarConstraint.from("snr", inOp, 5.0, 1));
    assertTrue(e.getMessage().contains("Operator Type: IN is not supported"));
  }

  @Test
  void testIsSatisfied() {
    final double value = 5.0;
    final Operator operator = Operator.from(Type.EQ, false);
    final NumericScalarConstraint scalar = NumericScalarConstraint
      .from("snr", operator, value, 1);

    assertAll(
      () -> assertTrue(scalar.test(value)),
      () -> assertFalse(scalar.test(value + 1.0)),
      () -> assertFalse(scalar.test(value - 1.0e-15))
    );
  }

  @Test
  void testIsSatisfiedValidatesParameter() {
    NumericScalarConstraint numConstraint =
      NumericScalarConstraint.from("snr", Operator.from(Type.EQ, false), 5.0, 1);
    NullPointerException e = assertThrows(NullPointerException.class,
      () -> numConstraint.test(null));

    assertTrue(e.getMessage().contains("selector can't be null"));
  }
}

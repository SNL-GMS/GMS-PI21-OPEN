package gms.shared.frameworks.osd.coi;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ParameterValidation} utility operations.
 */
class ParameterValidationTests {

  @Test
  void testRequireTrue() {
    assertDoesNotThrow(() -> ParameterValidation.requireTrue(Predicate.isEqual("test"), "test", "valid test"));
    assertDoesNotThrow(() -> ParameterValidation.requireTrue(String::equals, "test", "test", "valid test"));
  }

  @Test
  void testRequireTruePredicateExpectIllegalArgumentException() {
    final String exceptionMessage = "invalid test";
    final Predicate<String> testPred = Predicate.isEqual("test");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> ParameterValidation
        .requireTrue(testPred, "not test", exceptionMessage));
    assertTrue(exception.getMessage().contains(exceptionMessage));
  }

  @Test
  void testRequireTrueBiPredicateExpectIllegalArgumentException() {
    final String exceptionMessage = "invalid test";
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> ParameterValidation
        .requireTrue(String::equals, "test", "not test", exceptionMessage));
    assertTrue(exception.getMessage().contains(exceptionMessage));
  }
}

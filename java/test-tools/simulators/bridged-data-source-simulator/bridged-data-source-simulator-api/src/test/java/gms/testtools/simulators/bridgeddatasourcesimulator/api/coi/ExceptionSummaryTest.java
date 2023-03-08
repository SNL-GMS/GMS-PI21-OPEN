package gms.testtools.simulators.bridgeddatasourcesimulator.api.coi;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ExceptionSummaryTest {

  @ParameterizedTest
  @MethodSource("getCreateArguments")
  void testCreateValidation(String expectedMessage,
    Instant exceptionTime,
    String exceptionType,
    String message) {

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
      () -> ExceptionSummary.create(exceptionTime, exceptionType, message));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getCreateArguments() {
    return Stream.of(
      arguments("Exception type cannot be blank", Instant.EPOCH, "", "test"),
      arguments("Message cannot be blank", Instant.EPOCH, "test", ""));
  }

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(ExceptionSummary.create(Instant.now(), "Test Type", "test value"),
      ExceptionSummary.class);
  }

}
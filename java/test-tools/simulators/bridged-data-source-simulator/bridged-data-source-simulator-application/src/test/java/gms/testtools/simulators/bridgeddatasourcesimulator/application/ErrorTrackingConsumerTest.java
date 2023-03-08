package gms.testtools.simulators.bridgeddatasourcesimulator.application;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.ExceptionSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ErrorTrackingConsumerTest {

  @Test
  void testCreateValidation() {
    NullPointerException exception = assertThrows(NullPointerException.class,
      () -> ErrorTrackingConsumer.create(null));
    assertEquals("simulatorExceptions cannot be null", exception.getMessage());
  }

  @Test
  void testCreate() {
    ErrorTrackingConsumer consumer = assertDoesNotThrow(() -> ErrorTrackingConsumer.create(Map.of()));
    assertNotNull(consumer);
  }

  @ParameterizedTest
  @MethodSource("getAcceptArguments")
  void testAccept(Integer expectedExceptionCount, DataSimulatorConsumerResult result) {
    Map<String, List<ExceptionSummary>> simulatorExceptionSummaries = new HashMap<>();
    ErrorTrackingConsumer consumer = ErrorTrackingConsumer.create(simulatorExceptionSummaries);
    consumer.accept(result);
    if (expectedExceptionCount != 0) {
      assertEquals(expectedExceptionCount, simulatorExceptionSummaries.get(result.getSimulatorName()).size());
    } else {
      assertTrue(simulatorExceptionSummaries.isEmpty());
    }
  }

  static Stream<Arguments> getAcceptArguments() {
    ExceptionSummary exceptionSummary = ExceptionSummary.create(Instant.now(), "getAcceptArguments",
      "runtime exception");
    return Stream.of(
      arguments(1, DataSimulatorConsumerResult.create("test", Optional.of(exceptionSummary))),
      arguments(0, DataSimulatorConsumerResult.create("test", Optional.empty())));
  }

}
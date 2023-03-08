package gms.testtools.simulators.bridgeddatasourcesimulator.application;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.ExceptionSummary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ErrorCapturingFunctionTest {

  @Mock
  private Consumer<BridgedDataSourceDataSimulator> mockConsumer;

  @Test
  void testCreateValidation() {
    Exception ex = assertThrows(NullPointerException.class, () -> ErrorCapturingFunction.create(null));
    assertEquals("dataSimulatorFunction cannot be null", ex.getMessage());
  }

  @Test
  void testCreate() {
    ErrorCapturingFunction function = assertDoesNotThrow(() -> ErrorCapturingFunction.create(mockConsumer));
    assertNotNull(function);
  }

  @ParameterizedTest
  @MethodSource("getApplyParameters")
  void testApply(
    Consumer<BridgedDataSourceDataSimulator> consumer,
    DataSimulatorConsumerResult expected,
    BridgedDataSourceDataSimulator simulator) {

    ErrorCapturingFunction function = ErrorCapturingFunction.create(consumer);
    DataSimulatorConsumerResult actual = function.apply(simulator);
    assertEquals(expected.getSimulatorName(), actual.getSimulatorName());

    expected.getExceptionSummary().ifPresentOrElse(exception -> assertTrue(actual.getExceptionSummary().isPresent()),
      () -> actual.getExceptionSummary().ifPresent(exception -> Assertions.fail("Exception thrown when not expected")));
    verify(consumer).accept(simulator);
    verifyNoMoreInteractions(consumer, simulator);
  }

  static Stream<Arguments> getApplyParameters() {
    Consumer<BridgedDataSourceDataSimulator> errorThrowingMock = mock(Consumer.class);
    doThrow(new RuntimeException("mock runtime exception.")).when(errorThrowingMock).accept(any());
    BridgedDataSourceDataSimulator simulator = mock(BridgedDataSourceDataSimulator.class);

    Consumer<BridgedDataSourceDataSimulator> successMock = mock(Consumer.class);
    ExceptionSummary exceptionSummary = ExceptionSummary.create(Instant.now(), "runtimeException",
      "mock runtime exception.");
    return Stream.of(
      arguments(errorThrowingMock,
        DataSimulatorConsumerResult.create(simulator.getClass().getSimpleName(), Optional.of(exceptionSummary)),
        simulator),
      arguments(successMock,
        DataSimulatorConsumerResult.create(simulator.getClass().getSimpleName(), Optional.empty()),
        simulator));
  }

}
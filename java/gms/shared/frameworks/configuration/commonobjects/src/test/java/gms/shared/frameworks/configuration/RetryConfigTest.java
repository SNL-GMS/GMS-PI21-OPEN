package gms.shared.frameworks.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryConfigTest {

  @ParameterizedTest
  @MethodSource("createSource")
  void testCreate(long initialDelay, long maxDelay, int maxAttempts, boolean shouldFail) {
    Executable create = () -> RetryConfig.create(initialDelay, maxDelay, ChronoUnit.MILLIS, maxAttempts);
    if (shouldFail) {
      assertThrows(IllegalArgumentException.class, create);
    } else {
      assertDoesNotThrow(create);
    }
  }

  public static Stream<Arguments> createSource() {
    return Stream.of(
      Arguments.arguments(1, 2, 1, false),
      Arguments.arguments(1, 2, -1, false),
      Arguments.arguments(0, 1, 1, true),
      Arguments.arguments(1, 1, 1, true),
      Arguments.arguments(1, 2, 0, true),
      Arguments.arguments(1, 2, -2, true)
    );
  }

  @Test
  void toBaseRetryPolicy() {
    long initalDelay = 1;
    long maxDelay = 2;
    var delayUnits = ChronoUnit.MILLIS;
    int maxAttempts = 3;

    var retryConfig = RetryConfig.create(initalDelay, maxDelay, delayUnits, maxAttempts);

    var retryPolicy = assertDoesNotThrow(() -> retryConfig.toBaseRetryPolicy());

    assertEquals(Duration.of(initalDelay, delayUnits), retryPolicy.getDelay());
    assertEquals(Duration.of(maxDelay, delayUnits), retryPolicy.getMaxDelay());
    assertEquals(maxAttempts, retryPolicy.getMaxAttempts());
  }
}
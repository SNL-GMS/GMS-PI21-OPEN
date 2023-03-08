package gms.shared.signaldetection.database.connector.config;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class WaveformTrimDefinitionTest {

  @ParameterizedTest
  @MethodSource("getCreateValidationArguments")
  void testCreateValidation(String expectedMessage,
    Duration measuredWaveformLeadDuration,
    Duration measuredWaveformLagDuration) {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> WaveformTrimDefinition.create(measuredWaveformLeadDuration, measuredWaveformLagDuration));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getCreateValidationArguments() {
    return Stream.of(
      arguments("Measured waveform lead duration cannot be negative",
        Duration.ofHours(-1),
        Duration.ofHours(1)),
      arguments("Measured waveform lag duration cannot be negative",
        Duration.ofHours(1),
        Duration.ofHours(-1)));
  }

  @Test
  void testSerialization() throws IOException {
    WaveformTrimDefinition definition = WaveformTrimDefinition.create(Duration.ofHours(1), Duration.ofHours(2));
    TestUtilities.assertSerializes(definition, WaveformTrimDefinition.class);
  }
}
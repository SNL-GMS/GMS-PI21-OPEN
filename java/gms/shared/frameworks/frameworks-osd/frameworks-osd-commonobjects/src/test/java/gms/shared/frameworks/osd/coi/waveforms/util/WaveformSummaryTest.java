package gms.shared.frameworks.osd.coi.waveforms.util;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaveformSummaryTest {

  private final String id = "TESTNET.STALOC.CHAN";
  private final Instant startTime = Instant.EPOCH;
  private final Instant endTime = Instant.EPOCH.plusMillis(2000);

  @Test
  void testWaveformSummaryFromChecksNullArguments() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      WaveformSummary.class, "from",
      id, startTime, endTime);
  }

  @ParameterizedTest
  @MethodSource("validateSource")
  void testValidate(Class<? extends Exception> exceptionClass, Instant startTime, Instant endTime) {
    if (exceptionClass != null) {
      assertThrows(exceptionClass, () -> WaveformSummary.from("TEST", startTime, endTime));
    } else {
      assertDoesNotThrow(() -> WaveformSummary.from("TEST", startTime, endTime));
    }
  }

  private static Stream<Arguments> validateSource() {
    return Stream.of(
      Arguments.arguments(IllegalArgumentException.class, Instant.MAX, Instant.EPOCH),
      Arguments.arguments(null, Instant.EPOCH, Instant.EPOCH),
      Arguments.arguments(null, Instant.EPOCH, Instant.MAX)
    );
  }
}

package gms.shared.utilities.signalprocessing.snr;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import gms.shared.utilities.signalprocessing.normalization.Transform;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SignalNoiseRatioTests {

  private static Waveform waveform;
  private static Instant noiseWindowStart;
  private static Instant noiseWindowEnd;
  private static Instant signalWindowStart;
  private static Instant signalWindowEnd;
  private static Duration slidingWindowSize;
  private static double expectedSnr = 149.6665;

  @BeforeAll
  public static void init() {
    double[] waveformValues = new double[200];
    for (int i = 0; i < 150; i++) {
      waveformValues[i] = 1;
    }

    waveformValues[150] = 2;
    waveformValues[151] = 3;
    waveformValues[152] = -3;
    waveformValues[153] = -2;

    for (int i = 154; i < 200; i++) {
      waveformValues[i] = 1;
    }

    noiseWindowStart = Instant.EPOCH;
    waveform = Waveform.from(noiseWindowStart, 2, waveformValues);

    noiseWindowEnd = waveform.computeSampleTime(144);
    signalWindowStart = waveform.computeSampleTime(146);
    signalWindowEnd = waveform.computeSampleTime(157);
    slidingWindowSize = Duration.ofSeconds(1);
  }

  @ParameterizedTest
  @MethodSource("getSnrNullParameters")
  public void testGetSnrNullParameters(String expectedMessage, Waveform waveform,
    Instant noiseWindowStart, Instant noiseWindowEnd, Instant signalWindowStart,
    Instant signalWindowEnd, Duration slidingWindowSize, Transform transform) {

    NullPointerException actual = assertThrows(NullPointerException.class,
      () -> SignalNoiseRatio
        .getSnr(waveform, noiseWindowStart, noiseWindowEnd, signalWindowStart, signalWindowEnd,
          slidingWindowSize, transform));

    assertEquals(expectedMessage, actual.getMessage());
  }

  private static Stream<Arguments> getSnrNullParameters() {
    return Stream.of(
      arguments("SNR cannot be calculated from a null waveform", null, noiseWindowStart,
        noiseWindowEnd, signalWindowStart, signalWindowEnd,
        slidingWindowSize, Transform.ABS),
      arguments("SNR cannot be calculated from a null Noise Window Start", waveform,
        null, noiseWindowEnd, signalWindowStart, signalWindowEnd,
        slidingWindowSize, Transform.ABS),
      arguments("SNR cannot be calculated from a null Noise Window End", waveform,
        noiseWindowStart, null, signalWindowStart, signalWindowEnd,
        slidingWindowSize, Transform.ABS),
      arguments("SNR cannot be calculated from a null Signal Window Start", waveform,
        noiseWindowStart, noiseWindowEnd, null, signalWindowEnd,
        slidingWindowSize, Transform.ABS),
      arguments("SNR cannot be calculated from a null Signal Window End", waveform,
        noiseWindowStart, noiseWindowEnd, signalWindowStart, null,
        slidingWindowSize, Transform.ABS),
      arguments("SNR cannot be calculated from a null Sliding Window Size", waveform,
        noiseWindowStart, noiseWindowEnd, signalWindowStart, signalWindowEnd,
        null, Transform.ABS),
      arguments("SNR cannot be calculated from a null Transform", waveform, noiseWindowStart,
        noiseWindowEnd, signalWindowStart, signalWindowEnd,
        slidingWindowSize, null)
    );
  }

  @ParameterizedTest
  @MethodSource("getSnrIllegalParameters")
  public void testGetSnrIllegalParameters(String expectedMessage, Waveform waveform,
    Instant noiseWindowStart, Instant noiseWindowEnd, Instant signalWindowStart,
    Instant signalWindowEnd, Duration slidingWindowSize, Transform transform) {

    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
      () -> SignalNoiseRatio
        .getSnr(waveform, noiseWindowStart, noiseWindowEnd, signalWindowStart, signalWindowEnd,
          slidingWindowSize, transform));

    assertEquals(expectedMessage, actual.getMessage());
  }

  private static Stream<Arguments> getSnrIllegalParameters() {
    return Stream.of(
      arguments("Noise Window Start must be before Noise Window End", waveform, noiseWindowEnd,
        noiseWindowStart, signalWindowStart, signalWindowEnd, slidingWindowSize, Transform.ABS),
      arguments("Signal Window Start must be before Signal Window End", waveform,
        noiseWindowStart, noiseWindowEnd, signalWindowEnd, signalWindowStart,
        slidingWindowSize, Transform.ABS),
      arguments("Noise and Signal Windows cannot overlap", waveform,
        noiseWindowStart, signalWindowStart, noiseWindowEnd, signalWindowEnd, slidingWindowSize,
        Transform.ABS),
      arguments("Noise window cannot start before the waveform start time", waveform,
        waveform.getStartTime().minusSeconds(1), noiseWindowEnd, signalWindowStart,
        signalWindowEnd, slidingWindowSize, Transform.ABS),
      arguments("Noise window cannot end after the waveform end time", waveform, noiseWindowStart,
        waveform.getEndTime().plusNanos(10), waveform.getEndTime().plusNanos(2000),
        waveform.getEndTime().plusNanos(500000), slidingWindowSize, Transform.ABS),
      arguments("Signal window cannot start before the waveform start time", waveform,
        signalWindowStart, signalWindowEnd, waveform.getStartTime().minusSeconds(1),
        noiseWindowEnd, slidingWindowSize, Transform.ABS),
      arguments("Signal window cannot end after the waveform end time", waveform,
        noiseWindowStart,
        noiseWindowEnd, signalWindowStart, waveform.getEndTime().plusSeconds(1),
        slidingWindowSize, Transform.ABS),
      arguments("Sliding window cannot be larger than the signal window", waveform,
        noiseWindowStart, noiseWindowEnd, signalWindowStart, signalWindowEnd,
        Duration.between(signalWindowStart, signalWindowEnd.plusNanos(5)), Transform.ABS)
    );
  }

  @Test
  void testGetSnr() {
    double snr = SignalNoiseRatio.getSnr(waveform,
      noiseWindowStart,
      noiseWindowEnd,
      signalWindowStart,
      signalWindowEnd,
      slidingWindowSize,
      Transform.ABS);
    assertEquals(expectedSnr, snr, .001);
  }

}

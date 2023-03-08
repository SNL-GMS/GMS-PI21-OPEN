package gms.shared.waveform.coi;

import com.google.common.collect.Range;
import gms.shared.waveform.testfixture.WaveformTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.stream.Stream;

import static gms.shared.waveform.testfixture.WaveformTestFixtures.WAVEFORM_1;
import static gms.shared.waveform.testfixture.WaveformTestFixtures.randomSamples0To1;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class WaveformTest {

  private final Instant startTime = Instant.EPOCH;
  private final double sampleRate = 5.0;

  @ParameterizedTest
  @MethodSource("getCreateArguments")
  void testCreateValidation(Class<? extends Exception> expectedException,
    Instant startTime,
    double sampleRate,
    double[] values) {
    assertThrows(expectedException,
      () -> Waveform.create(startTime, sampleRate, values));
  }

  static Stream<Arguments> getCreateArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, 40.0, new double[]{1.0, 2.0, 3.0, 4.0}),
      arguments(IllegalArgumentException.class, Instant.now(), 0.0, new double[]{1.0, 2.0, 3.0, 4.0}),
      arguments(IllegalArgumentException.class, Instant.now(), -1.0, new double[]{1.0, 2.0, 3.0, 4.0}),
      arguments(NullPointerException.class, Instant.now(), 40.0, null),
      arguments(IllegalArgumentException.class, Instant.now(), 40.0, new double[]{})
    );
  }

  @Test
  void createRoundingTest() {
    var values = new double[]{-36.488949, -62.91744, -39.425449, -219.70204999999999, -162.59755};
    var result = new double[]{-36.4889, -62.9174, -39.4254, -219.7021, -162.5976};

    final Waveform waveform = Waveform.create(startTime, sampleRate, values);
    assertArrayEquals(result, waveform.getSamples());
  }

  @Test
  void createValidTest() {
    final int sampleCount = 5;
    final Timeseries t = Waveform.create(startTime, sampleRate, new double[sampleCount]);
    assertEquals(sampleCount, t.getSampleCount());
    // 5 samples, one sample every 200ms,
    // end time should be 800ms later (not 1000ms because first sample is at 0)
    assertEquals(Instant.EPOCH.plusMillis(800), t.getEndTime());
  }

  @Test
  void createNegativeSampleRateTest() {
    assertThrows(IllegalArgumentException.class,
      () -> Waveform.create(startTime, -0.005, new double[1]));
  }

  @Test
  void createZeroSampleRateTest() {
    assertThrows(IllegalArgumentException.class, () -> Waveform.create(startTime, 0, new double[1]));
  }

  @Test
  void testTimeForSample() {
    Instant start = Instant.EPOCH;
    Timeseries t = Waveform.create(start, sampleRate, new double[2000]);
    assertEquals(Instant.EPOCH, t.computeSampleTime(0));
    assertEquals(Instant.EPOCH.plusSeconds(1), t.computeSampleTime(5));
    assertEquals(Instant.EPOCH.plusSeconds(20), t.computeSampleTime(100));
  }

  @Test
  void testTimeForSampleBeyondRangeExpect() {
    Timeseries t = Waveform.create(Instant.EPOCH, sampleRate, new double[1]);
    int sampleCount = t.getSampleCount();
    assertThrows(IllegalArgumentException.class, () -> t.computeSampleTime(sampleCount)
    );
  }

  @Test
  void testTimeForSampleBeforeRange() {
    Waveform waveform = Waveform.create(Instant.EPOCH, sampleRate, new double[1]);
    assertThrows(IllegalArgumentException.class,
      () -> waveform.computeSampleTime(-1));
  }

  @Test
  void testTimeRange() {
    Timeseries t = Waveform.create(startTime, sampleRate, new double[5]);
    Range<Instant> range = t.computeTimeRange();

    Timeseries t2 = Waveform.create(startTime.plusSeconds(30), sampleRate, new double[5]);
    Range<Instant> range2 = t2.computeTimeRange();

    assertEquals(t.getStartTime(), range.lowerEndpoint());
    assertEquals(t.getEndTime(), range.upperEndpoint());

    assertEquals(t2.getStartTime(), range2.lowerEndpoint());
    assertEquals(t2.getEndTime(), range2.upperEndpoint());
  }

  @Test
  void testCreate() {
    assertNotNull(Waveform.create(Instant.now(), 20.0, new double[]{1.0, 2.0, 3.0, 4.0}));
  }

  @ParameterizedTest
  @MethodSource("getTrimArguments")
  void testTrimValidation(Class<? extends Exception> expectedException,
    Waveform waveform,
    Instant start,
    Instant end) {

    assertThrows(expectedException, () -> waveform.trim(start, end));
  }

  static Stream<Arguments> getTrimArguments() {
    Waveform waveform = Waveform.create(Instant.EPOCH.plus(5, ChronoUnit.MINUTES),
      40,
      new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0});

    return Stream.of(arguments(NullPointerException.class, waveform, null, Instant.now()),
      arguments(NullPointerException.class, waveform, Instant.EPOCH, null),
      arguments(IllegalStateException.class, waveform, Instant.now(), Instant.EPOCH));
  }

  @Test
  void testTrim() {
    Waveform waveform = WaveformTestFixtures.epochStart100RandomSamples(2);
    Waveform trimmed = waveform.trim(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(4));

    assertNotNull(trimmed);
    assertEquals(Instant.EPOCH.plusSeconds(2), trimmed.getStartTime());
    assertEquals(Instant.EPOCH.plusSeconds(4), trimmed.getEndTime());

    double[] expectedValues = Arrays.copyOfRange(waveform.getSamples(), 4, 9);
    assertEquals(expectedValues.length, trimmed.getSamples().length);
    assertArrayEquals(expectedValues, trimmed.getSamples());
  }

  @ParameterizedTest
  @MethodSource("getWindowArguments")
  void testWindowValidation(Class<? extends Exception> expectedException,
    Waveform waveform,
    Instant start,
    Instant end) {

    assertThrows(expectedException, () -> waveform.window(start, end));
  }

  static Stream<Arguments> getWindowArguments() {
    Waveform waveform = WaveformTestFixtures.epochStart100RandomSamples(2);
    return Stream.of(arguments(NullPointerException.class, waveform, null, waveform.getEndTime()),
      arguments(NullPointerException.class, waveform, waveform.getStartTime(), null),
      arguments(IllegalArgumentException.class, waveform, waveform.getStartTime().minusMillis(1), waveform.getEndTime()),
      arguments(IllegalArgumentException.class, waveform, waveform.getStartTime(), waveform.getEndTime().plusMillis(1)),
      arguments(IllegalStateException.class, waveform, waveform.getStartTime().plusMillis(20), waveform.getStartTime().plusMillis(10)));
  }

  @Test
  void testWindow() {
    Waveform waveform = WaveformTestFixtures.epochStart100RandomSamples(2);
    Waveform trimmed = waveform.window(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(4));

    assertNotNull(trimmed);
    assertEquals(Instant.EPOCH.plusSeconds(2), trimmed.getStartTime());
    assertEquals(Instant.EPOCH.plusSeconds(4), trimmed.getEndTime());

    double[] expectedValues = Arrays.copyOfRange(waveform.getSamples(), 4, 9);
    assertEquals(expectedValues.length, trimmed.getSamples().length);
    assertArrayEquals(expectedValues, trimmed.getSamples());
  }

  @Test
  void testGetSampleCount() {
    assertEquals(5, WAVEFORM_1.getSampleCount());
  }

  @Test
  void testGetFirstSample() {
    assertEquals(1.1, WAVEFORM_1.getFirstSample(), 0.0000001);
  }

  @Test
  void testGetLastSample() {
    assertEquals(5.5, WAVEFORM_1.getLastSample(), 0.0000001);
  }

  @Test
  void testGetSamplePeriod() {
    assertEquals(Duration.ofMillis(500), WAVEFORM_1.getSamplePeriod());
  }

  @ParameterizedTest
  @MethodSource("getCompareToArguments")
  void testCompareTo(int expected, Waveform base, Waveform compare) {
    assertEquals(expected, base.compareTo(compare));
  }

  static Stream<Arguments> getCompareToArguments() {
    Instant start1 = Instant.EPOCH;
    Instant start2 = Instant.EPOCH.plusSeconds(2);
    Instant end1 = start1.plusSeconds(10);
    Instant end2 = start2.plusSeconds(10);

    Waveform base = randomSamples0To1(start1, end1, 2);
    Waveform second = randomSamples0To1(start1, end1, 2);
    return Stream.of(
      arguments(start1.compareTo(start2), base, randomSamples0To1(start2, end1, 2)),
      arguments(start2.compareTo(start1), randomSamples0To1(start2, end1, 2), base),
      arguments(end1.compareTo(end2), base, randomSamples0To1(start1, end2, 2)),
      arguments(end2.compareTo(end1), randomSamples0To1(start1, end2, 2), base),
      arguments(1, base, second),
      arguments(0, base, base));
  }

}
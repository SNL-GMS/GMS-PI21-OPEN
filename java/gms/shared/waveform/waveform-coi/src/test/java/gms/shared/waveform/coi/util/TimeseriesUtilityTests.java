package gms.shared.waveform.coi.util;

import com.google.common.collect.Range;
import gms.shared.waveform.coi.Timeseries;
import gms.shared.waveform.coi.Waveform;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static gms.shared.waveform.testfixture.WaveformTestFixtures.randomSamples0To1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TimeseriesUtilityTests {

  private static final Instant END = Instant.EPOCH.plusSeconds(10);
  private static final Waveform SINGLE = randomSamples0To1(Instant.EPOCH, END, 2);

  private static final Waveform DISJOINT_1 = randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(2), 2);
  private static final Waveform DISJOINT_2 = randomSamples0To1(Instant.EPOCH.plusSeconds(3), Instant.EPOCH.plusSeconds(6), 2);
  private static final Waveform DISJOINT_3 = randomSamples0To1(Instant.EPOCH.plusSeconds(8), END, 2);

  private final Instant startTime = Instant.EPOCH;
  private final double sampleRate = 5.0;

  @ParameterizedTest
  @MethodSource("getComputeSpanValidationArguments")
  void testComputeSpanValidation(Class<? extends Exception> expectedException,
    Collection<? extends Timeseries> timeseries) {
    assertThrows(expectedException, () -> TimeseriesUtility.computeSpan(timeseries));
  }

  static Stream<Arguments> getComputeSpanValidationArguments() {
    return Stream.of(arguments(NullPointerException.class, null),
      arguments(IllegalArgumentException.class, List.of()));
  }

  @ParameterizedTest
  @MethodSource("getComputeSpanArguments")
  void testComputeSpan(Range<Instant> expected, Collection<? extends Timeseries> timeseries) {
    assertEquals(expected, TimeseriesUtility.computeSpan(timeseries));
  }

  static Stream<Arguments> getComputeSpanArguments() {
    Instant end = Instant.EPOCH.plusSeconds(5);
    Range<Instant> range = Range.closed(Instant.EPOCH, end);
    return Stream.of(arguments(range, List.of(randomSamples0To1(Instant.EPOCH, end, 2))),
      arguments(range,
        List.of(randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(2), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(5), 2))),
      arguments(range,
        List.of(randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(2), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(3), end, 2))),
      arguments(range,
        List.of(randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(3), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(2), end, 2))));
  }

  static Stream<Arguments> getAllConnectedArguments() {
    return Stream.of(arguments(true, List.of()),
      arguments(false,
        List.of(randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(2), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(3), Instant.EPOCH.plusSeconds(5), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(6), Instant.EPOCH.plusSeconds(9), 2))),
      arguments(false,
        List.of(randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(3), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(4), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(5), 2, 8))),
      arguments(true,
        List.of(randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(2), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(5), 2),
          randomSamples0To1(Instant.EPOCH.plusSeconds(4), 2, 10))));
  }

  @Test
  void testNoneOverlapped() {
    // test: none of the three overlap
    Timeseries t1 = Waveform.create(startTime, 1.0, new double[5]);
    assertEquals(startTime.plusSeconds(4), t1.getEndTime());  // just checking my math
    Timeseries t2 = Waveform.create(t1.getEndTime().plusMillis(1), 1.0, new double[5]);
    Timeseries t3 = Waveform.create(t2.getEndTime().plusMillis(1), 1.0, new double[5]);

    assertTrue(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // test: t1 and t2 overlap on end.  Note the change to 'minusMillis' instead of 'plusMillis'.
    t2 = Waveform.create(t1.getEndTime().minusMillis(1), 1.0, new double[5]);
    // passing unordered collection (set) to ensure it's not depending on order of a list or something
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // same thing, but now t1.end = t2.start
    t2 = Waveform.create(t1.getEndTime(), 1.0, new double[5]);
    assertEquals(t1.getEndTime(), t2.getStartTime());  // just to verify
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // test: t2 and 53 have overlapping start times
    // below: set t2 back to what it was (doesn't overlap with t1)
    t2 = Waveform.create(t1.getEndTime().plusMillis(1), 1.0, new double[5]);
    t3 = Waveform.create(t2.getStartTime().minusMillis(1), 1.0, new double[5]);
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // same test but now t2 and t3 have equal start times.
    // altering sample count too, otherwise waveforms are identical
    t3 = Waveform.create(t2.getStartTime(), 1.0, new double[10]);
    assertEquals(t2.getStartTime(), t3.getStartTime()); // just to verify
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // try single timeseries, should always return false.
    assertTrue(TimeseriesUtility.noneOverlapped(Set.of(t1)));
  }

  @Test
  void testClusterByConnectedValidation() {
    assertThrows(NullPointerException.class, () -> TimeseriesUtility.clusterByConnected(null));
  }

  @ParameterizedTest
  @MethodSource("getClusterByConnectedArguments")
  void testClusterByConnected(Map<Range<Instant>, List<Waveform>> expected,
    List<Waveform> timeseries) {
    Map<Range<Instant>, List<Waveform>> actual = TimeseriesUtility.clusterByConnected(timeseries);
    assertEquals(expected.size(), actual.size());
    assertTrue(actual.keySet().containsAll(expected.keySet()));
    expected.entrySet().stream()
      .forEach(expectedEntry -> assertEquals(expectedEntry.getValue(), actual.get(expectedEntry.getKey())));
  }

  static Stream<Arguments> getClusterByConnectedArguments() {
    Instant end = Instant.EPOCH.plusSeconds(10);

    Waveform mixed1 = randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(4), 2);
    Waveform mixed2 = randomSamples0To1(Instant.EPOCH.plusSeconds(2), Instant.EPOCH.plusSeconds(6), 2);
    Waveform mixed3 = randomSamples0To1(Instant.EPOCH.plusSeconds(8), end, 2);

    Waveform connected1 = randomSamples0To1(Instant.EPOCH, Instant.EPOCH.plusSeconds(3), 2);
    Waveform connected2 = randomSamples0To1(Instant.EPOCH.plusSeconds(3), Instant.EPOCH.plusSeconds(8), 2);
    Waveform connected3 = randomSamples0To1(Instant.EPOCH.plusSeconds(5), end, 2);

    return Stream.of(arguments(Map.of(), List.of()),
      arguments(Map.of(Range.closed(Instant.EPOCH, end), List.of(SINGLE)), List.of(SINGLE)),
      arguments(Map.of(Range.closed(Instant.EPOCH, Instant.EPOCH.plusSeconds(2)), List.of(DISJOINT_1),
          Range.closed(Instant.EPOCH.plusSeconds(3), Instant.EPOCH.plusSeconds(6)), List.of(DISJOINT_2),
          Range.closed(Instant.EPOCH.plusSeconds(8), end), List.of(DISJOINT_3)),
        List.of(DISJOINT_1, DISJOINT_2, DISJOINT_3)),
      arguments(Map.of(Range.closed(Instant.EPOCH, Instant.EPOCH.plusSeconds(6)), List.of(mixed1, mixed2),
          Range.closed(Instant.EPOCH.plusSeconds(8), end), List.of(mixed3)),
        List.of(mixed1, mixed2, mixed3)),
      arguments(Map.of(Range.closed(Instant.EPOCH, end), List.of(connected1, connected2, connected3)),
        List.of(connected2, connected1, connected3)));
  }

  @Test
  void testFilterEnclosedValidation() {
    assertThrows(NullPointerException.class, () -> TimeseriesUtility.filterEnclosed(null));
  }

  @ParameterizedTest
  @MethodSource("getFilterEnclosedArguments")
  void testFilterEnclosed(List<Timeseries> expected, List<Timeseries> timeseries) {
    assertEquals(expected, TimeseriesUtility.filterEnclosed(timeseries));
  }

  static Stream<Arguments> getFilterEnclosedArguments() {
    return Stream.of(arguments(List.of(SINGLE), List.of(SINGLE)),
      arguments(List.of(DISJOINT_1, DISJOINT_2, DISJOINT_3), List.of(DISJOINT_3, DISJOINT_2, DISJOINT_1)),
      arguments(List.of(SINGLE), List.of(DISJOINT_3, SINGLE, DISJOINT_1, DISJOINT_2)));
  }
}

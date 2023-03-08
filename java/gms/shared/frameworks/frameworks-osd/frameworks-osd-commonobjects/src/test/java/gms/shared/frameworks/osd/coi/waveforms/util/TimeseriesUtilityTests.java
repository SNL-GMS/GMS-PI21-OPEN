package gms.shared.frameworks.osd.coi.waveforms.util;

import com.google.common.collect.Range;
import gms.shared.frameworks.osd.coi.waveforms.Timeseries;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeseriesUtilityTests {

  private final Instant startTime = Instant.EPOCH;
  private final double sampleRate = 5.0;

  @Test
  void createValidTest() {
    final int sampleCount = 5;
    final Timeseries t = Waveform.from(startTime, sampleRate, new double[sampleCount]);
    assertEquals(sampleCount, t.getSampleCount());
    // 5 samples, one sample every 200ms,
    // end time should be 800ms later (not 1000ms because first sample is at 0)
    assertEquals(Instant.EPOCH.plusMillis(800), t.getEndTime());
  }

  @Test
  void createNegativeSampleRateTest() {
    assertThrows(IllegalArgumentException.class,
      () -> Waveform.from(startTime, -0.005, new double[1]));
  }

  @Test
  void createZeroSampleRateTest() {
    assertThrows(IllegalArgumentException.class, () -> Waveform.from(startTime, 0, new double[1]));
  }

  @Test
  void testTimeForSample() {
    Instant start = Instant.EPOCH;
    Timeseries t = Waveform.from(start, sampleRate, new double[2000]);
    assertEquals(Instant.EPOCH, t.computeSampleTime(0));
    assertEquals(Instant.EPOCH.plusSeconds(1), t.computeSampleTime(5));
    assertEquals(Instant.EPOCH.plusSeconds(20), t.computeSampleTime(100));
  }

  @Test
  void testTimeForSampleBeyondRangeExpect() {
    Timeseries t = Waveform.from(Instant.EPOCH, sampleRate, new double[1]);
    int sampleCount = t.getSampleCount();
    assertThrows(IllegalArgumentException.class, () ->
      t.computeSampleTime(sampleCount));
  }

  @Test
  void testTimeForSampleBeforeRange() {
    Timeseries t = Waveform.from(Instant.EPOCH, sampleRate, new double[1]);
    assertThrows(IllegalArgumentException.class,
      () -> t.computeSampleTime(-1));
  }

  @Test
  void testTimeRange() {
    Timeseries t = Waveform.from(startTime, sampleRate, new double[5]);
    Range<Instant> range = t.computeTimeRange();

    Timeseries t2 = Waveform.from(startTime.plusSeconds(30), sampleRate, new double[5]);
    Range<Instant> range2 = t2.computeTimeRange();

    assertEquals(t.getStartTime(), range.lowerEndpoint());
    assertEquals(t.getEndTime(), range.upperEndpoint());

    assertEquals(t2.getStartTime(), range2.lowerEndpoint());
    assertEquals(t2.getEndTime(), range2.upperEndpoint());
  }

  @Test
  void testNoneOverlapped() {
    // test: none of the three overlap
    Timeseries t1 = Waveform.from(startTime, 1.0, new double[5]);
    assertEquals(startTime.plusSeconds(4), t1.getEndTime());  // just checking my math
    Timeseries t2 = Waveform.from(t1.getEndTime().plusMillis(1), 1.0, new double[5]);
    Timeseries t3 = Waveform.from(t2.getEndTime().plusMillis(1), 1.0, new double[5]);

    assertTrue(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // test: t1 and t2 overlap on end.  Note the change to 'minusMillis' instead of 'plusMillis'.
    t2 = Waveform.from(t1.getEndTime().minusMillis(1), 1.0, new double[5]);
    // passing unordered collection (set) to ensure it's not depending on order of a list or something
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // same thing, but now t1.end = t2.start
    t2 = Waveform.from(t1.getEndTime(), 1.0, new double[5]);
    assertEquals(t1.getEndTime(), t2.getStartTime());  // just to verify
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // test: t2 and 53 have overlapping start times
    // below: set t2 back to what it was (doesn't overlap with t1)
    t2 = Waveform.from(t1.getEndTime().plusMillis(1), 1.0, new double[5]);
    t3 = Waveform.from(t2.getStartTime().minusMillis(1), 1.0, new double[5]);
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // same test but now t2 and t3 have equal start times.
    // altering sample count too, otherwise waveforms are identical
    t3 = Waveform.from(t2.getStartTime(), 1.0, new double[10]);
    assertEquals(t2.getStartTime(), t3.getStartTime()); // just to verify
    assertFalse(TimeseriesUtility.noneOverlapped(Set.of(t1, t2, t3)));

    // try single timeseries, should always return false.
    assertTrue(TimeseriesUtility.noneOverlapped(Set.of(t1)));
  }
}

package gms.shared.frameworks.utilities;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeMarkerTest {

  @Test
  void markTimeAndTimeSinceMark() {
    TimeMarker<Integer> timeMarker = new TimeMarker<>();
    timeMarker.markTime(0);
    double ns = timeMarker.timeSinceMark(0, TimeUnit.NANOSECONDS, true);
    assertFalse(Double.isNaN(ns));
    assertTrue(ns >= 0.0);
    ns = timeMarker.timeSinceMark(0, TimeUnit.NANOSECONDS, false);
    assertTrue(Double.isNaN(ns));
  }

  @Test
  void testVariousTimeUnits() throws Exception {
    TimeMarker<Integer> timeMarker = new TimeMarker<>();
    Integer key = 0;

    timeMarker.markTime(key);

    Object waitMonitor = new Object();
    synchronized (waitMonitor) {
      waitMonitor.wait(1000L);
    }

    double nanos = timeMarker.timeSinceMark(key, TimeUnit.NANOSECONDS, false);
    double micros = timeMarker.timeSinceMark(key, TimeUnit.MICROSECONDS, false);
    double millis = timeMarker.timeSinceMark(key, TimeUnit.MILLISECONDS, false);
    double seconds = timeMarker.timeSinceMark(key, TimeUnit.SECONDS, false);
    double minutes = timeMarker.timeSinceMark(key, TimeUnit.MINUTES, false);
    double hours = timeMarker.timeSinceMark(key, TimeUnit.HOURS, false);
    double days = timeMarker.timeSinceMark(key, TimeUnit.DAYS, false);

    // Use very permissive deviations (10%) -- just a rough test.
    assertEquals(24.0, hours / days, 2.4);
    assertEquals(60.0, minutes / hours, 6.0);
    assertEquals(60.0, seconds / minutes, 6.0);
    assertEquals(1000.0, millis / seconds, 100.0);
    assertEquals(1000.0, micros / millis, 100.0);
    assertEquals(1000.0, nanos / micros, 100.0);
  }

}
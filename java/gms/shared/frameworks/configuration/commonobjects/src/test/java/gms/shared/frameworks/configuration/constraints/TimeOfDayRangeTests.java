package gms.shared.frameworks.configuration.constraints;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeOfDayRangeTests {

  @Test
  void testContains() throws Exception {
    TimeOfDayRange tr = TimeOfDayRange.from(LocalTime.parse("23:00:00"), LocalTime.MAX);
    assertTrue(tr.contains(LocalTime.parse("23:30:00")));
  }

  @Test
  void testTimeRangeSpansMidnight() throws Exception {
    TimeOfDayRange tr = TimeOfDayRange.from(LocalTime.parse("23:00:00"), LocalTime.parse("01:00:00"));
    assertTrue(tr.contains(LocalTime.MAX));
    assertTrue(tr.contains(LocalTime.MIN));
    assertTrue(tr.contains(LocalTime.MIDNIGHT));

    assertTrue(tr.contains(LocalTime.MAX, false, false));
    assertTrue(tr.contains(LocalTime.MIN, false, false));
    assertTrue(tr.contains(LocalTime.MIDNIGHT, false, false));
  }

  @Test
  void testBoundsExclusive() throws Exception {
    TimeOfDayRange tr = TimeOfDayRange.from(LocalTime.parse("23:00:00"), LocalTime.MAX);
    assertFalse(tr.contains(LocalTime.parse("23:00:00"), false, true));
    assertFalse(tr.contains(LocalTime.MAX, true, false));
  }

  @Test
  void testBoundsInclusive() throws Exception {
    TimeOfDayRange tr = TimeOfDayRange.from(LocalTime.parse("23:00:00"), LocalTime.MAX);
    assertTrue(tr.contains(LocalTime.parse("23:00:00"), true, false));
    assertTrue(tr.contains(LocalTime.MAX, false, true));
  }

  @Test
  void testDefaultBounds() throws Exception {
    TimeOfDayRange tr = TimeOfDayRange.from(LocalTime.parse("23:00:00"), LocalTime.MAX);

    // defaults to lower bound inclusive
    assertTrue(tr.contains(LocalTime.parse("23:00:00")));

    // defaults to upper bound exclusive
    assertFalse(tr.contains(LocalTime.MAX));
  }
}

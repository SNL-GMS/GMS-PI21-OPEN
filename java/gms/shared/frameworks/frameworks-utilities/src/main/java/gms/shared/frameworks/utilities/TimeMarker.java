package gms.shared.frameworks.utilities;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple utility class for keeping track of the instants that events start and
 * the time since they started. This class is thread-safe.
 *
 * @param <K> The type of event being tracked
 */
public class TimeMarker<K> {

  // Associates keys with the instant that markTime() is called for the keys.
  private final Map<K, Instant> timingMap = new ConcurrentHashMap<>();

  /**
   * Clears all information.
   */
  public void clear() {
    timingMap.clear();
  }

  /**
   * Associate the current instant with a key.
   *
   * @param key
   * @return
   */
  public Instant markTime(final K key) {
    return timingMap.compute(key, (k, tinfo) -> {
      return Instant.now();
    });
  }

  /**
   * Get the amount of time since {@code markTime(key)} was called.
   *
   * @param key the key
   * @param timeUnit the time unit for the result.
   * @param clear whether or not to remove the time mark associated with the key.
   * @return the amount of time or {@code Double.NaN} if no time mark has been
   * associated with the key.
   */
  public double timeSinceMark(final K key, final TimeUnit timeUnit, final boolean clear) {
    AtomicReference<Instant> startRef = new AtomicReference<>();
    timingMap.computeIfPresent(key, (k, ins) -> {
      startRef.set(ins);
      return clear ? null : ins;
    });
    Instant start = startRef.get();
    if (start != null) {
      var now = Instant.now();
      switch (timeUnit) {
        case DAYS:
          return getDaysBetween(start, now);
        case HOURS:
          return getHoursBetween(start, now);
        case MINUTES:
          return getMinutesBetween(start, now);
        case SECONDS:
          return getSecondsBetween(start, now);
        case MILLISECONDS:
          return getMillisecondsBetween(start, now);
        case MICROSECONDS:
          return getMicrosecondsBetween(start, now);
        case NANOSECONDS:
          return getNanosecondsBetween(start, now);
      }
    }
    return Double.NaN;
  }

  private static final double SECONDS_PER_DAY = 86_400.0;
  private static final double MILLISECONDS_PER_HOUR = 3_600_000.0;
  private static final double MILLISECONDS_PER_MINUTE = 60_000.0;
  private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;
  private static final double NANOSECONDS_PER_MILLISECOND = 1_000_000.0;
  private static final double NANOSECONDS_PER_MICROSECONDS = 1_000.0;

  public static double getDaysBetween(Instant start, Instant end) {
    double seconds = Duration.between(start, end).toSeconds();
    return seconds / SECONDS_PER_DAY;
  }

  public static double getHoursBetween(Instant start, Instant end) {
    double milliseconds = Duration.between(start, end).toMillis();
    return milliseconds / MILLISECONDS_PER_HOUR;
  }

  public static double getMinutesBetween(Instant start, Instant end) {
    double milliseconds = Duration.between(start, end).toMillis();
    return milliseconds / MILLISECONDS_PER_MINUTE;
  }

  public static double getSecondsBetween(Instant start, Instant end) {
    double nanos = Duration.between(start, end).toNanos();
    return nanos / NANOSECONDS_PER_SECOND;
  }

  public static double getMillisecondsBetween(Instant start, Instant end) {
    double nanos = Duration.between(start, end).toNanos();
    return nanos / NANOSECONDS_PER_MILLISECOND;
  }

  public static double getMicrosecondsBetween(Instant start, Instant end) {
    double nanos = Duration.between(start, end).toNanos();
    return nanos / NANOSECONDS_PER_MICROSECONDS;
  }

  public static double getNanosecondsBetween(Instant start, Instant end) {
    return Duration.between(start, end).toNanos();
  }
}

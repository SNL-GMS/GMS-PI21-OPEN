package gms.dataacquisition.data.preloader.generator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class AceiBooleanStatusGeneratorTest {

  private static final List<Duration> durations = new ArrayList<>();
  private static final double MEAN_OCCURRENCES_PER_YEAR = 1.5e5; // roughly 25% of occurrences
  private static final double MEAN_HOURS_OF_PERSISTENCE = 5.0 / 60.0;  // 5 minutes

  @BeforeAll
  static void init() {
    IntStream.range(0, 50)
      .forEach(i -> durations.add(Duration.ofMinutes(1)));
  }

  @Test
  void testGenerateBooleanStatuses() {
    double meanOccurrencesPerYear;

    // zero occurrences
    meanOccurrencesPerYear = 0.0;
    AceiBooleanStatusGenerator
      .generateBooleanStatuses(durations, meanOccurrencesPerYear, MEAN_HOURS_OF_PERSISTENCE)
      .forEach(b -> assertFalse(b));

    // constant occurrences
    meanOccurrencesPerYear = 1.0e100;
    AceiBooleanStatusGenerator
      .generateBooleanStatuses(durations, meanOccurrencesPerYear, MEAN_HOURS_OF_PERSISTENCE)
      .forEach(b -> assertTrue(b));

    // infinite persistence
    final double meanHoursOfPersistence = 1.0;
    AtomicBoolean expected = new AtomicBoolean(false);
    AceiBooleanStatusGenerator
      .generateBooleanStatuses(durations, MEAN_OCCURRENCES_PER_YEAR, meanHoursOfPersistence)
      .forEach(b -> {
        if (b && !expected.get()) {
          expected.set(true);
        }
        assertEquals(expected.get(), b);
      });

    // zero persistence
    AtomicBoolean previous = new AtomicBoolean(false);
    AceiBooleanStatusGenerator
      .generateBooleanStatuses(durations, MEAN_OCCURRENCES_PER_YEAR, 0.0)
      .forEach(b -> {
        if (previous.get()) {
          assertFalse(b);
        }
      });

    // empty duration list
    assertTrue(AceiBooleanStatusGenerator
      .generateBooleanStatuses(
        new ArrayList<>(), MEAN_OCCURRENCES_PER_YEAR, MEAN_HOURS_OF_PERSISTENCE)
      .isEmpty()
    );
  }

  @Test
  void testGenerateBooleanStatuses_badArgs() {
    assertThrows(NullPointerException.class,
      () -> AceiBooleanStatusGenerator
        .generateBooleanStatuses(null, MEAN_OCCURRENCES_PER_YEAR, MEAN_HOURS_OF_PERSISTENCE)
    );

    final double meanOccurrencesPerYear = -1.0;
    assertThrows(IllegalArgumentException.class,
      () -> AceiBooleanStatusGenerator
        .generateBooleanStatuses(durations, meanOccurrencesPerYear, MEAN_HOURS_OF_PERSISTENCE)
    );

    final double meanHoursOfPersistence = -1.0;
    assertThrows(IllegalArgumentException.class,
      () -> AceiBooleanStatusGenerator
        .generateBooleanStatuses(durations, MEAN_OCCURRENCES_PER_YEAR, meanHoursOfPersistence)
    );
  }

  @ParameterizedTest
  @MethodSource("roundUpParametersSource")
  void testRoundUp(int value, long multiple, long expected) {
    Duration duration = AceiBooleanStatusGenerator.roundUp(value, multiple);
    assertEquals(expected, duration.get(ChronoUnit.SECONDS));
  }

  static Stream<Arguments> roundUpParametersSource() {
    return Stream.of(
      arguments(1, 2L, 2L),
      arguments(10, 3L, 12L),
      arguments(1, 1L, 1L),
      arguments(10, 5L, 10L),
      arguments(0, 5L, 0L)
    );
  }

  @Test
  void testGenerateNextDuration() {
    Duration duration;

    duration = AceiBooleanStatusGenerator.generateNextDuration(
      Duration.ofSeconds(60L),
      false,
      MEAN_OCCURRENCES_PER_YEAR,
      0.0);
    assertEquals(0L, duration.get(ChronoUnit.SECONDS));

    duration = AceiBooleanStatusGenerator.generateNextDuration(
      Duration.ofSeconds(60L),
      true,
      0.0,
      MEAN_HOURS_OF_PERSISTENCE);
    assertEquals(0L, duration.get(ChronoUnit.SECONDS));

    duration = AceiBooleanStatusGenerator.generateNextDuration(
      Duration.ofSeconds(60L),
      false,
      MEAN_OCCURRENCES_PER_YEAR,
      MEAN_HOURS_OF_PERSISTENCE
    );
    assertTrue(0L < duration.get(ChronoUnit.SECONDS));
  }

  @Test
  void testGenerateDurations() {

    final long SECONDS_PER_DAY = 86_400L;
    final long DAYS = 30L;
    List<Duration> durations;
    long sumSeconds;

    durations = AceiBooleanStatusGenerator.generateDurations(
      Duration.ofDays(DAYS), Duration.ofHours(24L), false,
      1.0, 24. * 5.);
    sumSeconds = durations.stream().reduce(
      0L, (sum, d) -> sum += d.get(ChronoUnit.SECONDS), Long::sum);
    assertEquals(DAYS * SECONDS_PER_DAY, sumSeconds);
    testGenerateNextDuration();

    durations = AceiBooleanStatusGenerator.generateDurations(
      Duration.ofDays(DAYS), Duration.ofHours(24L), true,
      1.0, 24. * 5.);
    sumSeconds = durations.stream().reduce(
      0L, (sum, d) -> sum += d.get(ChronoUnit.SECONDS), Long::sum);
    assertEquals(DAYS * SECONDS_PER_DAY, sumSeconds);

    final Duration ONE_HOUR = Duration.ofHours(1L);
    double manyOccurrencesPerSecond = 1000. * 60. * 60. * 24. * 365.25;
    double longHoursOfPersistence = 1L;
    durations = AceiBooleanStatusGenerator.generateDurations(
      ONE_HOUR, Duration.ofMinutes(5L), true,
      manyOccurrencesPerSecond, longHoursOfPersistence);
    assertEquals(1, durations.size());
    assertEquals(ONE_HOUR.getSeconds(), durations.get(0).getSeconds());
  }

  @Test
  void testGenerateDurations_badArgs() {
    Duration minusOneDay = Duration.ofDays(-1L);
    Duration thirtyDays = Duration.ofDays(30L);
    Duration minusOneHour = Duration.ofHours(-1L);
    Duration oneHour = Duration.ofHours(1L);

    assertThrows(IllegalArgumentException.class,
      () -> AceiBooleanStatusGenerator.generateDurations(minusOneDay, oneHour, false,
        MEAN_OCCURRENCES_PER_YEAR, MEAN_HOURS_OF_PERSISTENCE));

    assertThrows(IllegalArgumentException.class,
      () -> AceiBooleanStatusGenerator.generateDurations(thirtyDays, minusOneHour, false,
        MEAN_OCCURRENCES_PER_YEAR, MEAN_HOURS_OF_PERSISTENCE));

    assertThrows(IllegalArgumentException.class,
      () -> AceiBooleanStatusGenerator.generateDurations(thirtyDays, oneHour, false,
        -1.0, MEAN_HOURS_OF_PERSISTENCE));

    assertThrows(IllegalArgumentException.class,
      () -> AceiBooleanStatusGenerator.generateDurations(thirtyDays, oneHour, false,
        MEAN_OCCURRENCES_PER_YEAR, -1.0));
  }

}

package gms.dataacquisition.data.preloader.generator;

import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;

import static java.lang.Math.exp;


/**
 * This class contains static methods for generating random statuses and durations for boolean ACEI
 * ({@link gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean})
 * objects.
 * <p>
 * An "occurrence" is the random event of an ACEI status flipping from false to true. When an
 * "occurrence" occurs, that occurrence persists (i.e., remains true) for some random period of
 * time.  That amount of time is referred to as the "persistence." The rate at which "occurrences"
 * occur is specified by the mean number of occurrences per year.  Persistence is governed by the
 * mean hours of persistence (i.e., the average number of hours that an occurrence remains {@code
 * true}.
 */
public class AceiBooleanStatusGenerator {

  private static final double SECONDS_PER_HOUR = 3600.0;
  private static final double SECONDS_PER_YEAR = SECONDS_PER_HOUR * 24 * 365.25;
  private static final SecureRandom random = new SecureRandom();

  private AceiBooleanStatusGenerator() {
  }

  /**
   * Generates a random list of temporally contiguous {@code Duration}s.  Each duration represents a
   * {@link gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean}. The
   * status of the first ACEI in the list of durations is {@code initialStatus}.  The status of the
   * remaining ACEIs alternate: !initialStatus, initialStatus, !initialStatus, initialStatus, etc.
   * The sum of the durations returned will equal {@code totalDuration}.
   *
   * @param totalDuration the total amount of time for which durations are generated
   * @param increment the smallest increment of time by which to grow durations
   * @param initialStatus status of first returned duration (subsequent durations alternate status)
   * @param meanOccurrencesPerYear parameterizes the distribution of flipping from {@code false} to
   * {@code true}
   * @param meanHoursOfPersistence parameterizes the distribution of the amount of time status
   * remains {@code true}
   * @return list of temporally contiguous durations
   */
  public static List<Duration> generateDurations(Duration totalDuration, Duration increment,
    boolean initialStatus, double meanOccurrencesPerYear, double meanHoursOfPersistence) {

    Validate.isTrue(!totalDuration.isNegative());
    Validate.isTrue(!increment.isNegative());
    Validate.isTrue(0.0 <= meanOccurrencesPerYear);
    Validate.isTrue(0.0 <= meanHoursOfPersistence);

    List<Duration> durations = new ArrayList<>();
    var firstDuration = generateNextDuration(
      increment, !initialStatus, meanOccurrencesPerYear, meanHoursOfPersistence
    );

    if (totalDuration.compareTo(firstDuration) > 0) {
      var timeFilledDuration = Duration.ZERO;
      var currentDuration = firstDuration;
      var currentStatus = initialStatus;

      while (timeFilledDuration.plus(currentDuration).compareTo(totalDuration) <= 0) {
        durations.add(currentDuration);
        timeFilledDuration = timeFilledDuration.plus(currentDuration);
        currentDuration = generateNextDuration(
          increment, currentStatus, meanOccurrencesPerYear, meanHoursOfPersistence
        );
        currentStatus = !currentStatus;
      }
      durations.add(totalDuration.minus(timeFilledDuration));
    } else {
      durations.add(totalDuration);
    }

    return durations;
  }


  static Duration generateNextDuration(Duration increment, boolean currentStatus,
    double meanOccurrencesPerYear, double meanHoursOfPersistence) {

    var duration = Duration.ZERO;

    if (currentStatus) {
      // Current status is true, so the status of the returned duration will be false.
      // The length of the duration will be the time until the next "occurrence".

      if (0.0 < meanOccurrencesPerYear) {
        double probabilityOfOccurrence;
        do {
          // Each time through this loop, the duration is lengthened by increment until
          // an "occurrence" occurs.
          duration = duration.plus(increment);
          // This is the probability of one or more occurrences over the duration.
          // Drawn from a Poisson distribution with meanOccurrencesPerYear:  1.0 - Prob(X=0).
          probabilityOfOccurrence = 1.0 - exp(
            -meanOccurrencesPerYear * duration.getSeconds() / SECONDS_PER_YEAR);
        } while (probabilityOfOccurrence < random.nextDouble());
      } else {
        duration = Duration.ZERO;
      }

    } else {
      // Current status is false, so the status of the returned duration will be true.
      // The length of the duration will be the time that the occurrence persists.

      PoissonDistribution persistenceDistribution;
      if (0.0 < meanHoursOfPersistence) {
        persistenceDistribution =
          new PoissonDistribution(meanHoursOfPersistence * SECONDS_PER_HOUR);
        duration = roundUp(persistenceDistribution.sample(), increment.getSeconds());
      } else {
        duration = Duration.ZERO;
      }

    }

    return duration;
  }


  /*
   * Round up the given value so that the end result is a multiple of {@code multiple}.
   * value:  the value to round up.  >= 0
   * multiple:  the multiple by which to round up.  > 0
   * return:  the next greater value of "value" that is a multiple of "multiple"
   */
  static Duration roundUp(int value, long multiple) {
    Duration duration;
    long remainder = value % multiple;
    if (remainder == 0) {
      duration = Duration.ofSeconds(value);
    } else {
      duration = Duration.ofSeconds(value + multiple - remainder);
    }

    return duration;
  }


  /**
   * Randomly generate a boolean status for each of the given temporal intervals.  The intervals
   * represent temporally contiguous boolean ACEI ({@link gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean})
   * objects. The status is initialized to {@code false}, randomly flips to {@code true}, and
   * remains {@code true} for a random amount of time.
   *
   * @param durations list of durations of records
   * @param meanOccurrencesPerYear parameterizes the distribution of flipping from {@code false} to
   * {@code true}
   * @param meanHoursOfPersistence parameterizes the distribution of the amount of time status
   * remains {@code true}
   * @return List of Boolean statuses
   */
  public static List<Boolean> generateBooleanStatuses(List<Duration> durations,
    double meanOccurrencesPerYear, double meanHoursOfPersistence) {

    Validate.notNull(durations);
    if (durations.isEmpty()) {
      return new ArrayList<>();
    }
    Validate.isTrue(0.0 <= meanOccurrencesPerYear);
    Validate.isTrue(0.0 <= meanHoursOfPersistence);

    /*
     * An "occurrence" is the random event of an ACEI boolean flipping from false to true.
     * When we have an occurrence, that occurrence persists (i.e., remains true) for some
     * random period of time.  That amount of time is referred to as the "persistence."
     */

    // Given the mean hours of persistence, the number of seconds that an occurrence
    // will persist follows a Poisson distribution.  This distribution is scaled to seconds.
    final IntSupplier sampleSupplier;
    if (0.0 < meanHoursOfPersistence) {
      var persistenceDistribution = new PoissonDistribution(
        meanHoursOfPersistence * SECONDS_PER_HOUR);
      sampleSupplier = persistenceDistribution::sample;
    } else {
      sampleSupplier = () -> 0;
    }

    List<Boolean> booleanList = new ArrayList<>();
    var remainingPersistenceSeconds = 0; // assume initial ACEI status is false

    for (Duration duration : durations) {

      // This is the probability of one or more occurrences from a Poisson distribution
      // with meanOccurrencesPerYear:  1.0 - Prob(X=0).
      double probabilityOfOccurrence =
        1.0 - exp(-meanOccurrencesPerYear * duration.getSeconds() / SECONDS_PER_YEAR);

      if (0 < remainingPersistenceSeconds) {
        booleanList.add(true);
        remainingPersistenceSeconds -= duration.getSeconds();
      } else if (random.nextDouble() <= probabilityOfOccurrence) {
        booleanList.add(true);

        remainingPersistenceSeconds = sampleSupplier.getAsInt();
      } else {
        booleanList.add(false);
      }

    }

    return booleanList;
  }

}

package gms.core.performancemonitoring.soh.control;

import java.time.Duration;
import java.util.Optional;

/**
 * Represents an Aggregator, which can be used in Stream.reduce (for example)
 *
 * @param <T> Type of thing that is being operated on
 */
public interface Aggregator<T> {

  /**
   * "accept" a single value, in other words, do whatever needs to be done with the value
   * to compute the overall aggregation.
   *
   * @param value value to accept
   */
  void accept(T value);

  /**
   * Perform the aggregation.
   *
   * @return An Optional of T. Optional incase an aggregation cant be performed (for example, an
   * average that divides by the number of items, when that number is zero)
   */
  Optional<T> aggregate();

  /**
   * Combine this aggregator in an associative way with another aggregator. This can be used as
   * the combiner for Stream.reduce (for example).
   *
   * @param other the other aggregator
   * @return a "combined" aggregator.
   */
  Aggregator<T> combine(Aggregator<T> other);

  //
  // Some useful canned aggregators
  //

  /**
   * Get an aggregator that performs averaging of Duration objects.
   *
   * @return Averaging aggregator
   */
  static Aggregator<Duration> getDurationAverager() {

    return new Aggregator<>() {

      Duration runningSum = Duration.ZERO;
      int count = 0;

      @Override
      public void accept(Duration value) {

        runningSum = runningSum.plus(value);
        count++;
      }

      @Override
      public Optional<Duration> aggregate() {
        return Optional.ofNullable(
          count == 0 ? null : runningSum.dividedBy(count)
        );
      }

      @Override
      public Aggregator<Duration> combine(Aggregator<Duration> other) {
        this.runningSum = this.runningSum.plus(this.getClass().cast(other).runningSum);
        this.count += this.getClass().cast(other).count;
        return this;
      }
    };
  }

  /**
   * Get an aggregator that finds the maximum duration
   *
   * @return Maximizing aggregator
   */
  static Aggregator<Duration> getDurationMaximizer() {

    return new Aggregator<>() {
      Optional<Duration> currentMaxOpt = Optional.empty();

      @Override
      public void accept(Duration value) {
        if (currentMaxOpt.isEmpty()) {
          currentMaxOpt = Optional.ofNullable(value);
        } else {

          if (currentMaxOpt.get().compareTo(value) < 0) {
            this.currentMaxOpt = Optional.of(value);
          }
        }
      }

      @Override
      public Optional<Duration> aggregate() {
        return currentMaxOpt;
      }

      @Override
      public Aggregator<Duration> combine(Aggregator<Duration> other) {
        this.accept(this.getClass().cast(other).currentMaxOpt.orElse(null));
        return this;
      }
    };
  }
}

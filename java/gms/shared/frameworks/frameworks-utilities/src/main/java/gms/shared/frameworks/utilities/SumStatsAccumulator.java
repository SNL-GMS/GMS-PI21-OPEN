package gms.shared.frameworks.utilities;

import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maintains summary statistics associated to a variety of keys.
 *
 * @param <K> type of keys for which summary statistics are maintained.
 */
public class SumStatsAccumulator<K> {

  private final Map<K, SummaryStatistics> statisticsMap;
  private Instant startTime;

  /**
   * Default constructor
   */
  public SumStatsAccumulator() {
    this(new HashMap<>());
  }

  /**
   * Constructor which takes the map used for accumulation. This form is provided so an
   * efficient map, such as an {@code EnumMap} may be passed in.
   *
   * @param map
   */
  public SumStatsAccumulator(Map<K, SummaryStatistics> map) {
    this.statisticsMap = map;
    reset();
  }

  /**
   * Clears all accumulated values and sets the start of accumulation to the current instant.
   */
  public void reset() {
    reset(Instant.now());
  }

  /**
   * Clears all accumulated values and sets the start of accumulated to the specified instant.
   *
   * @param startTime
   */
  public void reset(Instant startTime) {
    Validate.notNull(startTime, "startTime must not be null");
    this.startTime = startTime;
    // Never remove or replace the values themselves, since one returned by
    // getSummaryStatistics() may be used elsewhere in the code.
    statisticsMap.values().forEach(SummaryStatistics::clear);
  }

  public Instant getStartTime() {
    return startTime;
  }

  /**
   * Returns the duration since accumulation of statistics began. This is the time
   * since the last call to {@code reset()}.
   *
   * @return a duration, never null.
   */
  public Duration getAccumulationDuration() {
    return Duration.between(startTime, Instant.now());
  }

  /**
   * Get the summary statistics object associated with the given key.
   *
   * @param key
   * @return the summary statistics object, never null. If nothing has been
   * associated with the key before, a new instance is created.
   */
  public SummaryStatistics getSummaryStatistics(final K key) {
    return statisticsMap.computeIfAbsent(key, lbl -> {
      return new SummaryStatistics();
    });
  }

  /**
   * Adds a value to the data associated with a key.
   *
   * @param key
   * @param value
   */
  public void addValue(final K key, final double value) {
    getSummaryStatistics(key).addValue(value);
  }

  /**
   * Returns the geometric mean of the values that have been associated with the key.
   *
   * @param key
   * @return The geometric, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getGeometricMean(final K key) {
    return getSummaryStatistics(key).getGeometricMean();
  }

  /**
   * Returns the minimum of the values that have been associated with the key.
   *
   * @param key
   * @return The minimum, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getMin(final K key) {
    return getSummaryStatistics(key).getMin();
  }

  /**
   * Returns the maximum of the values that have been associated with the key.
   *
   * @param key
   * @return The maximum, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getMax(final K key) {
    return getSummaryStatistics(key).getMax();
  }

  /**
   * Returns the mean of the values that have been associated with the key.
   *
   * @param key
   * @return The mean, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getMean(final K key) {
    return getSummaryStatistics(key).getMean();
  }

  /**
   * Returns the number of of values that have been associated with the key.
   *
   * @param key
   * @return the number of values.
   */
  public long getN(final K key) {
    return getSummaryStatistics(key).getN();
  }

  /**
   * Returns the population variance of the values that have been associated with the key.
   *
   * @param key
   * @return The population variance, or {@code Double.NaN} if no values
   * have been associated with the key.
   */
  public double getPopulationVariance(final K key) {
    return getSummaryStatistics(key).getPopulationVariance();
  }

  /**
   * Returns the quadratic mean of the values that have been associated with the key.
   * The quadratic mean is the root-mean-square of the values.
   *
   * @param key
   * @return The quadratic mean , or {@code Double.NaN} if no
   * values have been associated with the key.
   */
  public double getQuadraticMean(final K key) {
    return getSummaryStatistics(key).getQuadraticMean();
  }

  /**
   * Returns the second central moment of the values that have been associated with the key.
   * This is the sum of the squared deviations from the sample mean of the values.
   *
   * @param key
   * @return The second moment, or {@code Double.NaN} if no values have been associated
   * with the key.
   */
  public double getSecondMoment(final K key) {
    return getSummaryStatistics(key).getSecondMoment();
  }

  /**
   * Returns the standard deviation of the values that have been associated with the key.
   *
   * @param key
   * @return The standard deviation, or {@code Double.NaN} if no values have
   * been associated with the key.
   */
  public double getStandardDeviation(final K key) {
    return getSummaryStatistics(key).getStandardDeviation();
  }

  /**
   * Returns the sum of the values that have been associated with the key.
   *
   * @param key
   * @return The sum, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getSum(final K key) {
    return getSummaryStatistics(key).getSum();
  }

  public StatisticalSummary getSummary(final K key) {
    return getSummaryStatistics(key).getSummary();
  }

  /**
   * Returns the sum of the logs of the values that have been associated with the key.
   *
   * @param key
   * @return The sum of logs, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getSumOfLogs(final K key) {
    return getSummaryStatistics(key).getSumOfLogs();
  }

  /**
   * Returns the sum of the squares of the values that have been associated with the key.
   *
   * @param key
   * @return The sum of squares, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getSumsq(final K key) {
    return getSummaryStatistics(key).getSumsq();
  }

  /**
   * Returns the variance of the values that have been associated with the key.
   *
   * @param key
   * @return The variance, or {@code Double.NaN} if no values have been associated with the key.
   */
  public double getVariance(final K key) {
    return getSummaryStatistics(key).getVariance();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.statisticsMap, this.startTime);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o != null && o.getClass() == this.getClass()) {
      SumStatsAccumulator that = (SumStatsAccumulator) o;
      return Objects.equals(this.startTime, that.startTime) &&
        Objects.equals(this.statisticsMap, that.statisticsMap);
    }
    return false;
  }
}

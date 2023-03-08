package gms.shared.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.ObjectName;
import java.util.function.BiFunction;

public class CustomMetric<T, U> implements CustomMetricMBean<T, U> {
  private static final Logger logger = LoggerFactory.getLogger(CustomMetric.class);
  private U metricVal;

  /**
   * The update BiFunction is the generic operation to perform to update the metric based on the current metric value
   * which is of type U and the state of an arbitrary/generic object of type T. The function will be applied to the
   * current value of the metric and the input object and returns the next value of the metric.
   */
  private final BiFunction<U, T, U> update;

  private CustomMetric(BiFunction<U, T, U> update, String name, U initVal) {
    this.update = update;
    this.metricVal = initVal;

    try {
      MetricRegister.register(this, new ObjectName(name));
    } catch (JMException e) {
      e.printStackTrace();
      logger.warn("Metrics {} failed to register. No data will be exported for this metric due to this exception: {}  "
        , name, e.getMessage());
    }
  }

  /**
   * Create an instance of the gms.shared.metrics.CustomMetric
   *
   * @param updater The function to use that updates the metric
   * @param name The string name of the metric, this name must follow the format described here: https://docs.oracle.com/javase/9/docs/api/javax/management/ObjectName.html
   * @param initVal The initial value of the metric
   * @param <V> The type of the "attached object"
   * @param <W> The type of the metric
   * @return A new instance of the metric
   */
  public static <V, W> CustomMetric<V, W> create(BiFunction<W, V, W> updater, String name, W initVal) {
    return new CustomMetric<>(updater, name, initVal);
  }

  public U getMetricVal() {
    return this.metricVal;
  }

  /**
   * Sets the new metric value to the return value of the update BiFunction when applied to the current metric
   * value and the "current" attached input object
   *
   * @param current The current state of the object the metric is "attached" to
   */
  public void updateMetric(T current) {
    this.metricVal = this.update.apply(this.metricVal, current);
  }

  /**
   * Utility static method which can be used as the update BiFunction for an instantiation of the gms.shared.metrics.CustomMetric.
   * This "update" "function" ignores the "attached" object and simply increments the metric each time it's called.
   *
   * @param l The previous value of the metric
   * @param _current The current value of the attached object. Ignored
   * @param <V> The type of the attached object
   * @return The incremented value of the metric.
   */
  public static <V> Long incrementer(Long l, V _current) {
    return l + 1;
  }

  /**
   * Utility static method which can be used as the update BiFunction for an instantiation of the gms.shared.metrics.CustomMetric.
   * This "update" "function" ignores the "attached" object and stores the value passed in
   *
   * @param l The previous value of the metric
   * @param _current The current value of the metric, in milliseconds
   * @param <V> The type of the attached object
   * @return The incremented value of the metric.
   */
  public static <V> Long updateTimingData(Long l, Long _current) {
    return _current;
  }
}

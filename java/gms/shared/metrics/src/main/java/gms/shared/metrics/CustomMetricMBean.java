package gms.shared.metrics;

/**
 * Generic interface for a simple custom metric that produces a generic metric from the "getMetricVal". This is a
 * simple "markov" style metric where the next metric value is based only on the current "state" wherein the "state"
 * refers to the combination of two things:
 * <p>
 * 1. The current "metric" value
 * 2. The value of a generic Java object that the metric is attached to.
 * <p>
 * For example, a metric whose value is a Double that is attached to an operation of a Foo object and when the metric
 * is updated, the next value is the combination of the current/previous metric value and a property of the Foo object.
 *
 * @param <T> The type of the object that the metric is attached to
 * @param <U> The type of the metric itself (ex. Long, Double)
 */
public interface CustomMetricMBean<T, U> {
  /**
   * Although this appears to be unused, it is actually necessary by the javax metrics registry to export the
   * metric that you're interested in exposing. The implementation can be a simple getter. Please see the
   * documentation in fk-control-service/docs for more details.
   */
  public U getMetricVal();

  /**
   * Update the metric based on the input object
   *
   * @param current The current state of the object the metric is "attached" to
   */
  public void updateMetric(T current);
}

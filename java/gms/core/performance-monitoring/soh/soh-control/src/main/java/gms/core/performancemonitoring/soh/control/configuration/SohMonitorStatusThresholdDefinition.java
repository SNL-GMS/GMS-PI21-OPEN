package gms.core.performancemonitoring.soh.control.configuration;

/**
 * Common interface for classes that contain threshold information.
 *
 * @param <T> The data type of the thresholded value.
 */
public interface SohMonitorStatusThresholdDefinition<T extends Comparable<T>> {

  /**
   * @return minimum value that can be considered good.
   */
  T getGoodThreshold();

  /**
   * @return minimum value that can be considered marginal
   */
  T getMarginalThreshold();

}

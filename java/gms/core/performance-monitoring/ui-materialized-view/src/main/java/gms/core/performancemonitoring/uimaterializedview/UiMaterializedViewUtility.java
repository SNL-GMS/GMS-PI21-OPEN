package gms.core.performancemonitoring.uimaterializedview;

import java.time.Duration;

/**
 * Utility for a series of static helper functions
 */
public class UiMaterializedViewUtility {

  // Hide default constructor
  private UiMaterializedViewUtility() {
  }

  /**
   * Converts the duration threshold (Latency) into seconds
   *
   * @param duration
   * @return formatted duration in seconds
   */
  public static Double getDurationInSeconds(Duration duration) {
    return setDecimalPrecisionAsNumber(duration.toMillis() / 1000.0, 2);
  }

  /**
   * Round value to specified precision.
   *
   * @param value Double to round
   * @param precision the precision definition.
   * @return rounded value
   */
  public static Double setDecimalPrecisionAsNumber(Double value, int precision) {
    double pow = Math.pow(10.0, precision);
    return Math.round(value * pow) / pow;
  }
}

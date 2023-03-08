package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.util.Optional;

/**
 * Utility set of static functions to help format the SystemMessage
 * message in a more readable format.
 */
public class UISystemMessageUtils {
  private UISystemMessageUtils() {

  }

  /**
   * Formats an Optional Double or Duration to a set percision
   *
   * @param value Optional value might be null
   * @param <T>
   * @return Formatted string of the value if set else returns 'Unknown'
   */
  public static <T> String convertValueToString(Optional<?> value) {
    final String VALUE_NOT_AVAILABLE = "Unknown";

    if (value.isPresent() && value.get() instanceof Double) {
      return UISystemMessageUtils.convertNumberToHumanReadable((Double) value.get()) + "";
    } else if (value.isPresent() && value.get() instanceof Duration) {
      return UISystemMessageUtils.convertDurationToHumanReadable((Duration) value.get()) + "";
    } else {
      return VALUE_NOT_AVAILABLE;
    }
  }

  /**
   * Round value to specified precision.
   *
   * @param value Double to round
   * @param precision
   * @return rounded value
   */
  private static double setPrecision(double value, int precision) {
    double pow = Math.pow(10.0, precision);
    return Math.round(value * pow) / pow;
  }


  /**
   * Convert Duration to string
   * example:
   * PT5M prints as 5 minutes
   * PT1D5M6S prints as 1 day 5 minutes 6 seconds
   * Note: the string will skip any empty entries i.e. above 0 hours
   *
   * @param timeInterval
   * @return Formatted string representing the interval
   */
  public static String convertDurationToHumanReadable(Duration timeInterval) {
    if (timeInterval != null && !timeInterval.isNegative()) {
      return DurationFormatUtils.formatDurationWords(
        timeInterval.toMillis(),
        true,
        true
      );
    }
    return "Unknown";
  }

  /**
   * Converts the number to 2 significant digits
   *
   * @param number
   * @return double with a percision of 2 digits
   */
  public static double convertNumberToHumanReadable(double number) {
    return setPrecision(number, 2);
  }

  /**
   * Returns a more readable SohMonitorType string. This implementation
   * is the same as the UI Environmental display.
   *
   * @param input
   * @return Pretty SohMonitorType string represenation
   */
  public static String prettyPrintMonitorType(SohMonitorType input) {
    String prefix = "ENV_";
    String value = input.name();
    if (value.startsWith(prefix)) {
      value = value.replaceFirst(prefix, "");
    }
    String[] words = value.toLowerCase().split("_");
    StringBuilder prettyStr = new StringBuilder();
    for (String word : words) {
      prettyStr
        .append(word.substring(0, 1).toUpperCase())
        .append(word.substring(1))
        .append(" ");
    }

    return prettyStr.substring(0, prettyStr.length() - 1);
  }
}

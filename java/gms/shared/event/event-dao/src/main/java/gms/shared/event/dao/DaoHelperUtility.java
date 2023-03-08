package gms.shared.event.dao;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Provides utility methods for processing DAOs
 */
public class DaoHelperUtility {

  static final String STRINGINTRANGE = ". It must be in the range %c%d %d%c.";
  static final String STRINGDOUBLERANGE = ". It must be in the range %c%f %f%c.";
  static final String STRINGCHARGRANGE = ". Length of string must not be greater than %d";
  static final String STRINGGREATERTHAN = ". Must be greater than %d";

  private DaoHelperUtility() {
  }

  /**
   * Returns a String describing a required numerical range.
   * i.e "It must be the range [5 6]"
   *
   * @param min the minimum of the range
   * @param max the maximum of the range
   * @param begin the beginning character (i.e '[', '(')
   * @param end the ending character (i.e. ']', ')')
   * @return A string describing the allowable range
   */
  static String createRangeStringInt(int min, int max, char begin, char end) {
    return String.format(STRINGINTRANGE, begin, min, max, end);
  }

  /**
   * Returns a String describing a required numerical range.
   * i.e "It must be the range [5 6]"
   *
   * @param min the minimum of the range
   * @param max the maximum of the range
   * @param begin the beginning character (i.e '[', '(')
   * @param end the ending character (i.e. ']', ')')
   * @return A string describing the allowable range
   */
  static String createRangeStringInt(int min, long max, char begin, char end) {
    return String.format(STRINGINTRANGE, begin, min, max, end);
  }

  /**
   * Returns a String describing a required numerical range.
   * i.e "It must be the range [5 6]"
   *
   * @param min the minimum of the range
   * @param max the maximum of the range
   * @param begin the beginning character (i.e '[', '(')
   * @param end the ending character (i.e. ']', ')')
   * @return A string describing the allowable range
   */
  static String createRangeStringDouble(double min, double max, char begin, char end) {
    return String.format(STRINGDOUBLERANGE, begin, min, max, end);
  }

  /**
   * Returns a String describing a required maximum string length.
   * i.e "Length of string must not be greater than 5"
   *
   * @param maxLength maximum allowable length of string
   * @return A string describing the allowable string length
   */
  static String createCharLengthString(int maxLength) {
    return String.format(STRINGCHARGRANGE, maxLength);
  }

  /**
   * Returns a String describing a required minimum numerical value.
   * i.e "Must be greater than 5"
   *
   * @param value minimum allowable size
   * @return A string describing the minimum allowable size
   */
  static String createGreaterThanString(int value) {
    return String.format(STRINGGREATERTHAN, value);
  }

  /**
   * Verifies that provided object is within desired range
   *
   * @param objectToCheck object to be verified
   * @param outOfRangeAllowedValue Value objectToCheck may equal, although out of range
   * @param lowerBound the lower bound of the desired range
   * @param upperBound the upper bound of the desired range
   * @param lowerBoundExclusive true if the lowerBound is exclusive, false if inclusive
   * @param upperBoundExclusive true if the upperBound is exclusive, false if inclusive
   * @param message The message provided if the objectToCheck is out of range
   * @param <T> no-op if objectToCheck is allowable
   */
  static <T extends Comparable<T>> void checkRange(
    T objectToCheck,
    T outOfRangeAllowedValue,
    T lowerBound,
    T upperBound,
    boolean lowerBoundExclusive,
    boolean upperBoundExclusive,
    String message) {

    if (objectToCheck.compareTo(outOfRangeAllowedValue) == 0) {
      return;
    }

    checkArgument(
      objectToCheck.compareTo(lowerBound) >= 0
        && objectToCheck.compareTo(upperBound) <= 0,
      message
    );

    if (lowerBoundExclusive) {
      checkArgument(objectToCheck.compareTo(lowerBound) != 0, message);
    }

    if (upperBoundExclusive) {
      checkArgument(objectToCheck.compareTo(upperBound) != 0, message);
    }
  }
}

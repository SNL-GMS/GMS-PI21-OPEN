package gms.dataacquisition.cssreader.utilities;

import gms.shared.frameworks.osd.coi.event.MagnitudeModel;
import gms.shared.frameworks.osd.coi.event.MagnitudeType;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.TimeZone;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class CssReaderUtility {

  private static final Logger logger = LoggerFactory.getLogger(CssReaderUtility.class);

  private static final FastDateFormat dateFormat = FastDateFormat
    .getInstance("yyyyMMdd hh:mm:ss", TimeZone.getTimeZone("UTC"));

  private CssReaderUtility() {
  }

  /**
   * Converts a string formatted as seconds.milliseconds into a time Instant object
   */
  public static Optional<Instant> toInstant(String epochSecondsString) {
    checkNotNull(epochSecondsString);
    checkState(!(epochSecondsString.isEmpty() || epochSecondsString.isBlank()), "A time field is empty");
    //if no date boundary, return null and pick it up in Optional...
    if ("9999999999.99900".equals(epochSecondsString)) {
      return Optional.empty();
    }
    var epochSeconds = Double.parseDouble(epochSecondsString);
    long epochMilli = (long) (1000L * epochSeconds);
    return Optional.of(Instant.ofEpochMilli(epochMilli));
  }

  /**
   * Parse a date string which has this format: yyyyMMdd hh:mm:ss (default setting for LDDate).
   *
   * @param dateString The date string.
   * @return An Instant representing the date and time, or null if unable to parse.
   */
  public static Optional<Instant> parseDate(String dateString) {
    checkNotNull(dateString);
    checkState(!(dateString.isEmpty() || dateString.isBlank()), "A time field is empty");
    try {
      var parsedDate = dateFormat.parse(dateString.trim());
      return Optional.of(parsedDate.toInstant());
    } catch (Exception e) {
      logger.error("Failed to parse date string: {} with format: {}", dateString, dateFormat);
      return Optional.empty();
    }
  }

  /**
   * Julian Dates in CSS are in the form yyyyddd hh:mm:ss.mmm This will create an instance with the
   * correct time and year on Jan 1st then add the days, to easily handle the 'ddd' to 'mmdd'
   * conversion Example Julian Date Input: 2017346 23:20:00.142 Example Instance Output:
   * 2017-12-13T23:20:00.142Z
   *
   * @param jd timestamp from CD11 in the form yyyyddd hh:mm:ss.mmm
   * @return Instant object with UTC format
   * @throws IllegalArgumentException Thrown when input string is not the correct length.
   * @throws DateTimeParseException Thrown when date cannot be parsed from the input string.
   */
  public static Optional<Instant> jdToInstant(String jd) {
    checkNotNull(jd);
    checkState(!(jd.isEmpty() || jd.isBlank()), "A jd field is empty");
    if (jd.length() != 7) {
      throw new IllegalArgumentException("Julian Date not length 7 (year=4, day=3)");
      //If CSS 3.0 format, no offdate should pass -1, and NnsaKbCore should be 2286324.
      //Return null, it will be picked up by Optional...
    } else if ("2286324".equals(jd) || "-1".equals(jd)) {
      return Optional.empty();
    }

    var year = jd.substring(0, 4);
    // minus one because nothing to add when days = 001
    int days = Integer.parseInt(jd.substring(4, 7)) - 1;
    String utc = year + "-01-01T00:00:00.000Z";
    var jan1 = Instant.parse(utc);
    return Optional.of(jan1.plus(days, ChronoUnit.DAYS));
  }

  /**
   * Given a magnitude type string, return the correct {@link MagnitudeType} enum value
   *
   * @param val magnitude type as a string
   * @return MagnitudeType enum value corresponding to the specified string
   */
  public static MagnitudeType getMagnitudeType(String val) {
    if (val == null) {
      val = "";
    }
    switch (val) {
      case "mb_ave":
        return MagnitudeType.MB;
      case "mb_mle":
        return MagnitudeType.MBMLE;
      case "mb1":
        return MagnitudeType.MB1;
      case "mb1mle":
        return MagnitudeType.MB1MLE;
      case "mb_tmp":
        return MagnitudeType.MB;
      case "mlppn":
        return MagnitudeType.ML;
      case "ms_ave":
        return MagnitudeType.MS;
      case "ms_mle":
        return MagnitudeType.MSMLE;
      case "ms1":
        return MagnitudeType.MS1;
      case "ms1mle":
        return MagnitudeType.MS1MLE;
      default:
        return MagnitudeType.UNKNOWN;
    }
  }

  /**
   * Given a magnitude model string, return the correct {@link MagnitudeModel} enum value
   *
   * @param val magnitude model as a string
   * @return MagnitudeModel enum value corresponding to the specified string
   */
  public static MagnitudeModel getMagnitudeModel(String val) {
    if (val == null) {
      val = "";
    }
    switch (val) {
      case "qfvc":
      case "qfvc1":
        return MagnitudeModel.VEITH_CLAWSON;
      case "rez_pearce":
        return MagnitudeModel.REZAPOUR_PEARCE;
      case "mcoefs_def1":
      case "mcoefs_def2":
      case "mcoefs_JKA":
      case "mcoefs_JNU":
      case "mcoefs_LPAZ":
      case "mcoefs_PLCA":
      case "mcoefs_WRA":
      default:
        return MagnitudeModel.UNKNOWN;
    }
  }
}

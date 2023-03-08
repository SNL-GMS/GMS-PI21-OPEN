package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Trailer;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Collection of useful functions for dealing with CD-1.1 frames.
 */
public class FrameUtilities {

  // Expected length of timestamps in CD-1.1.
  public static final int TIMESTAMP_LEN = 20;

  private FrameUtilities() {
  }

  /**
   * Pads the string to the specified length with whitespace.
   *
   * @param s the string to pad
   * @param length the desired length of the string
   * @param paddingCharacter the character to use as padding
   *
   * @return the original string s, padded to the right with paddingCharacter up to length
   *
   * @throws IllegalArgumentException if the length of the input string is greater than the requested length
   */
  public static String padToLength(String s, int length, char paddingCharacter) {
    if (s.length() > length) {
      throw new IllegalArgumentException("String too large already; length = "
        + s.length() + ", padded request was to " + length);
    }
    var sBuilder = new StringBuilder(s);
    while (sBuilder.length() < length) {
      sBuilder.append(paddingCharacter);
    }
    s = sBuilder.toString();
    return s;
  }

  /**
   * Pads string to length using ASCII null character ('\0')
   *
   * @param s the string to pad
   * @param length the desired length of the string
   *
   * @return the original string s, padded to the right with ASCII null up to length
   *
   * @throws IllegalArgumentException if the length of the input string is greater than the requested length
   */
  public static String padToLength(String s, int length) {
    return padToLength(s, length, '\0');
  }

  /**
   * Calculates the padded length of a field so the correct amount of bytes can be extracted from a byte buffer.
   *
   * @param unpaddedLength the unpadded length
   * @param divisibleBy what the padded length should be divisible by
   *
   * @return The padded length of a field, which includes padding.
   */
  public static int calculatePaddedLength(int unpaddedLength, int divisibleBy) {
    int padded = unpaddedLength;
    //Pad the auth value size so it's divisible by supplied value (which is typically 4)
    if (unpaddedLength % divisibleBy != 0) {
      padded += (divisibleBy - (unpaddedLength % divisibleBy));
    }
    return padded;
  }

  /**
   * Calculates the padding needed to make the field length divisible by some value.
   *
   * @param size The uppadded length of the value
   * @param divisibleBy The value the field should be divisible by (typically 4) in CD1.1
   *
   * @return The number of bytes needed for padding.
   */
  public static int calculateNeededPadding(int size, int divisibleBy) {
    int modSize = size % divisibleBy;
    return (modSize == 0) ? 0 : divisibleBy - modSize;
  }

  /**
   * Parses a fixed number of bytes from a byte buffer, converts them to a string, and removes nulls and white space.
   *
   * @param frameBytesBuffer the byte buffer to parse from.
   * @param length the number of bytes to parse.
   *
   * @return String
   *
   * @throws IllegalArgumentException if byte buffer is null or doesn't have at least 'length' bytes
   */
  public static String readBytesAsString(ByteBuffer frameBytesBuffer, int length) {
    checkNotNull(frameBytesBuffer);
    checkState(frameBytesBuffer.remaining() >= length,
      "Not enough bytes in this bytebuff to read; need %s but only %s",
      length, frameBytesBuffer.remaining());

    var stringBytes = new byte[length];
    frameBytesBuffer.get(stringBytes);
    // In case strings are null-terminated (c style), replace ASCII '0' with empty.
    return stripString(new String(stringBytes));
  }

  /**
   * Parses a fixed number of bytes from a byte buffer, converts them to a string, and removes nulls and white space.
   *
   * @param frameBytesBuffer the byte buffer to parse from.
   * @param length the number of bytes to parse.
   *
   * @return String
   *
   * @throws java.nio.BufferUnderflowException If there are fewer than length bytes remaining in this buffer
   */
  public static byte[] readBytes(ByteBuffer frameBytesBuffer, int length) {
    checkNotNull(frameBytesBuffer);
    checkArgument(length <= frameBytesBuffer.remaining(),
      "Error reading ByteBuffer, requested length is larger than remaining bytes");
    checkArgument(length >= 0,
      "Error reading ByteBuffer, negative requested length");
    var bytesArray = new byte[length];
    frameBytesBuffer.get(bytesArray);

    return bytesArray;
  }

  /**
   * Remove nul characters and whitespace from a string. Assumes the string is left justified and padded on the right
   * with nul characters.
   *
   * @param str The input string.
   *
   * @return String
   */
  public static String stripString(String str) {

    // Remove all null characters from the string.
    str = str.replace("\0", "");

    // Trim whitespace from the ends of the string.
    str = str.trim();

    return str;
  }

  /**
   * Julian Dates in CD11 are in the form yyyyddd hh:mm:ss.mmm This will create an instance with the correct time and
   * year on Jan 1st then add the days, to easily handle the 'ddd' to 'mmdd' conversion:
   * <p>
   * - Example Julian Date Input: 2017346 23:20:00.142
   * <p>
   * - Example Instance Output: 2017-12-13T23:20:00.142Z
   *
   * @param jd timestamp from CD11 in the form yyyyddd hh:mm:ss.mmm
   *
   * @return Instant object with UTC format
   *
   * @throws IllegalArgumentException Thrown when input string is not the correct length.
   * @throws DateTimeParseException Thrown when date cannot be parsed from the input string.
   */
  public static Instant jdToInstant(String jd) {
    if (jd.length() == TIMESTAMP_LEN) {
      var yearString = jd.substring(0, 4);
      // minus one because nothing to add when days = 001
      var days = Integer.parseInt(jd.substring(4, 7)) - 1;
      var hmsm = jd.substring(8, TIMESTAMP_LEN).trim();
      var utcString = yearString + "-01-01T" + hmsm + "Z";
      var jan1 = Instant.parse(utcString);
      return jan1.plus(days, ChronoUnit.DAYS);
    } else {
      throw new IllegalArgumentException("Julian Date not length " + TIMESTAMP_LEN);
    }
  }

  /**
   * Creates a 'julian date' string (jd), given an instant.
   *
   * @param i the instant
   *
   * @return Example Instant input '2017-12-13T23:20:00.142Z' will provide this result: '2017346 23:20:00.142'.
   */
  public static String instantToJd(Instant i) {
    var localDateTime = LocalDateTime.ofInstant(i, ZoneOffset.UTC);
    var dateString = String.format("%04d%03d %02d:%02d:%02d.%03d",
      localDateTime.getYear(),
      localDateTime.getDayOfYear(),
      localDateTime.getHour(),
      localDateTime.getMinute(),
      localDateTime.getSecond(),
      localDateTime.get(ChronoField.MILLI_OF_SECOND));

    return padToLength(dateString, TIMESTAMP_LEN);
  }

  /**
   * Determine if the input parameter is of the form yyyyddd hh:mm:ss.ttt.
   *
   * @param jd The Julian data string to check
   *
   * @return true if valid Julian date false otherwise
   */
  public static boolean validJulianDate(String jd) {
    return (jd.matches("\\d{4}\\d{3} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}"));
  }

  public static boolean validChannelString(String channelString) {
    return (channelString.matches("[\\w|\\x00]{10}"));
  }

  /**
   * Validates this Cd11Frame as a specific type of frame, and checks that the header FrameType matches expected for
   * that class of frame.
   *
   * @param payload the type the frame is expected to be; determines the return type
   * @param frameType the type of the return value, determined by frame
   *
   * @return the frame casted into the desired type
   *
   * @throws IllegalArgumentException if the frame or clazz is null, if there is no class implementation known for the
   * type of frame, or if the FrameType in the header of the frame is unknown or not as expected for the requested
   * class.
   */
  public static <T extends Cd11Payload> T asPayloadType(Cd11Payload payload, FrameType frameType) {
    checkNotNull(payload);
    // Check that the expected and actual FrameType match, otherwise throw an exception.
    checkArgument(frameType.getClassName().isInstance(payload),
      "Wrong type of frame (expected frameType %s, received %s).",
      frameType.getClassName(), payload.getClass());
    return (T) payload;
  }

  /**
   * Calculate the CRC over the entire frame and compare with the CRC in the frame footer.
   *
   * @param frame Frame to calculate CRC for
   *
   * @return TRUE if CRC is verified, otherwise FALSE. If there is an IO error generating the bytes to compute the CRC
   * value, false is returned (i.e. this method does not throw IOException)
   */
  public static boolean isValidCRC(byte[] rawBytes, Cd11Frame frame) {
    Cd11Trailer frameTrailer = frame.getTrailer();

    // Replace commverification bytes with all zeros before we computing the CRC.
    for (int i = (rawBytes.length - Long.BYTES); i < rawBytes.length; i++) {
      rawBytes[i] = 0;
    }

    // Compute the CRC value.
    return CRC64.isValidCrc(
      rawBytes, rawBytes.length,
      frameTrailer.getCommVerification());
  }
}

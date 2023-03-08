package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Collection of useful functions for dealing with CD-1.1 frames.
 */
class FrameUtilitiesTest {

  @Test
  void testPadding() {
    assertEquals("ABC\0\0\0\0\0",
      FrameUtilities.padToLength("ABC", 8));
    assertEquals("ABCD",
      FrameUtilities.padToLength("ABCD", 4));
  }

  @Test
  void testPadLen() {
    assertEquals(8, FrameUtilities.calculatePaddedLength(5, 4));
    assertEquals(8, FrameUtilities.calculatePaddedLength(8, 4));
  }

  @Test
  void testUnpadLen() {
    assertEquals(3, FrameUtilities.calculateNeededPadding(5, 4));
    assertEquals(0, FrameUtilities.calculateNeededPadding(4, 4));
  }

  /**
   * Test the conversion of a timestamp to an Instant. The CD1.1 uses timestamps in this format: "yyyyddd hh:mm:ss.xxx"
   */
  @Test
  void testTimeTransformation() {
    Instant instant = FrameUtilities.jdToInstant("2017001 13:12:11.123");
    LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
    assertEquals(2017, ldt.get(ChronoField.YEAR));
    assertEquals(1, ldt.get(ChronoField.MONTH_OF_YEAR));
    assertEquals(1, ldt.get(ChronoField.DAY_OF_MONTH));
    assertEquals(13, ldt.get(ChronoField.HOUR_OF_DAY));
    assertEquals(12, ldt.get(ChronoField.MINUTE_OF_HOUR));
    assertEquals(11, ldt.get(ChronoField.SECOND_OF_MINUTE));
    assertEquals(123, ldt.get(ChronoField.MILLI_OF_SECOND));
  }

  /**
   * This timestamp is not of the proper length, so it should be rejected.
   */
  @Test
  void testTimeTransformation2() {
    assertThrows(IllegalArgumentException.class,
      () -> FrameUtilities.jdToInstant("2017001 13:12:11.01"));
  }

  /**
   * This timestamp is not valid (bad hour field), so it should be rejected.
   */
  @Test
  void testTimeTransformation3() {
    assertThrows(DateTimeParseException.class,
      () -> FrameUtilities.jdToInstant("2017001 33:12:11.000"));
  }

  @Test
  void testStripString() {
    String str = "ABC \0\0";
    assertEquals("ABC", FrameUtilities.stripString(str));
  }

  @Test
  void testReadBytesAsString() {
    String str = " ABC  \0\0 ";
    ByteBuffer bb = ByteBuffer.allocate(str.length());
    bb.put(str.getBytes());
    assertEquals("ABC", FrameUtilities.readBytesAsString(bb.rewind(), str.length()));
  }

  @Test
  void testAsPayloadType() {
    Cd11Alert frameToCast = Cd11Alert.create("ALERT");

    assertThrows(IllegalArgumentException.class,
      () -> FrameUtilities.asPayloadType(frameToCast, FrameType.ACKNACK));
    assertDoesNotThrow(() -> FrameUtilities.asPayloadType(frameToCast, FrameType.ALERT));
  }

  @Test
  void testReadBytesBufferUnderflowCaught() {
    int bufferLength = 32;
    ByteBuffer bb = ByteBuffer.allocate(bufferLength);
    assertThrows(IllegalArgumentException.class, () -> FrameUtilities.readBytes(bb, bufferLength + 1));
  }

  @Test
  void testReadBytesBufferNegativeLength() {
    ByteBuffer bb = ByteBuffer.allocate(32);
    assertDoesNotThrow(() -> FrameUtilities.readBytes(bb, 0));
    assertThrows(IllegalArgumentException.class, () -> FrameUtilities.readBytes(bb, -1));
  }
}

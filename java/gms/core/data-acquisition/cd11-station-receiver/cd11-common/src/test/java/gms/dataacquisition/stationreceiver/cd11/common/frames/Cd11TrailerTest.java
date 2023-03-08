package gms.dataacquisition.stationreceiver.cd11.common.frames;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Cd11TrailerTest {

  private static final int AUTH_KEY = 123;                  // [4] 0 - 3
  private static final int AUTH_SIZE = 8;                   // [4] 4 - 7
  private static final long AUTH_VALUE = 1512076158000L;    // [N] <dependent upon authSize>
  private static final long COMM_VERIFY = 1512076209000L;   // [8] <dependent upon authValue>

  private static ByteBuffer initTrailerSegment1() {
    ByteBuffer TEST_TRAILER_SEGMENT1 = ByteBuffer.allocate(8);

    TEST_TRAILER_SEGMENT1.putInt(AUTH_KEY);
    TEST_TRAILER_SEGMENT1.putInt(AUTH_SIZE);

    return TEST_TRAILER_SEGMENT1;
  }

  private static ByteBuffer initTrailerSegment2() {
    ByteBuffer TEST_TRAILER_SEGMENT2 = ByteBuffer.allocate(16);

    TEST_TRAILER_SEGMENT2.putLong(AUTH_VALUE);
    TEST_TRAILER_SEGMENT2.putLong(COMM_VERIFY);

    return TEST_TRAILER_SEGMENT2;
  }

  @Test
  void testTrailerSegmentParsing() {
    ByteBuffer TEST_TRAILER_SEGMENT1 = initTrailerSegment1();
    ByteBuffer TEST_TRAILER_SEGMENT2 = initTrailerSegment2();

    Cd11Trailer frameTrailer = Cd11Trailer.fromSegments(TEST_TRAILER_SEGMENT1,
      TEST_TRAILER_SEGMENT2);

    assertEquals(AUTH_KEY, frameTrailer.getAuthenticationKeyIdentifier());
    assertEquals(AUTH_SIZE, frameTrailer.getAuthenticationSize());
    assertEquals(AUTH_VALUE, ByteBuffer.wrap(frameTrailer.getAuthenticationValue()).getLong());
    assertEquals(0, frameTrailer.getAuthValuePadding());
    assertEquals(COMM_VERIFY, frameTrailer.getCommVerification());

    // Build a single byte buffer containing the full trailer.
    byte[] frameTrailerBytes = frameTrailer.toBytes();
    ByteBuffer TEST_TRAILER = ByteBuffer.allocate(Cd11Trailer.MINIMUM_TRAILER_LENGTH + 8);
    TEST_TRAILER.put(TEST_TRAILER_SEGMENT1.array());
    TEST_TRAILER.put(TEST_TRAILER_SEGMENT2.array());
    byte[] testTrailerBytes = TEST_TRAILER.array();

    assertArrayEquals(testTrailerBytes, frameTrailerBytes);
  }

  @Test
  void testTrailerRead() {
    var trailerBuffer = ByteBuffer.allocate(Cd11Trailer.MINIMUM_TRAILER_LENGTH + 8)
      .putInt(AUTH_KEY)
      .putInt(AUTH_SIZE)
      .putLong(AUTH_VALUE)
      .putLong(COMM_VERIFY)
      .rewind();
    Cd11Trailer frameTrailer = Cd11Trailer.read(trailerBuffer);

    assertEquals(AUTH_KEY, frameTrailer.getAuthenticationKeyIdentifier());
    assertEquals(AUTH_SIZE, frameTrailer.getAuthenticationSize());
    assertEquals(AUTH_VALUE, ByteBuffer.wrap(frameTrailer.getAuthenticationValue()).getLong());
    assertEquals(0, frameTrailer.getAuthValuePadding());
    assertEquals(COMM_VERIFY, frameTrailer.getCommVerification());

    assertArrayEquals(trailerBuffer.array(), frameTrailer.toBytes());
  }

  @Test
  void testTrailerReadPadded() {
    String sevenBytePaddedString = "7-bytes" + "\0";
    int authSizeNoPadding = 7;
    var trailerBuffer = ByteBuffer.allocate(Cd11Trailer.MINIMUM_TRAILER_LENGTH + 8)
      .putInt(AUTH_KEY)
      .putInt(authSizeNoPadding)
      .put(sevenBytePaddedString.getBytes(StandardCharsets.UTF_8))
      .putLong(COMM_VERIFY)
      .rewind();
    Cd11Trailer frameTrailer = Cd11Trailer.read(trailerBuffer);

    assertEquals(AUTH_KEY, frameTrailer.getAuthenticationKeyIdentifier());
    assertEquals(authSizeNoPadding, frameTrailer.getAuthenticationSize());
    assertEquals(sevenBytePaddedString,
      new String(ByteBuffer.wrap(frameTrailer.getAuthenticationValue()).array()));
    assertEquals(1, frameTrailer.getAuthValuePadding());
    assertEquals(COMM_VERIFY, frameTrailer.getCommVerification());

    assertArrayEquals(trailerBuffer.array(), frameTrailer.toBytes());
  }
}

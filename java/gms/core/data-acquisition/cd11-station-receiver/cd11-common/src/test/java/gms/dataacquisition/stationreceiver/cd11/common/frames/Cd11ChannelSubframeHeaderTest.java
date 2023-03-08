package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Cd11ChannelSubframeHeaderTest {

  private static final int NUMBER_OF_CHANNELS = 1;
  private static final int FRAME_TIME_LENGTH = 2000;
  private static final int EXPECTED_HASHCODE = -971664176;
  private static final int CHANNEL_STRING_LENGTH = 10;
  private static final String CHANNEL_STRING = "ABCD1234XY";
  private static ByteBuffer byteBuffer;
  private static final Instant nominalTime = Instant.parse("2017-12-06T02:03:04.098Z");

  @BeforeEach
  public void setup() {
    //the 2 is for padding check
    int size1 = 44 + 2;
    byteBuffer = ByteBuffer.allocate(size1);
    byteBuffer.putInt(NUMBER_OF_CHANNELS);
    byteBuffer.putInt(FRAME_TIME_LENGTH);
    byteBuffer.put(FrameUtilities.instantToJd(nominalTime).getBytes());
    byteBuffer.putInt(CHANNEL_STRING_LENGTH);
    byteBuffer.put(FrameUtilities.padToLength(CHANNEL_STRING, 12).getBytes());
    byteBuffer.putShort(
      (short) 0); // needed to test valid channel string padding -- upper half word of channel lenght field
    ByteBuffer badBuffer = ByteBuffer.allocate(size1);
    badBuffer.put(byteBuffer.array());
  }

  @AfterAll
  public static void tearDown() {

  }

  /**
   * Check for successful parsing of the header.
   */
  @Test
  void testSuccessfulParse() {
    Cd11ChannelSubframeHeader header = Cd11ChannelSubframeHeader.read(byteBuffer.rewind());

    assertEquals(FRAME_TIME_LENGTH, header.frameTimeLength);
    assertEquals(NUMBER_OF_CHANNELS, header.numOfChannels);
    assertEquals(nominalTime, header.nominalTime);
    assertEquals(FrameUtilities.stripString(CHANNEL_STRING), header.channelString);
  }

  /**
   * Pass the wrong size for the channel string, this should fail.
   */
  @Test
  void testUnsuccessfulParse2() {
    byteBuffer.putInt(28, 50);
    byteBuffer.rewind();
    assertThrows(IllegalArgumentException.class, () -> Cd11ChannelSubframeHeader.read(byteBuffer));
  }

  /**
   * Test the toBytes method.
   */
  @Test
  void testToBytes() {
    byteBuffer.rewind();
    Cd11ChannelSubframeHeader header = Cd11ChannelSubframeHeader.read(byteBuffer.rewind());
    assertNotNull(header);
    byteBuffer.rewind();
    byte[] bytes1 = header.toBytes();
    byte[] bytes2 = new byte[byteBuffer.remaining()];
    byteBuffer.get(bytes2);

    for (int i = 0; i < bytes1.length; ++i) {
      assertEquals(bytes1[i], bytes2[i]);
    }

  }

  @ParameterizedTest
  @MethodSource("invalidInputs")
  void testInvalidInput(int numOfChannels, int frameTimeLength, Instant nominalTime, int chanStringCount,
    String chanString, Class<? extends Exception> expectedExceptionType) {
    assertThatExceptionOfType(expectedExceptionType)
      .isThrownBy(() -> new Cd11ChannelSubframeHeader(numOfChannels, frameTimeLength, nominalTime, chanStringCount,
      chanString));
  }

  static Stream<Arguments> invalidInputs() {
    return Stream.of(
      arguments(0, FRAME_TIME_LENGTH, nominalTime, CHANNEL_STRING_LENGTH, CHANNEL_STRING, IllegalArgumentException.class),
      arguments(NUMBER_OF_CHANNELS, -1, nominalTime, CHANNEL_STRING_LENGTH, CHANNEL_STRING, IllegalArgumentException.class),
      arguments(NUMBER_OF_CHANNELS, FRAME_TIME_LENGTH, null, CHANNEL_STRING_LENGTH, CHANNEL_STRING, NullPointerException.class),
      arguments(NUMBER_OF_CHANNELS, FRAME_TIME_LENGTH, nominalTime, CHANNEL_STRING_LENGTH - 1, CHANNEL_STRING, IllegalArgumentException.class),
      arguments(NUMBER_OF_CHANNELS, FRAME_TIME_LENGTH, nominalTime, CHANNEL_STRING_LENGTH, CHANNEL_STRING.substring(1), IllegalArgumentException.class));
  }

  @Test
  void testHashCode() {
    byteBuffer.rewind();
    Cd11ChannelSubframeHeader header = Cd11ChannelSubframeHeader.read(byteBuffer.rewind());
    assertEquals(EXPECTED_HASHCODE, header.hashCode());

  }

  @Test
  void testEquals() {
    byteBuffer.rewind();
    Cd11ChannelSubframeHeader header = Cd11ChannelSubframeHeader.read(byteBuffer.rewind());
    byteBuffer.rewind();
    Cd11ChannelSubframeHeader header2 = Cd11ChannelSubframeHeader.read(byteBuffer.rewind());

    assertEquals(header, header2);
  }
}

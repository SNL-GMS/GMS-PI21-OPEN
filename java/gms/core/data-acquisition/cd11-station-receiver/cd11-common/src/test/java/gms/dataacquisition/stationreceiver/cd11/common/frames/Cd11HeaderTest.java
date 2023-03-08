package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Cd11HeaderTest {

  private static final FrameType FRAME_TYPE = FrameType.CONNECTION_REQUEST;                     // [4] 0 - 3
  private static final int TRAILER_OFFSET = 68;                 // [4] 4 - 7
  public static final String CREATOR = "XDCXDC_1";             // [8] 8 - 13
  public static final String DESTINATION = "XDCXDC_2";         // [8] 14 - 21
  private static final long SEQUENCE_NUMBER = 1512074377000L;  // [8] 22 - 29
  public static final int SERIES = 123;                        // [4] 30 - 33

  private static ByteBuffer initHeader() {
    ByteBuffer TEST_HEADER = ByteBuffer.allocate(Cd11Header.FRAME_LENGTH);

    TEST_HEADER.putInt(FRAME_TYPE.getValue());
    TEST_HEADER.putInt(TRAILER_OFFSET);
    TEST_HEADER.put(CREATOR.getBytes());
    TEST_HEADER.put(DESTINATION.getBytes());
    TEST_HEADER.putLong(SEQUENCE_NUMBER);
    TEST_HEADER.putInt(SERIES);

    return TEST_HEADER;
  }

  @Test
  void testHeaderParsing() {
    ByteBuffer TEST_HEADER = initHeader();

    Cd11Header frameHeader = Cd11Header.read(TEST_HEADER);

    assertEquals(FRAME_TYPE, frameHeader.getFrameType());
    assertEquals(TRAILER_OFFSET, frameHeader.getTrailerOffset());
    assertEquals(CREATOR, frameHeader.getFrameCreator());
    assertEquals(DESTINATION, frameHeader.getFrameDestination());
    assertEquals(SEQUENCE_NUMBER, frameHeader.getSequenceNumber());
    assertEquals(SERIES, frameHeader.getSeries());

    byte[] frameHeaderBytes = frameHeader.toBytes();
    assertArrayEquals(frameHeaderBytes, TEST_HEADER.array());
  }
}

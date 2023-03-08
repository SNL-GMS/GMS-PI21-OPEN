package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame.Kind;
import gms.dataacquisition.stationreceiver.cd11.common.ParseCd11FromByteBufferException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MalformedTest {


  private static final int GAP_COUNT = 2;

  @Test
  void testMalformed() {

    // Create header, body, and trailer.
    Cd11Header TEST_HEADER = FrameHeaderTestUtility.createHeaderForAcknack(
      Cd11HeaderTest.CREATOR, Cd11HeaderTest.DESTINATION, GAP_COUNT);

    ByteBuffer TEST_BODY = initBody();
    byte[] TEST_BODY_array = TEST_BODY.array();

    Cd11Trailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
      TEST_HEADER, TEST_BODY_array);
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    ByteBuffer CD11 = ByteBuffer.allocate(Cd11Header.FRAME_LENGTH +
      TEST_BODY_array.length +
      TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_BODY_array);
    CD11.put(TEST_TRAILER_array);

    Cd11OrMalformedFrame frame = Cd11FrameReader.readFrame(CD11);
    assertEquals(Kind.MALFORMED, frame.getKind());
    MalformedFrame malformedFrame = frame.malformed();


    assertEquals(TEST_HEADER, malformedFrame.getPartialFrame().getHeader().get());
    assertEquals(36, malformedFrame.getReadPosition());
    assertEquals(ParseCd11FromByteBufferException.class, malformedFrame.getCause().getClass());
  }

  private ByteBuffer initBody() {
    ByteBuffer TEST_BODY = ByteBuffer.allocate(4);
    TEST_BODY.putInt(1);
    return TEST_BODY;
  }

}

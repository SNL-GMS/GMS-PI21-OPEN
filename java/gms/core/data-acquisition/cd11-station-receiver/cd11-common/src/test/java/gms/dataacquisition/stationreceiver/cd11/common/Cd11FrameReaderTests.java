package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame.Kind;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Trailer;
import gms.dataacquisition.stationreceiver.cd11.common.frames.FrameHeaderTestUtility;
import gms.dataacquisition.stationreceiver.cd11.common.frames.FrameTrailerTestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11FrameReaderTests {

  ByteBuffer frameBuffer;
  ByteBuffer failureFrame;

  @BeforeEach
  void setUp() {
    Cd11Header TEST_HEADER = FrameHeaderTestUtility.createHeaderForAcknack(
      "creator", "dest", 0);

    Cd11Acknack TEST_ACKNACK = Cd11Acknack.builder()
      .setFrameSetAcked("creator:dest")
      .setLowestSeqNum(0)
      .setHighestSeqNum(0)
      .setGapRanges(new long[]{})
      .setGapCount(0)
      .build();

    byte[] acknackPayload = TEST_ACKNACK.toBytes();
    Cd11Trailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
      TEST_HEADER, acknackPayload);

    // Place all into a CD1.1 frame.
    frameBuffer = ByteBuffer.allocate(Cd11Header.FRAME_LENGTH +
      TEST_ACKNACK.getByteCount() +
      TEST_TRAILER.toBytes().length);
    frameBuffer.put(TEST_HEADER.toBytes());
    frameBuffer.put(acknackPayload);
    frameBuffer.put(TEST_TRAILER.toBytes());
    frameBuffer.rewind();

    failureFrame = ByteBuffer.allocate(Cd11Header.FRAME_LENGTH +
      TEST_ACKNACK.getByteCount());
    failureFrame.put(TEST_HEADER.toBytes());
    failureFrame.put(acknackPayload);
    failureFrame.rewind();
  }

  @Test
  void testParseCd11Header() {

    Cd11Header parsedHeader = Cd11FrameReader.tryReadHeader(frameBuffer);

    assertEquals("creator", parsedHeader.getFrameCreator());
    assertEquals("dest", parsedHeader.getFrameDestination());
    assertEquals(0, parsedHeader.getSeries());
    assertEquals(0, parsedHeader.getSequenceNumber());
  }

  @Test
  void testParseCd11Body() {

    Cd11Header parsedHeader = Cd11FrameReader.tryReadHeader(frameBuffer);
    Cd11Payload payload = Cd11FrameReader.tryReadBody(frameBuffer, parsedHeader);
    assertEquals(Cd11Acknack.MINIMUM_FRAME_LENGTH, payload.getByteCount());

  }

  @Test
  void testParseCd11Trailer() {
    Cd11Trailer parsedTrailer = Cd11FrameReader.tryReadTrailer(frameBuffer);
    assertEquals(92, parsedTrailer.toBytes().length);
  }

  @Test
  void testParseValidFrame() {
    Cd11OrMalformedFrame frame = Cd11FrameReader.readFrame(frameBuffer);
    assertEquals(Kind.CD11, frame.getKind());
    Cd11Frame cd11Frame = frame.cd11();

    assertEquals("creator", cd11Frame.getHeader().getFrameCreator());
    assertEquals(Cd11Acknack.MINIMUM_FRAME_LENGTH, cd11Frame.getPayload().getByteCount());
    assertEquals(16, cd11Frame.getTrailer().toBytes().length);
  }

  @Test
  void testParseMalformedFrame() {
    Cd11OrMalformedFrame frame = Cd11FrameReader.readFrame(failureFrame);
    assertEquals(Kind.MALFORMED, frame.getKind());
  }
}
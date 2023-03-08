package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Cd11AcknackTest {

  private static final String FRAMESET_ACKED = "STA12345678901234567";  // [20] 0 - 19
  private static final long LOWEST_SEQ_NUM = 1512074377000L;            // [8] 20 - 27
  private static final long HIGHEST_SEQ_NUM = 1512076209000L;           // [8] 28 - 35
  private static final int GAP_COUNT = 2;                               // [4] 36 - 39
  private static final long[] GAPS = new long[]{1, 2, 3, 4};           // [32] 40 - 71

  private static ByteBuffer buildAcknackBytes() {
    ByteBuffer TEST_ACKNACK = ByteBuffer.allocate(Cd11Acknack.MINIMUM_FRAME_LENGTH + 32);

    TEST_ACKNACK.put(FRAMESET_ACKED.getBytes());
    TEST_ACKNACK.putLong(LOWEST_SEQ_NUM);
    TEST_ACKNACK.putLong(HIGHEST_SEQ_NUM);
    TEST_ACKNACK.putInt(GAP_COUNT);
    for (long l : GAPS) {
      TEST_ACKNACK.putLong(l);
    }

    return TEST_ACKNACK.rewind();
  }

  @Test
  void testAcknackRoundTrip() {

    //generate a frame
    Cd11Acknack expectedAcknack = Cd11Acknack.builder()
      .setFrameSetAcked(FRAMESET_ACKED)
      .setLowestSeqNum(LOWEST_SEQ_NUM)
      .setHighestSeqNum(HIGHEST_SEQ_NUM)
      .setGapCount(GAP_COUNT)
      .setGapRanges(GAPS)
      .build();

    Cd11Acknack actualAcknack = Cd11PayloadReader
      .tryReadAcknack(ByteBuffer.wrap(expectedAcknack.toBytes()));

    assertEquals(expectedAcknack, actualAcknack);
  }

  @Test
  void testPayloadRoundTrip() {

    //generate a frame
    Cd11Acknack expectedAcknack = Cd11Acknack.builder()
      .setFrameSetAcked(FRAMESET_ACKED)
      .setLowestSeqNum(LOWEST_SEQ_NUM)
      .setHighestSeqNum(HIGHEST_SEQ_NUM)
      .setGapCount(GAP_COUNT)
      .setGapRanges(GAPS)
      .build();

    Cd11Payload actualPayload = Cd11PayloadReader
      .tryReadPayload(FrameType.ACKNACK, ByteBuffer.wrap(expectedAcknack.toBytes()));
    assertEquals(expectedAcknack, actualPayload);
  }

  @Test
  void testAcknackRoundTripBytes() {
    ByteBuffer expectedBytes = buildAcknackBytes();
    Cd11Acknack actualAcknack = Cd11PayloadReader.tryReadAcknack(expectedBytes);
    assertEquals(FRAMESET_ACKED, actualAcknack.getFrameSetAcked());
    assertEquals(HIGHEST_SEQ_NUM, actualAcknack.getHighestSeqNum());
    assertEquals(LOWEST_SEQ_NUM, actualAcknack.getLowestSeqNum());
    assertEquals(GAP_COUNT, actualAcknack.getGapCount());
    assertArrayEquals(GAPS, actualAcknack.getGapRanges());

    byte[] actualBytes = actualAcknack.toBytes();
    assertArrayEquals(expectedBytes.array(), actualBytes);
  }

}

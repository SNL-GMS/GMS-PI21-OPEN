package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class Cd11AlertTest {

  public static final int SIZE = 8;                 // [4] 0 - 3
  public static final String MESSAGE = "TERM1234";  // [8] 4 - 11

  private static ByteBuffer buildAlertBytes() {
    ByteBuffer TEST_ALERT = ByteBuffer.allocate(Cd11Alert.MINIMUM_FRAME_LENGTH + 8);

    TEST_ALERT.putInt(SIZE);
    TEST_ALERT.put(MESSAGE.getBytes());

    return TEST_ALERT.rewind();
  }

  @Test
  void testAlertRoundTrip() {
    Cd11Alert expectedAlert = Cd11Alert.create(MESSAGE);
    assertEquals(SIZE, expectedAlert.getSize());

    Cd11Alert actualAlert = Cd11PayloadReader
      .tryReadAlert(ByteBuffer.wrap(expectedAlert.toBytes()));
    assertEquals(expectedAlert, actualAlert);
  }

  @Test
  void testPayloadRoundTrip() {
    Cd11Alert expectedAlert = Cd11Alert.create(MESSAGE);
    assertEquals(SIZE, expectedAlert.getSize());

    Cd11Payload actualPayload = Cd11PayloadReader
      .tryReadPayload(FrameType.ALERT, ByteBuffer.wrap(expectedAlert.toBytes()));
    assertEquals(expectedAlert, actualPayload);
  }


  @Test
  void testAlertRoundTripBytes() {
    ByteBuffer expectedBytes = buildAlertBytes();
    Cd11Alert actualAlert = Cd11PayloadReader.tryReadAlert(expectedBytes);
    assertEquals(SIZE, actualAlert.getSize());
    assertEquals(MESSAGE, actualAlert.getMessage());
    byte[] actualBytes = actualAlert.toBytes();
    assertArrayEquals(expectedBytes.array(), actualBytes);
  }

  @Test
  void testCreate() {
    Cd11Alert alert = Cd11Alert.create("Test Alert Message");
    assertEquals("Test Alert Message", alert.getMessage());
    assertEquals(18, alert.getSize());
  }
}

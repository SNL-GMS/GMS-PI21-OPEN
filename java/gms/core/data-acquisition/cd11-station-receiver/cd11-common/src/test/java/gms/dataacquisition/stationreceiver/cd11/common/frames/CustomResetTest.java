package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomResetTest {

  @Test
  void testCustomResetCreate() {

    byte[] expectedBytes = {1};
    CustomReset actualCustomReset = CustomReset.create(expectedBytes);
    assertEquals(expectedBytes, actualCustomReset.toBytes());
  }

  @Test
  void testReadPayload() {

    CustomReset expectedCustomReset = CustomReset.create(new byte[]{1});
    Cd11Payload actualPayload = Cd11PayloadReader.tryReadPayload(FrameType.CUSTOM_RESET_FRAME, ByteBuffer
      .wrap(expectedCustomReset.toBytes()));
    assertEquals(expectedCustomReset, actualPayload);
  }

}
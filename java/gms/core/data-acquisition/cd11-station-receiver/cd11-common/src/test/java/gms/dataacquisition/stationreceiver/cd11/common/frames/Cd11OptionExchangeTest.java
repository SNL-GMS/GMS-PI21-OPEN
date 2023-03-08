package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11OptionExchangeTest {

  @Test
  void testOptionExchangeRoundTrip() {
    Cd11OptionExchange expectedOptionExchange = Cd11OptionExchange.builder()
      .setOptionType(1)
      .setOptionValue("Request")
      .build();

    Cd11OptionExchange actualOptionExchange = Cd11PayloadReader
      .tryReadOptionExchange(ByteBuffer.wrap(
        expectedOptionExchange.toBytes()));

    assertEquals(expectedOptionExchange, actualOptionExchange);
  }

  @Test
  void testPayloadRoundTrip() {
    Cd11OptionExchange expectedOptionExchange = Cd11OptionExchange.builder()
      .setOptionType(1)
      .setOptionValue("Request")
      .build();

    Cd11Payload actualPayload = Cd11PayloadReader.tryReadPayload(FrameType.OPTION_REQUEST,
      ByteBuffer.wrap(expectedOptionExchange.toBytes()));

    assertEquals(expectedOptionExchange, actualPayload);
  }

}
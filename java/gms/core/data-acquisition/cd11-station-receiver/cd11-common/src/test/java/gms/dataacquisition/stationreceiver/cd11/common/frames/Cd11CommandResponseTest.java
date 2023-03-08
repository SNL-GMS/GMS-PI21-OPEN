package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11CommandResponseTest {

  @Test
  void testCommandResponseRoundTrip() {

    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    Cd11CommandResponse expectedCommandResponse = Cd11CommandResponse.builder()
      .setResponderStation("name")
      .setSite("site")
      .setChannel("cnl")
      .setLocName("ln")
      .setTimestamp(timestamp)
      .setCommandRequestMessage("message")
      .setResponseMessage("response")
      .build();

    Cd11CommandResponse actualCommandResponse = Cd11PayloadReader
      .tryReadCommandResponse(ByteBuffer.wrap(
        expectedCommandResponse.toBytes()));

    assertEquals(expectedCommandResponse, actualCommandResponse);
  }

  @Test
  void testPayloadRoundTrip() {

    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    Cd11CommandResponse expectedCommandResponse = Cd11CommandResponse.builder()
      .setResponderStation("name")
      .setSite("site")
      .setChannel("cnl")
      .setLocName("ln")
      .setTimestamp(timestamp)
      .setCommandRequestMessage("message")
      .setResponseMessage("response")
      .build();

    Cd11Payload actualPayload = Cd11PayloadReader.tryReadPayload(FrameType.COMMAND_RESPONSE,
      ByteBuffer.wrap(expectedCommandResponse.toBytes()));
    assertEquals(expectedCommandResponse, actualPayload);
  }

}
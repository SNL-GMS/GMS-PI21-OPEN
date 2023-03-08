package gms.dataacquisition.stationreceiver.cd11.common.frames;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11CommandRequestTest {

  @Test
  void testCommandRequestRoundTrip() {
    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    Cd11CommandRequest expectedCommandRequest = Cd11CommandRequest.builder()
      .setStationName("name")
      .setSite("site")
      .setChannel("cnl")
      .setLocName("ln")
      .setTimestamp(timestamp)
      .setCommandMessage("message")
      .build();

    Cd11CommandRequest actualCommandRequest = Cd11PayloadReader
      .tryReadCommandRequest(ByteBuffer.wrap(expectedCommandRequest.toBytes()));
    assertEquals(expectedCommandRequest, actualCommandRequest);
  }

  @Test
  void testPayloadRoundTrip() {
    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    Cd11CommandRequest expectedCommandRequest = Cd11CommandRequest.builder()
      .setStationName("name")
      .setSite("site")
      .setChannel("cnl")
      .setLocName("ln")
      .setTimestamp(timestamp)
      .setCommandMessage("message")
      .build();

    Cd11Payload actualPayload = Cd11PayloadReader.tryReadPayload(FrameType.COMMAND_REQUEST,
      ByteBuffer.wrap(expectedCommandRequest.toBytes()));
    assertEquals(expectedCommandRequest, actualPayload);
  }

}
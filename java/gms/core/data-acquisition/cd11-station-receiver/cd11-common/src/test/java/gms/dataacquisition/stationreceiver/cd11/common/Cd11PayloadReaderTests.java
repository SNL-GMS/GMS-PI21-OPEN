package gms.dataacquisition.stationreceiver.cd11.common;

import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandRequest;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Payload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class Cd11PayloadReaderTests {

  @ParameterizedTest
  @MethodSource("readPayloadSource")
  void testTryReadPayload(FrameType frameType, Cd11Payload expectedPayload,
    Class<? extends Cd11Payload> expectedClass) {
    Cd11Payload actualPayload = Cd11PayloadReader
      .tryReadPayload(frameType, ByteBuffer.wrap(expectedPayload.toBytes()));
    assertTrue(expectedClass.isInstance(actualPayload));
    assertEquals(expectedPayload, actualPayload);
  }

  private static Stream<Arguments> readPayloadSource() {
    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    return Stream.of(
      arguments(
        FrameType.ACKNACK,
        Cd11Acknack.builder()
          .setFrameSetAcked("Test:0")
          .setLowestSeqNum(0)
          .setHighestSeqNum(1)
          .setGapCount(0)
          .setGapRanges(new long[0])
          .build(),
        Cd11Acknack.class),
      arguments(FrameType.ALERT, Cd11Alert.create("testing alert"), Cd11Alert.class),
      arguments(
        FrameType.COMMAND_REQUEST,
        Cd11CommandRequest.builder()
          .setStationName("name")
          .setSite("Site")
          .setChannel("cnl")
          .setLocName("ln")
          .setTimestamp(timestamp)
          .setCommandMessage("message")
          .build(),
        Cd11CommandRequest.class)
    );
  }
}
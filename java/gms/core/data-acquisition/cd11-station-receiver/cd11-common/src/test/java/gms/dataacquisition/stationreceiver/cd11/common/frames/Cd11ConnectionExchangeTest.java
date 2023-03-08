package gms.dataacquisition.stationreceiver.cd11.common.frames;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame.Kind;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11PayloadReader;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class Cd11ConnectionExchangeTest {

  private static final short MAJOR_VERSION = 1;         // [2] 0 - 1
  private static final short MINOR_VERSION = 1;         // [2] 2 - 3
  private static final String RESPONDER = "STA12345";   // [8] 4 - 11
  private static final String RESPONDER_TYPE = "IMS\0";  // [4] 12 - 15
  private static final String SERVICE_TYPE = "TCP\0";    // [4] 16 - 19
  private static final String IP = "127.0.0.1";      // [4] 20 - 23
  private static final int PORT = 8080;            // [2] 24 - 25
  private static final String IP2 = "192.168.0.1";   // [4] 26 - 29
  private static final int PORT2 = 8181;           // [2] 30 - 31

  private static ByteBuffer initConnExchange() throws UnknownHostException {
    ByteBuffer TEST_CONN_EXCH = ByteBuffer.allocate(Cd11ConnectionExchange.FRAME_LENGTH);

    TEST_CONN_EXCH.putShort(MAJOR_VERSION);
    TEST_CONN_EXCH.putShort(MINOR_VERSION);
    TEST_CONN_EXCH.put(RESPONDER.getBytes());
    TEST_CONN_EXCH.put(RESPONDER_TYPE.getBytes());
    TEST_CONN_EXCH.put(SERVICE_TYPE.getBytes());
    TEST_CONN_EXCH.putInt((ByteBuffer.wrap(InetAddress.getByName(IP).getAddress())).getInt());
    TEST_CONN_EXCH.putChar((char) PORT);
    TEST_CONN_EXCH.putInt((ByteBuffer.wrap(InetAddress.getByName(IP2).getAddress())).getInt());
    TEST_CONN_EXCH.putChar((char) PORT2);

    return TEST_CONN_EXCH;
  }

  @Test
  void testConnectionExchangeRoundTrip() {
    InetAddress address = null;
    try {
      address = InetAddress.getByName("192.168.0.1");
    } catch (UnknownHostException e) {
      fail(e);
    }

    int ipAddress = InetAddresses.coerceToInteger(address);

    Cd11ConnectionExchange expectedConnectionExchange = Cd11ConnectionExchange.builder()
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 1)
      .setStationOrResponderName("DC")
      .setStationOrResponderType("IDC")
      .setServiceType("TCP")
      .setIpAddress(ipAddress)
      .setPort(8080)
      .build();

    Cd11ConnectionExchange actualConnectionExchange = Cd11PayloadReader
      .tryReadConnectionExchange(ByteBuffer.wrap(expectedConnectionExchange.toBytes()));
    assertEquals(expectedConnectionExchange.toBuilder()
      .setSecondIpAddress(0)
      .setSecondPort(0)
      .build(), actualConnectionExchange);
  }

  @Test
  void testPayloadRoundTrip() {
    InetAddress address = null;
    try {
      address = InetAddress.getByName("192.168.0.1");
    } catch (UnknownHostException e) {
      fail(e);
    }

    int ipAddress = InetAddresses.coerceToInteger(address);

    Cd11ConnectionExchange expectedConnectionExchange = Cd11ConnectionExchange.builder()
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 1)
      .setStationOrResponderName("DC")
      .setStationOrResponderType("IDC")
      .setServiceType("TCP")
      .setIpAddress(ipAddress)
      .setPort(8080)
      .build();

    Cd11Payload actualPayload = Cd11PayloadReader.tryReadPayload(FrameType.CONNECTION_REQUEST,
      ByteBuffer.wrap(expectedConnectionExchange.toBytes()));
    assertEquals(expectedConnectionExchange.toBuilder()
      .setSecondIpAddress(0)
      .setSecondPort(0)
      .build(), actualPayload);

    actualPayload = Cd11PayloadReader.tryReadPayload(FrameType.CONNECTION_RESPONSE,
      ByteBuffer.wrap(expectedConnectionExchange.toBytes()));
    assertEquals(expectedConnectionExchange.toBuilder()
      .setSecondIpAddress(0)
      .setSecondPort(0)
      .build(), actualPayload);
  }

  @ParameterizedTest
  @MethodSource("testConnExchangeParsingSource")
  void testConnRespParsing(FrameType frameType) throws IOException {

    // Create header, body, and trailer.
    Cd11Header TEST_HEADER = FrameHeaderTestUtility.createHeaderForConnectionExchange(
      Cd11HeaderTest.CREATOR, Cd11HeaderTest.DESTINATION, frameType);

    ByteBuffer TEST_CONN_EXCH = initConnExchange();

    Cd11Trailer TEST_TRAILER = FrameTrailerTestUtility.createTrailerWithoutAuthentication(
      TEST_HEADER, TEST_CONN_EXCH.array());
    byte[] TEST_TRAILER_array = TEST_TRAILER.toBytes();

    // Place all into a CD1.1 frame.
    ByteBuffer CD11 = ByteBuffer.allocate(Cd11Header.FRAME_LENGTH +
      Cd11ConnectionExchange.FRAME_LENGTH + TEST_TRAILER_array.length);
    CD11.put(TEST_HEADER.toBytes());
    CD11.put(TEST_CONN_EXCH.array());
    CD11.put(TEST_TRAILER_array);

    // Perform tests.
    Cd11OrMalformedFrame frame = Cd11FrameReader.readFrame(CD11);
    assertEquals(Kind.CD11, frame.getKind());
    Cd11Frame cd11Frame = frame.cd11();
    Cd11ConnectionExchange responseFrame = (Cd11ConnectionExchange) cd11Frame.getPayload();

    assertEquals(MAJOR_VERSION, responseFrame.getMajorVersion());
    assertEquals(MINOR_VERSION, responseFrame.getMinorVersion());
    assertEquals(RESPONDER, responseFrame.getStationOrResponderName());
    assertEquals(RESPONDER_TYPE.trim(), responseFrame.getStationOrResponderType().trim());
    assertEquals(SERVICE_TYPE.trim(), responseFrame.getServiceType().trim());
    assertEquals(IP, InetAddresses.toAddrString(InetAddresses.fromInteger(responseFrame.getIpAddress())));
    assertEquals(PORT, responseFrame.getPort());
    assertEquals(IP2, InetAddresses.toAddrString(InetAddresses.fromInteger(responseFrame.getSecondIpAddress().orElseThrow())));
    assertEquals(PORT2, responseFrame.getSecondPort().orElseThrow());
  }

  private static Stream<Arguments> testConnExchangeParsingSource() {
    return Stream.of(
      Arguments.arguments(FrameType.CONNECTION_REQUEST),
      Arguments.arguments(FrameType.CONNECTION_RESPONSE)
    );
  }
}

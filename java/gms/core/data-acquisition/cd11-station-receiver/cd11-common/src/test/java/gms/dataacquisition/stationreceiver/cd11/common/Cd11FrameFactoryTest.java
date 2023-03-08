package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Acknack;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandRequest;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponse;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11OptionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11PayloadFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class Cd11FrameFactoryTest {

  Cd11FrameFactory frameFactory;

  @BeforeEach
  void setUp() {
    frameFactory = Cd11FrameFactory.createDefault();
  }

  @Test
  void testDefaultValues() {
    assertEquals(0, frameFactory.getAuthenticationKeyIdentifier());
    assertEquals("TEST", frameFactory.getFrameCreator());
    assertEquals("0", frameFactory.getFrameDestination());
  }

  @Test
  void testWrapAlert() {
    Cd11Alert expectedPayload = Cd11Alert.create("testing alert");
    Cd11Frame actualFrame = frameFactory.wrap(expectedPayload);
    assertDefaultHeader(actualFrame.getHeader(), FrameType.ALERT, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapAcknack() {
    Cd11Acknack expectedPayload = Cd11Acknack.builder()
      .setFrameSetAcked("Test:0")
      .setLowestSeqNum(0)
      .setHighestSeqNum(1)
      .setGapCount(0)
      .setGapRanges(new long[0])
      .build();

    Cd11Frame actualFrame = frameFactory.wrap(expectedPayload);
    assertDefaultHeader(actualFrame.getHeader(), FrameType.ACKNACK, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapCommandRequest() {
    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    Cd11CommandRequest expectedPayload = Cd11CommandRequest.builder()
      .setStationName("name")
      .setSite("site")
      .setChannel("cnl")
      .setLocName("ln")
      .setTimestamp(timestamp)
      .setCommandMessage("message")
      .build();

    Cd11Frame actualFrame = frameFactory.wrap(expectedPayload);
    assertDefaultHeader(actualFrame.getHeader(), FrameType.COMMAND_REQUEST, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapCommandResponse() {
    long expectedSequenceNumber = 11235813;
    Instant timestamp = Instant.now().truncatedTo(
      ChronoUnit.SECONDS); //Truncate because the frame has a lower number of decimals then the base object
    Cd11CommandResponse expectedPayload = Cd11CommandResponse.builder()
      .setResponderStation("name")
      .setSite("site")
      .setChannel("cnl")
      .setLocName("ln")
      .setTimestamp(timestamp)
      .setCommandRequestMessage("message")
      .setResponseMessage("response")
      .build();

    Cd11Frame actualFrame = frameFactory.wrap(expectedPayload, expectedSequenceNumber);
    assertDefaultHeader(actualFrame.getHeader(), FrameType.COMMAND_RESPONSE,
      expectedSequenceNumber);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapConnectionRequest() {
    InetAddress address = null;
    try {
      address = InetAddress.getByName("192.168.0.1");
    } catch (UnknownHostException e) {
      fail(e);
    }

    int ipAddress = InetAddresses.coerceToInteger(address);

    Cd11ConnectionExchange expectedPayload = Cd11ConnectionExchange.builder()
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 1)
      .setStationOrResponderName("DC")
      .setStationOrResponderType("IDC")
      .setServiceType("TCP")
      .setIpAddress(ipAddress)
      .setPort(8080)
      .build();

    Cd11Frame actualFrame = frameFactory.wrapRequest(expectedPayload);
    assertDefaultHeader(actualFrame.getHeader(), FrameType.CONNECTION_REQUEST, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapConnectionResponse() {
    InetAddress address = null;
    try {
      address = InetAddress.getByName("192.168.0.1");
    } catch (UnknownHostException e) {
      fail(e);
    }

    int ipAddress = InetAddresses.coerceToInteger(address);

    Cd11ConnectionExchange expectedPayload = Cd11ConnectionExchange.builder()
      .setMajorVersion((short) 1)
      .setMinorVersion((short) 1)
      .setStationOrResponderName("DC")
      .setStationOrResponderType("IDC")
      .setServiceType("TCP")
      .setIpAddress(ipAddress)
      .setPort(8080)
      .build();

    Cd11Frame actualFrame = frameFactory.wrapResponse(expectedPayload);
    assertDefaultHeader(actualFrame.getHeader(), FrameType.CONNECTION_RESPONSE, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapData() {
    Cd11Data expectedPayload = Cd11PayloadFixtures.cd11Data();
    long expectedSequenceNumber = 11235813;

    Cd11Frame actualFrame = frameFactory.wrap(expectedPayload, expectedSequenceNumber);
    Cd11Header actualHeader = actualFrame.getHeader();
    assertDefaultHeader(actualHeader, FrameType.DATA, expectedSequenceNumber);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapOptionRequest() {
    Cd11OptionExchange expectedPayload = Cd11OptionExchange.builder()
      .setOptionType(1)
      .setOptionValue("Request")
      .build();

    Cd11Frame actualFrame = frameFactory.wrapRequest(expectedPayload);
    Cd11Header actualHeader = actualFrame.getHeader();
    assertDefaultHeader(actualHeader, FrameType.OPTION_REQUEST, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  @Test
  void testWrapOptionResponse() {
    Cd11OptionExchange expectedPayload = Cd11OptionExchange.builder()
      .setOptionType(1)
      .setOptionValue("Response")
      .build();

    Cd11Frame actualFrame = frameFactory.wrapResponse(expectedPayload);
    Cd11Header actualHeader = actualFrame.getHeader();
    assertDefaultHeader(actualHeader, FrameType.OPTION_RESPONSE, 0);
    assertEquals(expectedPayload, actualFrame.getPayload());
    assertEquals(frameFactory.getAuthenticationKeyIdentifier(),
      actualFrame.getTrailer().getAuthenticationKeyIdentifier());
  }

  private void assertDefaultHeader(Cd11Header actualHeader, FrameType frameType,
    long expectedSequenceNumber) {
    assertEquals(frameType, actualHeader.getFrameType());
    assertEquals(frameFactory.getFrameCreator(), actualHeader.getFrameCreator());
    assertEquals(frameFactory.getFrameDestination(), actualHeader.getFrameDestination());
    assertEquals(expectedSequenceNumber, actualHeader.getSequenceNumber());
    assertEquals(0, actualHeader.getSeries());
  }
}
package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11SocketConfigTest {

  public static final String DEFAULT_STATION_OR_RESPONDER_NAME = "H04N";
  public static final String DEFAULT_STATION_OR_RESPONDER_TYPE = "IDC";
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "TEST";
  public static final String DEFAULT_FRAME_DESTINATION = "0";
  public static final short DEFAULT_PROTOCOL_MAJOR_VERSION = 1;
  public static final short DEFAULT_PROTOCOL_MINOR_VERSION = 1;
  public static final int DEFAULT_AUTHENTICATION_KEY_IDENTIFIER = 0;


  @Test
  void testBuilderWithDefaults() {
    Cd11SocketConfig config = Cd11SocketConfig.builderWithDefaults().build();

    assertEquals(DEFAULT_STATION_OR_RESPONDER_NAME, config.getStationOrResponderName());
    assertEquals(DEFAULT_STATION_OR_RESPONDER_TYPE, config.getStationOrResponderType());
    assertEquals(DEFAULT_SERVICE_TYPE, config.getServiceType());
    assertEquals(DEFAULT_FRAME_CREATOR, config.getFrameCreator());
    assertEquals(DEFAULT_FRAME_DESTINATION, config.getFrameDestination());
    assertEquals(DEFAULT_PROTOCOL_MAJOR_VERSION, config.getProtocolMajorVersion());
    assertEquals(DEFAULT_PROTOCOL_MINOR_VERSION, config.getProtocolMinorVersion());
    assertEquals(DEFAULT_AUTHENTICATION_KEY_IDENTIFIER, config.getAuthenticationKeyIdentifier());
  }


  @Test
  void testBuilder() {
    Cd11SocketConfig config = Cd11SocketConfig.builder().setStationOrResponderName("B12A")
      .setStationOrResponderType("IMS").setServiceType("TCP").setFrameCreator("JUNIT")
      .setFrameDestination("1").setProtocolMajorVersion((short) 2)
      .setProtocolMinorVersion((short) 3).setAuthenticationKeyIdentifier(4).build();

    assertEquals("B12A", config.getStationOrResponderName());
    assertEquals("IMS", config.getStationOrResponderType());
    assertEquals("TCP", config.getServiceType());
    assertEquals("JUNIT", config.getFrameCreator());
    assertEquals("1", config.getFrameDestination());
    assertEquals(2, config.getProtocolMajorVersion());
    assertEquals(3, config.getProtocolMinorVersion());
    assertEquals(4, config.getAuthenticationKeyIdentifier());
  }
}

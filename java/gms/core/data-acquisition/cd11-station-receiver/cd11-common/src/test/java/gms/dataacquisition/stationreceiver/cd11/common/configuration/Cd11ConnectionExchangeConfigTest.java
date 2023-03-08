package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Cd11ConnectionExchangeConfigTest {


  @Test
  void buildTest() {
    Cd11ConnectionConfig config = Cd11ConnectionConfig.builder()
      .setProtocolMajorVersion((short) 1).setProtocolMinorVersion((short) 2)
      .setServiceType("test")
      .build();

    assertEquals(1, config.getProtocolMajorVersion());
    assertEquals(2, config.getProtocolMinorVersion());
    assertEquals("test", config.getServiceType());
  }
}

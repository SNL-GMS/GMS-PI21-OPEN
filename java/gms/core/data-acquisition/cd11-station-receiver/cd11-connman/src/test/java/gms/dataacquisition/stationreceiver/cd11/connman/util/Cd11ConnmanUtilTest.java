package gms.dataacquisition.stationreceiver.cd11.connman.util;


import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetAddress;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.willReturn;


@ExtendWith(MockitoExtension.class)
class Cd11ConnmanUtilTest {


  @Mock
  SystemConfig systemConfig;

  static Stream<Arguments> getInetAddresses() {
    return Stream.of(
      Arguments.arguments("192.168.0.1", "192.168.0.1")
    );
  }


  @ParameterizedTest
  @MethodSource("getInetAddresses")
  void testResolveInetAddress(String host, String ip) {
    willReturn(host)
      .given(systemConfig).getValue("sk1");

    InetAddress result = Cd11ConnManUtil.resolveInetAddress(systemConfig, "sk1");
    assertEquals(ip, result.getHostAddress());
  }

  @Test
  void testBuildAddressMap() {
    willReturn("192.168.0.1")
      .given(systemConfig).getValue("sk1");

    Map<String, InetAddress> result = Cd11ConnManUtil.buildAddressMap(systemConfig, "sk1");

    assertEquals(1, result.size());
    assertEquals("192.168.0.1", result.get("sk1").getHostAddress());

  }

}

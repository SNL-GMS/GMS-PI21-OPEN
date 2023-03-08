package gms.dataacquisition.stationreceiver.cd11.connman;

import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParameters;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnManConfig;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpServer;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class Cd11ConnectionManagerTest {

  @Mock
  SystemConfig systemConfig;
  @Mock
  Cd11ConnManConfig connManConfig;
  @Mock
  Cd11DataConsumerParameters acquiredConsumer;
  @Mock
  Cd11DataConsumerParameters unacquiredConsumer;

  @Mock
  TcpServer tcpServer;

  @Test
  void testConnManInit() {

    given(systemConfig.getValueAsInt("connection-manager-well-known-port")).willReturn(8100);
    given(connManConfig.getCd11StationParameters())
      .willReturn(List.of(acquiredConsumer, unacquiredConsumer));
    given(acquiredConsumer.isAcquired()).willReturn(true);
    given(unacquiredConsumer.isAcquired()).willReturn(false);
    given(acquiredConsumer.getStationName()).willReturn("acqStat");
    given(unacquiredConsumer.getStationName()).willReturn("unacqStat");
    given(acquiredConsumer.getPort()).willReturn(8100);

    willReturn("192.168.0.1")
      .given(systemConfig).getValue("data-manager-ip-address");
    willReturn("127.0.0.1")
      .given(systemConfig).getValue("data-provider-ip-address");

    Cd11ConnectionManager connMan = Cd11ConnectionManager.create(systemConfig, connManConfig);
    connMan.initialize();

    Mockito.verify(acquiredConsumer).getPort();
    Mockito.verify(unacquiredConsumer, times(0)).getPort();

    assertEquals(2, connMan.getInetAddressMap().size());
    assertEquals("192.168.0.1", connMan.getInetAddressMap().get("data-manager-ip-address").getHostAddress());
    assertEquals("127.0.0.1", connMan.getInetAddressMap().get("data-provider-ip-address").getHostAddress());
    assertNotNull(connMan.getIgnoredStationsMap().get("unacqStat"));
    assertNotNull(connMan.getCd11StationsLookup().get("acqStat"));
  }

  @Test
  void testConnManBind() {
    given(systemConfig.getValueAsInt("connection-manager-well-known-port")).willReturn(8100);
    given(systemConfig.getValueAsInt("bind-retries")).willReturn(2);
    given(systemConfig.getValueAsDuration("bind-initial-wait")).willReturn(Duration.ZERO);

    Cd11ConnectionManager connMan = Cd11ConnectionManager.create(systemConfig, connManConfig);
    connMan.initForBindTest(tcpServer);

    var exception = new RuntimeException("NOPE");
    given(tcpServer.bind()).willReturn(Mono.error(exception));

    StepVerifier.withVirtualTime(connMan::bind)
      .verifyError();

    given(tcpServer.bind()).willReturn(Mono.empty());

    StepVerifier.withVirtualTime(connMan::bind)
      .verifyComplete();
  }

}

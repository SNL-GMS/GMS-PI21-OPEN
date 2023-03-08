package gms.dataacquisition.stationreceiver.cd11.connman;


import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.netty.DisposableServer;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Cd11ConnManServiceTest {

  @Mock
  SystemConfig systemConfig;

  @Mock
  ControlContext context;

  private static String configurationBase;

  @BeforeAll
  static void setUp() {
    configurationBase = Thread.currentThread().getContextClassLoader()
      .getResource("gms/shared/frameworks/processing/configuration/service/configuration-base")
      .getPath();
  }

  @Test
  void testCreate() {

    FileConfigurationRepository fileRepo = FileConfigurationRepository.create(new File(configurationBase).toPath());

    final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);

    final ConfigurationConsumerUtility configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(fileRepo)
      .retryConfiguration(retryConfig)
      .configurationNamePrefixes(List.of("connman."))
      .build();

    given(context.getSystemConfig()).willReturn(systemConfig);
    given(systemConfig.getValueAsInt("cd11-dataconsumer-baseport")).willReturn(8100);
    given(context.getProcessingConfigurationConsumerUtility()).willReturn(configurationConsumerUtility);

    Cd11ConnManService service = Cd11ConnManService.create(context);

    assertNotNull(service);
    assertNotNull(service.getCd11ConnMan());
  }

  @Test
  void testShutdown() {
    var connMan = mock(Cd11ConnectionManager.class);
    var connManServer = mock(DisposableServer.class);

    assertDoesNotThrow(() -> Cd11ConnManService.shutdown(connMan, connManServer));

    verify(connMan).shutdown();
    verify(connManServer).dispose();
  }

}

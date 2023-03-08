package gms.shared.frameworks.service;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceGeneratorTests {

  @Mock
  private ServiceGeneratorWorker mockWorker;

  @Mock
  private SystemConfig mockSysConfig;

  @Mock
  private HttpService mockService;

  @BeforeEach
  void setup() {
    ServiceGenerator.setWorker(mockWorker);
  }

  @Test
  void testRunService() {
    final String instanceObj = "foo";
    when(mockWorker.createService(instanceObj, mockSysConfig)).thenReturn(mockService);
    ServiceGenerator.runService(instanceObj, mockSysConfig);
    verify(mockService).start();
  }
}

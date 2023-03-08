package gms.shared.frameworks.service;

import gms.shared.frameworks.common.config.ServerConfig;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ServiceGeneratorWorkerTests {

  private static final ServiceGeneratorWorker worker = new ServiceGeneratorWorker();

  private static final ServerConfig serverConfig = ServerConfig.from(
    8080, 1, 2, Duration.ofMillis(3));

  private static final HttpControl control = new HttpControl();

  @Mock
  private SystemConfig mockSysConfig;

  @Path("/test")
  private static class HttpControl {

    private static final String FOO_ROUTE = "foo";
    private static final String FULL_FOO_ROUTE = "/test/" + FOO_ROUTE;

    @Path(FOO_ROUTE)
    @POST
    public int foo(int input) {
      return input * 2;
    }
  }

  @Test
  void testCreateService() {
    doReturn(serverConfig).when(mockSysConfig).getServerConfig();
    final HttpService service = worker.createService(control, mockSysConfig);
    assertNotNull(service);
    assertFalse(service.isRunning());
    assertServiceDefinitionAsExpected(service.getDefinition());
  }

  @Test
  void testCreateServiceDefinition() {
    doReturn(serverConfig).when(mockSysConfig).getServerConfig();
    assertServiceDefinitionAsExpected(worker.createServiceDefinition(
      control, mockSysConfig));
  }

  private static class ControlRouteHandlerInvalidPath {
    @Path("/|`^")
    public Object handleInt(int foo) {
      return -1;
    }
  }

  @Test
  void testCreateServiceRouteGenerationFailsExpectIllegalArgumentException() {
    assertEquals("Could not create a service definition for the provided object.",
      assertThrows(IllegalArgumentException.class,
        () -> worker.createService(ControlRouteHandlerInvalidPath.class, mockSysConfig))
        .getMessage());
  }

  private static void assertServiceDefinitionAsExpected(ServiceDefinition serviceDef) {
    assertNotNull(serviceDef);
    final Set<Route> routes = serviceDef.getRoutes();
    assertAll(
      () -> assertEquals(1, routes.size()),
      () -> assertTrue(routes.stream()
        .findFirst()
        .map(Route::getPath)
        .filter(HttpControl.FULL_FOO_ROUTE::equals)
        .isPresent()),
      () -> assertEquals(serverConfig, serviceDef.getServerConfig())
    );
  }
}

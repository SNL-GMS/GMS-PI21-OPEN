package gms.core.ui.processing.configuration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import gms.core.ui.processing.configuration.manager.UiProcessingConfigurationManager;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.spring.utilities.framework.SpringTestBase;

@WebMvcTest(UiProcessingConfigurationManager.class)
class UiProcessingConfigurationManagerTest extends SpringTestBase {

  private static final String DEFAULT_USER = "defaultUser";

  @MockBean
  private SystemConfig systemConfig;

  @MockBean
  private RetryConfig retryConfig;

  @MockBean
  private ConfigurationConsumerUtility configurationConsumerUtility;

  @Test
  void testResolve() throws Exception {

    ConfigQuery configQuery = TestFixture.query;
    MockHttpServletResponse result = postResult("/resolve", configQuery);
    assertAll("Should return OK status with empty JSON object as body",
      () -> assertEquals(HttpStatus.OK.value(), result.getStatus()),
      () -> {
        try {
          assertEquals("{}", result.getContentAsString());
        } catch (Exception e) {
          fail(e);
        }
      });
  }
  
  @Test
  void testUpdate() throws Exception {
    var mockBuilder = Mockito.mock(ConfigurationConsumerUtility.Builder.class);
    Mockito.when(configurationConsumerUtility.toBuilder()).thenReturn(mockBuilder);
    Mockito.when(mockBuilder.build()).thenReturn(configurationConsumerUtility);
    
    var result = postResultNoBody("/update");
    assertAll("Should return OK status with empty JSON object as body",
      () -> assertEquals(HttpStatus.OK.value(), result.getStatus()),
      () -> {
        try {
          assertEquals("{}", result.getContentAsString());
        } catch (Exception e) {
          fail(e);
        }
      });
  }

  @Test
  void testUpdateExceptionReturnsISE() throws Exception {
    var mockBuilder = Mockito.mock(ConfigurationConsumerUtility.Builder.class);
    Mockito.when(configurationConsumerUtility.toBuilder()).thenReturn(mockBuilder);
    Mockito.when(mockBuilder.build()).thenThrow(new RuntimeException("NOPE"));
    
    var result = postResultNoBody("/update");
    assertAll("Should return OK status with empty JSON object as body",
      () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus()),
      () -> {
        try {
          assertTrue(result.getContentAsString().contains(RuntimeException.class.getName()));
        } catch (Exception e) {
          fail(e);
        }
      });
  }
}

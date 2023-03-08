package gms.shared.user.manager;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.spring.utilities.framework.SpringTestBase;
import gms.shared.user.preferences.api.UserPreferencesRepositoryInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.persistence.EntityManagerFactory;

import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_DEFAULT;
import static org.mockito.Mockito.times;

@WebMvcTest(UserManager.class)
class UserManagerTest extends SpringTestBase {
  private static final String DEFAULT_USER = "defaultUser";

  @MockBean
  private SystemConfig systemConfig;

  @MockBean
  private RetryConfig retryConfig;

  @MockBean
  private ConfigurationConsumerUtility util;

  @MockBean
  private UserPreferencesRepositoryInterface userPreferencesRepositoryImpl;

  @MockBean
  private EntityManagerFactory entityManagerFactory;

  @Test
  void getUserPreferencesByUserId() throws Exception {
    MockHttpServletResponse response = postResultTextPlain(
      "/user-preferences",
      DEFAULT_USER,
      HttpStatus.OK);

    Mockito.verify(userPreferencesRepositoryImpl, times(1)).getUserPreferencesByUserId(
      DEFAULT_USER
    );
  }

  @Test
  void setUserPreferences() throws Exception {
    MockHttpServletResponse response = postResult(
      "/user-preferences/store",
      USER_PREFERENCES_DEFAULT,
      HttpStatus.OK);

    Mockito.verify(userPreferencesRepositoryImpl, times(1)).setUserPreferences(
      USER_PREFERENCES_DEFAULT
    );
  }
}

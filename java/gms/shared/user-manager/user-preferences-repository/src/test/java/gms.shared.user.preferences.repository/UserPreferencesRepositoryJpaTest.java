package gms.shared.user.preferences.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.user.preferences.coi.UserPreferences;
import gms.shared.user.preferences.dao.UserPreferencesDao;
import gms.shared.user.preferences.repository.util.UserManagerPostgresTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Optional;

import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_AUDIBLE_DEFAULT;
import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_AUDIBLE_TWO;
import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_AUDIBLE_UPDATE;
import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_DEFAULT;
import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_JSON;
import static gms.shared.user.preferences.testfixtures.UserPreferencesTestFixtures.USER_PREFERENCES_TWO;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class UserPreferencesRepositoryJpaTest extends UserManagerPostgresTest {

  @Test
  void setUserPreferences() {

    UserPreferencesRepositoryJpa userPreferencesRepository =
      new UserPreferencesRepositoryJpa(entityManagerFactory);
    userPreferencesRepository.setUserPreferences(USER_PREFERENCES_DEFAULT);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    UserPreferencesDao userPreferencesDao = entityManager.find(UserPreferencesDao.class,
      USER_PREFERENCES_DEFAULT.getUserId());

    assertNotNull(userPreferencesDao);
    assertEquals(USER_PREFERENCES_DEFAULT, userPreferencesDao.toCoi());
    entityManager.close();
  }

  @Test
  void getUserPreferencesByUserId() {

    UserPreferencesRepositoryJpa userPreferencesRepository =
      new UserPreferencesRepositoryJpa(entityManagerFactory);
    userPreferencesRepository.setUserPreferences(USER_PREFERENCES_TWO);

    Optional<UserPreferences> actual =
      userPreferencesRepository.getUserPreferencesByUserId(USER_PREFERENCES_TWO.getUserId());
    assertTrue(actual.isPresent());
    assertEquals(USER_PREFERENCES_TWO, actual.get());
  }

  @Test
  void setUserPreferences_withNotifications() {

    UserPreferencesRepositoryJpa userPreferencesRepository = new UserPreferencesRepositoryJpa(entityManagerFactory);
    userPreferencesRepository.setUserPreferences(USER_PREFERENCES_AUDIBLE_DEFAULT);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    UserPreferencesDao userPreferencesDao =
      entityManager.find(UserPreferencesDao.class, USER_PREFERENCES_AUDIBLE_DEFAULT.getUserId());

    assertNotNull(userPreferencesDao);
    assertEquals(USER_PREFERENCES_AUDIBLE_DEFAULT, userPreferencesDao.toCoi());
    entityManager.close();
  }

  @Test
  void setUserPreferences_updateNotifications() {

    UserPreferencesRepositoryJpa userPreferencesRepository = new UserPreferencesRepositoryJpa(entityManagerFactory);
    userPreferencesRepository.setUserPreferences(USER_PREFERENCES_AUDIBLE_DEFAULT);
    userPreferencesRepository.setUserPreferences(USER_PREFERENCES_AUDIBLE_UPDATE);

    EntityManager entityManager = entityManagerFactory.createEntityManager();
    UserPreferencesDao userPreferencesDao = entityManager.find(UserPreferencesDao.class,
      USER_PREFERENCES_AUDIBLE_DEFAULT.getUserId());
    assertNotNull(userPreferencesDao);
    assertEquals(USER_PREFERENCES_AUDIBLE_UPDATE, userPreferencesDao.toCoi());
    entityManager.close();
  }

  @Test
  void getUserPreferencesByUserId_withNotifications() {

    UserPreferencesRepositoryJpa userPreferencesRepository = new UserPreferencesRepositoryJpa(entityManagerFactory);
    userPreferencesRepository.setUserPreferences(USER_PREFERENCES_AUDIBLE_TWO);

    Optional<UserPreferences> actual =
      userPreferencesRepository.getUserPreferencesByUserId(USER_PREFERENCES_AUDIBLE_TWO.getUserId());
    assertTrue(actual.isPresent());
    assertEquals(USER_PREFERENCES_AUDIBLE_TWO, actual.get());
  }

  @Test
  void testUpdate() throws IOException {

    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    UserPreferences preferences = objectMapper.readValue(USER_PREFERENCES_JSON, UserPreferences.class);

    UserPreferencesRepositoryJpa userPreferencesRepository = new UserPreferencesRepositoryJpa(entityManagerFactory);
    assertDoesNotThrow(() -> userPreferencesRepository.setUserPreferences(preferences));
  }
}
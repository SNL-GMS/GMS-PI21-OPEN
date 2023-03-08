package gms.shared.frameworks.osd.repository.utils;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.EntityType;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility for creating EntityManagerFactory's for JPA. These know about all of the core Data Access Objects for GMS.
 */
public class CoiEntityManagerFactory {

  public static final String UNIT_NAME = "gms";
  public static final String HIBERNATE_CONNECTION_PROP_KEY = "hibernate.connection.url";
  public static final String HIBERNATE_CONNECTION_USERNAME_KEY = "hibernate.connection.username";
  public static final String HIBERNATE_CONNECTION_PW_KEY = "hibernate.connection.password";
  public static final String HIBERNATE_C3P0_POOL_SIZE_KEY = "hibernate.c3p0.max_size";
  public static final String HIBERNATE_FLUSH_MODE = "hibernate.flushMode";

  private static final Logger logger = LoggerFactory.getLogger(CoiEntityManagerFactory.class);

  private static final String GMS_CONFIG_SQL_PW_KEY = "sql_password";

  private static final String GMS_CONFIG_SQL_ELEV_PW_KEY = "sql_elev_password";
  private static final String GMS_CONFIG_SQL_USERNAME_KEY = "sql_user";

  private static final String GMS_CONFIG_SQL_ELEV_USERNAME_KEY = "sql_elev_user";
  private static final String GMS_CONFIG_SQL_URL_KEY = "sql_url";
  private static final String GMS_CONFIG_C3P0_POOL_SIZE_KEY = "c3p0_connection_pool_size";

  private static final String FLUSH_MODE_VALUE = "FLUSH_AUTO";
  private static final String UNABLE_TO_RETRIEVE_THE_SQL_PW_ERROR = "Unable to retrieve the sql password";

  /**
   * Creates an EntityManagerFactory with defaults.
   *
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory create() {
    return create(Map.of());
  }

  public static EntityManagerFactory create(String unitName, SystemConfig config) {
    Objects.requireNonNull(unitName, "UnitName cannot be null");
    Objects.requireNonNull(config, "Config cannot be null");

    Map<String, String> propertiesOverrides = Map.of(HIBERNATE_CONNECTION_PROP_KEY, config.getValue(GMS_CONFIG_SQL_URL_KEY),
      HIBERNATE_CONNECTION_USERNAME_KEY, config.getValue(GMS_CONFIG_SQL_USERNAME_KEY),
      HIBERNATE_CONNECTION_PW_KEY,
      Optional.ofNullable(config.getValue(GMS_CONFIG_SQL_PW_KEY))
        .orElseThrow(() -> new IllegalStateException(
          UNABLE_TO_RETRIEVE_THE_SQL_PW_ERROR)),
      HIBERNATE_C3P0_POOL_SIZE_KEY, config.getValue(GMS_CONFIG_C3P0_POOL_SIZE_KEY),
      HIBERNATE_FLUSH_MODE, FLUSH_MODE_VALUE);

    try {
      return Persistence.createEntityManagerFactory(unitName, propertiesOverrides);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + unitName, e);
    }

  }

  /**
   * Creates an EntityManagerFactory using system configuration.
   *
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory create(SystemConfig config) {
    return create(Map.of(HIBERNATE_CONNECTION_PROP_KEY, config.getValue(GMS_CONFIG_SQL_URL_KEY),
      HIBERNATE_CONNECTION_USERNAME_KEY, config.getValue(GMS_CONFIG_SQL_USERNAME_KEY),
      HIBERNATE_CONNECTION_PW_KEY,
      Optional.ofNullable(config.getValue(GMS_CONFIG_SQL_PW_KEY))
        .orElseThrow(() -> new IllegalStateException(
          UNABLE_TO_RETRIEVE_THE_SQL_PW_ERROR)),
      HIBERNATE_C3P0_POOL_SIZE_KEY, config.getValue(GMS_CONFIG_C3P0_POOL_SIZE_KEY),
      HIBERNATE_FLUSH_MODE, FLUSH_MODE_VALUE));
  }

  /**
   * Creates an EntityManagerFactory using system configuration for an elevated user.
   *
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory createElev(SystemConfig config) {
    return create(Map.of(HIBERNATE_CONNECTION_PROP_KEY, config.getValue(GMS_CONFIG_SQL_URL_KEY),
      HIBERNATE_CONNECTION_USERNAME_KEY, config.getValue(GMS_CONFIG_SQL_ELEV_USERNAME_KEY),
      HIBERNATE_CONNECTION_PW_KEY,
      Optional.ofNullable(config.getValue(GMS_CONFIG_SQL_ELEV_PW_KEY))
        .orElseThrow(() -> new IllegalStateException(
          UNABLE_TO_RETRIEVE_THE_SQL_PW_ERROR)),
      HIBERNATE_C3P0_POOL_SIZE_KEY, config.getValue(GMS_CONFIG_C3P0_POOL_SIZE_KEY),
      HIBERNATE_FLUSH_MODE, FLUSH_MODE_VALUE));
  }

  /**
   * Creates an EntityManagerFactory with the specified property overrides. These are given directly to the JPA
   * provider; they can be used to override things like the URL of the database.
   *
   * @param propertiesOverrides a map of properties to override and their values
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory create(Map<String, String> propertiesOverrides) {
    Objects.requireNonNull(propertiesOverrides, "Property overrides cannot be null");
    try {
      return Persistence.createEntityManagerFactory(UNIT_NAME, propertiesOverrides);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + UNIT_NAME, e);
    }
  }

  public static void main(String[] args) {
    EntityManagerFactory emf = create();
    var entityManager = emf.createEntityManager();
    for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
      logger.debug(entityType.getName());
    }
  }
}

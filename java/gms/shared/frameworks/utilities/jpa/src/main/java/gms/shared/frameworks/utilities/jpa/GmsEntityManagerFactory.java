package gms.shared.frameworks.utilities.jpa;

import gms.shared.frameworks.systemconfig.SystemConfig;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility for creating EntityManagerFactory's for JPA. These know about all of the core Data Access
 * Objects for GMS.
 */
public class GmsEntityManagerFactory {
  private static final String GMS_CONFIG_SQL_PW_KEY = "sql_password";
  private static final String GMS_CONFIG_SQL_USERNAME_KEY = "sql_user";
  private static final String GMS_CONFIG_SQL_URL_KEY = "sql_url";
  private static final String GMS_CONFIG_C3P0_POOL_SIZE_KEY = "c3p0_connection_pool_size";

  private GmsEntityManagerFactory() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Creates an EntityManagerFactory with defaults.
   *
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory create(String unitName) {
    return GmsEntityManagerFactory.create(Map.of(), unitName);
  }

  /**
   * Creates an EntityManagerFactory using system configuration.
   *
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory create(SystemConfig config, String unitName) {
    return GmsEntityManagerFactory.create(Map.of(
      "hibernate.connection.url", config.getValue(GMS_CONFIG_SQL_URL_KEY),
      "hibernate.connection.username", config.getValue(GMS_CONFIG_SQL_USERNAME_KEY),
      "hibernate.connection.password",
      Optional.ofNullable(config.getValue(GMS_CONFIG_SQL_PW_KEY))
        .orElseThrow(() -> new IllegalStateException(
          "password not found")),
      "hibernate.c3p0.max_size", config.getValue(GMS_CONFIG_C3P0_POOL_SIZE_KEY)), unitName);
  }

  /**
   * Creates an EntityManagerFactory with the specified property overrides. These are given directly
   * to the JPA provider; they can be used to override things like the URL of the database.
   *
   * @param propertiesOverrides a map of properties to override and their values
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory create(Map<String, String> propertiesOverrides,
    String unitName) {
    Objects.requireNonNull(propertiesOverrides, "Property overrides cannot be null");
    try {
      return Persistence.createEntityManagerFactory(unitName, propertiesOverrides);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + unitName, e);
    }
  }
}

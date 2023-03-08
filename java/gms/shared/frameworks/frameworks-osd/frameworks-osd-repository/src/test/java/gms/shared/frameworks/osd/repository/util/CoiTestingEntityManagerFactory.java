package gms.shared.frameworks.osd.repository.util;

import gms.shared.frameworks.osd.repository.utils.CoiEntityManagerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.Map;

/**
 * Used to create EntityManagerFactory's for use in tests.
 */
public class CoiTestingEntityManagerFactory {

  private static final String UNIT_NAME = CoiEntityManagerFactory.UNIT_NAME;

  private static final Map<String, String> testProperties = Map.of(
    "hibernate.connection.driver_class", "org.postgresql.Driver",
    "hibernate.connection.url", "jdbc:postgresql://localhost:5432/gms",
    "hibernate.connection.username", "gms_soh_test_application",
    "hibernate.default_schema", "gms_soh_test",
    "hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect",
    "hibernate.hbm2ddl.auto", "create-drop",
    "hibernate.flushMode", "FLUSH_AUTO");

  /**
   * Creates an EntityManagerFactory for testing; connects to an in-memory database.
   *
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory createTesting() {
    try {
      return Persistence.createEntityManagerFactory(UNIT_NAME, testProperties);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + UNIT_NAME, e);
    }
  }
}

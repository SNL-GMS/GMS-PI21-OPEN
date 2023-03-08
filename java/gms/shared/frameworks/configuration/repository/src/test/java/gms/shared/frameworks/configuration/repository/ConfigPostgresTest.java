package gms.shared.frameworks.configuration.repository;

import gms.shared.utilities.db.test.utils.PostgresTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;

public abstract class ConfigPostgresTest extends PostgresTest {

  protected static EntityManagerFactory entityManagerFactory;

  @BeforeAll
  protected static void setUpPersistence() {
    final var jdbcUrl = container.getJdbcUrl() + "&reWriteBatchedInserts=true";
    Map<String, String> props = new HashMap<>(
      Map.ofEntries(
        Map.entry("hibernate.connection.driver_class", "org.postgresql.Driver"),
        Map.entry("hibernate.connection.url", jdbcUrl),
        Map.entry("hibernate.connection.username", GMS_DB_USER),
        Map.entry("hibernate.connection.password", GMS_DB_PASSFAKE),
        Map.entry("hibernate.default_schema", "gms_config"),
        Map.entry("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect"),
        Map.entry("hibernate.hbm2ddl.auto", "validate"),
        Map.entry("hibernate.flushMode", "FLUSH_AUTO"),
        Map.entry("hibernate.jdbc.batch_size", "50"),
        Map.entry("hibernate.order_inserts", "true"),
        Map.entry("hibernate.order_updates", "true"),
        Map.entry("hibernate.jdbc.batch_versioned_data", "true")
      ));
    try {
      entityManagerFactory = Persistence.createEntityManagerFactory("processing-cfg", props);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit processing-cfg", e);
    }

  }

  @AfterAll
  protected static void tearDownPersistence() {
    entityManagerFactory.close();
  }

}

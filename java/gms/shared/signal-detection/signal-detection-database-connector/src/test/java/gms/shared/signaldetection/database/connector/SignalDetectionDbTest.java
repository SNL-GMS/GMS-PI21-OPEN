package gms.shared.signaldetection.database.connector;

import gms.shared.utilities.bridge.database.connector.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.function.Executable;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("component")
abstract class SignalDetectionDbTest<T extends DatabaseConnector> {
  private static final ClassLoader classLoader = SignalDetectionDbTest.class.getClassLoader();
  private static final URL SIGNAL_DETECTION_DDL = getResource("signaldetection/signaldetection_gms_ddl.sql");
  private static final URL SIGNAL_DETECTION_ARRIVAL_DATA = getResource("signaldetection/data/signaldetection_gms_arrival.sql");
  private static final URL SIGNAL_DETECTION_WFDISC_DATA = getResource("signaldetection/data/signaldetection_gms_wfdisc.sql");
  private static final URL SIGNAL_DETECTION_ASSOC_DATA = getResource("signaldetection/data/signaldetection_gms_assoc.sql");
  private static final URL SIGNAL_DETECTION_AMPLITUDE_DATA = getResource("signaldetection/data/signaldetection_gms_amplitude.sql");

  private static EntityManagerFactory entityManagerFactory;

  protected T repository;

  @BeforeAll
  protected static void setUp() {

    final List<URL> signalDetectionSqlScripts = List.of(
      SIGNAL_DETECTION_DDL,
      SIGNAL_DETECTION_ARRIVAL_DATA,
      SIGNAL_DETECTION_WFDISC_DATA,
      SIGNAL_DETECTION_ASSOC_DATA,
      SIGNAL_DETECTION_AMPLITUDE_DATA);

    final String jdbcUrl = "jdbc:h2:mem:signaldetection_test;USER=GMS_GLOBAL;MODE=Oracle";
    final String initJdbcUrl = String.format("%s;INIT=%s", jdbcUrl, getInitScriptRunCommand(signalDetectionSqlScripts));

    Map<String, String> props = Map.ofEntries(
      Map.entry("hibernate.connection.driver_class", "org.h2.Driver"),
      Map.entry("hibernate.connection.url", initJdbcUrl),
      Map.entry("hibernate.dialect", "org.hibernate.dialect.H2Dialect"),
      Map.entry("hibernate.default_schema", "GMS_GLOBAL"),
      Map.entry("hibernate.hbm2ddl.auto", "none"),
      Map.entry("hibernate.flushMode", "FLUSH_AUTO"),
      Map.entry("hibernate.jdbc.batch_size", "50"),
      Map.entry("hibernate.order_inserts", "true"),
      Map.entry("hibernate.order_updates", "true"),
      Map.entry("hibernate.jdbc.batch_versioned_data", "true")
    );
    entityManagerFactory = Persistence.createEntityManagerFactory("gms_signal_detection_test", props);
    assertNotNull(entityManagerFactory);
    assertTrue(entityManagerFactory.isOpen());
  }

  @AfterAll
  protected static void tearDown() {
    entityManagerFactory.close();
    assertAll(
      () -> assertFalse(entityManagerFactory.isOpen())
    );

    entityManagerFactory = null;
    assertAll(
      () -> assertNull(entityManagerFactory)
    );
  }

  @BeforeEach
  public void testSetup() {
    repository = getRepository(entityManagerFactory);
  }

  protected abstract T getRepository(EntityManagerFactory entityManagerFactory);

  private static URL getResource(String resourceName) {
    final URL resource = classLoader.getResource(resourceName);
    if (resource == null) {
      throw new IllegalArgumentException(
        String.format("Requested resource was not found: '%s'", resourceName));
    }
    return resource;
  }

  private static String getInitScriptRunCommand(List<URL> cssSqlScripts) {
    return cssSqlScripts.stream()
      .map(s -> String.format("runscript from '%s'", s))
      .collect(Collectors.joining("\\;"));
  }

  protected void assertErrorThrown(Class<NullPointerException> expectedType,
    String expectedErrorMessage, Executable executable) {
    final NullPointerException exception = assertThrows(expectedType,
      executable);

    assertNotNull(exception);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}

package gms.shared.utilities.bridge.database;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.validation.PathValidation;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

/**
 * Utility for creating EntityManagerFactory's for JPA. These know about all of the core Data Access Objects for GMS.
 */
public class BridgedEntityManagerFactoryProvider {

  private static final Logger logger = LoggerFactory.getLogger(BridgedEntityManagerFactoryProvider.class);

  public static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
  public static final String HIBERNATE_C3P0_POOL_SIZE_KEY = "hibernate.c3p0.max_size";
  public static final String HIBERNATE_DEFAULT_SCHEMA_KEY = "hibernate.default_schema";
  public static final String HIBERNATE_FLUSH_MODE = "hibernate.flushMode";

  public static final String WALLET_LOCATION_PROPERTY_KEY = "oracle.net.wallet_location";
  public static final String TNS_LOCATION_PROPERTY_KEY = "oracle.net.tns_admin";

  public static final String TNS_ENTRY_LOCATION_CONFIG_KEY = "tns_entry_location";
  public static final String ORACLE_WALLET_LOCATION_CONFIG_KEY = "oracle_wallet_location";
  public static final String CONNECTION_POOL_SIZE_CONFIG_KEY = "c3p0_connection_pool_size";
  public static final String JDBC_URL_CONFIG_KEY = "jdbc_url";
  public static final String SCHEMA = "schema";
  private static final String WALLET_BASE_PATH = "/opt/gms/";


  private BridgedEntityManagerFactoryProvider(String hibernateSchemaName) {
  }

  /**
   * Create a {@link BridgedEntityManagerFactoryProvider} that connects to the database's
   * default schema.
   *
   * @return
   */
  public static BridgedEntityManagerFactoryProvider create() {
    return new BridgedEntityManagerFactoryProvider("");
  }

  /**
   * Create a {@link BridgedEntityManagerFactoryProvider} that connects to the database
   * using the specified schema name.
   *
   * @param hibernateSchemaName
   * @return
   */
  public static BridgedEntityManagerFactoryProvider create(String hibernateSchemaName) {
    Validate.isTrue(StringUtils.isNotBlank(hibernateSchemaName),
      "Schema name provided is missing or blank.");
    return new BridgedEntityManagerFactoryProvider(hibernateSchemaName);
  }

  /**
   * Creates an EntityManagerFactory using persistence unit name, jdbc url and system configuration.
   *
   * @param unitName persistence unit name
   * @param config system configuration
   * @return EntityManagerFactory
   */
  public EntityManagerFactory getEntityManagerFactory(String unitName, String jdbcUrl, SystemConfig config) {
    Objects.requireNonNull(unitName, "Persistence unit name cannot be null.");
    Objects.requireNonNull(jdbcUrl, "JDBC Url cannot be null.");
    Objects.requireNonNull(config, "SystemConfig cannot be null.");

    checkState(!unitName.isEmpty(), "Persistence unit name must not be empty");
    checkState(!jdbcUrl.isEmpty(), "JDBC Url must not be empty");

    String walletLocation = config.getValue(ORACLE_WALLET_LOCATION_CONFIG_KEY);
    Objects.requireNonNull(walletLocation, "oracle_wallet_location cannot be null.");

    String tnsLocation = config.getValue(TNS_ENTRY_LOCATION_CONFIG_KEY);
    Objects.requireNonNull(tnsLocation, "tns_entry_location cannot be null.");

    System.setProperty(WALLET_LOCATION_PROPERTY_KEY,
      config.getValue(ORACLE_WALLET_LOCATION_CONFIG_KEY));
    System.setProperty(TNS_LOCATION_PROPERTY_KEY, config.getValue(TNS_ENTRY_LOCATION_CONFIG_KEY));

    if (!walletExists(System.getProperty(WALLET_LOCATION_PROPERTY_KEY))) {
      logger.error("Could not find database wallet at {}",
        StructuredArguments
          .v("Wallet Location", System.getProperty(WALLET_LOCATION_PROPERTY_KEY)));
      throw new MissingResourceException("Database wallet not found", String.class.toString(),
        System.getProperty(WALLET_LOCATION_PROPERTY_KEY));
    }

    final Map<String, String> propertiesOverrides = new HashMap<>();
    logger.info("Creating emf for jdbc url {}", jdbcUrl);
    propertiesOverrides.put(JAVAX_PERSISTENCE_JDBC_URL, jdbcUrl);
    propertiesOverrides
      .put(HIBERNATE_C3P0_POOL_SIZE_KEY, config.getValue(CONNECTION_POOL_SIZE_CONFIG_KEY));
    propertiesOverrides.put(HIBERNATE_FLUSH_MODE, "FLUSH_AUTO");

    try {
      final String schemaName = config.getValue(SCHEMA);
      if (schemaName != null && !schemaName.isBlank()) {
        logger.info("Creating EMF for schema {}", schemaName);
        propertiesOverrides.put(HIBERNATE_DEFAULT_SCHEMA_KEY, schemaName);
      }
    } catch (MissingResourceException e) {
      logger.warn("No default schema name found.");
    }
    return getEntityManagerFactory(unitName, propertiesOverrides);
  }

  /**
   * Creates an EntityManagerFactory using system configuration.
   *
   * @param unitName persistence unit name
   * @param config system configuration
   * @return EntityManagerFactory
   */
  public EntityManagerFactory getEntityManagerFactory(String unitName, SystemConfig config) {
    Objects.requireNonNull(config, "SystemConfig cannot be null.");

    String walletLocation = config.getValue(ORACLE_WALLET_LOCATION_CONFIG_KEY);
    Objects.requireNonNull(walletLocation, "oracle_wallet_location cannot be null.");

    String tnsLocation = config.getValue(TNS_ENTRY_LOCATION_CONFIG_KEY);
    Objects.requireNonNull(tnsLocation, "tns_entry_location cannot be null.");

    System.setProperty(WALLET_LOCATION_PROPERTY_KEY,
      config.getValue(ORACLE_WALLET_LOCATION_CONFIG_KEY));
    System.setProperty(TNS_LOCATION_PROPERTY_KEY, config.getValue(TNS_ENTRY_LOCATION_CONFIG_KEY));

    if (!walletExists(System.getProperty(WALLET_LOCATION_PROPERTY_KEY))) {
      logger.error("Could not find database wallet at {}",
        StructuredArguments
          .v("Wallet Location", System.getProperty(WALLET_LOCATION_PROPERTY_KEY)));
      throw new MissingResourceException("Database wallet not found", String.class.toString(),
        System.getProperty(WALLET_LOCATION_PROPERTY_KEY));
    }

    final Map<String, String> propertiesOverrides = new HashMap<>();
    try {
      propertiesOverrides.put(JAVAX_PERSISTENCE_JDBC_URL, config.getValue(JDBC_URL_CONFIG_KEY));
      logger.info("Connecting with URL {}", config.getValue(JDBC_URL_CONFIG_KEY));
    } catch (MissingResourceException e) {
      logger.warn("Could not load property for " + JDBC_URL_CONFIG_KEY + ". Ensure it is defined in the persistence.xml");
    }
    propertiesOverrides
      .put(HIBERNATE_C3P0_POOL_SIZE_KEY, config.getValue(CONNECTION_POOL_SIZE_CONFIG_KEY));
    propertiesOverrides.put(HIBERNATE_FLUSH_MODE, "FLUSH_AUTO");

    try {
      final String schemaName = config.getValue(SCHEMA);
      if (schemaName != null && !schemaName.isBlank()) {
        propertiesOverrides.put(HIBERNATE_DEFAULT_SCHEMA_KEY, schemaName);
        logger.info("Overriding default schema with {}", schemaName);
      }
    } catch (MissingResourceException e) {
      logger.warn("No default schema name found.");
    }
    return getEntityManagerFactory(unitName, propertiesOverrides);
  }


  /**
   * Creates an EntityManagerFactory with the specified property overrides. These are given directly to the JPA
   * provider; they can be used to override things like the URL of the database.
   *
   * @param unitName Persistence unit name
   * @param propertiesOverrides a map of properties to override and their values
   * @return EntityManagerFactory
   */
  public static EntityManagerFactory getEntityManagerFactory(String unitName, Map<String, String> propertiesOverrides) {
    Objects.requireNonNull(propertiesOverrides, "Property overrides cannot be null");
    try {
      return Persistence.createEntityManagerFactory(unitName, propertiesOverrides);
    } catch (PersistenceException e) {
      throw new IllegalArgumentException("Could not create persistence unit " + unitName, e);
    }
  }

  public static boolean walletExists(String walletPath) {

    try {
      var pathOfWallet = PathValidation.getValidatedPath(walletPath, WALLET_BASE_PATH);
      return Files.exists(pathOfWallet)
        && Files.exists(pathOfWallet.resolve("cwallet.sso"))
        && Files.exists(pathOfWallet.resolve("ewallet.p12"));
    } catch (IllegalArgumentException e) {

      logger.warn("Path manipulation detected, returning wallet does not exist.");
      return false;
    }
  }
}

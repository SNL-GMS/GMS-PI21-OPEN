package gms.shared.emf.stationdefinition;

import com.mchange.v2.c3p0.DataSources;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.validation.PathValidation;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;

public class OracleSpringUtilites {

  private OracleSpringUtilites() {
  }

  private static final Logger logger = LoggerFactory.getLogger(OracleSpringUtilites.class);

  public static final String HIBERNATE_C3P0_POOL_MIN_SIZE_KEY = "hibernate.c3p0.min_size";
  public static final String CONNECTION_POOL_SIZE_CONFIG_KEY = "c3p0_connection_pool_size";
  public static final String HIBERNATE_C3P0_POOL_SIZE_KEY = "hibernate.c3p0.max_size";
  public static final String HIBERNATE_C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
  public static final String HIBERNATE_C3P0_TIMEOUT = "hibernate.c3p0.timeout";
  public static final String HIBERNATE_FLUSH_MODE = "hibernate.flushMode";
  public static final String HIBERNATE_DIALECT = "hibernate.dialect";
  public static final String HIBERNATE_AUTO = "hibernate.hbm2ddl.auto";
  public static final String HIBERNATE_TIMEZONE = "hibernate.jdbc.time_zone";
  public static final String HIBERNATE_RETRY_ATTEMPTS = "hibernate.c3p0.acquireRetryAttempts";
  public static final String HIBERNATE_SYNONYMS = "hibernate.synonyms";
  public static final String HIBERNATE_UNRETURNED_CONNECTION_TIMEOUT = "hibernate.c3p0.unreturnedConnectionTimeout";
  public static final String HIBERNATE_JDBC_BATCH_SIZE = "hibernate.jdbc.batch_size";
  public static final String HIBERNATE_ORDER_INSERTS = "hibernate.order_inserts";
  public static final String HIBERNATE_ORDER_UPDATES = "hibernate.order_updates";
  public static final String HIBERNATE_JDBC_BATCH_VERSIONED_DATA = "hibernate.jdbc.batch_versioned_data";
  public static final String WALLET_LOCATION_PROPERTY_KEY = "oracle.net.wallet_location";
  public static final String TNS_LOCATION_PROPERTY_KEY = "oracle.net.tns_admin";
  public static final String TNS_ENTRY_LOCATION_CONFIG_KEY = "tns_entry_location";
  public static final String ORACLE_WALLET_LOCATION_CONFIG_KEY = "oracle_wallet_location";
  public static final String JDBC_URL_CONFIG_KEY = "jdbc_url";
  private static final String WALLET_BASE_PATH = "/opt/gms/";


  public static LocalContainerEntityManagerFactoryBean getEntityManagerFactory(SystemConfig systemConfig,
    DataSource dataSource, String persistenceUnitName) {

    var emf = new LocalContainerEntityManagerFactoryBean();

    emf.setPersistenceUnitName(persistenceUnitName);
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setDataSource(dataSource);
    emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    emf.setJpaProperties(jpaHibernateProperties(systemConfig));

    return emf;
  }

  public static DataSource getDataSource(SystemConfig systemConfig) {
    return getDataSource(systemConfig, systemConfig.getValue(JDBC_URL_CONFIG_KEY));
  }

  public static DataSource getDataSource(SystemConfig systemConfig,
    String jdbcUrl) {

    String walletLocation = systemConfig.getValue(ORACLE_WALLET_LOCATION_CONFIG_KEY);
    Objects.requireNonNull(walletLocation, "oracle_wallet_location cannot be null.");

    String tnsLocation = systemConfig.getValue(TNS_ENTRY_LOCATION_CONFIG_KEY);
    Objects.requireNonNull(tnsLocation, "tns_entry_location cannot be null.");

    System.setProperty(WALLET_LOCATION_PROPERTY_KEY, walletLocation);
    System.setProperty(TNS_LOCATION_PROPERTY_KEY, tnsLocation);

    if (!walletExists(System.getProperty(WALLET_LOCATION_PROPERTY_KEY))) {

      logger.error("Could not find database wallet at Wallet Location: {}",
        System.getProperty(WALLET_LOCATION_PROPERTY_KEY));
      throw new MissingResourceException("Database wallet not found", String.class.toString(),
        System.getProperty(WALLET_LOCATION_PROPERTY_KEY));
    }

    DataSource returnSource;
    var dataSource = new DriverManagerDataSource();

    dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
    dataSource.setUrl(jdbcUrl);

    try {
      returnSource = DataSources.pooledDataSource(dataSource);
    } catch (SQLException e) {
      logger.error("Pooled connection could not be created, will use unpooled, " +
        "this will affect db connection speed", e);
      returnSource = dataSource;
    }

    return returnSource;
  }

  private static Properties jpaHibernateProperties(SystemConfig systemConfig) {

    var properties = new Properties();

    properties.put(HIBERNATE_C3P0_POOL_MIN_SIZE_KEY, 1);
    properties.put(HIBERNATE_C3P0_POOL_SIZE_KEY, systemConfig.getValue(CONNECTION_POOL_SIZE_CONFIG_KEY));
    properties.put(HIBERNATE_C3P0_ACQUIRE_INCREMENT, 2);
    properties.put(HIBERNATE_C3P0_TIMEOUT, 10);
    properties.put(HIBERNATE_FLUSH_MODE, "FLUSH_AUTO");
    properties.put(HIBERNATE_DIALECT, "org.hibernate.dialect.Oracle12cDialect");
    properties.put(HIBERNATE_AUTO, "none");
    properties.put(HIBERNATE_TIMEZONE, "UTC");
    properties.put(HIBERNATE_RETRY_ATTEMPTS, 2);
    properties.put(HIBERNATE_SYNONYMS, true);

    // Useful for debugging connection leaks: time out and give a stack trace if a connection cannot be acquired in time
    properties.put(HIBERNATE_UNRETURNED_CONNECTION_TIMEOUT, 300);

    // enabling batch inserts
    properties.put(HIBERNATE_JDBC_BATCH_SIZE, 50);
    properties.put(HIBERNATE_ORDER_INSERTS, true);
    properties.put(HIBERNATE_ORDER_UPDATES, true);
    properties.put(HIBERNATE_JDBC_BATCH_VERSIONED_DATA, true);

    return properties;
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

package gms.shared.event.repository.config;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.validation.PathValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.util.MissingResourceException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides new {@link DataSource}s with the specified jdbc url and schema
 */
@Configuration
@ComponentScan(basePackages = "gms.shared.spring",
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "gms\\.shared\\.spring\\.persistence\\..*"))
public class DataSourceConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);

  public static final String WALLET_LOCATION_PROPERTY_KEY = "oracle.net.wallet_location";
  public static final String TNS_LOCATION_PROPERTY_KEY = "oracle.net.tns_admin";
  public static final String TNS_ENTRY_LOCATION_CONFIG_KEY = "tns_entry_location";
  public static final String ORACLE_WALLET_LOCATION_CONFIG_KEY = "oracle_wallet_location";
  private static final String WALLET_BASE_PATH = "/opt/gms/";

  private final SystemConfig systemConfig;

  /**
   * @param systemConfig used to load {@link DataSource} configuration
   */
  @Autowired
  public DataSourceConfiguration(SystemConfig systemConfig) {
    this.systemConfig = systemConfig;
  }

  /**
   * Creates new DataSources with the specified jdbc url and schema
   *
   * @param jdbcUrl The jdbc url the DataSource will connect to
   * @return New DataSource using the specified jdbc url and schema
   */
  @Bean(name = "event-dataSource")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public DataSource dataSource(String jdbcUrl) {
    logger.info("Loading event-dataSource with jdbcUrl [" + jdbcUrl + "]");

    String walletLocation = systemConfig.getValue(ORACLE_WALLET_LOCATION_CONFIG_KEY);
    checkNotNull(walletLocation, "oracle_wallet_location cannot be null.");

    String tnsLocation = systemConfig.getValue(TNS_ENTRY_LOCATION_CONFIG_KEY);
    checkNotNull(tnsLocation, "tns_entry_location cannot be null.");

    var dataSource = new DriverManagerDataSource();

    System.setProperty(WALLET_LOCATION_PROPERTY_KEY, walletLocation);
    System.setProperty(TNS_LOCATION_PROPERTY_KEY, tnsLocation);
    if (!walletExists(System.getProperty(WALLET_LOCATION_PROPERTY_KEY))) {
      logger.error("Could not find database wallet at Wallet Location: {}",
        System.getProperty(WALLET_LOCATION_PROPERTY_KEY));
      throw new MissingResourceException("Database wallet not found", String.class.toString(),
        System.getProperty(WALLET_LOCATION_PROPERTY_KEY));
    }

    dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
    dataSource.setUrl(jdbcUrl);

    return dataSource;
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

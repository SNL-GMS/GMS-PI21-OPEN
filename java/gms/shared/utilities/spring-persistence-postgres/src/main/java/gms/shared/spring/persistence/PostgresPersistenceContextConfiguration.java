package gms.shared.spring.persistence;

import com.mchange.v2.c3p0.DataSources;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class PostgresPersistenceContextConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(PostgresPersistenceContextConfiguration.class);

  public static final String HIBERNATE_C3P0_POOL_MIN_SIZE_KEY = "hibernate.c3p0.min_size";
  public static final String HIBERNATE_C3P0_POOL_MAX_SIZE_KEY = "hibernate.c3p0.max_size";
  public static final String HIBERNATE_C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
  public static final String HIBERNATE_C3P0_TIMEOUT = "hibernate.c3p0.timeout";
  public static final String HIBERNATE_DIALECT = "hibernate.dialect";
  public static final String HIBERNATE_AUTO = "hibernate.hbm2ddl.auto";
  public static final String HIBERNATE_TIMEZONE = "hibernate.jdbc.time_zone";
  public static final String HIBERNATE_UNRETURNED_CONNECTION_TIMEOUT = "hibernate.c3p0.unreturnedConnectionTimeout";
  public static final String HIBERNATE_DEBUG_UNRETURNED_CONNECTION_STACK_TRACES = "hibernate.c3p0.debugUnreturnedConnectionStackTraces";
  public static final String HIBERNATE_SHOW_SQL = "hibernate.show_sql";
  public static final String HIBERNATE_CONNECTION_AUTOCOMMIT = "hibernate.connection.autocommit";
  public static final String HIBERNATE_FLUSHMODE = "hibernate.flushmode";

  // Postgres system configuration keys
  public static final String CONNECTION_POOL_SIZE_CONFIG_KEY = "c3p0_connection_pool_size";
  private static final String POSTGRES_SCHEMA_KEY = "schema";
  private static final String POSTGRES_URL_CONFIG_KEY = "sql_url";
  private static final String POSTGRES_SQL_USER_KEY = "sql_user";
  private static final String POSTGRES_SQL_PW_KEY = "sql_password";

  // Postgres driver and dialect
  private static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";
  private static final String POSTGRES_DIALECT = "org.hibernate.dialect.PostgreSQL95Dialect";

  @Value("${persistenceUnitName}")
  private String persistenceUnitName;

  //https://stackoverflow.com/questions/18882683/how-to-mention-persistenceunitname-when-packagestoscan-property
  @Bean
  public DataSource dataSource(@Autowired SystemConfig systemConfig) {

    DataSource returnSource;
    var dataSource = new DriverManagerDataSource();

    dataSource.setDriverClassName(POSTGRES_DRIVER_CLASS);
    dataSource.setUrl(systemConfig.getValue(POSTGRES_URL_CONFIG_KEY));
    dataSource.setUsername(systemConfig.getValue(POSTGRES_SQL_USER_KEY));
    dataSource.setPassword(systemConfig.getValue(POSTGRES_SQL_PW_KEY));

    try {
      final String schemaName = systemConfig.getValue(POSTGRES_SCHEMA_KEY);
      if (schemaName != null && !schemaName.isBlank()) {
        dataSource.setSchema(schemaName);
      }
    } catch (MissingResourceException e) {
      logger.warn("No default schema name found.");
    }

    try {
      returnSource = DataSources.pooledDataSource(dataSource);
    } catch (SQLException e) {
      logger.error("Pooled connection could not be created, will use unpooled, " +
        "this will affect db connection speed", e);
      returnSource = dataSource;
    }

    return returnSource;
  }

  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean(name = "entityManagerFactory")
  @Primary
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired SystemConfig systemConfig,
    @Autowired DataSource dataSource) {
    var emf = new LocalContainerEntityManagerFactoryBean();

    emf.setPersistenceUnitName(persistenceUnitName);
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setDataSource(dataSource);
    emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    emf.setJpaProperties(jpaHibernateProperties(systemConfig));

    return emf;
  }

  private Properties jpaHibernateProperties(SystemConfig systemConfig) {
    var properties = new Properties();
    properties.put(HIBERNATE_C3P0_POOL_MIN_SIZE_KEY, 1);
    properties.put(HIBERNATE_C3P0_POOL_MAX_SIZE_KEY, systemConfig.getValue(CONNECTION_POOL_SIZE_CONFIG_KEY));
    properties.put(HIBERNATE_C3P0_ACQUIRE_INCREMENT, 2);
    properties.put(HIBERNATE_C3P0_TIMEOUT, 30);
    properties.put(HIBERNATE_DIALECT, POSTGRES_DIALECT);
    properties.put(HIBERNATE_AUTO, "none");
    properties.put(HIBERNATE_TIMEZONE, "UTC");
    properties.put(HIBERNATE_UNRETURNED_CONNECTION_TIMEOUT, 30);
    properties.put(HIBERNATE_DEBUG_UNRETURNED_CONNECTION_STACK_TRACES, "true");
    properties.put(HIBERNATE_SHOW_SQL, "false");
    properties.put(HIBERNATE_CONNECTION_AUTOCOMMIT, "false");
    properties.put(HIBERNATE_FLUSHMODE, "FLUSH_AUTO");

    return properties;
  }
}

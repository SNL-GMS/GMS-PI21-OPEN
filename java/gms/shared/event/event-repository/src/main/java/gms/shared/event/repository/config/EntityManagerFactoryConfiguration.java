package gms.shared.event.repository.config;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Provides new {@link javax.persistence.EntityManagerFactory} instances
 */
@Configuration
@ComponentScan(basePackages = "gms.shared.spring",
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "gms\\.shared\\.spring\\.persistence\\..*"))
public class EntityManagerFactoryConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(EntityManagerFactoryConfiguration.class);

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
  public static final String HIBERNATE_NAMING_STRATEGY = "hibernate.physical_naming_strategy";
  public static final String HIBERNATE_UNRETURNED_CONNECTION_TIMEOUT = "hibernate.c3p0.unreturnedConnectionTimeout";
  public static final String HIBERNATE_JDBC_BATCH_SIZE = "hibernate.jdbc.batch_size";
  public static final String HIBERNATE_ORDER_INSERTS = "hibernate.order_inserts";
  public static final String HIBERNATE_ORDER_UPDATES = "hibernate.order_updates";
  public static final String HIBERNATE_JDBC_BATCH_VERSIONED_DATA = "hibernate.jdbc.batch_versioned_data";

  private final SystemConfig systemConfig;
  private final ObjectProvider<DataSource> dataSourceObjectProvider;

  /**
   * @param systemConfig Configuration values for {@link javax.persistence.EntityManagerFactory}
   * @param dataSourceObjectProvider Provides new DataSources using a specified jdbc url and schema
   */
  @Autowired
  public EntityManagerFactoryConfiguration(SystemConfig systemConfig,
    @Qualifier("event-dataSource") ObjectProvider<DataSource> dataSourceObjectProvider) {
    this.systemConfig = systemConfig;
    this.dataSourceObjectProvider = dataSourceObjectProvider;
  }

  /**
   * Provides new {@link javax.persistence.EntityManagerFactory}s using the specified jdbc url
   *
   * @param jdbcUrl jdbc url the EntityManagerFactory will connect to
   * @return new EntityManagerFactory using the provided schema
   */
  @Bean(name = "event-entityManagerFactory")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public EntityManagerFactory entityManagerFactory(
    String jdbcUrl,
    String persistenceUnitName) {

    var emf = new LocalContainerEntityManagerFactoryBean();

    logger.info("Loading event-entityManagerfactory with jdbcUrl [" + jdbcUrl + "]");

    var dataSource = dataSourceObjectProvider.getObject(jdbcUrl);

    emf.setPersistenceUnitName(persistenceUnitName);
    emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    emf.setDataSource(dataSource);
    emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);
    emf.setJpaProperties(jpaHibernateProperties(systemConfig));

    emf.afterPropertiesSet();
    return emf.getNativeEntityManagerFactory();
  }

  private Properties jpaHibernateProperties(SystemConfig systemConfig) {

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

}

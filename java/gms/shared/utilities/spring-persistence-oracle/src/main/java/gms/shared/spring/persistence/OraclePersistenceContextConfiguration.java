package gms.shared.spring.persistence;

import gms.shared.emf.stationdefinition.OracleSpringUtilites;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import gms.shared.utilities.javautilities.objectmapper.LivenessException;
import gms.shared.utilities.javautilities.objectmapper.OracleLivenessCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class OraclePersistenceContextConfiguration {

  @Value("${persistenceUnitName}")
  private String persistenceUnitName;

  //https://stackoverflow.com/questions/18882683/how-to-mention-persistenceunitname-when-packagestoscan-property
  @Bean
  public DataSource dataSource(@Autowired SystemConfig systemConfig) throws LivenessException {

    DatabaseLivenessCheck livenessCheck = OracleLivenessCheck.create(systemConfig);
    if (!livenessCheck.isLive()) {
      throw new LivenessException();
    }

    return OracleSpringUtilites.getDataSource(systemConfig);
  }

  @Bean(name = "transactionManager")
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Autowired SystemConfig systemConfig,
    @Autowired DataSource dataSource) {

    return OracleSpringUtilites.getEntityManagerFactory(systemConfig, dataSource, persistenceUnitName);
  }


}

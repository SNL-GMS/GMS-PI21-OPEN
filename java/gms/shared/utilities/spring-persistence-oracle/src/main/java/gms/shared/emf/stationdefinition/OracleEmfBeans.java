package gms.shared.emf.stationdefinition;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import gms.shared.utilities.javautilities.objectmapper.LivenessException;
import gms.shared.utilities.javautilities.objectmapper.OracleLivenessCheck;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

import static gms.shared.emf.stationdefinition.OracleSpringUtilites.JDBC_URL_CONFIG_KEY;

@Configuration
public class OracleEmfBeans {

  @Value("${globalJdbcUrl:#{null}}")
  private String globalJdbcUrl;

  @Value("${persistenceUnitName}")
  private String persistenceUnitName;

  @Bean
  public DatabaseLivenessCheck databaseLivenessCheck(SystemConfig systemConfig) {
    return OracleLivenessCheck.create(systemConfig);
  }

  //default data source used for station definition and waveform
  @Bean(name = "defaultDataSource")
  public DataSource dataSource(SystemConfig systemConfig,
    DatabaseLivenessCheck livenessCheck) throws LivenessException {

    if (!livenessCheck.isLive()) {
      throw new LivenessException();
    }

    if (globalJdbcUrl == null) {

      //changes to jdbc url are made to waveform and staion defnition configs
      if (!"waveform-manager".equals(systemConfig.getComponentName()) && !"station-definition".equals(systemConfig.getComponentName())) {
        var stationDefinitionConfig = SystemConfig.create("station-definition");
        return OracleSpringUtilites.getDataSource(systemConfig, stationDefinitionConfig.getValue(JDBC_URL_CONFIG_KEY));
      }

      return OracleSpringUtilites.getDataSource(systemConfig);
    }

    return OracleSpringUtilites.getDataSource(systemConfig, globalJdbcUrl);
  }

  //default emf for waveform and station definition
  @Bean(name = "entityManagerFactory")
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
    @Qualifier("defaultDataSource") DataSource dataSource,
    SystemConfig systemConfig) {

    return OracleSpringUtilites.getEntityManagerFactory(systemConfig, dataSource, persistenceUnitName);
  }


}

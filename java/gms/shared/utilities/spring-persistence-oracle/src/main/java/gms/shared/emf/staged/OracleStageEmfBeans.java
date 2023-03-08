package gms.shared.emf.staged;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

import static gms.shared.emf.stationdefinition.OracleSpringUtilites.getDataSource;
import static gms.shared.emf.stationdefinition.OracleSpringUtilites.getEntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class OracleStageEmfBeans {

  private static final Logger logger = LoggerFactory.getLogger(OracleStageEmfBeans.class);

  @Value("${signalDetectionPersistenceUnitName}")
  private String signalDetectionPersistenceUnitName;

  @Bean
  public EntityManagerFactoriesByStageId stageIdEmfMap(
    Map<WorkflowDefinitionId, String> databaseAccountsByStage, SystemConfig systemConfig,
    DatabaseLivenessCheck databaseLivenessCheck) {

    if (!databaseLivenessCheck.isLive()) {
      logger.info("Could not establish database liveness.  Exiting.");
      System.exit(1);
    }

    var currMap = databaseAccountsByStage.keySet().stream()
      .collect(ImmutableMap.toImmutableMap(Functions.identity(),
        stage -> {
          var emf = getEntityManagerFactory(
            systemConfig,
            getDataSource(systemConfig, databaseAccountsByStage.get(stage)),
            signalDetectionPersistenceUnitName
          );
          emf.afterPropertiesSet();
          return emf.getNativeEntityManagerFactory();
        }
      ));

    return EntityManagerFactoriesByStageId.builder()
      .setStageIdEmfMap(currMap)
      .build();
  }

}

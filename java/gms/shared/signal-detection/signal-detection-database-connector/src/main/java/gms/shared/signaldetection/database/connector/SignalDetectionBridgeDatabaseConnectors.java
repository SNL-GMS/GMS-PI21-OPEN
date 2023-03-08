package gms.shared.signaldetection.database.connector;

import com.google.common.collect.ImmutableMap;
import gms.shared.emf.staged.EntityManagerFactoriesByStageId;
import gms.shared.signaldetection.database.connector.config.SignalDetectionBridgeDefinition;
import gms.shared.utilities.bridge.database.connector.BridgedDatabaseConnectors;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;

@Component
public class SignalDetectionBridgeDatabaseConnectors extends BridgedDatabaseConnectors {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectionBridgeDatabaseConnectors.class);

  @Autowired
  public SignalDetectionBridgeDatabaseConnectors(
    SignalDetectionBridgeDefinition signalDetectionBridgeDefinition,
    ObjectProvider<AmplitudeDatabaseConnector> amplitudeDatabaseConnectorProvider,
    ObjectProvider<ArrivalDatabaseConnector> arrivalDatabaseConnectorProvider,
    ObjectProvider<AssocDatabaseConnector> assocDatabaseConnectorProvider,
    EntityManagerFactoriesByStageId emfByStageId,
    DatabaseLivenessCheck databaseLivenessCheck) {
    super();

    if (!databaseLivenessCheck.isLive()) {
      logger.info("Could not establish database liveness.  Exiting.");
      System.exit(1);
    }
    logger.info("Connection to database successful");

    var currentStageEmfMap = emfByStageId.getStageIdEmfMap();

    // Initialize the connectors using signal detection bridge definition and entity manager factory provider
    initializeDatabaseConnectors(signalDetectionBridgeDefinition,
      amplitudeDatabaseConnectorProvider, arrivalDatabaseConnectorProvider, assocDatabaseConnectorProvider,
      currentStageEmfMap);
  }

  private void initializeDatabaseConnectors(
    SignalDetectionBridgeDefinition signalDetectionBridgeDefinition,
    ObjectProvider<AmplitudeDatabaseConnector> amplitudeDatabaseConnectorProvider,
    ObjectProvider<ArrivalDatabaseConnector> arrivalDatabaseConnectorProvider,
    ObjectProvider<AssocDatabaseConnector> assocDatabaseConnectorProvider,
    ImmutableMap<WorkflowDefinitionId, EntityManagerFactory> currentStageEmfMap) {

    // loop through ordered stages and create maps of signal detection connectors for current/previous stages
    var orderedStages = signalDetectionBridgeDefinition.getOrderedStages();

    // create current stage database connectors
    orderedStages.forEach(stageId -> {
      var stageName = stageId.getName();

      var ampConnector = amplitudeDatabaseConnectorProvider.getObject(currentStageEmfMap.get(stageId));
      var arrivalConnector = arrivalDatabaseConnectorProvider.getObject(currentStageEmfMap.get(stageId));
      var assocConnector = assocDatabaseConnectorProvider.getObject(currentStageEmfMap.get(stageId));

      addConnectorForCurrentStage(stageName, ampConnector);
      addConnectorForCurrentStage(stageName, arrivalConnector);
      addConnectorForCurrentStage(stageName, assocConnector);

      var ind = orderedStages.indexOf(stageId);

      if (ind < orderedStages.size() - 1) {

        addConnectorForPreviousStage(orderedStages.get(ind + 1).getName(), ampConnector);
        addConnectorForPreviousStage(orderedStages.get(ind + 1).getName(), arrivalConnector);
        addConnectorForPreviousStage(orderedStages.get(ind + 1).getName(), assocConnector);

      }
    });
  }

  @Override
  public <T> Class<?> getClassForConnector(T databaseConnector) {
    Class<?> connectorClass;
    if (databaseConnector instanceof AmplitudeDatabaseConnector) {
      connectorClass = AmplitudeDatabaseConnector.class;
    } else if (databaseConnector instanceof ArrivalDatabaseConnector) {
      connectorClass = ArrivalDatabaseConnector.class;
    } else if (databaseConnector instanceof AssocDatabaseConnector) {
      connectorClass = AssocDatabaseConnector.class;
    } else {
      throw new IllegalArgumentException(String.format("Connector type [%s] has not been registered in %s",
        databaseConnector.getClass().getSimpleName(), this.getClass().getSimpleName()));
    }

    return connectorClass;
  }
}

package gms.shared.event.repository.connector;

import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.utilities.bridge.database.connector.BridgedDatabaseConnectors;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.Optional;

/**
 * Establishes database connectors for the event bridge
 */
@Component
public class EventBridgeDatabaseConnectors extends BridgedDatabaseConnectors {

  private static final Logger logger = LoggerFactory.getLogger(EventBridgeDatabaseConnectors.class);

  @Autowired
  public EventBridgeDatabaseConnectors(
    @Qualifier("event-entityManagerFactory") ObjectProvider<EntityManagerFactory> entityManagerFactoryProvider,
    ObjectProvider<EventDatabaseConnector> eventDatabaseConnectorProvider,
    ObjectProvider<EventControlDatabaseConnector> eventControlDatabaseConnectorProvider,
    ObjectProvider<OriginDatabaseConnector> originDatabaseConnectorProvider,
    ObjectProvider<OriginErrDatabaseConnector> originErrDatabaseConnectorProvider,
    ObjectProvider<GaTagDatabaseConnector> gaTagDatabaseConnectorProvider,
    @Qualifier("event-assocDatabaseConnector") ObjectProvider<AssocDatabaseConnector> assocDatabaseConnectorProvider,
    ObjectProvider<NetMagDatabaseConnector> netMagDatabaseConnectorObjectProvider,
    ObjectProvider<StaMagDatabaseConnector> staMagDatabaseConnectorsProvider,
    ObjectProvider<ArInfoDatabaseConnector> arInfoDatabaseConnectorObjectProvider,
    EventBridgeDefinition eventBridgeDefinition,
    DatabaseLivenessCheck livenessCheck) {

    if (!livenessCheck.isLive()) {
      logger.info("Could not establish database liveness.  Exiting.");
      System.exit(1);
    }
    logger.info("Connection to database successful");

    eventBridgeDefinition.getDatabaseUrlByStage().keySet().forEach(stage -> {
      //create current stage database connectors
      Optional.ofNullable(eventBridgeDefinition.getDatabaseUrlByStage().get(stage))
        .ifPresentOrElse(databaseUrl -> {
          var stageName = stage.getName();
          var currentStageEntityManagerFactory = entityManagerFactoryProvider.getObject(databaseUrl, "gms_event");
          var currentStageEntityManager = currentStageEntityManagerFactory.createEntityManager();
          addConnectorForCurrentStage(stageName, eventDatabaseConnectorProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, eventControlDatabaseConnectorProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, originDatabaseConnectorProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, originErrDatabaseConnectorProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, gaTagDatabaseConnectorProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, assocDatabaseConnectorProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, netMagDatabaseConnectorObjectProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, staMagDatabaseConnectorsProvider.getObject(currentStageEntityManager));
          addConnectorForCurrentStage(stageName, arInfoDatabaseConnectorObjectProvider.getObject(currentStageEntityManager));
        }, () -> logger.warn("No URL mapping found for stage [{}], verify configuration is correct.", stage));

      // create previous stage database connectors
      Optional.ofNullable(eventBridgeDefinition.getPreviousDatabaseUrlByStage().get(stage))
        .ifPresentOrElse(databaseUrl -> {
          var stageName = stage.getName();
          var previousStageEntityManagerFactory = entityManagerFactoryProvider.getObject(databaseUrl, "gms_event_prev");
          var previousStageEntityManager = previousStageEntityManagerFactory.createEntityManager();
          addConnectorForPreviousStage(stageName, eventDatabaseConnectorProvider.getObject(previousStageEntityManager));
          addConnectorForPreviousStage(stageName, originDatabaseConnectorProvider.getObject(previousStageEntityManager));
          addConnectorForPreviousStage(stageName, originErrDatabaseConnectorProvider.getObject(previousStageEntityManager));
          addConnectorForPreviousStage(stageName, assocDatabaseConnectorProvider.getObject(previousStageEntityManager));
        }, () -> logger.warn("No URL mapping found for stage [{}]. Verify configuration is correct if this stage is expected to have a previous stage DB.", stage));
    });
  }

  @Override
  public <T> Class<?> getClassForConnector(T databaseConnector) {

    Class<?> connectorClass;
    if (databaseConnector instanceof ArInfoDatabaseConnector) {
      connectorClass = ArInfoDatabaseConnector.class;
    } else if (databaseConnector instanceof AssocDatabaseConnector) {
      connectorClass = AssocDatabaseConnector.class;
    } else if (databaseConnector instanceof EventDatabaseConnector) {
      connectorClass = EventDatabaseConnector.class;
    } else if (databaseConnector instanceof EventControlDatabaseConnector) {
      connectorClass = EventControlDatabaseConnector.class;
    } else if (databaseConnector instanceof GaTagDatabaseConnector) {
      connectorClass = GaTagDatabaseConnector.class;
    } else if (databaseConnector instanceof NetMagDatabaseConnector) {
      connectorClass = NetMagDatabaseConnector.class;
    } else if (databaseConnector instanceof OriginDatabaseConnector) {
      connectorClass = OriginDatabaseConnector.class;
    } else if (databaseConnector instanceof OriginErrDatabaseConnector) {
      connectorClass = OriginErrDatabaseConnector.class;
    } else if (databaseConnector instanceof StaMagDatabaseConnector) {
      connectorClass = StaMagDatabaseConnector.class;
    } else {
      throw new IllegalArgumentException(String.format("Connector type [%s] has not been registered in %s",
        databaseConnector.getClass().getSimpleName(), this.getClass().getSimpleName()));
    }
    return connectorClass;
  }
}

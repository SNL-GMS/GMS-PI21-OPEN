package gms.shared.event.repository.factory;

import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

/**
 * Provides {@link OriginSimulatorDatabaseConnector}s
 */
public class OriginSimulatorDatabaseConnectorFactory {

  private final EntityManagerFactory originEntityManagerFactory;

  private EventDatabaseConnector eventDatabaseConnector;
  private OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector;
  private OriginErrDatabaseConnector originErrDatabaseConnector;


  private OriginSimulatorDatabaseConnectorFactory(EntityManagerFactory entityManagerFactory) {
    originEntityManagerFactory = entityManagerFactory;
  }

  /**
   * Creates an IOC factory to build the station definition jpa repositories
   *
   * @param entityManagerFactory the entity manager that supplies the connections to be used by the
   * db connectors
   * @return stationDefinitionDatabaseConnectorFactory object
   */
  public static OriginSimulatorDatabaseConnectorFactory create(
    EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory, "An EntityManagerFactory must be provided.");
    return new OriginSimulatorDatabaseConnectorFactory(entityManagerFactory);
  }

  public EventDatabaseConnector getEventDatabaseConnectorInstance() {
    if (eventDatabaseConnector == null) {
      eventDatabaseConnector = new EventDatabaseConnector(originEntityManagerFactory.createEntityManager());
    }

    return eventDatabaseConnector;
  }

  public OriginErrDatabaseConnector getOrigErrDatabaseConnectorInstance() {
    if (originErrDatabaseConnector == null) {
      originErrDatabaseConnector = new OriginErrDatabaseConnector(originEntityManagerFactory.createEntityManager());
    }

    return originErrDatabaseConnector;
  }

  public OriginSimulatorDatabaseConnector getOriginDatabaseConnectorInstance() {
    if (originSimulatorDatabaseConnector == null) {
      originSimulatorDatabaseConnector = OriginSimulatorDatabaseConnector
        .create(originEntityManagerFactory);
    }

    return originSimulatorDatabaseConnector;
  }
}

package gms.shared.workflow.repository;

import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;

/**
 * Responsible for creating and providing a single {@link IntervalDatabaseConnector} per
 * IntervalDatabaseConnectorFactory instance
 */
public class IntervalDatabaseConnectorFactory {

  EntityManagerFactory entityManagerFactory;
  IntervalDatabaseConnector intervalDatabaseConnectorInstance;

  /**
   * Instantiates a new IntervalDatabaseConnectorFactory
   *
   * @param entityManagerFactory Used to create the {@link IntervalDatabaseConnector}
   */
  public IntervalDatabaseConnectorFactory(EntityManagerFactory entityManagerFactory) {

    this.entityManagerFactory = entityManagerFactory;
  }

  /**
   * Creates and returns a new IntervalDatabaseConnectorFactory
   *
   * @param entityManagerFactory Used to create the {@link IntervalDatabaseConnector}
   * @return A new IntervalDatabaseConnectorFactory
   */
  public static IntervalDatabaseConnectorFactory create(EntityManagerFactory entityManagerFactory) {
    Validate.notNull(entityManagerFactory);
    return new IntervalDatabaseConnectorFactory(entityManagerFactory);
  }

  /**
   * If no {@link IntervalDatabaseConnector} instance has been created, create and return one. Otherwise, return the
   * existing IntervalDatabaseConnecctor instance
   *
   * @return An IntervalDatabaseConnector instance
   */
  public IntervalDatabaseConnector getIntervalDatabaseConnectorInstance() {
    if (intervalDatabaseConnectorInstance == null) {
      intervalDatabaseConnectorInstance = new IntervalDatabaseConnector(entityManagerFactory);
    }
    return intervalDatabaseConnectorInstance;
  }
}

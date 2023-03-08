package gms.shared.signaldetection.database.connector;

import gms.shared.utilities.bridge.database.connector.DatabaseConnectorType;

/**
 * Assoc Signal Detection Database Connector type
 */
public class AssocDatabaseConnectorType implements DatabaseConnectorType<AssocDatabaseConnector> {
  @Override
  public Class<AssocDatabaseConnector> getConnectorClass() {
    return AssocDatabaseConnector.class;
  }
}

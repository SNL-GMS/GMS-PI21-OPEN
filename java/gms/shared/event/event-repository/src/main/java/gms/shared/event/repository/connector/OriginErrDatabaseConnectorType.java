package gms.shared.event.repository.connector;

import gms.shared.utilities.bridge.database.connector.DatabaseConnectorType;

public class OriginErrDatabaseConnectorType implements DatabaseConnectorType<OriginErrDatabaseConnector> {
  public Class<OriginErrDatabaseConnector> getConnectorClass() {
    return OriginErrDatabaseConnector.class;
  }
}

package gms.shared.event.repository.connector;

import gms.shared.utilities.bridge.database.connector.DatabaseConnectorType;

public class OriginDatabaseConnectorType implements DatabaseConnectorType<OriginDatabaseConnector> {
  public Class<OriginDatabaseConnector> getConnectorClass() {
    return OriginDatabaseConnector.class;
  }
}

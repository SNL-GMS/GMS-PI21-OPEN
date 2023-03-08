package gms.shared.event.repository.connector;

import gms.shared.utilities.bridge.database.connector.DatabaseConnectorType;

public class GaTagDatabaseConnectorType implements DatabaseConnectorType<GaTagDatabaseConnector> {
  @Override
  public Class<GaTagDatabaseConnector> getConnectorClass() {
    return GaTagDatabaseConnector.class;
  }
}

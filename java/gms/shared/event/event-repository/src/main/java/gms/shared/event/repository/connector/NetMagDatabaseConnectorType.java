package gms.shared.event.repository.connector;

import gms.shared.utilities.bridge.database.connector.DatabaseConnectorType;

public class NetMagDatabaseConnectorType implements DatabaseConnectorType<NetMagDatabaseConnector> {
  @Override
  public Class<NetMagDatabaseConnector> getConnectorClass() {
    return NetMagDatabaseConnector.class;
  }
}

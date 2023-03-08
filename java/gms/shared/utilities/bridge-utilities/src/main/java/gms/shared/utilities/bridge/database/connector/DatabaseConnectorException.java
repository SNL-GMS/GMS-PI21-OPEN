package gms.shared.utilities.bridge.database.connector;

public class DatabaseConnectorException extends
  RuntimeException {
  public DatabaseConnectorException(String message, Exception e) {
    super(message, e);
  }
}

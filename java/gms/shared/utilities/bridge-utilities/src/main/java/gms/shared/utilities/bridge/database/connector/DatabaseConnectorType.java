package gms.shared.utilities.bridge.database.connector;

/**
 * Signal Detection Database Connector type that gets the specified connector class
 * when given a particular Signal Detection Database Connector enum type
 *
 * @param <T>
 */
public interface DatabaseConnectorType<T> {
  Class<T> getConnectorClass();
}

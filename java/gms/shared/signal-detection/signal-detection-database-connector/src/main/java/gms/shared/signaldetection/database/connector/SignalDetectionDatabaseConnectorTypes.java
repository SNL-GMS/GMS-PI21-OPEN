package gms.shared.signaldetection.database.connector;

/**
 * Signal Detection Database Connector Types that initializes all individual
 * Signal Detection Database Connector Type classes
 */
public class SignalDetectionDatabaseConnectorTypes {

  private SignalDetectionDatabaseConnectorTypes() {
  }

  public static final AmplitudeDatabaseConnectorType AMPLITUDE_CONNECTOR_TYPE = new AmplitudeDatabaseConnectorType();

  public static final ArrivalDatabaseConnectoryType ARRIVAL_CONNECTOR_TYPE = new ArrivalDatabaseConnectoryType();

  public static final AssocDatabaseConnectorType ASSOC_CONNECTOR_TYPE = new AssocDatabaseConnectorType();
}

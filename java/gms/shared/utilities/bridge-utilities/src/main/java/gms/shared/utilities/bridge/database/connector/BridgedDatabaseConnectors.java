package gms.shared.utilities.bridge.database.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Bridged database connectors contains mappings for previous and current
 * stage database connectors. Database connectors are stored and found
 * using string stageId and database connector types
 */
public abstract class BridgedDatabaseConnectors {

  private final Map<String, Map<Class<?>, Object>> currentStageDatabaseConnectors;
  private final Map<String, Map<Class<?>, Object>> previousStageDatabaseConnectors;

  protected BridgedDatabaseConnectors() {
    currentStageDatabaseConnectors = new HashMap<>();
    previousStageDatabaseConnectors = new HashMap<>();
  }

  public abstract <T> Class<?> getClassForConnector(T databaseConnector);

  public <T> void addConnectorForCurrentStage(String stageName, T databaseConnector) {
    getCurrentStageDatabaseConnectors().computeIfAbsent(stageName, stage -> new HashMap<>())
      .put(getClassForConnector(databaseConnector), databaseConnector);
  }

  public <T> void addConnectorForPreviousStage(String currentStageName, T databaseConnector) {
    getPreviousStageDatabaseConnectors().computeIfAbsent(currentStageName, stage -> new HashMap<>())
      .put(getClassForConnector(databaseConnector), databaseConnector);
  }

  public <T> T getConnectorForCurrentStageOrThrow(String stageName,
    DatabaseConnectorType<T> connectorType) {

    return getConnectorForCurrentStage(stageName, connectorType).orElseThrow(() ->
      new IllegalArgumentException(String.format("No connector of type [%s] exists for stage [%s]",
        connectorType.getConnectorClass().getSimpleName(), stageName)));
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> getConnectorForCurrentStage(String stageName,
    DatabaseConnectorType<T> connectorType) {

    var connectorClass = connectorType.getConnectorClass();

    var connectorsByType = currentStageDatabaseConnectors.get(stageName);
    if (isNull(connectorsByType)) {
      return Optional.empty();
    }

    var connector = connectorsByType.get(connectorClass);
    if (isNull(connector)) {
      return Optional.empty();
    }

    return Optional.of((T) connector);
  }

  public <T> T getConnectorForPreviousStageOrThrow(String currentStageName,
    DatabaseConnectorType<T> connectorType) {

    return getConnectorForPreviousStage(currentStageName, connectorType).orElseThrow(() ->
      new IllegalArgumentException(String.format("No connector of type [%s] exists for the stage previous to [%s]",
        connectorType.getConnectorClass().getSimpleName(), currentStageName)));
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<T> getConnectorForPreviousStage(String currentStageName,
    DatabaseConnectorType<T> connectorType) {

    var connectorClass = connectorType.getConnectorClass();

    var connectorsByType = previousStageDatabaseConnectors.get(currentStageName);
    if (isNull(connectorsByType)) {
      return Optional.empty();
    }

    var connector = connectorsByType.get(connectorClass);
    if (isNull(connector)) {
      return Optional.empty();
    }

    return Optional.of((T) connector);
  }

  public <T> boolean connectorExistsForPreviousStage(String currentStageName,
    DatabaseConnectorType<T> connectorType) {

    return previousStageDatabaseConnectors.containsKey(currentStageName)
      && previousStageDatabaseConnectors.get(currentStageName).containsKey(connectorType.getConnectorClass());
  }

  public Map<String, Map<Class<?>, Object>> getCurrentStageDatabaseConnectors() {
    return currentStageDatabaseConnectors;
  }

  public Map<String, Map<Class<?>, Object>> getPreviousStageDatabaseConnectors() {
    return previousStageDatabaseConnectors;
  }
}

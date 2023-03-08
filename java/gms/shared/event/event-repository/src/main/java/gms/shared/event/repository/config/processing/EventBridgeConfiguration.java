package gms.shared.event.repository.config.processing;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.signaldetection.database.connector.config.StagePersistenceDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Retrieves EventBridgeDefinitions from processing configuration
 */
@Component
public class EventBridgeConfiguration {

  private static final String MONITORING_ORGANIZATION_CONFIG = "event-repository-bridged.monitoring-organization";
  private static final String MONITORING_ORGANIZATION_KEY = "monitoringOrganization";

  private static final String DATABASE_ACCOUNTS_BY_STAGE_CONFIG = "event-repository-bridged.database-accounts-by-stage";

  static final String WORKFLOW_DEFINITION_CONFIG = "workflow-manager.workflow-definition";
  private static final String STAGE_NAMES_KEY = "stageNames";

  private static final String JDBC_URL = "_jdbc_url";

  private final ConfigurationConsumerUtility configurationConsumerUtility;
  private final SystemConfig systemConfig;

  /**
   * Creates a new EventBridgeConfigurationInstance given a ConfigurationConsumerUtility used to resolve processing
   * configuration
   *
   * @param configurationConsumerUtility Resolves processing configuration
   * @param systemConfig Resolves database account to URL mappings
   */
  @Autowired
  public EventBridgeConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    SystemConfig systemConfig) {

    this.configurationConsumerUtility = configurationConsumerUtility;
    this.systemConfig = systemConfig;
  }

  /**
   * Retrieves the default EventBridgeDefinition from Spring ConfigurationProperties
   *
   * @return Default EventBridgeDefinition created from Spring ConfigurationProperties
   */
  @Bean
  public EventBridgeDefinition getCurrentEventBridgeDefinition() {

    final var monitoringOrganization = configurationConsumerUtility.resolve(MONITORING_ORGANIZATION_CONFIG, List.of())
      .get(MONITORING_ORGANIZATION_KEY).toString();

    final var orderedStageNames = resolveStageNames();

    final var orderedStages = orderedStageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(Collectors.toList());

    final var stageToDbMap = resolveDatabaseAccountsByStage();

    final UnaryOperator<Map<WorkflowDefinitionId, String>> concatenateJDBCUrl = stageToDatabaseMap ->
      stageToDatabaseMap.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          stageToAccount -> systemConfig.getValue(stageToAccount.getValue() + JDBC_URL)));

    return EventBridgeDefinition.builder()
      .setMonitoringOrganization(checkNotNull(monitoringOrganization))
      .setOrderedStages(orderedStages)
      .setDatabaseUrlByStage(
        concatenateJDBCUrl.apply(stageToDbMap.get("current"))
      )
      .setPreviousDatabaseUrlByStage(
        concatenateJDBCUrl.apply(stageToDbMap.get("previous"))
      )
      .build();
  }

  /**
   * Resolves the mappings from Stage to the corresponding current and previous (when applicable) database account
   *
   * @return Map with keys "current" and "previous" to maps from Stage to database account
   */
  private Map<String, Map<WorkflowDefinitionId, String>> resolveDatabaseAccountsByStage() {

    final var stagePersistenceDefinition =
      configurationConsumerUtility.resolve(DATABASE_ACCOUNTS_BY_STAGE_CONFIG, List.of(), StagePersistenceDefinition.class);
    final var databaseAccountsByStage =
      stagePersistenceDefinition.getDatabaseAccountsByStageMap();
    final var previousDatabaseAccountsByStage =
      stagePersistenceDefinition.getPreviousDatabaseAccountsByStageMap();

    return getCurrentAndPreviousDatabaseAccountsByStageMap(databaseAccountsByStage, previousDatabaseAccountsByStage);
  }

  /**
   * Returns a map with keys "current" and "previous" of which the values are maps of {@link WorkflowDefinitionId}s to
   * database URLs
   */
  private Map<String, Map<WorkflowDefinitionId, String>> getCurrentAndPreviousDatabaseAccountsByStageMap(
    Map<WorkflowDefinitionId, String> currentDatabaseAccountsByStage,
    Map<WorkflowDefinitionId, String> previousDatabaseAccountsByStage) {

    UnaryOperator<Map<WorkflowDefinitionId, String>> stageToUrlMappingMethod = stageToUrlMap ->
      stageToUrlMap.entrySet().stream()
      .collect(
        Collectors.toMap(
          Map.Entry::getKey,
          stageToDatabaseAccount -> {
            if (stageToDatabaseAccount.getValue() instanceof String) {
              return (String) stageToDatabaseAccount.getValue();
            } else {
              throw new IllegalArgumentException(String.format("[%s] configuration value [%s] must be String",
                DATABASE_ACCOUNTS_BY_STAGE_CONFIG, stageToDatabaseAccount.getValue()));
            }
          }
        )
      );

    return Map.of("current", stageToUrlMappingMethod.apply(currentDatabaseAccountsByStage),
                  "previous", stageToUrlMappingMethod.apply(previousDatabaseAccountsByStage));

  }

  /**
   * Resolves the ordered List of Stage names from processing configuration
   *
   * @return Ordered List of Stage names
   */
  private List<String> resolveStageNames() {

    var stageListObject = configurationConsumerUtility.resolve(WORKFLOW_DEFINITION_CONFIG, List.of())
      .get(STAGE_NAMES_KEY);

    var orderedStageNames = new ArrayList<String>();

    if (stageListObject instanceof List<?>) {

      ((List<?>) stageListObject).forEach(stageObject -> {

        if (stageObject instanceof String) {
          orderedStageNames.add((String) stageObject);
        } else {
          throw new IllegalArgumentException(String.format("[%s] configuration value must be List of Strings", STAGE_NAMES_KEY));
        }
      });
    } else {
      throw new IllegalArgumentException(String.format("[%s] configuration value must be List of Strings", STAGE_NAMES_KEY));
    }

    return orderedStageNames;
  }

}

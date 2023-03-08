package gms.shared.event.repository.config.processing;

import com.google.auto.value.AutoValue;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.List;
import java.util.Map;

/**
 * Defines processing configuration for EventRepositoryBridged
 */
@AutoValue
public abstract class EventBridgeDefinition {

  /**
   * Gets the string defining how to assign the monitoringOrganization attribute for bridged Event objects
   *
   * @return A string defining how to assign the monitoringOrganization attribute for bridged Event objects
   */
  public abstract String getMonitoringOrganization();

  /**
   * Gets the ordered list of Stage identifiers corresponding to the Stage ordering in the Workflow
   *
   * @return An ordered list of Stage identifiers corresponding to the Stage ordering in the Workflow
   */
  public abstract List<WorkflowDefinitionId> getOrderedStages();

  /**
   * Gets a map of {@link WorkflowDefinitionId}s to their corresponding legacyDB URLs
   *
   * @return A map of WorkflowDefinitionIds to legacyDB urls
   */
  public abstract Map<WorkflowDefinitionId, String> getDatabaseUrlByStage();

  /**
   * Gets a map of {@link WorkflowDefinitionId}s to their corresponding previous stage legacyDB URLs
   *
   * @return A map of WorkflowDefinitionIds to legacyDB urls
   */
  public abstract Map<WorkflowDefinitionId, String> getPreviousDatabaseUrlByStage();

  public static Builder builder() {
    return new AutoValue_EventBridgeDefinition.Builder();
  }

  /**
   * Builder for constructing EventBridgeDefinition instances
   */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Returns a Builder with the monitoringOrganization EventBridgeDefinition field set
     *
     * @param monitoringOrganization The EventBridgeDefinition monitoringOrganization value to set
     * @return A Builder with the monitoringOrganization EventBridgeDefinition field set
     */
    public abstract Builder setMonitoringOrganization(String monitoringOrganization);

    /**
     * Returns a Builder with the orderedStages EventBridgeDefinition field set
     *
     * @param orderedStages The EventBridgeDefinition orderedStages value to set
     * @return A Builder with the orderedStages EventBridgeDefinition field set
     */
    public abstract Builder setOrderedStages(List<WorkflowDefinitionId> orderedStages);

    /**
     * Returns a Builder with the databaseAccountByStage EventBridgeDefinition field set
     *
     * @param databaseAccountByStage The EventBridgeDefinition databaseAccountByStage value to set
     * @return A Builder with the databaseAccountByStage EventBridgeDefinition field set
     */
    public abstract Builder setDatabaseUrlByStage(Map<WorkflowDefinitionId, String> databaseAccountByStage);

    /**
     * Returns a Builder with the previousDatabaseAccountByStage EventBridgeDefinition field set
     *
     * @param previousDatabaseAccountByStage The EventBridgeDefinition previousDatabaseAccountByStage value to set
     * @return A Builder with the previousDatabaseAccountByStage EventBridgeDefinition field set
     */
    public abstract Builder setPreviousDatabaseUrlByStage(Map<WorkflowDefinitionId, String> previousDatabaseAccountByStage);

    /**
     * Creates a new EventBridgeDefinition instance with attributes initialized by the Builder
     *
     * @return A new EventBridgeDefinition instance with attributes initialized by the Builder
     */
    public abstract EventBridgeDefinition build();

  }

}

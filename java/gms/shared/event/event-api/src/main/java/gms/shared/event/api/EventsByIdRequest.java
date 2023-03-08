package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.List;
import java.util.UUID;

/**
 * Defines the request body for EventManager.findEventsById()
 */
@AutoValue
public abstract class EventsByIdRequest {

  /**
   * Gets the List of UUIDs for which to retrieve Events
   *
   * @return List of UUIDs
   */
  public abstract List<UUID> getUuids();

  /**
   * Gets the stageId for which tto retrieve Events
   *
   * @return WorkflowDefinitionId
   */
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Creates a new EventByIdRequest instance
   *
   * @param uuids List of UUIDs for which to retrieve Events
   * @param stageId StageId for which to retrieve Events
   * @return New EventByIdRequest instance
   */
  @JsonCreator
  public static EventsByIdRequest create(
    @JsonProperty("uuids") List<UUID> uuids,
    @JsonProperty("stageId") WorkflowDefinitionId stageId) {

    return new AutoValue_EventsByIdRequest(uuids, stageId);
  }

}

package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.UUID;

/**
 * Represents a request body for the EventManager.updateEventStatus endpoint
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EventStatusUpdateRequest {

  /**
   * Gets the StageId of the update request
   *
   * @return A {@link WorkflowDefinitionId} representing the stageId
   */
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Gets the ID of the {@link Event} to update
   *
   * @return the ID of the {@link Event} to update
   */
  public abstract UUID getEventId();

  /**
   * Gets the {@link EventStatusInfo} for the {@link Event} update
   *
   * @return The {@link EventStatusInfo} for the update
   */
  public abstract EventStatusInfo getEventStatusInfo();

  /**
   * Creates and returns a {@link EventStatusUpdateRequest} for updating an {@link Event}
   *
   * @param stageId The {@link WorkflowDefinitionId} representing the stageId
   * @param eventId The {@link UUID} of the {@link Event}
   * @param eventStatusInfo The {@link EventStatusInfo} of the update
   * @return a newly created {@link EventStatusUpdateRequest}
   */
  @JsonCreator
  public static EventStatusUpdateRequest from(
    @JsonProperty("stageId") WorkflowDefinitionId stageId,
    @JsonProperty("eventId") UUID eventId,
    @JsonProperty("eventStatusInfo") EventStatusInfo eventStatusInfo) {
    return new AutoValue_EventStatusUpdateRequest(stageId, eventId, eventStatusInfo);
  }

}

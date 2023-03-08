package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.List;
import java.util.UUID;

/**
 * Defines the request body for EventManager.findEventStatusInfoByStageIdAndEventIds()
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EventStatusInfoByStageIdAndEventIdsRequest {

  /**
   * Gets the StageId of the request
   *
   * @return A {@link WorkflowDefinitionId} representing the stageId
   */
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Gets the list of event id's from the request
   *
   * @return A List of event UUID's
   */
  public abstract List<UUID> getEventIds();

  /**
   * Creates and returns a {@link EventStatusInfoByStageIdAndEventIdsRequest} for finding a {@link EventStatusInfo}
   *
   * @param stageId The {@link WorkflowDefinitionId} representing the stageId
   * @param eventIds A list of {@link UUID}s corresponding to the sought after {@link EventStatusInfo}s
   * @return a newly created {@link EventStatusInfoByStageIdAndEventIdsRequest}
   */
  @JsonCreator
  public static EventStatusInfoByStageIdAndEventIdsRequest create(
    @JsonProperty("stageId") WorkflowDefinitionId stageId,
    @JsonProperty("eventIds") List<UUID> eventIds) {
    return new AutoValue_EventStatusInfoByStageIdAndEventIdsRequest(stageId, eventIds);
  }
}

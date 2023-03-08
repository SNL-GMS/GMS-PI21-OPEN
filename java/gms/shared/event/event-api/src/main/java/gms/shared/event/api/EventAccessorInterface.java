package gms.shared.event.api;

import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Defines EventAccessor methods
 */
public interface EventAccessorInterface extends EventRepositoryInterface {

  /**
   * Finds Events occurring in the time range provided and for the provided stage.
   *
   * @param startTime Beginning of the time range
   * @param endTime End of the time range
   * @param stageId The stageId of the Events to return
   * @param facetingDefinitionOpt an optional faceting definition for faceting {@link Event}s
   * @return Set of Events
   */
  Set<Event> findByTime(Instant startTime, Instant endTime, WorkflowDefinitionId stageId,
    Optional<FacetingDefinition> facetingDefinitionOpt);

  /**
   * Retrieves Events that occur within the specified time range for the provided stage along with their associated
   * SignalDetections and ChannelSegments
   *
   * @param startTime Beginning of the time range
   * @param endTime End of the time range
   * @param stageId StageId of the Events to return
   * @return EventsWithDetectionsAndSegments
   */
  EventsWithDetectionsAndSegments findEventsWithDetectionsAndSegmentsByTime(Instant startTime, Instant endTime,
    WorkflowDefinitionId stageId);

  /**
   * Retrieves EventStatusInfos corresponding to the specified event Ids and the provided stage
   *
   * @param eventIds A list of {@link UUID}s corresponding to the sought after {@link EventStatusInfo}s
   * @param stageId The {@link WorkflowDefinitionId} representing the stageId
   * @return {@link EventStatusInfoByStageIdAndEventIdsResponse} which maps eventIds to EventInfoStatus objects for the given stageId
   */
  EventStatusInfoByStageIdAndEventIdsResponse findEventStatusInfoByStageIdAndEventIds(WorkflowDefinitionId stageId,
    List<UUID> eventIds);

  /**
   * Inserts or updates the {@link EventStatusInfo} for a given {@link Event} inside the EventAccessor
   * EventStatusInfoCache
   *
   * @param stageId The {@link WorkflowDefinitionId} representing the stageId
   * @param eventId The {@link UUID} of the {@link Event}
   * @param eventStatusInfo The {@link EventStatusInfo} containing the update information
   */
  void updateEventStatusInfo(WorkflowDefinitionId stageId, UUID eventId, EventStatusInfo eventStatusInfo);

}

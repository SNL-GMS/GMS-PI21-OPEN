package gms.shared.event.accessor;


import gms.shared.event.coi.EventStatus;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides the {@link IgniteCache} for storing {@link EventStatusInfo} objects mapped by {@link WorkflowDefinitionId}
 * and Event {@link UUID}. It also provides methods for adding and retrieving records to the cache.
 */
@Component
public class EventStatusInfoCache {

  private static final Logger logger = LoggerFactory.getLogger(EventStatusInfoCache.class);

  public static final CacheInfo EVENT_STATUS_INFO_CACHE = new CacheInfo("event-status-info-cache", CacheMode.REPLICATED,
    CacheAtomicityMode.ATOMIC, true, Optional.empty());

  private final IgniteCache<Pair<WorkflowDefinitionId, UUID>, EventStatusInfo> eventStatusInfoIgniteCache;

  @Autowired
  EventStatusInfoCache() {
    this(IgniteConnectionManager.getOrCreateCache(EVENT_STATUS_INFO_CACHE));
  }

  @TestOnly
  EventStatusInfoCache(IgniteCache<Pair<WorkflowDefinitionId, UUID>, EventStatusInfo> eventStatusInfoIgniteCache) {
    this.eventStatusInfoIgniteCache = eventStatusInfoIgniteCache;
  }

  /**
   * Gets the EventStatusInfo object associated with the provided {@link WorkflowDefinitionId} and Event {@link UUID}.
   * If no EventStatusInfo object exists, creates a new one with NOT_STARTED status and an empty analyst list.
   *
   * @param stageId Workflow StageId
   * @param eventId Event UUID
   * @return An {@link Optional} containing the EventStatusInfo object mapped to the provided WorkflowDefinitionId and
   * Event UUID.
   */
  public EventStatusInfo getOrCreateEventStatusInfo(WorkflowDefinitionId stageId, UUID eventId) {
    return getEventStatusInfo(stageId, eventId).orElseGet(() -> {
      var newEventStatusInfo = EventStatusInfo.from(EventStatus.NOT_STARTED, Collections.emptyList());
      addEventStatusInfo(stageId, eventId, newEventStatusInfo);
      return newEventStatusInfo;
    });
  }

  /**
   * Adds the provided {@link EventStatusInfo} to the cache using the {@link WorkflowDefinitionId} and Event {@link UUID}
   * as keys.
   *
   * @param stageId Workflow StageId
   * @param eventId Event UUID
   * @param eventStatusInfo EventStatusInfo object to be added to cache
   */
  public void addEventStatusInfo(WorkflowDefinitionId stageId, UUID eventId, EventStatusInfo eventStatusInfo) {

    checkNotNull(stageId);
    checkNotNull(eventId);
    checkNotNull(eventStatusInfo);
    eventStatusInfoIgniteCache.put(Pair.of(stageId, eventId), eventStatusInfo);
    logger.debug("EventStatusInfoCache: StageId [{}], EventId [{}] has EventStatusInfo [{}]",
      stageId, eventId, eventStatusInfo);
  }

  /**
   * Gets the EventStatusInfo object associated with the provided {@link WorkflowDefinitionId} and Event {@link UUID}
   *
   * @param stageId Workflow StageId
   * @param eventId Event UUID
   * @return An {@link Optional} containing the EventStatusInfo object mapped to the provided WorkflowDefinitionId and
   * Event UUID. If no mapping exists for the provided keys, the returned Optional is empty.
   */
  private Optional<EventStatusInfo> getEventStatusInfo(WorkflowDefinitionId stageId, UUID eventId) {
    checkNotNull(stageId);
    checkNotNull(eventId);

    return Optional.ofNullable(eventStatusInfoIgniteCache.get(Pair.of(stageId, eventId)));
  }


}

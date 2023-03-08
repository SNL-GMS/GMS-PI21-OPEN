package gms.shared.event.repository.util.id;

import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates deterministic {@link gms.shared.event.coi.Event} and {@link gms.shared.event.coi.EventHypothesis} ids, as
 * well as manages mappings from COI Event and EventHypothesis ids to evids and (orid, stage)s, respectively.
 */
@Component
public class EventIdUtility {

  private static final Logger logger = LoggerFactory.getLogger(gms.shared.event.repository.util.id.EventIdUtility.class);

  public static final CacheInfo EVENT_ID_EVENT_RECORD_ID = new CacheInfo("event-id-event-record-id", CacheMode.REPLICATED,
    CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo EVENT_RECORD_ID_EVENT_ID = new CacheInfo("event-record-id-event-id", CacheMode.REPLICATED,
    CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID = new CacheInfo("event-hypothesis-id-origin-unique-id", CacheMode.REPLICATED,
    CacheAtomicityMode.ATOMIC, true, Optional.empty());
  public static final CacheInfo ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID = new CacheInfo("origin-unique-id-event-hypothesis-id", CacheMode.REPLICATED,
    CacheAtomicityMode.ATOMIC, true, Optional.empty());

  private final IgniteCache<UUID, Long> eventIdtoEvid;
  private final IgniteCache<Long, UUID> evidToEventId;
  private final IgniteCache<UUID, OriginUniqueIdentifier> eventHypothesisIdToOriginUniqueIdentifier;
  private final IgniteCache<OriginUniqueIdentifier, UUID> originUniqueIdentifierToEventHypothesisId;

  private final EventBridgeDefinition eventBridgeDefinition;

  @Autowired
  public EventIdUtility(@Autowired EventBridgeDefinition eventBridgeDefinition) {
    this.eventIdtoEvid = IgniteConnectionManager
      .getOrCreateCache(EVENT_ID_EVENT_RECORD_ID);
    this.evidToEventId = IgniteConnectionManager
      .getOrCreateCache(EVENT_RECORD_ID_EVENT_ID);
    this.eventHypothesisIdToOriginUniqueIdentifier = IgniteConnectionManager
      .getOrCreateCache(EVENT_HYPOTHESIS_ID_ORIGIN_UNIQUE_ID);
    this.originUniqueIdentifierToEventHypothesisId = IgniteConnectionManager
      .getOrCreateCache(ORIGIN_UNIQUE_ID_EVENT_HYPOTHESIS_ID);

    this.eventBridgeDefinition = eventBridgeDefinition;
  }

  @TestOnly
  public EventIdUtility(
    EventBridgeDefinition eventBridgeDefinition,
    IgniteCache<UUID, Long> eventIdtoEvid,
    IgniteCache<Long, UUID> evidToEventId,
    IgniteCache<UUID, OriginUniqueIdentifier> eventHypothesisIdToOriginUniqueIdentifier,
    IgniteCache<OriginUniqueIdentifier, UUID> originUniqueIdentifierToEventHypothesisId) {

    this.eventBridgeDefinition = eventBridgeDefinition;

    this.eventIdtoEvid = eventIdtoEvid;
    this.evidToEventId = evidToEventId;
    this.eventHypothesisIdToOriginUniqueIdentifier = eventHypothesisIdToOriginUniqueIdentifier;
    this.originUniqueIdentifierToEventHypothesisId = originUniqueIdentifierToEventHypothesisId;
  }

  /**
   * Generates a deterministic {@link gms.shared.event.coi.Event} {@link UUID} from the provided evid. If an Event UUID
   * mapping for this evid already exists, that UUID is returned. Otherwise, a new UUID is generated and a new mapping
   * between that UUID and the provided evid is created.
   *
   * @param evid evid used to get the mapped Event UUID or generate a deterministic Event UUID
   * @return Event UUID
   */
  public UUID getOrCreateEventId(long evid) {

    return getEventId(evid).orElseGet(() -> {
      var eventId = UUID.nameUUIDFromBytes(Long.toString(evid).getBytes(StandardCharsets.UTF_8));
      eventIdtoEvid.put(eventId, evid);
      evidToEventId.put(evid, eventId);
      logger.debug("ID: Evid [{}] is associated with EventId [{}]", evid, eventId);
      return eventId;
    });
  }

  /**
   * Generates a deterministic {@link gms.shared.event.coi.EventHypothesis} {@link UUID} from the provided origin id
   * and stage. If an EventHypothesis UUID mapping for this unique identifying combination already exists, that
   * UUID is returned. Otherwise, a new UUID is generated and a new mapping between that UUID and the provided
   * OriginUniqueIdentifier is created.
   *
   * @param orid origin id
   * @param stage Workflow stage name
   * @return EventHypothesis UUID
   */
  public UUID getOrCreateEventHypothesisId(long orid, String stage) {
    checkNotNull(stage);
    return getOrCreateEventHypothesisId(OriginUniqueIdentifier.create(orid, stage));
  }

  /**
   * Generates a deterministic {@link gms.shared.event.coi.EventHypothesis} {@link UUID} from the provided {@link
   * OriginUniqueIdentifier}. If an EventHypothesis UUID mapping for this OriginUniqueIdentifier already exists, that
   * UUID is returned. Otherwise, a new UUID is generated and a new mapping between that UUID and the provided
   * OriginUniqueIdentifier is created.
   *
   * @param originUniqueIdentifier originUniqueIdentifier used to get he mapped EventHypothesis UUID or generate a
   * deterministic EventHypothesis UUID
   * @return EventHypothesis UUID
   */
  public UUID getOrCreateEventHypothesisId(OriginUniqueIdentifier originUniqueIdentifier) {
    checkNotNull(originUniqueIdentifier);

    return getEventHypothesisId(originUniqueIdentifier).orElseGet(() -> {
      var eventHypothesisId = originUniqueIdentifierToUUID(originUniqueIdentifier);
      eventHypothesisIdToOriginUniqueIdentifier.put(eventHypothesisId, originUniqueIdentifier);
      originUniqueIdentifierToEventHypothesisId.put(originUniqueIdentifier, eventHypothesisId);
      logger.debug("ID: EventHypothesisUUID [{}] is associated with OriginUniqueIdentifier [{}]", eventHypothesisId, originUniqueIdentifier);
      return eventHypothesisId;
    });
  }

  /**
   * Adds the provided {@link gms.shared.event.coi.Event} {@link UUID} and evid to the mappings contained in this class
   *
   * @param eventId Event UUID
   * @param evid EventDao evid
   */
  public void addEventIdToEvid(UUID eventId, long evid) {

    checkNotNull(eventId);
    eventIdtoEvid.put(eventId, evid);
    evidToEventId.put(evid, eventId);
    logger.debug("ID: Evid [{}] is associated with EventId [{}]", evid, eventId);
  }

  /**
   * Adds the provided {@link gms.shared.event.coi.EventHypothesis} {@link UUID} and {@link OriginUniqueIdentifier} to
   * the mappings contained in this class
   *
   * @param eventHypothesisId EventHypothesis UUID
   * @param originUniqueIdentifier OriginUniqueIdentifier containing orid and stage
   */
  public void addEventHypothesisIdToOriginUniqueIdentifier(UUID eventHypothesisId,
    OriginUniqueIdentifier originUniqueIdentifier) {

    checkNotNull(eventHypothesisId);
    checkNotNull(originUniqueIdentifier);
    eventHypothesisIdToOriginUniqueIdentifier.put(eventHypothesisId, originUniqueIdentifier);
    originUniqueIdentifierToEventHypothesisId.put(originUniqueIdentifier, eventHypothesisId);
    logger.debug("ID: EventHypothesis [{}] is associated with OriginUniqueIdentifier [{}]", eventHypothesisId, originUniqueIdentifier);
  }

  /**
   * Gets the {@link gms.shared.event.coi.Event} {@link UUID} associated with the provided evid
   *
   * @param evid evid mapped to the returned Event UUID
   * @return An {@link Optional} containing the Event UUID mapped to the provided evid. If no mapping exists for the
   * provided evid, the returned Optional is empty.
   */
  public Optional<UUID> getEventId(long evid) {
    return Optional.ofNullable(evidToEventId.get(evid));
  }

  /**
   * Gets the evid associated with the provided {@link gms.shared.event.coi.Event} {@link UUID}
   *
   * @param eventId Event UUID mapped to the returned evid
   * @return An {@link Optional} containing the evid mapped to the provided Event UUID. If no mapping exists for the
   * provided Event UUID, the returned Optional is empty.
   */
  public Optional<Long> getEvid(UUID eventId) {
    return Optional.ofNullable(eventIdtoEvid.get(checkNotNull(eventId)));
  }

  /**
   * Gets the {@link gms.shared.event.coi.EventHypothesis} {@link UUID} associated with the provided origin id and
   * workflow stage
   *
   * @param orid Origin id
   * @param stage Workflow stage name
   * @return An {@link Optional} containing the EventHypothesis UUID mapped to the provided OriginUniqueIdentifier. If
   * no mapping exists for the provided OriginUniqueIdentifier, the returned Optional is empty.
   */
  public Optional<UUID> getEventHypothesisId(long orid, String stage) {
    return getEventHypothesisId(OriginUniqueIdentifier.create(orid, stage));
  }

  /**
   * Gets the {@link gms.shared.event.coi.EventHypothesis} {@link UUID} associated with the provided {@link
   * OriginUniqueIdentifier}
   *
   * @param originUniqueIdentifier OriginUniqueIdentifier mapped to the returned EventHypothesis UUID
   * @return An {@link Optional} containing the EventHypothesis UUID mapped to the provided OriginUniqueIdentifier. If
   * no mapping exists for the provided OriginUniqueIdentifier, the returned Optional is empty.
   */
  public Optional<UUID> getEventHypothesisId(OriginUniqueIdentifier originUniqueIdentifier) {
    var eventHypothesisUUID = Optional.ofNullable(originUniqueIdentifierToEventHypothesisId.get(checkNotNull(originUniqueIdentifier)));
    if (eventHypothesisUUID.isPresent()) {
      logger.debug("Retrieving ID: EventHypothesisUUID [{}] is associated with OriginUniqueIdentifier [{}]", eventHypothesisUUID.get(), originUniqueIdentifier);
    } else {
      logger.debug("Retrieving ID: EventHypothesisUUID was not found to be associated with OriginUniqueIdentifier [{}]", originUniqueIdentifier);
    }

    return eventHypothesisUUID;
  }

  /**
   * Gets the {@link OriginUniqueIdentifier} associated with the provided {@link gms.shared.event.coi.EventHypothesis}
   * {@link UUID}
   *
   * @param eventHypothesisId EventHypothesis UUID mapped to the returned OriginUniqueIdentifier
   * @return An {@link Optional} containing the OriginUniqueIdentifier mapped to the provided EventHypothesis UUID. If
   * no mapping exists for the provided EventHypothesis UUID, the returned Optional is empty.
   */
  public Optional<OriginUniqueIdentifier> getOriginUniqueIdentifier(UUID eventHypothesisId) {
    return Optional.ofNullable(eventHypothesisIdToOriginUniqueIdentifier.get(checkNotNull(eventHypothesisId)));
  }

  public UUID originUniqueIdentifierToUUID(OriginUniqueIdentifier originUniqueIdentifier) {
    var legacyDb = eventBridgeDefinition.getDatabaseUrlByStage()
      .get(WorkflowDefinitionId.from(originUniqueIdentifier.getStage()));

    var stringSeed = originUniqueIdentifier.getOrid() + legacyDb;
    return UUID.nameUUIDFromBytes(stringSeed.getBytes(StandardCharsets.UTF_8));
  }

}

package gms.shared.event.repository;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.Multimaps;
import gms.shared.event.api.EventRepositoryInterface;
import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.GaTagDao;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.event.repository.connector.EventBridgeDatabaseConnectors;
import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.GaTagDatabaseConnector;
import gms.shared.event.repository.connector.OriginDatabaseConnector;
import gms.shared.event.repository.connector.StaMagDatabaseConnector;
import gms.shared.event.repository.converter.EventConverter;
import gms.shared.event.repository.util.id.EventIdUtility;
import gms.shared.event.repository.util.id.OriginUniqueIdentifier;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisAssocIdComponents;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Optionals;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.AR_INFO_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.ASSOC_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.EVENT_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.EVENT_CONTROL_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.GA_TAG_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.NETMAG_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.ORIGERR_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.ORIGIN_CONNECTOR_TYPE;
import static gms.shared.event.repository.connector.EventBridgeDatabaseConnectorTypes.STAMAG_CONNECTOR_TYPE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Bridged implementation of EventRepository
 */
@Component
public class EventRepositoryBridged implements EventRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(EventRepositoryBridged.class);

  public static final String ANALYST_REJECTED = "analyst_rejected";
  public static final String OBJECT_TYPE_A = "a";
  public static final String OBJECT_TYPE_O = "o";

  private final EventBridgeDatabaseConnectors eventBridgeDatabaseConnectors;
  private final SignalDetectionLegacyAccessor signalDetectionLegacyAccessor;
  private final EventIdUtility eventIdUtility;
  private final EventConverter eventConverter;
  private final EventBridgeDefinition eventBridgeDefinition;
  private final EventStages eventStages;

  @Autowired
  public EventRepositoryBridged(
    EventBridgeDatabaseConnectors eventBridgeDatabaseConnectors,
    SignalDetectionLegacyAccessor signalDetectionLegacyAccessor,
    EventIdUtility eventIdUtility,
    EventConverter eventConverter,
    EventBridgeDefinition eventBridgeDefinition,
    EventStages eventStages) {

    logger.info("{} loaded EventBridgeDefinition: {}", this.getClass().getSimpleName(), eventBridgeDefinition);
    this.eventBridgeDefinition = eventBridgeDefinition;
    this.eventIdUtility = eventIdUtility;
    this.eventConverter = eventConverter;
    this.eventBridgeDatabaseConnectors = eventBridgeDatabaseConnectors;
    this.signalDetectionLegacyAccessor = signalDetectionLegacyAccessor;
    this.eventStages = eventStages;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Event> findByTime(Instant startTime, Instant endTime, WorkflowDefinitionId stageId) {
    checkNotNull(startTime, "startTime shall not be null");
    checkNotNull(endTime, "endTime shall not be null");
    checkArgument(startTime.isBefore(endTime), String.format("startTime [%s] must come before endTime [%s]", startTime, endTime));
    checkNotNull(stageId, "stageId shall not be null");

    if (!eventBridgeDefinition.getOrderedStages().contains(stageId)) {
      logger.warn("Requested Stage ID {} not in definition. Returning empty events.", stageId.getName());
      return new HashSet<>();
    }

    var stageName = stageId.getName();
    var timeNow = Instant.now();
    if (startTime.isAfter(timeNow)) {
      logger.warn("findByTime query startTime is in the future.  This may impact expected results. Current Time [{}] Query startTime [{}]", timeNow, startTime);
    }

    var eventDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, EVENT_CONNECTOR_TYPE);
    var originDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ORIGIN_CONNECTOR_TYPE);
    var gaTagDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, GA_TAG_CONNECTOR_TYPE);

    var prevOriginDatabaseConnectorExists = eventBridgeDatabaseConnectors
      .connectorExistsForPreviousStage(stageName, ORIGIN_CONNECTOR_TYPE);

    var currentStageEventIdToEventDaos = eventDatabaseConnector.findEventsByTime(startTime, endTime).stream()
      .collect(Collectors.toMap(EventDao::getEventId, Function.identity()));

    Set<Event> currentStageEvents = new HashSet<>();
    if (!currentStageEventIdToEventDaos.keySet().isEmpty()) {
      var eventIdToOriginDaos = Multimaps.index(
        originDatabaseConnector.findByEventIds(new ArrayList<>(currentStageEventIdToEventDaos.keySet())),
        OriginDao::getEventId);

      var eventIdToObjectProcessAndGaTagDaos = Multimaps.index(
        gaTagDatabaseConnector.findGaTagsByObjectTypesProcessStatesAndEvids(
          List.of(OBJECT_TYPE_A, OBJECT_TYPE_O), List.of(ANALYST_REJECTED), new ArrayList<>(currentStageEventIdToEventDaos.keySet())),
        GaTagDao::getRejectedArrivalOriginEvid);

      currentStageEvents = currentStageEventIdToEventDaos.keySet().stream()
        .map(eventId -> eventConverter.fromLegacyToDefaultFacetedEvent(
          currentStageEventIdToEventDaos.get(eventId),
          eventIdToOriginDaos.get(eventId),
          eventIdToObjectProcessAndGaTagDaos.get(eventId),
          stageId))
        .collect(toSet());
    }

    logger.debug("Querying current stageId: {}.  [{}] Events collected", stageId, currentStageEvents.size());

    if (prevOriginDatabaseConnectorExists) {
      currentStageEvents = addPreviousStageEvents(startTime, endTime, stageId, stageName, gaTagDatabaseConnector, currentStageEvents);
    }
    return currentStageEvents;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Event> findByIds(Collection<UUID> eventIds, WorkflowDefinitionId stageId) {
    checkNotNull(eventIds, "findByIds requires a non-null list of eventIds");
    checkNotNull(stageId, "findByIds requires a non-null stageId");

    if (eventIds.isEmpty()) {
      logger.info("No event ids to search");
      return new HashSet<>();
    }

    if (!eventBridgeDefinition.getOrderedStages().contains(stageId)) {
      logger.warn("Requested Stage ID {} not in definition. Returning empty events.", stageId.getName());
      return new HashSet<>();
    }

    var stageName = stageId.getName();
    var eventDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, EVENT_CONNECTOR_TYPE);
    var originDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ORIGIN_CONNECTOR_TYPE);
    var gaTagDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, GA_TAG_CONNECTOR_TYPE);

    logger.debug("Processing {} EventIds", eventIds.size());
    var eventSet = eventIds.stream().map(eventId ->
        getFacetedEventByStageConnector(eventDatabaseConnector, originDatabaseConnector,
          gaTagDatabaseConnector, eventId, stageId))
      .flatMap(Optional::stream)
      .collect(toSet());
    logger.debug("Querying current stageId: {}.  [{}] Events collected", stageId, eventSet.size());
    logger.debug("Current Stage Events Found: [{}]", eventSet);

    if (eventBridgeDatabaseConnectors.connectorExistsForPreviousStage(stageName, EVENT_CONNECTOR_TYPE) &&
      eventBridgeDatabaseConnectors.connectorExistsForPreviousStage(stageName, ORIGIN_CONNECTOR_TYPE) &&
      eventStages.getPreviousStage(stageId).isPresent()) {
      var previousStageId = eventStages.getPreviousStage(stageId)
        .orElseThrow(() -> new IllegalStateException("No previous stage found"));

      var prevStageEventDatabaseConnector = eventBridgeDatabaseConnectors
        .getConnectorForPreviousStageOrThrow(stageName, EVENT_CONNECTOR_TYPE);
      var prevStageOriginDatabaseConnector = eventBridgeDatabaseConnectors
        .getConnectorForPreviousStageOrThrow(stageName, ORIGIN_CONNECTOR_TYPE);

      var previousStageEvents = eventIds.stream().map(eventId ->
          getFacetedEventByStageConnector(prevStageEventDatabaseConnector, prevStageOriginDatabaseConnector,
            gaTagDatabaseConnector, eventId, previousStageId))
        .flatMap(Optional::stream)
        .collect(toSet());
      logger.debug("Querying previous stageId: {}.  [{}] Events collected", previousStageId,
        previousStageEvents.size());
      logger.debug("Previous Stage Events Found: [{}]", previousStageEvents);

      var eventSetSize = eventSet.size();
      eventSet.addAll(previousStageEvents);
      logger.debug("Number of unique previous stage Events added to Event List: {} New Total: {}",
        eventSet.size() - eventSetSize, eventSet.size());
    }

    return eventSet;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<EventHypothesis> findHypothesesByIds(Collection<EventHypothesis.Id> eventHypothesisIds) {
    checkNotNull(eventHypothesisIds, "eventHypothesisIds must not be null");
    logger.info("Retrieving Hypotheses for ids: {}", eventHypothesisIds);
    return eventHypothesisIds.stream()
      .map(this::retrieveAndCombineIds)
      .flatMap(Optional::stream)
      .map(this::findHypothesis)
      .flatMap(Collection::stream)
      .collect(toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Event> findByAssociatedDetectionHypotheses(Collection<SignalDetectionHypothesis> signalDetectionHypotheses,
    WorkflowDefinitionId stageId) {

    var legacyAccountIdToAssocIdComponents = Multimaps.index(
      signalDetectionLegacyAccessor.getSignalDetectionHypothesesAssocIdComponents(signalDetectionHypotheses),
      SignalDetectionHypothesisAssocIdComponents::getLegacyDatabaseAccountId);

    var legacyAccountIdToEventDatabaseConnectorMap = legacyAccountIdToAssocIdComponents
      .keySet().stream()
      .distinct()
      .collect(Collectors.toMap(Function.identity(), legacyDatabaseAccountId ->
        eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(
          SignalDetectionLegacyAccessor.legacyDatabaseAccountToStageId(legacyDatabaseAccountId).getName(), EVENT_CONNECTOR_TYPE
        )));
    
    List<Long> evids = new ArrayList<>();

    legacyAccountIdToEventDatabaseConnectorMap.forEach((legacyAccountId, databaseConnector) -> {
      var arids = legacyAccountIdToAssocIdComponents
        .get(legacyAccountId).stream().map(SignalDetectionHypothesisAssocIdComponents::getArid).collect(toList());
      evids.addAll(databaseConnector.findEventIdsByArids(arids));
    });
    
    var eventIds = evids.stream().map(eventIdUtility::getOrCreateEventId).collect(toList());

    return findByIds(eventIds, stageId).stream().map(event -> {
      var eventData = event.getData().orElseThrow();
      var ehIds = eventData.getEventHypotheses().stream().map(EventHypothesis::getId).collect(Collectors.toSet());
      var eventHypotheses = findHypothesesByIds(ehIds);
      return event.toBuilder()
        .setData(eventData.toBuilder().setEventHypotheses(eventHypotheses).build())
        .build();
    }).collect(Collectors.toSet());
  }

  /**
   * Add previous stage {@link Event}s to the current stage set of {@link Event}s
   *
   * @param startTime query start time
   * @param endTime query end time
   * @param stageId current stage {@link WorkflowDefinitionId}
   * @param stageName current stage name string
   * @param gaTagDatabaseConnector {@link GaTagDatabaseConnector}
   * @param currentStageEvents set of current stage {@link Event}s
   * @return set of previous and current stage {@link Event}s
   */
  private Set<Event> addPreviousStageEvents(Instant startTime, Instant endTime, WorkflowDefinitionId stageId,
    String stageName,
    GaTagDatabaseConnector gaTagDatabaseConnector,
    Set<Event> currentStageEvents) {

    var prevOriginDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForPreviousStageOrThrow(stageName, ORIGIN_CONNECTOR_TYPE);
    var previousStageId = eventStages.getPreviousStage(stageId)
      .orElseThrow(() -> new IllegalStateException("No previous stage found"));

    var evids = currentStageEvents.stream().map(event -> eventIdUtility.getEvid(event.getId()))
      .flatMap(Optional::stream)
      .distinct()
      .collect(toList());

    if (!evids.isEmpty()) {
      var originDaoList = prevOriginDatabaseConnector.findByEventIds(evids);
      currentStageEvents = currentStageEvents.stream().map(event -> {
        var eventId = eventIdUtility.getEvid(event.getId());
        var eventHypotheses = originDaoList.stream()
          .filter(originDao -> originDao.getEventId() == eventId.orElseThrow())
          .map(originDao -> {
            var originUniqueId = OriginUniqueIdentifier.create(originDao.getOriginId(), previousStageId.getName());
            var eventHypothesisUUID = eventIdUtility.getOrCreateEventHypothesisId(originUniqueId);
            return EventHypothesis.createEntityReference(EventHypothesis.Id.from(event.getId(), eventHypothesisUUID));
          }).collect(Collectors.toList());

        var updatedData = event.getData().orElseThrow(() ->
            new IllegalStateException("Event [" + event.getId() + "] does not contain Data"))
          .toBuilder().addAllEventHypotheses(eventHypotheses).build();
        return event.toBuilder().setData(updatedData).build();
      }).collect(toSet());
    }

    var oridsForCurrentStageEvents = currentStageEvents.stream().map(Event::getData)
      .flatMap(Optional::stream)
      .map(Event.Data::getEventHypotheses)
      .flatMap(Collection::stream).map(EventHypothesis::getId)
      .map(id -> eventIdUtility.getOriginUniqueIdentifier(id.getHypothesisId()))
      .flatMap(Optional::stream)
      .map(OriginUniqueIdentifier::getOrid)
      .collect(toSet());

    var previousStageOriginDaos = prevOriginDatabaseConnector.findByTime(startTime, endTime).stream()
      .filter(originDao -> !oridsForCurrentStageEvents.contains(originDao.getOriginId()))
      .collect(toSet());

    var previousStageEventConnector = this.eventBridgeDatabaseConnectors.getConnectorForPreviousStageOrThrow(stageName, EVENT_CONNECTOR_TYPE);
    buildPreviousStageEvents(previousStageId, currentStageEvents, previousStageEventConnector, gaTagDatabaseConnector, previousStageOriginDaos);

    return currentStageEvents;
  }

  /**
   * Build previous stage events if appropriate connectors and event ids exists
   *
   * @param previousStageId {@link WorkflowDefinitionId}
   * @param currentStageEvents current set of {@link Event}s to update
   * @param prevEventDatabaseConnector previous stage {@link EventDatabaseConnector}
   * @param gaTagDatabaseConnector {@link GaTagDatabaseConnector}
   * @param previousStageOriginDaos list of previous stage {@link OriginDao}
   */
  private void buildPreviousStageEvents(WorkflowDefinitionId previousStageId,
    Collection<Event> currentStageEvents,
    EventDatabaseConnector prevEventDatabaseConnector,
    GaTagDatabaseConnector gaTagDatabaseConnector,
    Collection<OriginDao> previousStageOriginDaos) {

    if (!previousStageOriginDaos.isEmpty()) {
      var previousStageEventDaos = previousStageOriginDaos.stream()
        .map(OriginDao::getEventId)
        .distinct()
        .map(prevEventDatabaseConnector::findEventById)
        .flatMap(Optional::stream)
        .collect(toSet());
      var previousStageEvids = previousStageEventDaos.stream().map(EventDao::getEventId).collect(toSet());
      var gaTagDaos = gaTagDatabaseConnector.findGaTagsByObjectTypesProcessStatesAndEvids(
        List.of(OBJECT_TYPE_A, OBJECT_TYPE_O), List.of(ANALYST_REJECTED), previousStageEvids);

      var previousStageEvents = previousStageEventDaos.stream().map(eventDao ->
        eventConverter.fromLegacyToDefaultFacetedEvent(eventDao,
          previousStageOriginDaos.stream().filter(originDao -> originDao.getEventId() == eventDao.getEventId()).collect(toSet()),
          gaTagDaos.stream().filter(gaTagDao -> gaTagDao.getRejectedArrivalOriginEvid() == eventDao.getEventId()).collect(toSet()),
          previousStageId)
      ).collect(toSet());

      logger.debug("Querying previous stageId: {}.  [{}] Events collected", previousStageId, previousStageEvents.size());

      currentStageEvents.addAll(previousStageEvents);

      logger.debug("Number of unique previous stage Events added to Event List: {} New Total: {}",
        previousStageEvents.size(), currentStageEvents.size());
    }
  }

  private Optional<OriginIdentifiers> retrieveAndCombineIds(EventHypothesis.Id eventHypothesisId) {
    var evidOpt = eventIdUtility.getEvid(eventHypothesisId.getEventId());
    var originUniqueIdentifierOpt = eventIdUtility.getOriginUniqueIdentifier(eventHypothesisId.getHypothesisId());

    if (evidOpt.isEmpty()) {
      logger.debug("No evid mapping exists for Event id [{}]", eventHypothesisId.getEventId());
    }

    if (originUniqueIdentifierOpt.isEmpty()) {
      logger.debug("No origin unique identifier mapping exists for Event Hypothesis id [{}]", eventHypothesisId.getHypothesisId());
    }

    return Optionals.mapIfAllPresent(evidOpt, originUniqueIdentifierOpt,
      (evid, originUniqueId) -> OriginIdentifiers.from(originUniqueId.getStage(), evid, originUniqueId.getOrid()));
  }

  private Collection<EventHypothesis> findHypothesis(OriginIdentifiers originIdentifiers) {
    var stageId = originIdentifiers.getStageId();
    var evid = originIdentifiers.getEventId();
    var orid = originIdentifiers.getOrid();

    var ehInfo = assembleBridgedEhInformation(stageId, evid, orid);
    var sdhInfo = assembleBridgedSdhInformation(stageId, orid);

    return ehInfo.map(info -> {
      try {
        return eventConverter.fromLegacyToDefaultFacetedEventHypothesis(stageId, info, sdhInfo);
      } catch (NullPointerException | IllegalArgumentException | IllegalStateException e) {
        logger.error("Error bridging event hypothesis for id {}, caused by {}", originIdentifiers, e);
        return Collections.<EventHypothesis>emptyList();
      }
    }).orElseGet(Collections::emptyList);
  }

  private Optional<BridgedEhInformation> assembleBridgedEhInformation(WorkflowDefinitionId stageId, long evid,
    long orid) {
    var stageName = stageId.getName();
    var originConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ORIGIN_CONNECTOR_TYPE);
    var originErrConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ORIGERR_CONNECTOR_TYPE);
    var eventControlConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, EVENT_CONTROL_CONNECTOR_TYPE);
    var gaTagConnector = eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(stageName, GA_TAG_CONNECTOR_TYPE);

    var originDao = originConnector.findById(orid);
    var originErrDao = originErrConnector.findById(orid);
    var eventControlDao = eventControlConnector.findByEventIdOriginId(evid, orid);
    var gaTagDao = gaTagConnector.findGaTagByObjectTypeProcessStateAndEvid(OBJECT_TYPE_O, ANALYST_REJECTED, evid)
      .stream().findFirst();

    Set<NetMagDao> netMagDaos = new HashSet<>(eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(stageName,
      NETMAG_CONNECTOR_TYPE).findNetMagByOrid(orid));

    Set<EventHypothesis.Id> parentEventHypotheses = getParentHypotheses(stageId, evid).stream().collect(toSet());

    return Optionals.mapIfAllPresent(originDao, originErrDao, (origin, originErr) -> {
      var ehInfoBuilder = BridgedEhInformation.builder()
        .setEventStages(this.eventStages)
        .setOriginDao(origin)
        .setOrigerrDao(originErr)
        .setNetMagDaos(netMagDaos)
        .setParentEventHypotheses(parentEventHypotheses);

      gaTagDao.ifPresent(ehInfoBuilder::setGaTagDao);
      eventControlDao.ifPresent(ehInfoBuilder::setEventControlDao);
      return ehInfoBuilder.build();
    });
  }

  private Set<BridgedSdhInformation> assembleBridgedSdhInformation(WorkflowDefinitionId stageId, long orid) {
    var stageName = stageId.getName();
    logger.info("Assembling BridgedSdhInformation for stageId[{}] and orid[{}]", stageName, orid);
    var assocConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, ASSOC_CONNECTOR_TYPE);
    var assocs = assocConnector.findAssocsByOrids(List.of(orid));
    var arInfoDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, AR_INFO_CONNECTOR_TYPE);
    var staMagDatabaseConnector = eventBridgeDatabaseConnectors
      .getConnectorForCurrentStageOrThrow(stageName, STAMAG_CONNECTOR_TYPE);

    var mapOfArInfos = arInfoDatabaseConnector.findArInfosByAssocs(assocs);
    var mapOfStaMagDaos = Multimaps.index(
      staMagDatabaseConnector.findStaMagDaosByAssocs(assocs),
      StaMagDatabaseConnector::staMagDaoKeyTransformer);

    var assocDaoPairMap = assocs.stream()
      .map(AssocDao::assocDaoToAridOridKeyTransformer)
      .collect(Collectors.toMap(assocKey -> assocKey,
        assocKey -> Pair.of(Optional.ofNullable(mapOfArInfos.get(assocKey)), Optional.of(mapOfStaMagDaos.get(assocKey)))));

    var bridgedSdhInformationSet = new HashSet<BridgedSdhInformation>();
    assocs.forEach(assoc -> {

      var bridgedSdhInfoBuilder = BridgedSdhInformation.builder();
      signalDetectionLegacyAccessor.findHypothesisByStageIdAridAndOrid(stageId,
        assoc.getId().getArrivalId(), assoc.getId().getOriginId()).ifPresent(bridgedSdhInfoBuilder::setSignalDetectionHypothesis);

      var assocDaoPair =
        assocDaoPairMap.get(AssocDao.assocDaoToAridOridKeyTransformer(assoc));

      bridgedSdhInfoBuilder
        .setAssocDao(assoc)
        .setArInfoDao(assocDaoPair.getLeft().orElse(null));
      assocDaoPair.getRight().ifPresent(bridgedSdhInfoBuilder::setStaMagDaos);
      bridgedSdhInformationSet.add(bridgedSdhInfoBuilder.build());
    });

    return bridgedSdhInformationSet;
  }

  /**
   * Takes and origin record from the current stage and determines if there are any associated parent EventHypotheses
   *
   * @param currentStageId The currentStageId
   * @param evid evid of interest
   * @return parentEventHypotheses as a list of {@link Optional} faceted {@link EventHypothesis.Id}
   */
  Optional<EventHypothesis.Id> getParentHypotheses(WorkflowDefinitionId currentStageId, long evid) {
    checkNotNull(currentStageId);
    var currentStageName = currentStageId.getName();

    if (eventStages.getPreviousStage(currentStageId).isEmpty()) {
      logger.debug("No Previous Stage found for current stage [{}]", currentStageId);
      return Optional.empty();
    }

    var eventConnector = eventBridgeDatabaseConnectors.connectorExistsForPreviousStage(
      currentStageName,
      EVENT_CONNECTOR_TYPE) ?
      eventBridgeDatabaseConnectors.getConnectorForPreviousStageOrThrow(currentStageName, EVENT_CONNECTOR_TYPE) :
      eventBridgeDatabaseConnectors.getConnectorForCurrentStageOrThrow(currentStageName, EVENT_CONNECTOR_TYPE);

    var event = eventConnector.findEventById(evid);
    var eventId = this.eventIdUtility.getOrCreateEventId(evid);
    return event
      .map(eventDao -> eventIdUtility.getOrCreateEventHypothesisId(eventDao.getPreferredOrigin(), currentStageId.getName()))
      .map(hypothesisId -> EventHypothesis.Id.from(eventId, hypothesisId));
  }

  private Optional<Event> getFacetedEventByStageConnector(EventDatabaseConnector localEventDatabaseConnector,
    OriginDatabaseConnector localOriginDatabaseConnector, GaTagDatabaseConnector gaTagDatabaseConnector,
    UUID eventId, WorkflowDefinitionId stageId) {
    return eventIdUtility.getEvid(eventId).flatMap(evid ->
      localEventDatabaseConnector.findEventById(evid).map(eventDao -> {
        var originDaos = localOriginDatabaseConnector.findByEventIds(List.of(eventDao.getEventId()));

        var gaTagDaos = gaTagDatabaseConnector
          .findGaTagByObjectTypeProcessStateAndEvid(OBJECT_TYPE_O, ANALYST_REJECTED, eventDao.getEventId());

        return eventConverter.fromLegacyToDefaultFacetedEvent(eventDao, originDaos, gaTagDaos, stageId);
      })

    );
  }

  @AutoValue
  protected abstract static class OriginIdentifiers {
    public abstract String getStage();

    @Memoized
    public WorkflowDefinitionId getStageId() {
      return WorkflowDefinitionId.from(getStage());
    }

    public abstract long getEventId();

    public abstract long getOrid();

    public static OriginIdentifiers from(String stage, long evid, long orid) {
      return new AutoValue_EventRepositoryBridged_OriginIdentifiers(stage, evid, orid);
    }
  }
}

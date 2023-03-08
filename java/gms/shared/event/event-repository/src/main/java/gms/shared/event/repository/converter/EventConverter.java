package gms.shared.event.repository.converter;

import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.coi.PreferredEventHypothesis;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.GaTagDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.repository.BridgedEhInformation;
import gms.shared.event.repository.BridgedSdhInformation;
import gms.shared.event.repository.EventStages;
import gms.shared.event.repository.config.processing.EventBridgeDefinition;
import gms.shared.event.repository.util.id.EventIdUtility;
import gms.shared.event.repository.util.id.OriginUniqueIdentifier;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class to help create both {@link Event}s and {@link EventHypothesis}s COI objects
 */
@Component
public class EventConverter {

  private static final Logger logger = LoggerFactory.getLogger(EventConverter.class);

  private final EventIdUtility eventIdUtility;
  private final SignalDetectionIdUtility signalDetectionIdUtility;
  private final EventBridgeDefinition eventBridgeDefinition;
  private final EventStages eventStages;

  @Autowired
  public EventConverter(
    EventIdUtility eventIdUtility,
    SignalDetectionIdUtility signalDetectionIdUtility,
    EventBridgeDefinition eventBridgeDefinition,
    EventStages eventStages) {
    this.eventIdUtility = eventIdUtility;
    this.signalDetectionIdUtility = signalDetectionIdUtility;
    this.eventBridgeDefinition = eventBridgeDefinition;
    this.eventStages = eventStages;
  }

  /**
   * Converts legacy event and origin into a defaulted faceted Event COI object
   *
   * @param eventDao The eventDao
   * @param originDaos The originDaos associated with the event
   * @param workflowDefinitionId the workflowDefinitionId that provided the query
   * @return a default faceted Event
   */
  public Event fromLegacyToDefaultFacetedEvent(EventDao eventDao, Collection<OriginDao> originDaos,
    Collection<GaTagDao> gaTagDaos, WorkflowDefinitionId workflowDefinitionId) {

    checkNotNull(eventDao);
    checkNotNull(originDaos);
    checkNotNull(gaTagDaos);
    checkNotNull(workflowDefinitionId);

    var eventId = eventIdUtility.getOrCreateEventId(eventDao.getEventId());

    gaTagDaos.forEach(gaTagDao -> checkArgument(eventDao.getEventId() == gaTagDao.getRejectedArrivalOriginEvid()));

    var facetedEventHypotheses = originDaos.stream()
      .map(originDao -> eventIdUtility.getOrCreateEventHypothesisId(originDao.getOriginId(), workflowDefinitionId.getName()))
      .map(eventHypothesisId -> EventHypothesis.Id.from(eventId, eventHypothesisId))
      .map(EventHypothesis::createEntityReference)
      .collect(Collectors.toSet());

    final var preferredEventHypothesisByStage = new HashSet<PreferredEventHypothesis>();
    final var rejectedEventHypotheses = new HashSet<EventHypothesis>();
    // separate rejected OriginDaos into stages
    var rejectedOriginDaosByStage = rejectedOriginDaosByStage(originDaos, gaTagDaos);
    // these origins were rejected in the current stage
    eventStages.getPreviousStage(workflowDefinitionId).ifPresentOrElse(previousStage -> {
      if (rejectedOriginDaosByStage.containsKey(previousStage)) {
        var originDaosRejectedInCurrentStage = rejectedOriginDaosByStage.get(previousStage);
        originDaosRejectedInCurrentStage.forEach(originDao -> {
          var preferredEventHypothesisId = EventHypothesis.Id.from(
            eventId,
            eventIdUtility.getOrCreateEventHypothesisId(originDao.getOriginId(), workflowDefinitionId.getName())
          );
          var rejectedEventHypothesis = EventHypothesis.createEntityReference(preferredEventHypothesisId);
          if (!rejectedEventHypotheses.add(rejectedEventHypothesis)) {
            logger.warn("fromLegacyToDefaultFacetedEvent (1) Unable to add Rejected Event Hypothesis, already in Set. Id:{}", rejectedEventHypothesis);
          }
          preferredEventHypothesisByStage.add(
            PreferredEventHypothesis.from(
              workflowDefinitionId,
              Objects.requireNonNullElse(eventDao.getAuthor(), ""),
              rejectedEventHypothesis
            )
          );
        });
      }
    }, () -> logger.warn("No previous stage exists for {}", workflowDefinitionId));
    // origin was rejected in following stage
    if (rejectedOriginDaosByStage.containsKey(workflowDefinitionId)) {

      var originDaosRejectedInSubsequentStage = rejectedOriginDaosByStage.get(workflowDefinitionId);
      originDaosRejectedInSubsequentStage.forEach(originDao -> {
        var subsequentStage = eventStages.getNextStage(workflowDefinitionId)
          .orElseThrow(() -> new IllegalStateException(String.format("Orid %d rejected in stage subsequent to %s, but no subsequent stage exists",
            originDao.getOriginId(), workflowDefinitionId.getName())));
        var preferredEventHypothesisId = EventHypothesis.Id.from(
          eventId,
          eventIdUtility.getOrCreateEventHypothesisId(originDao.getOriginId(), subsequentStage.getName())
        );
        var rejectedEventHypothesis = EventHypothesis.createEntityReference(preferredEventHypothesisId);
        if (!rejectedEventHypotheses.add(rejectedEventHypothesis)) {
          logger.warn("fromLegacyToDefaultFacetedEvent (2) Unable to add Rejected Event Hypothesis, already in Set. Id:{}", rejectedEventHypothesis);
        }
        preferredEventHypothesisByStage.add(
          PreferredEventHypothesis.from(
            subsequentStage,
            Objects.requireNonNullElse(eventDao.getAuthor(), ""),
            rejectedEventHypothesis
          )
        );
      });
    }

    if (!rejectedEventHypotheses.isEmpty()) {
      var preSize = facetedEventHypotheses.size();
      var addSuccessful = facetedEventHypotheses.addAll(rejectedEventHypotheses);
      var expectedSize = preSize + rejectedEventHypotheses.size();
      if (!addSuccessful || facetedEventHypotheses.size() != expectedSize) {
        rejectedEventHypotheses.stream().filter(facetedEventHypotheses::contains)
          .forEach(duplicateEH -> logger.warn("Unable to add duplicate EH to Event: {}", duplicateEH));
      }
    }

    //create the preferredEventHypothesis
    var preferredEventHypothesis = EventHypothesis.createEntityReference(
      EventHypothesis.Id.from(eventId,
        eventIdUtility.getOrCreateEventHypothesisId(eventDao.getPreferredOrigin(), workflowDefinitionId.getName())));

    //the preferredEventHypothesis must exist in the set of eventHypotheses
    facetedEventHypotheses.add(preferredEventHypothesis);

    //create the preferredEventHypothesisByStage
    var auth = Objects.requireNonNullElse(eventDao.getAuthor(), "");
    preferredEventHypothesisByStage.add(PreferredEventHypothesis.from(workflowDefinitionId, auth, preferredEventHypothesis));

    var eventData = Event.Data.builder()
      .setRejectedSignalDetectionAssociations(getRejectedSignalDetectionAssociations(gaTagDaos, signalDetectionIdUtility))
      .setMonitoringOrganization(eventBridgeDefinition.getMonitoringOrganization())
      .setPreferredEventHypothesisByStage(preferredEventHypothesisByStage)
      .setOverallPreferred(preferredEventHypothesis)
      .setFinalEventHypothesisHistory(List.of()) //set to empty Collection by default for a faceted event
      .setEventHypotheses(facetedEventHypotheses).build();

    return Event.builder().setId(eventId).setData(eventData).build();
  }

  private Set<SignalDetection> getRejectedSignalDetectionAssociations(Collection<GaTagDao> analystRejectedArrivalGaTags,
    SignalDetectionIdUtility signalDetectionIdUtility) {

    return analystRejectedArrivalGaTags.stream()
      .filter(gaTagDao -> "a".equals(gaTagDao.getObjectType()) && "analyst_rejected".equals(gaTagDao.getProcessState()))
      .map(GaTagDao::getId)
      .map(signalDetectionIdUtility::getOrCreateSignalDetectionIdfromArid)
      .map(SignalDetection::createEntityReference)
      .collect(Collectors.toSet());
  }

  /**
   * Creates a default faceted EventHypothesis from combined, correlated COI and DAO information retrieved from
   * {@link gms.shared.event.repository.EventRepositoryBridged} and its related connectors and accessors.
   *
   * @param stageId The current Stage for the input daos
   * @param ehInfo Bridged Event Hypothesis COI and DAO information all correlated to the same legacy data
   * @param sdhInfo Bridged Signal Detection Hypothesis COI and DAO information all correlated to the same legacy data
   * @return A default faceted EventHypothesis converted from all available input information
   */
  public Collection<EventHypothesis> fromLegacyToDefaultFacetedEventHypothesis(WorkflowDefinitionId stageId,
    BridgedEhInformation ehInfo, Collection<BridgedSdhInformation> sdhInfo) {
    checkNotNull(stageId);
    checkNotNull(ehInfo);
    checkNotNull(sdhInfo);
    var ehSet = new HashSet<EventHypothesis>();

    long evid = ehInfo.getOriginDao().getEventId();
    long orid = ehInfo.getOriginDao().getOriginId();
    var eventHypothesisId = eventIdUtility.getOrCreateEventHypothesisId(orid, stageId.getName());
    var eventId = eventIdUtility.getOrCreateEventId(evid);

    var ehBuilder = EventHypothesis.Data.builder()
      .setParentEventHypotheses(ehInfo.getParentEventHypotheses().stream()
        .map(eHId -> EventHypothesis.builder().setId(eHId).build())
        .collect(Collectors.toList()))
      .setRejected(false);
    if (!sdhInfo.isEmpty()) {
      logger.warn("non-rejected EventHypotheses has no BridgedSdhInformation, meaning it likely has no ASSOC records");
    }
    var associatedSignalDetectionHypotheses = sdhInfo.stream()
      .map(BridgedSdhInformation::getSignalDetectionHypothesis)
      .flatMap(Optional::stream)
      .map(SignalDetectionHypothesis::toEntityReference)
      .collect(Collectors.toList());
    ehBuilder.setAssociatedSignalDetectionHypotheses(associatedSignalDetectionHypotheses);

    var locationSolution = LocationSolutionConverterUtility.fromLegacyToLocationSolution(ehInfo, sdhInfo);
    ehBuilder.setLocationSolutions(Collections.singleton(locationSolution));
    ehBuilder.setPreferredLocationSolution(locationSolution.toEntityReference());
    ehSet.add(EventHypothesis.builder()
      .setId(EventHypothesis.Id.from(eventId, eventHypothesisId))
      .setData(ehBuilder.build())
      .build());

    var isRejected = false;
    if (ehInfo.getGaTagDao().isPresent()) {
      var gaTag = ehInfo.getGaTagDao().orElseThrow();
      isRejected = ehInfo.getOriginDao().getOriginId() == gaTag.getId()
        && ehInfo.getOriginDao().getEventId() == gaTag.getRejectedArrivalOriginEvid();
    }

    if (isRejected) {
      var rejectedOrid = ehInfo.getGaTagDao().orElseThrow(() -> new IllegalStateException("No GaTagDao defined")).getId();
      var rejectEvid = ehInfo.getGaTagDao().orElseThrow(() -> new IllegalStateException("No GaTagDao defined")).getRejectedArrivalOriginEvid();
      var ehInfoEventStages = ehInfo.getEventStages().orElseThrow(() -> new IllegalStateException("No event stages defined"));
      var rejectedStage = "";
      rejectedStage = ehInfoEventStages.getNextStage(stageId).orElseThrow(() ->
          new IllegalStateException("Unable to retrieve next stage. Not able to create rejected Event Hypothesis"))
        .getName();//Update this for last stage also.

      var rejectedEHId = eventIdUtility.getOrCreateEventHypothesisId(rejectedOrid, rejectedStage);
      var rejectedEventId = eventIdUtility.getOrCreateEventId(rejectEvid);

      var ehParentUUID = ehInfoEventStages.getOrderedStages().stream()
        .map(stage -> OriginUniqueIdentifier.create(ehInfo.getOriginDao().getOriginId(), stage.getName()))
        .filter(id -> eventIdUtility.getEventHypothesisId(id).isPresent())
        .map(id -> eventIdUtility.getEventHypothesisId(id).orElseThrow(() -> new IllegalStateException("No Event Hypothesis Id defined"))).findFirst();

      if (ehParentUUID.isEmpty()) {
        throw new IllegalStateException("Could not find stage to associate parent hypothesis with rejected event hypothesis ");
      }
      if (!ehSet.add(EventHypothesis.createRejectedEventHypothesis(rejectedEventId, rejectedEHId, ehParentUUID.get()))) {
        logger.warn("Unable to add Rejected Event Hypothesis. Id already in Set");
      }
    }
    return ehSet;

  }

  private Map<WorkflowDefinitionId, Set<OriginDao>> rejectedOriginDaosByStage(Collection<OriginDao> originDaos,
    Collection<GaTagDao> gaTagDaos) {

    var gaTagOrids = gaTagDaos.stream().map(GaTagDao::getId).collect(Collectors.toSet());
    Predicate<OriginDao> originDaoHasCorrespondingGaTag = originDao -> gaTagOrids.contains(originDao.getOriginId());
    var rejectedOriginDaosByStage = new HashMap<WorkflowDefinitionId, Set<OriginDao>>();

    originDaos.stream().filter(originDaoHasCorrespondingGaTag).forEach(originDao ->
      eventBridgeDefinition.getOrderedStages().forEach(stage -> {
        if (eventIdUtility.getEventHypothesisId(originDao.getOriginId(), stage.getName()).isPresent()) {
          rejectedOriginDaosByStage.computeIfAbsent(stage, s -> new HashSet<>());
          rejectedOriginDaosByStage.get(stage).add(originDao);
        }
      })
    );

    return rejectedOriginDaosByStage;
  }
}

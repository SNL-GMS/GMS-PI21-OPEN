package gms.shared.event.accessor;

import gms.shared.event.accessor.facet.EventFacetingUtility;
import gms.shared.event.accessor.facet.FacetingTypes;
import gms.shared.event.api.EventAccessorInterface;
import gms.shared.event.api.EventRepositoryInterface;
import gms.shared.event.api.EventStatusInfoByStageIdAndEventIdsResponse;
import gms.shared.event.api.EventsWithDetectionsAndSegments;
import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 */
@Component
public class EventAccessor implements EventAccessorInterface {
  private static final Logger logger = LoggerFactory.getLogger(EventAccessor.class);

  private static final FacetingDefinition channelFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FacetingTypes.CHANNEL_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(FacetingTypes.RESPONSE_TYPE.toString(), FacetingDefinition.builder()
      .setClassType(FacetingTypes.RESPONSE_TYPE.toString())
      .setPopulated(false)
      .build())
    .build();

  private static final FacetingDefinition channelSegmentFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FacetingTypes.CHANNEL_SEGMENT_TYPE.toString())
    .setPopulated(true)
    .build();

  private static final FacetingDefinition featureMeasurementFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FacetingTypes.FEATURE_MEASUREMENT_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(FacetingTypes.CHANNELS_KEY.toString(), channelFacetingDefinition)
    .addFacetingDefinitions(FacetingTypes.MEASURED_CHANNEL_SEGMENT_KEY.toString(), channelSegmentFacetingDefinition)
    .build();

  private static final FacetingDefinition stationFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FacetingTypes.STATION_TYPE.toString())
    .setPopulated(false)
    .build();

  private static final FacetingDefinition signalDetectionHypothesisFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FacetingTypes.SDH_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(FacetingTypes.STATION_KEY.toString(), stationFacetingDefinition)
    .addFacetingDefinitions(FacetingTypes.FEATURE_MEASUREMENTS_KEY.toString(), featureMeasurementFacetingDefinition)
    .build();

  private static final FacetingDefinition rejectedSignalDetectionAssociationsFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(FacetingTypes.SIGNAL_DETECTION_TYPE.toString())
      .setPopulated(true)
      .addFacetingDefinitions(FacetingTypes.SD_HYPOTHESES_KEY.toString(), signalDetectionHypothesisFacetingDefinition)
      .addFacetingDefinitions(FacetingTypes.STATION_KEY.toString(), stationFacetingDefinition)
      .build();

  private static final FacetingDefinition preferredEventHypothesesFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(FacetingTypes.PREFERRED_EH_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition overallPreferredFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(FacetingTypes.SIGNAL_DETECTION_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition finalEventHypothesisHistoryFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(FacetingTypes.EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(true)
      .build();

  public static final FacetingDefinition defaultHypothesesFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(FacetingTypes.DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition detectionsAndSegmentsEventFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FacetingTypes.EVENT_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(FacetingTypes.REJECTED_SD_KEY.toString(), rejectedSignalDetectionAssociationsFacetingDefinition)
    .addFacetingDefinitions(FacetingTypes.EVENT_HYPOTHESIS_KEY.toString(), defaultHypothesesFacetingDefinition)
    .addFacetingDefinitions(FacetingTypes.PREFERRED_EH_KEY.toString(), preferredEventHypothesesFacetingDefinition)
    .addFacetingDefinitions(FacetingTypes.OVERALL_PREFERRED_KEY.toString(), overallPreferredFacetingDefinition)
    .addFacetingDefinitions(FacetingTypes.FINAL_EH_HISTORY_KEY.toString(), finalEventHypothesisHistoryFacetingDefinition)
    .build();

  private final EventRepositoryInterface eventRepository;
  private final EventFacetingUtility eventFacetingUtility;
  private final SignalDetectionAccessorInterface signalDetectionAccessor;
  private final EventStatusInfoCache eventStatusInfoCache;

  /**
   * Creates a new EventAccessor instance
   *
   * @param eventRepository EventRepository the created EventAccessor will delegate some operations to
   */
  @Autowired
  public EventAccessor(
    EventRepositoryInterface eventRepository,
    @Lazy EventFacetingUtility eventFacetingUtility,
    @Qualifier("bridgedSignalDetectionAccessor") SignalDetectionAccessorInterface signalDetectionAccessor,
    EventStatusInfoCache eventStatusInfoCache) {

    this.eventRepository = eventRepository;
    this.eventFacetingUtility = eventFacetingUtility;
    this.signalDetectionAccessor = signalDetectionAccessor;
    this.eventStatusInfoCache = eventStatusInfoCache;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Event> findByIds(Collection<UUID> uuids, WorkflowDefinitionId stageId) {
    return eventRepository.findByIds(uuids, stageId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Event> findByTime(Instant startTime, Instant endTime, WorkflowDefinitionId stageId) {
    return eventRepository.findByTime(startTime, endTime, stageId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Event> findByTime(Instant startTime, Instant endTime, WorkflowDefinitionId stageId,
    Optional<FacetingDefinition> facetingDefinitionOpt) {

    return facetingDefinitionOpt
      .map(facetingDefinition -> findByTime(startTime, endTime, stageId).stream()
        .map(event -> eventFacetingUtility.populateFacets(event, stageId, facetingDefinition)).collect(Collectors.toSet()))
      .orElseGet(() -> findByTime(startTime, endTime, stageId));
  }

  /**
   * Retrieves Event Hypotheses associated with the provided IDs adhering to the provided {@link FacetingDefinition}
   *
   * @param eventHypothesisIds IDs of the desired hypotheses
   * @param facetingDefinition Definition outlining the desired faceting structure of the resultant hypotheses
   * @return The Event Hypotheses associated with the provided IDs adhering to the provided {@link FacetingDefinition}
   */
  public List<EventHypothesis> findHypothesesByIds(Collection<EventHypothesis.Id> eventHypothesisIds,
    FacetingDefinition facetingDefinition) {
    // DEFAULT_FACETED_EVENT_HYPOTHESIS is a special case as eventRepository.findHypothesesByIds already returns
    // expected results with no further processing required
    return (facetingDefinition.getClassType().equals(FacetingTypes.DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE.toString())) ?
      eventRepository.findHypothesesByIds(eventHypothesisIds) :
      eventRepository.findHypothesesByIds(eventHypothesisIds).stream()
        .map(eH -> eventFacetingUtility.populateFacets(eH, facetingDefinition)).flatMap(List::stream).collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<EventHypothesis> findHypothesesByIds(Collection<EventHypothesis.Id> eventHypothesisIds) {
    var defaultEventHypothesisFacetingDefinition = FacetingDefinition.builder()
      .setPopulated(true)
      .setClassType(FacetingTypes.DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE.toString())
      .build();
    return findHypothesesByIds(eventHypothesisIds, defaultEventHypothesisFacetingDefinition);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EventsWithDetectionsAndSegments findEventsWithDetectionsAndSegmentsByTime(Instant startTime,
    Instant endTime, WorkflowDefinitionId stageId) {

    var events = eventRepository.findByTime(startTime, endTime, stageId);

    events = events.stream().map(event ->
        eventFacetingUtility.populateFacets(event, stageId, detectionsAndSegmentsEventFacetingDefinition))
      .collect(Collectors.toSet());

    var signalDetectionsWithChannelSegments = getAssociatedSignalDetectionsWithChannelSegments(events, stageId);

    return EventsWithDetectionsAndSegments.builder()
      .setEvents(events)
      .setDetectionsWithChannelSegments(SignalDetectionsWithChannelSegments.builder()
        .setSignalDetections(signalDetectionsWithChannelSegments.getSignalDetections())
        .setChannelSegments(signalDetectionsWithChannelSegments.getChannelSegments())
        .build())
      .build();
  }

  /**
   * Finds {@link Event}s associated with the provided signal detections and stage
   *
   * @param signalDetectionHypotheses A collection of {@link SignalDetectionHypothesis} objects
   * @param stageId The {@link WorkflowDefinitionId} of the Events to return
   * @return Set of Events
   */
  public Set<Event> findByAssociatedDetectionHypotheses(
    Collection<SignalDetectionHypothesis> signalDetectionHypotheses,
    WorkflowDefinitionId stageId) {
    return eventRepository.findByAssociatedDetectionHypotheses(signalDetectionHypotheses, stageId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EventStatusInfoByStageIdAndEventIdsResponse findEventStatusInfoByStageIdAndEventIds(
    WorkflowDefinitionId stageId, List<UUID> eventIds) {
    final var eventStatusInfos = eventIds.stream()
      .distinct()
      .map(event -> Pair.of(event, eventStatusInfoCache.getOrCreateEventStatusInfo(stageId, event)))
      .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

    return EventStatusInfoByStageIdAndEventIdsResponse.builder()
      .setStageId(stageId)
      .setEventStatusInfoMap(eventStatusInfos)
      .build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateEventStatusInfo(WorkflowDefinitionId stageId, UUID eventId, EventStatusInfo eventStatusInfo) {
    this.eventStatusInfoCache.addEventStatusInfo(stageId, eventId, eventStatusInfo);
  }

  /**
   * Gets the SignalDetections with ChannelSegments associated with the provided Events
   *
   * @param events Returns the SignalDetections and ChannelSegments associated with these Events
   * @param stageId Stage of the provided Events
   * @return SignalDetections with ChannelSegments associated with the provided Events
   */
  private SignalDetectionsWithChannelSegments getAssociatedSignalDetectionsWithChannelSegments(
    Collection<Event> events, WorkflowDefinitionId stageId) {

    var associatedSignalDetectionIds = events.stream()
      .flatMap(e -> e.getData().orElseThrow(
        () -> new IllegalStateException("Event Data was not there! cannot continue")
      ).getEventHypotheses().stream())
      .map(EventHypothesis::getData)
      .flatMap(Optional::stream)
      .flatMap(ehData -> ehData.getAssociatedSignalDetectionHypotheses().stream())
      .map(SignalDetectionHypothesis::getId)
      .map(SignalDetectionHypothesisId::getSignalDetectionId)
      .collect(Collectors.toSet());

    if (associatedSignalDetectionIds.isEmpty()) {
      return SignalDetectionsWithChannelSegments.builder().build();//TODO: remove when behavior is in findWithSegmentsByIds
    }
    logger.debug("Querying Signal Detection Accessor with associatedSignalDetectionIds: {}", associatedSignalDetectionIds);
    return signalDetectionAccessor.findWithSegmentsByIds(
      new ArrayList<>(associatedSignalDetectionIds),
      stageId
    );
  }
}

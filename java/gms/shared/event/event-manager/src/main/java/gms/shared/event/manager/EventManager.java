package gms.shared.event.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gms.shared.event.api.EventAccessorInterface;
import gms.shared.event.api.EventStatusInfoByStageIdAndEventIdsRequest;
import gms.shared.event.api.EventStatusInfoByStageIdAndEventIdsResponse;
import gms.shared.event.api.EventStatusUpdateRequest;
import gms.shared.event.api.EventsByAssociatedSignalDetectionHypothesesRequest;
import gms.shared.event.api.EventsByTimeRequest;
import gms.shared.event.api.EventsWithDetectionsAndSegments;
import gms.shared.event.api.EventsWithDetectionsAndSegmentsByTimeRequest;
import gms.shared.event.api.FeaturePredictionsByReceiverName;
import gms.shared.event.api.PredictFeaturesForEventLocationRequest;
import gms.shared.event.api.PredictFeaturesForLocationSolutionRequest;
import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventStatus;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.manager.config.EventManagerConfiguration;
import gms.shared.event.manager.config.FeaturePredictionsDefinitions;
import gms.shared.featureprediction.request.PredictForLocationRequest;
import gms.shared.featureprediction.request.PredictForLocationSolutionAndChannelRequest;
import gms.shared.featureprediction.utilities.math.GeoMath;
import gms.shared.frameworks.service.InvalidInputException;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.system.events.SystemEvent;
import gms.shared.system.events.SystemEventPublisher;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;

/**
 * Defines event-manager-service endpoints
 */
@RestController
@RequestMapping(value = "/event",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
public class EventManager {

  private static final String SYSTEM_MESSAGE_EVENT_TYPE = "events";
  private static final Logger logger = LoggerFactory.getLogger(EventManager.class);
  private final EventAccessorInterface eventAccessor;
  private final SystemEventPublisher systemEventPublisher;
  private final EventManagerConfiguration eventManagerConfiguration;
  private final WebRequests webRequests;

  @Autowired
  public EventManager(
    EventManagerConfiguration eventManagerConfiguration,
    EventAccessorInterface eventAccessor,
    SystemEventPublisher systemEventPublisher,
    WebRequests webRequests) {
    this.eventManagerConfiguration = eventManagerConfiguration;
    this.eventAccessor = eventAccessor;
    this.systemEventPublisher = systemEventPublisher;
    this.webRequests = webRequests;
  }

  /**
   * Query for an Event {@link UUID} with a stage
   *
   * @param stageId The {@link WorkflowDefinitionId} stage name
   * @param eventId The Event {@link UUID}
   *
   * @return {@link ResponseEntity} containing the Event
   */
  @GetMapping(value = "/{stageId}/{eventId}")
  @Operation(summary = "Retrieves Event from the Event UUID contained within the provided stage")
  public ResponseEntity<Object> findEventsById(
    @io.swagger.v3.oas.annotations.Parameter(description = "Stage id to query against", required = true)
    @PathVariable String stageId,
    @io.swagger.v3.oas.annotations.Parameter(description = "Event UUID to query for", required = true)
    @PathVariable String eventId) {
    try {
      return ResponseEntity.ok()
        .body(
          eventAccessor.findByIds(List.of(UUID.fromString(eventId)), WorkflowDefinitionId.from(stageId))
        );
    }
    catch (IllegalArgumentException ex) {
      var errorMessage = EventManagerExceptionHandler.INPUT_ERROR_MSG;
      if (ex.getClass() == NumberFormatException.class || ex.getMessage().contains("UUID")) {
        errorMessage = EventManagerExceptionHandler.INVALID_UUID_ERROR_MSG;
      }
      return ResponseEntity.badRequest()
        .body(Map.of(EventManagerExceptionHandler.ERROR_MSG_KEY, errorMessage));
    }
  }

  /**
   * Finds Events occurring in the time range provided and for the provided
   * stage
   *
   * @param request Request body defining the time range and stage
   *
   * @return Set of Events
   */
  @PostMapping(value = "/time")
  @Operation(summary = "Retrieves a list of Events within the provided time range and stage")
  public Set<Event> findEventsByTime(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The start time, end time, stage id, and an optional FacetingDefinition to query. Encapsulated as an EventsByTimeRequest request", required = true)
    @RequestBody EventsByTimeRequest request) {
    return eventAccessor.findByTime(request.getStartTime(), request.getEndTime(), request.getStageId(), request.getFacetingDefinition());
  }

  /**
   * Retrieves Events that occur within the specified time range for the
   * provided stage along with their associated SignalDetections and
   * ChannelSegments
   *
   * @param request Request body defining the time range and stage
   *
   * @return EventsWithDetectionsAndSegments
   */
  @PostMapping(value = "/detections-and-segments/time")
  @Operation(summary = "Retrieves a list of Events with detection and segment information within the provided time range and stage.")
  public EventsWithDetectionsAndSegments findEventsWithDetectionsAndSegmentsByTime(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The start time, end time, and stage to query. Encapsulated as an EventsWithDetectionsAndSegmentsByTimeRequest request", required = true)
    @RequestBody EventsWithDetectionsAndSegmentsByTimeRequest request) {
    return this.eventAccessor.findEventsWithDetectionsAndSegmentsByTime(request.getStartTime(), request.getEndTime(), request.getStageId());
  }

  /**
   * Retrieves EventStatusInfo objects for the provided stage and event ids
   *
   * @param eventStatusInfoRequest Request body defining the relevant event ids
   * and the stage
   *
   * @return EventStatusInfoByStageIdAndEventIdsResponse
   */
  @PostMapping(value = "/status")
  @Operation(summary = "Retrieves the current status of a list of Event associated with Stage")
  public EventStatusInfoByStageIdAndEventIdsResponse findEventStatusInfoByStageIdAndEventIds(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The stage id and list of Event UUIDs to query for status information. Encapsulated as a EventStatusInfoByStageIdAndEventIdsRequest request", required = true)
    @RequestBody EventStatusInfoByStageIdAndEventIdsRequest eventStatusInfoRequest) {
    return this.eventAccessor.findEventStatusInfoByStageIdAndEventIds(eventStatusInfoRequest.getStageId(), eventStatusInfoRequest.getEventIds());
  }

  /**
   * This operation updates and returns the provided {@link LocationSolution}
   * with FeaturePredictions calculated for the provided {@link Channel}s
   *
   * @param eventPredictionRequest Request body defining the relevant
   * {@link LocationSolution}, {@link Channel}s, and {@link PhaseType}s
   *
   * @return a {@link LocationSolution} updated with FeaturePredictions for the
   * provided {@link Channel}s
   */
  @PostMapping(value = "/predict")
  @Operation(summary = "Updates and returns the provided LocationSolution with FeaturePredictions calculated for the provided Channels")
  public ResponseEntity<Object> predictFeaturesForLocationSolution(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The location solution, channels, and phases to provide to the Feature Predictor Service. Encapsulated as a PredictFeaturesForLocationSolutionRequest request")
    @RequestBody PredictFeaturesForLocationSolutionRequest eventPredictionRequest) {

    // Organize channels and phases according to shared definitions
    var predictionDefinitionsToChannelsAndPhaseTypes = new HashMap<FeaturePredictionsDefinitions, Pair<List<Channel>, List<PhaseType>>>();

    var locationSolution = eventPredictionRequest.getLocationSolution();
    var locationSolutionData = locationSolution.getData().orElseThrow(() -> new EventRequestException("Provided LocationSolution has no data"));
    for (var ph : eventPredictionRequest.getPhases()) {
      for (var ch : eventPredictionRequest.getChannels()) {
        // resolve a FeaturePredictionsDefinitions for each channel and phase combination
        var distance = GeoMath.degToKm(
          GeoMath.greatCircleAngularSeparation(
            locationSolutionData.getLocation().getLatitudeDegrees(),
            locationSolutionData.getLocation().getLongitudeDegrees(),
            ch.getLocation().getLatitudeDegrees(),
            ch.getLocation().getLongitudeDegrees()
          )
        );
        var predictionForLocationSolutionDefinitions = eventManagerConfiguration.getPredictionDefinitions(
          ch.getStation().getName(),
          ch.getName(),
          ph,
          distance
        );
        for (var def : predictionForLocationSolutionDefinitions) {
          // for each definition, if there are any other channels/phases that use this definition, organize them
          // so the calls to FeaturePredictorService can be combined for these channels/phases
          var channelsAndPhaseTypes = predictionDefinitionsToChannelsAndPhaseTypes
            .computeIfAbsent(def, d -> Pair.of(new ArrayList<>(), new ArrayList<>()));
          var channels = channelsAndPhaseTypes.getLeft();
          if (!channels.contains(ch)) {
            channels.add(ch);
          }
          var phaseTypes = channelsAndPhaseTypes.getRight();
          if (!phaseTypes.contains(ph)) {
            phaseTypes.add(ph);
          }
        }
      }
    }
    return getLocationResponseEntity(predictionDefinitionsToChannelsAndPhaseTypes, locationSolution);

  }

  /**
   * Retrieves feature predictions of the requested receivers for an Event at
   * a specific location
   * @param eventPredictionRequest Request body defining the relevant
   * {@link EventLocation}, {@link PhaseType}s, and
   * {@link ReceiverLocationsAndTypes}s
   * @return Map of receiver locations and their feature predictions
   */
  @PostMapping(value = "/predict-for-event-location")
  @Operation(summary = "Creates a map of feature predictions for the requested receivers from the source event location and phases")
  public ResponseEntity<Object> predictFeaturesForEventLocation(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The source event location, phases, and receivers to provide to the Feature Predictor Service. Encapsulated as a PredictFeaturesForEventLocationRequest request")
    @RequestBody PredictFeaturesForEventLocationRequest eventPredictionRequest) {

    try {
      //receiver names must be unique within the PredictFeaturesForEventLocationRequest (Across all receivers (maybe check via builder??)
      Set<String> distinctReceiverNames = new HashSet<>();
      Multimap<Location, String> locationToReceiverMap = ArrayListMultimap.create();
      eventPredictionRequest.getReceivers().stream().map(receiver -> receiver.getReceiverLocationsByName().entrySet()).flatMap(Set::stream).forEach(entry -> {
        if (distinctReceiverNames.contains(entry.getKey())) {
          throw new EventRequestException("Encountered duplicate receiver name " + entry.getKey());
        }
        distinctReceiverNames.add(entry.getKey());
        locationToReceiverMap.put(entry.getValue(), entry.getKey());
      });

      var predictionDefinitionsAndPhasetypesToReceiverNamesAndLocations = buildPredictionDefinitionsToReceiverNameAndLocationMultimap(eventPredictionRequest);

      Map<String, FeaturePredictionContainer> receiverNameMapResponse = new HashMap<>();
      for (var predictionDefinitionAndPhaseTypesToReceiverNameAndLocation : predictionDefinitionsAndPhasetypesToReceiverNamesAndLocations.entrySet()) {
        var def = predictionDefinitionAndPhaseTypesToReceiverNameAndLocation.getKey().getLeft();
        var locs = predictionDefinitionAndPhaseTypesToReceiverNameAndLocation.getValue().stream().map(Pair::getRight).collect(Collectors.toList());
        var phases = predictionDefinitionAndPhaseTypesToReceiverNameAndLocation.getKey().getRight();
        var predictForLocationRequest = PredictForLocationRequest.from(
          def.getPredictionTypes(),
          eventPredictionRequest.getSourceLocation(),
          locs,
          new ArrayList<>(phases),
          def.getEarthModel(),
          def.getCorrectionDefinitions()
        );

        var featurePredictionContainer = webRequests.fpsWebRequestPredictForLocation(predictForLocationRequest);
        var receiverNamesToLocationsForThisRequest = ArrayListMultimap.<Location, String>create();
        predictionDefinitionAndPhaseTypesToReceiverNameAndLocation.getValue().forEach(nameAndLocationPair -> receiverNamesToLocationsForThisRequest.put(nameAndLocationPair.getRight(), nameAndLocationPair.getLeft()));
        parseFeaturePredictionsIntoReceivers(receiverNamesToLocationsForThisRequest, receiverNameMapResponse, featurePredictionContainer);
      }
      return ResponseEntity.ok().body(FeaturePredictionsByReceiverName.from(receiverNameMapResponse));
    }
    catch (EventRequestException | FeaturePredictionException e) {
      logger.error("Error occurred when reaching out to Feature Prediction service", e);
      return ResponseEntity.badRequest()
        .body(Map.of(EventManagerExceptionHandler.ERROR_MSG_KEY, e.getMessage()));
    }
  }

  private ResponseEntity<Object> getLocationResponseEntity(
    HashMap<FeaturePredictionsDefinitions, Pair<List<Channel>, List<PhaseType>>> predictionDefinitionsToChannelsAndPhaseTypes,
    LocationSolution locationSolution) {
    for (var definitionToChannelsAndPhaseTypes : predictionDefinitionsToChannelsAndPhaseTypes.entrySet()) {
      // call FeaturePredictorService for each combined definition, collection of channels, and collection of phaseTypes
      var predictionsForLocationSolutionDefinition = definitionToChannelsAndPhaseTypes.getKey();
      var channels = new ArrayList<>(definitionToChannelsAndPhaseTypes.getValue().getLeft());
      var phaseTypes = new ArrayList<>(definitionToChannelsAndPhaseTypes.getValue().getRight());
      try {
        locationSolution = callFeaturePredictorServiceForPredictionDefinition(predictionsForLocationSolutionDefinition, channels, locationSolution, phaseTypes);
      }
      catch (FeaturePredictionException e) {
        logger.error("Error occurred when reaching out to Feature Prediction service", e);
        return ResponseEntity.badRequest()
          .body(Map.of(EventManagerExceptionHandler.ERROR_MSG_KEY, e.getMessage()));
      }
    }

    return ResponseEntity.ok().body(locationSolution);
  }

  private LocationSolution callFeaturePredictorServiceForPredictionDefinition(
    FeaturePredictionsDefinitions featurePredictionsDefinitions,
    List<Channel> channels,
    LocationSolution locationSolution,
    List<PhaseType> phaseTypes) throws FeaturePredictionException {

    var predictForLocationSolutionAndChannelRequest = PredictForLocationSolutionAndChannelRequest
      .from(featurePredictionsDefinitions.getPredictionTypes(), locationSolution, channels, phaseTypes, featurePredictionsDefinitions.getEarthModel(), featurePredictionsDefinitions.getCorrectionDefinitions());

    return webRequests.fpsWebRequestPredictForLocationSolutionAndChannel(predictForLocationSolutionAndChannelRequest);
  }

  /**
   * Retrieves {@link Events}s associated with the provided
   * {@link SignalDetectionHypothesis}s
   * @param eventsByAssociatedSignalDetectionHypothesesRequest Request body
   * defining {@link SignalDetectionHypothesis}s and {@link WorkflowDefinitionId}
   * @return {@link Events}s associated with the provided
   * {@link SignalDetectionHypothesis}s
   */
  @PostMapping(value = "/associated-signal-detection-hypotheses")
  @Operation(summary = "Retrieves Events associated with the provided signal detections and stage")
  public Set<Event> findEventsByAssociatedSignalDetectionHypotheses(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The stage id and collection of signal detection hypotheses. Encapsulated in an EventsByAssociatedSignalDetectionHypothesesRequest request", required = true)
    @RequestBody EventsByAssociatedSignalDetectionHypothesesRequest eventsByAssociatedSignalDetectionHypothesesRequest) {
    return this.eventAccessor.findByAssociatedDetectionHypotheses(
      eventsByAssociatedSignalDetectionHypothesesRequest.getSignalDetectionHypotheses(),
      eventsByAssociatedSignalDetectionHypothesesRequest.getStageId());
  }

  /**
   * Updates the status of an @{Event}
   *
   * @param request Request body defining the update to be performed
   *
   * @return {@link ResponseEntity} containing update status
   */
  @PostMapping(value = "/update")
  @Operation(summary = "Updates the status of an Event")
  public ResponseEntity<Object> updateEventStatus(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The stage id, Event UUID, and Event status information. Encapsulated in an EventStatusUpdateRequest request", required = true)
    @RequestBody EventStatusUpdateRequest request) {

    final var cacheHit = this.eventAccessor
      .findEventStatusInfoByStageIdAndEventIds(request.getStageId(), List.of(request.getEventId()))
      .getEventStatusInfoMap().get(request.getEventId());
    final var incomingEventStatusInfo = request.getEventStatusInfo();

    EventStatusInfo outgoingEventStatusInfo;

    try {
      outgoingEventStatusInfo = consolidateIncomingAndExistingEventStatusInfo(incomingEventStatusInfo, cacheHit);
    }
    catch (InvalidInputException e) {
      return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(Map.of(EventManagerExceptionHandler.ERROR_MSG_KEY, e.getMessage()));
    }

    this.eventAccessor.updateEventStatusInfo(request.getStageId(), request.getEventId(), outgoingEventStatusInfo);

    publishEventStatusUpdate(EventStatusUpdateRequest
      .from(request.getStageId(), request.getEventId(), outgoingEventStatusInfo));

    return ResponseEntity
      .status(HttpStatus.OK)
      .body(EventStatusUpdateRequest
        .from(request.getStageId(), request.getEventId(), outgoingEventStatusInfo));
  }

  private EventStatusInfo consolidateIncomingAndExistingEventStatusInfo(
    EventStatusInfo incomingEventStatusInfo, EventStatusInfo existingEventStatusInfo) throws InvalidInputException {
    List<String> analystList;
    final var incomingEventStatus = incomingEventStatusInfo.getEventStatus();

    switch (incomingEventStatus) {
      case IN_PROGRESS:
        analystList = Stream.concat(incomingEventStatusInfo.getActiveAnalystIds().parallelStream(),
          existingEventStatusInfo.getActiveAnalystIds().parallelStream()).distinct().collect(Collectors.toList());
        return EventStatusInfo.from(EventStatus.IN_PROGRESS, analystList);
      case NOT_COMPLETE:
        if (existingEventStatusInfo.getEventStatus() != EventStatus.IN_PROGRESS) {
          throw new InvalidInputException("Analyst may only close an event that is open");
        }
        analystList = existingEventStatusInfo.getActiveAnalystIds().stream()
          .filter(id -> !incomingEventStatusInfo.getActiveAnalystIds().contains(id))
          .collect(Collectors.toList());

        return (analystList.isEmpty())
          ? EventStatusInfo.from(EventStatus.NOT_COMPLETE, analystList)
          : EventStatusInfo.from(existingEventStatusInfo.getEventStatus(), analystList);
      case COMPLETE:
        if (existingEventStatusInfo.getEventStatus() != EventStatus.IN_PROGRESS) {
          throw new InvalidInputException("Analyst may only mark an event complete if it is open");
        }

        return EventStatusInfo.from(EventStatus.COMPLETE, Collections.emptyList());
      case NOT_STARTED:
      default:
        if (existingEventStatusInfo.getEventStatus() == EventStatus.NOT_STARTED) {
          return existingEventStatusInfo;
        }
        throw new InvalidInputException(String.format("Analyst may not mark %s event as %s", existingEventStatusInfo.getEventStatus(), incomingEventStatus));
    }
  }

  private void publishEventStatusUpdate(EventStatusUpdateRequest update) {
    systemEventPublisher.sendSystemEvent(SystemEvent
      .from(SYSTEM_MESSAGE_EVENT_TYPE, List.of(update), 0));
  }

  private Map<Pair<FeaturePredictionsDefinitions, Set<PhaseType>>, Set<Pair<String, Location>>> buildPredictionDefinitionsToReceiverNameAndLocationMultimap(PredictFeaturesForEventLocationRequest predictFeaturesForEventLocationRequest) {
    var definitionAndPhasesToReceivers = new HashMap<Pair<FeaturePredictionsDefinitions, Set<PhaseType>>, Set<Pair<String, Location>>>();
    var receiverLocationsAndTypesList = predictFeaturesForEventLocationRequest.getReceivers();

    for (var receiverLocationAndTypes : receiverLocationsAndTypesList) {
      var receiverDataType = receiverLocationAndTypes.getReceiverDataType();
      var receiverBandType = receiverLocationAndTypes.getReceiverBandType();
      for (var nameToLocationEntry : receiverLocationAndTypes.getReceiverLocationsByName().entrySet()) {
        var definitionToReceiversAndPhases = new HashMap<FeaturePredictionsDefinitions, Pair<Set<Pair<String, Location>>, Set<PhaseType>>>();
        for (var ph : predictFeaturesForEventLocationRequest.getPhases()) {
          var distance = GeoMath.degToKm(
            GeoMath.greatCircleAngularSeparation(
              predictFeaturesForEventLocationRequest.getSourceLocation().getLatitudeDegrees(),
              predictFeaturesForEventLocationRequest.getSourceLocation().getLongitudeDegrees(),
              nameToLocationEntry.getValue().getLatitudeDegrees(),
              nameToLocationEntry.getValue().getLongitudeDegrees()
            )
          );
          var featurePredictionDefinitionsList = eventManagerConfiguration.getPredictionDefinitions(
            ph,
            distance,
            receiverDataType,
            receiverBandType
          );
          logger.info("Resolved FeaturePredictionDefinitions from config for location {}: {}", nameToLocationEntry.getValue(), featurePredictionDefinitionsList);
          for (var featurePredictionDefinitions : featurePredictionDefinitionsList) {
            definitionToReceiversAndPhases.computeIfAbsent(featurePredictionDefinitions, k -> Pair.of(new HashSet<>(), new HashSet<>()));
            definitionToReceiversAndPhases.get(featurePredictionDefinitions).getLeft().add(Pair.of(nameToLocationEntry.getKey(), nameToLocationEntry.getValue()));
            definitionToReceiversAndPhases.get(featurePredictionDefinitions).getRight().add(ph);
          }
        }
        definitionToReceiversAndPhases.forEach((def, receiversAndPhases) -> {
          definitionAndPhasesToReceivers.computeIfAbsent(Pair.of(def, receiversAndPhases.getRight()), k -> new HashSet<>());
          definitionAndPhasesToReceivers.get(Pair.of(def, receiversAndPhases.getRight())).addAll(receiversAndPhases.getLeft());
        });
      }
    }
    logger.info("Returning FeaturePredictionDefinitions: {}", definitionAndPhasesToReceivers.keySet());
    return definitionAndPhasesToReceivers;
  }

  private void parseFeaturePredictionsIntoReceivers(
    Multimap<Location, String> locationToReceiver,
    Map<String, FeaturePredictionContainer> receiverToFeaturePredictionContainer,
    FeaturePredictionContainer featurePredictionContainer) {

    featurePredictionContainer.map(Function.identity()).forEach(featurePrediction -> {
      var featurePredictionReceiverNames = Objects.requireNonNull(locationToReceiver.get(featurePrediction.getReceiverLocation()), "Unable to map receiverlocation for this prediction to a receiver name");
      featurePredictionReceiverNames.forEach(featurePredictionReceiverName -> {
        if (receiverToFeaturePredictionContainer.containsKey(featurePredictionReceiverName)) {
          var existingFeaturePredictionContainer = receiverToFeaturePredictionContainer.get(featurePredictionReceiverName);
          if (!existingFeaturePredictionContainer.contains(featurePrediction)) {
            receiverToFeaturePredictionContainer.put(featurePredictionReceiverName, existingFeaturePredictionContainer.union(FeaturePredictionContainer.of(featurePrediction)));
          }
        } else {
          receiverToFeaturePredictionContainer.put(featurePredictionReceiverName, FeaturePredictionContainer.of(featurePrediction));
        }
      });
    });

  }
}

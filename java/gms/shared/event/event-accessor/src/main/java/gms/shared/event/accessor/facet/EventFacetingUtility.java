package gms.shared.event.accessor.facet;

import gms.shared.event.api.EventAccessorInterface;
import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.coi.LocationBehavior;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.PreferredEventHypothesis;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.coi.featureprediction.value.FeaturePredictionValue;
import gms.shared.signaldetection.api.facet.SignalDetectionFacetingUtility;
import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.waveform.api.facet.WaveformFacetingUtility;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static gms.shared.event.accessor.EventAccessor.defaultHypothesesFacetingDefinition;

/**
 * Provides operations to populated faceted objects aggregated by {@link Event} and {@link EventHypothesis} according to
 * a {@link FacetingDefinition}.
 *
 * <p>EventFacetingUtility delegates to {@link StationDefinitionFacetingUtility} when it needs to update {@link Channel}
 * or {@link gms.shared.stationdefinition.coi.station.Station} objects and {@link SignalDetectionFacetingUtility} when it needs to update {@link SignalDetectionHypothesis}
 * objects</p>
 */
@Component
public class EventFacetingUtility {

  private final EventAccessorInterface eventAccessor;
  private final SignalDetectionFacetingUtility signalDetectionFacetingUtility;
  private final StationDefinitionFacetingUtility stationDefinitionFacetingUtility;
  private final WaveformFacetingUtility waveformFacetingUtility;

  private static final Logger logger = LoggerFactory.getLogger(EventFacetingUtility.class);

  @Autowired
  public EventFacetingUtility(EventAccessorInterface eventAccessor,
    SignalDetectionFacetingUtility signalDetectionFacetingUtility,
    StationDefinitionFacetingUtility stationDefinitionFacetingUtility,
    WaveformFacetingUtility waveformFacetingUtility) {
    this.eventAccessor = eventAccessor;
    this.signalDetectionFacetingUtility = signalDetectionFacetingUtility;
    this.stationDefinitionFacetingUtility = stationDefinitionFacetingUtility;
    this.waveformFacetingUtility = waveformFacetingUtility;
  }

  /**
   * Returns a populated {@link Event} based on the {@link FacetingDefinition} that is passed into the method.
   *
   * @param initial The {@link Event} to populate
   * @param stageId The {@link WorkflowDefinitionId} representing the current stage
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link Event}
   */
  public Event populateFacets(Event initial, WorkflowDefinitionId stageId, FacetingDefinition facetingDefinition) {

    facetingNullCheck(initial, facetingDefinition, Event.class.getSimpleName());

    //If the event was not populated with data attempt to populate it
    var initialEventData = initial.getData().orElseGet(
      () -> eventAccessor.findByIds(List.of(initial.getId()), stageId).stream().findFirst().orElseThrow(
        () -> new IllegalStateException(String.format("Could not find an Event with ID:%s", initial.getId()))
      ).getData().orElseThrow(() -> new IllegalStateException("No Event data was found, cannot continue"))
    );

    var rejectedSignalDetectionAssociationsDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.REJECTED_SD_KEY.toString());
    var hypothesesDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.EVENT_HYPOTHESIS_KEY.toString());
    var preferredEventHypothesesDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.PREFERRED_EH_KEY.toString());
    var overallPreferredDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.OVERALL_PREFERRED_KEY.toString());
    var finalEventHypothesisHistoryDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.FINAL_EH_HISTORY_KEY.toString());

    if (!facetingDefinition.isPopulated()) {
      return initial.toEntityReference();
    }

    var populatedEventDataBuilder = initialEventData.toBuilder();

    var rejectedSdhAssociations = getDataUsingNullableFacetingDefinition(
      rejectedSignalDetectionAssociationsDefinition,
      () -> retrieveRejectedSignalDetectionAssociations(stageId, initialEventData, rejectedSignalDetectionAssociationsDefinition),
      initialEventData::getRejectedSignalDetectionAssociations,
      SignalDetection.class.getSimpleName()
    );

    if (hypothesesDefinition == null) {
      logger.warn("Cannot further facet Event without {} faceting definition. Returning default, faceted Event",
        FacetingTypes.EVENT_HYPOTHESIS_KEY.toString());
      hypothesesDefinition = defaultHypothesesFacetingDefinition;
    }

    FacetingDefinition finalHypothesesDefinition = hypothesesDefinition;
    var bridgedEventHypotheses = initialEventData.getEventHypotheses().stream()
      .map(eh -> populateFacets(eh, finalHypothesesDefinition)).flatMap(List::stream).collect(Collectors.toList());

    var preferredEventHypotheses = getDataUsingNullableFacetingDefinition(
      preferredEventHypothesesDefinition,
      () -> buildPreferredEventHypothesis(initialEventData, preferredEventHypothesesDefinition, bridgedEventHypotheses),
      initialEventData::getPreferredEventHypothesisByStage,
      PreferredEventHypothesis.class.getSimpleName()
    );

    var overallPreferred = getDataUsingNullableFacetingDefinition(
      overallPreferredDefinition,
      () -> buildOverallPreferred(initialEventData, overallPreferredDefinition, bridgedEventHypotheses),
      initialEventData::getOverallPreferred,
      EventHypothesis.class.getSimpleName()
    );

    var finalEventHypothesesHistory = getDataUsingNullableFacetingDefinition(
      finalEventHypothesisHistoryDefinition,
      () -> buildFinalEventHypothesisHistory(initialEventData, finalEventHypothesisHistoryDefinition, bridgedEventHypotheses),
      initialEventData::getFinalEventHypothesisHistory,
      EventHypothesis.class.getSimpleName()
    );

    populatedEventDataBuilder
      .setEventHypotheses(bridgedEventHypotheses)
      .setPreferredEventHypothesisByStage(preferredEventHypotheses)
      .setOverallPreferred(overallPreferred.orElse(null))
      .setRejectedSignalDetectionAssociations(rejectedSdhAssociations)
      .setFinalEventHypothesisHistory(finalEventHypothesesHistory);

    return Event.builder()
      .setId(initial.getId())
      .setData(populatedEventDataBuilder.build())
      .autobuild();
  }

  /**
   * Returns a list of populated {@link EventHypothesis} based on the {@link FacetingDefinition} that is passed into the method.
   *
   * <p>Accounts for a rejected {@link Event} having two {@link EventHypothesis}</p>
   *
   * @param initial The {@link EventHypothesis} to populate
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a list of populated {@link EventHypothesis} (2 objects when EH is rejected)
   */
  public List<EventHypothesis> populateFacets(EventHypothesis initial, FacetingDefinition facetingDefinition) {

    return (initial.getData().isEmpty()) ?
      eventAccessor.findHypothesesByIds(List.of(initial.getId())).stream()
        .map(ehResult -> populateFacet(ehResult, facetingDefinition)).collect(Collectors.toList()) :
      List.of(populateFacet(initial, facetingDefinition));
  }

  /**
   * Returns a populated {@link EventHypothesis} based on the {@link FacetingDefinition} that is passed into the method.
   *
   * <p>Accounts for a rejected {@link Event} having two {@link EventHypothesis}</p>
   *
   * @param initial The {@link EventHypothesis} to populate
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link EventHypothesis}
   */
  private EventHypothesis populateFacet(EventHypothesis initial, FacetingDefinition facetingDefinition) {

    var initialEventHypothesisData = initial.getData()
      .orElseThrow(() -> new IllegalStateException("No EventHypothesis data was found, cannot continue"));

    if (facetingDefinition.getClassType().equals(FacetingTypes.DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE.toString())) {
      return EventHypothesis.builder()
        .setId(initial.getId())
        .setData(initialEventHypothesisData.toBuilder().build())
        .autobuild();
    }

    facetingNullCheck(initial, facetingDefinition, EventHypothesis.class.getSimpleName());

    var parentEventHypothesisDefinition = facetingDefinition
      .getFacetingDefinitionByName(FacetingTypes.PARENT_EH_KEY.toString());
    var associatedSignalDetectionHypothesisDefinition = facetingDefinition
      .getFacetingDefinitionByName(FacetingTypes.ASSOCIATED_SDH_KEY.toString());
    var preferredLocationSolutionDefinition = facetingDefinition
      .getFacetingDefinitionByName(FacetingTypes.PREFERRED_LOCATION_SOLUTION_KEY.toString());
    var locationSolutionsDefinition = facetingDefinition
      .getFacetingDefinitionByName(FacetingTypes.LOCATION_SOLUTION_KEY.toString());

    if (!facetingDefinition.isPopulated()) {
      return initial.toEntityReference();
    }
    var populatedEventHypothesisDataBuilder = initialEventHypothesisData.toBuilder();

    var parentEventHypotheses = getDataUsingNullableFacetingDefinition(
      parentEventHypothesisDefinition,
      () -> initialEventHypothesisData.getParentEventHypotheses().stream()
        .map(parentEh -> populateFacets(parentEh, parentEventHypothesisDefinition)).flatMap(List::stream).collect(Collectors.toList()),
      initialEventHypothesisData::getParentEventHypotheses,
      EventHypothesis.class.getSimpleName()
    );

    var associatedSignalDetectionHypotheses = getDataUsingNullableFacetingDefinition(
      associatedSignalDetectionHypothesisDefinition,
      () -> initialEventHypothesisData.getAssociatedSignalDetectionHypotheses().stream()
        .map(signalDetectionHypothesis -> signalDetectionFacetingUtility
          .populateFacets(signalDetectionHypothesis, associatedSignalDetectionHypothesisDefinition))
        .collect(Collectors.toList()),
      initialEventHypothesisData::getAssociatedSignalDetectionHypotheses,
      SignalDetectionHypothesis.class.getSimpleName());

    var locationSolutions = getDataUsingNullableFacetingDefinition(
      locationSolutionsDefinition,
      () -> initialEventHypothesisData.getLocationSolutions().stream()
        .map(ls -> populateFacets(ls, locationSolutionsDefinition)).collect(Collectors.toList()),
      initialEventHypothesisData::getLocationSolutions,
      LocationSolution.class.getSimpleName());

    populatedEventHypothesisDataBuilder
      .setParentEventHypotheses(parentEventHypotheses)
      .setAssociatedSignalDetectionHypotheses(associatedSignalDetectionHypotheses)
      .setLocationSolutions(locationSolutions);

    initialEventHypothesisData.getPreferredLocationSolution().ifPresent(preferredLocationSolution -> {
      if (preferredLocationSolutionDefinition == null || !preferredLocationSolutionDefinition.isPopulated()) {
        populatedEventHypothesisDataBuilder
          .setPreferredLocationSolution(initialEventHypothesisData.getPreferredLocationSolution().get().toEntityReference());
      } else {
        populatedEventHypothesisDataBuilder
          .setPreferredLocationSolution(locationSolutions.stream()
            .filter(ls -> ls.getId() == initialEventHypothesisData.getPreferredLocationSolution().get().getId())
            .findFirst().orElseThrow());
      }
    });

    return EventHypothesis.builder()
      .setId(initial.getId())
      .setData(populatedEventHypothesisDataBuilder.build())
      .autobuild();
  }

  /**
   * Returns a populated {@link LocationSolution} based on the {@link FacetingDefinition} that is passed into the method.
   *
   * @param initial The {@link LocationSolution} to populate
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link LocationSolution}
   */
  private LocationSolution populateFacets(LocationSolution initial, FacetingDefinition facetingDefinition) {
    facetingNullCheck(initial, facetingDefinition, LocationSolution.class.getSimpleName());

    var initialLocationSolutionData = initial.getData().orElseThrow(() -> new IllegalStateException("Cannot process an ID-only LocationSolution"));

    var featurePredictionDefinition = facetingDefinition
      .getFacetingDefinitionByName(FacetingTypes.FEATURE_PREDICTIONS_KEY.toString());
    var featureMeasurementDefinition = facetingDefinition
      .getFacetingDefinitionByName(FacetingTypes.FEATURE_MEASUREMENTS_KEY.toString());

    if (!facetingDefinition.isPopulated()) {
      logger.warn("LocationSolution set to be not populated so returning original LocationSolution");
      return initial.toEntityReference();
    }
    var populatedLocationSolutionDataBuilder = initialLocationSolutionData.toBuilder();
    Instant arrivalTime = null;

    if (featureMeasurementDefinition != null) {
      var arrivalLocationBehavior = initialLocationSolutionData.getLocationBehaviors().stream()
        .filter(lb -> lb.getFeatureMeasurement().getFeatureMeasurementType().equals(FeatureMeasurementTypes.ARRIVAL_TIME))
        .findFirst().orElseGet(() -> {
          logger.warn("Arrival Time Feature Measurement is missing from faceted LocationSolution Data. Returning original FeatureMeasurement");
          return null;
        });
      if (arrivalLocationBehavior != null) {
        var arrivalMeasurement = (FeatureMeasurement<ArrivalTimeMeasurementValue>) arrivalLocationBehavior.getFeatureMeasurement();
        arrivalTime = arrivalMeasurement.getMeasurementValue().getArrivalTime().getValue();
      }
    }

    Instant finalArrivalTime = arrivalTime;
    var newLocationBehaviors = initialLocationSolutionData.getLocationBehaviors().stream().map(locationBehavior ->
      {
        var initialFeaturePrediction = locationBehavior.getFeaturePrediction();
        var featurePrediction = initialFeaturePrediction.isEmpty() ?
          Optional.empty() :
          Optional.of(
            getDataUsingNullableFacetingDefinition(featurePredictionDefinition,
              () -> populateFacets(initialFeaturePrediction.get(), featurePredictionDefinition),
              initialFeaturePrediction::get,
              FeaturePrediction.class.getSimpleName()));

        FeatureMeasurement<?> featureMeasurement;


        featureMeasurement = getDataUsingNullableFacetingDefinition(featureMeasurementDefinition,
          () -> {
            if (featureMeasurementDefinition.isPopulated() && finalArrivalTime != null) {
              return signalDetectionFacetingUtility.populateFacets(locationBehavior.getFeatureMeasurement(), featureMeasurementDefinition, finalArrivalTime);
            } else {
              return locationBehavior.getFeatureMeasurement();
            }
          },
          locationBehavior::getFeatureMeasurement,
          FeatureMeasurement.class.getSimpleName());

        return LocationBehavior.from(
          locationBehavior.getResidual(),
          locationBehavior.getWeight(),
          locationBehavior.isDefining(),
          (Optional<FeaturePrediction<? extends FeaturePredictionValue<?, ?, ?>>>) featurePrediction,
          featureMeasurement
        );
      }
    ).collect(Collectors.toList());

    populatedLocationSolutionDataBuilder
      .setLocationBehaviors(newLocationBehaviors)
      .setFeaturePredictions(FeaturePredictionContainer
        .create(newLocationBehaviors.stream()
          .map(LocationBehavior::getFeaturePrediction)
          .flatMap(Optional::stream).collect(Collectors.toList())));

    return LocationSolution.builder()
      .setId(initial.getId())
      .setData(populatedLocationSolutionDataBuilder.build())
      .autobuild();
  }

  /**
   * Returns a populated {@link FeaturePrediction} based on the {@link FacetingDefinition} that is passed into the method.
   *
   * @param initial The {@link FeaturePrediction} to populate
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link FeaturePrediction}
   */
  private <T extends FeaturePredictionValue<?, ?, ?>> FeaturePrediction<T> populateFacets(
    FeaturePrediction<T> initial,
    FacetingDefinition facetingDefinition) {
    facetingNullCheck(initial, facetingDefinition, FeaturePrediction.class.getSimpleName());

    var channelDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.CHANNEL_KEY.toString());
    var channelSegmentDefinition = facetingDefinition.getFacetingDefinitionByName(FacetingTypes.CHANNEL_SEGMENT_KEY.toString());

    var finalChannel = initial.getChannel().isPresent() && channelDefinition != null ?
      buildChannel(initial.getChannel().orElseThrow(), channelDefinition) :
      initial.getChannel();
    var finalChannelSegment = initial.getPredictionChannelSegment().isPresent() && channelSegmentDefinition != null ?
      buildChannelSegment(initial.getPredictionChannelSegment().orElseThrow(), channelSegmentDefinition) :
      initial.getPredictionChannelSegment();

    return FeaturePrediction.<T>builder()
      .setPredictionValue(initial.getPredictionValue())
      .setPredictionType(initial.getPredictionType())
      .setPhase(initial.getPhase())
      .setExtrapolated(initial.isExtrapolated())
      .setSourceLocation(initial.getSourceLocation())
      .setChannel(finalChannel)
      .setPredictionChannelSegment(finalChannelSegment)
      .setReceiverLocation(initial.getReceiverLocation())
      .build();
  }

  /**
   * Returns a populated {@link Channel} based on the {@link FacetingDefinition} that is passed into the method.
   * Calls the {@link StationDefinitionFacetingUtility} to facet channel.
   *
   * @param channel The {@link Channel} to populate
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link Channel}
   */
  private Optional<Channel> buildChannel(Channel channel, FacetingDefinition facetingDefinition) {
    facetingNullCheck(channel, facetingDefinition, Channel.class.getSimpleName());

    if (!facetingDefinition.isPopulated()) {
      return Optional.ofNullable(channel.toEntityReference());
    }
    return Optional.ofNullable(stationDefinitionFacetingUtility.populateFacets(
      channel,
      facetingDefinition,
      channel.getEffectiveAt().orElseThrow(() -> new IllegalStateException("Cannot facet a channel without an EffectiveAt time"))));
  }

  /**
   * Returns a populated {@link ChannelSegment} based on the {@link FacetingDefinition} that is passed into the method.
   * Calls the {@link WaveformFacetingUtility} to facet channel segment.
   *
   * @param channelSegment The {@link ChannelSegment} to populate
   * @param facetingDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link ChannelSegment}
   */
  private <T extends Timeseries> Optional<ChannelSegment<T>> buildChannelSegment(
    ChannelSegment<T> channelSegment,
    FacetingDefinition facetingDefinition) {
    facetingNullCheck(channelSegment, facetingDefinition, ChannelSegment.class.getSimpleName());

    if (!facetingDefinition.isPopulated()) {
      return Optional.of(channelSegment);
    }
    return Optional.of((ChannelSegment<T>) waveformFacetingUtility.populateFacets(channelSegment, facetingDefinition));
  }

  /**
   * Returns a list of {@link EventHypothesis} history based on the initial {@link Event.Data},
   * {@link FacetingDefinition} and any provided EventHypotheses that are passed into the method.
   *
   * @param initialEventData The {@link Event.Data} of interest
   * @param finalEventHypothesisHistoryDefinition The {@link FacetingDefinition} defining which fields to populate
   * @param bridgedEventHypotheses The list of bridged EventHypotheses related to the provided event
   * @return a populated {@link List} of {@link EventHypothesis}
   */
  private List<EventHypothesis> buildFinalEventHypothesisHistory(Event.Data initialEventData,
    FacetingDefinition finalEventHypothesisHistoryDefinition, Collection<EventHypothesis> bridgedEventHypotheses) {
    if (!finalEventHypothesisHistoryDefinition.isPopulated()) {
      return initialEventData.getFinalEventHypothesisHistory()
        .stream()
        .map(EventHypothesis::toEntityReference)
        .collect(Collectors.toList());
    }

    List<EventHypothesis> eventHypothesesList = new ArrayList<>();

    initialEventData.getFinalEventHypothesisHistory().forEach(eventHypothesis -> {
      var bridgedEventHypothesisOpt = bridgedEventHypotheses
        .stream()
        .filter(eventHypothesis2 -> eventHypothesis2.getId().equals(eventHypothesis.getId()))
        .findFirst();
      bridgedEventHypothesisOpt.ifPresentOrElse(eventHypothesesList::add, () ->
        logger.warn("EventHypothesis with ID:[{}] from EventHypothesisHistory list was not found, cannot add to Event history"));
    });
    return eventHypothesesList;
  }

  /**
   * Returns an {@link Optional} {@link EventHypothesis} based on the provided initial {@link Event.Data},
   * {@link FacetingDefinition} and any provided EventHypotheses that are passed into the method.
   *
   * @param initialEventData The {@link Event.Data} of interest
   * @param overallPreferredDefinition The {@link FacetingDefinition} defining which fields to populate
   * @param bridgedEventHypotheses The list of bridged EventHypotheses related to the provided event
   * @return a populated {@link List} of {@link EventHypothesis}
   */
  private Optional<EventHypothesis> buildOverallPreferred(Event.Data initialEventData,
    FacetingDefinition overallPreferredDefinition, Collection<EventHypothesis> bridgedEventHypotheses) {

    var eventHypothesisOpt = initialEventData.getOverallPreferred();
    EventHypothesis eventHypothesis;
    if (eventHypothesisOpt.isPresent()) {
      eventHypothesis = eventHypothesisOpt.get();
    } else {
      return Optional.empty();
    }

    if (overallPreferredDefinition.isPopulated()) {
      var foundEventHypothesisOpt = bridgedEventHypotheses
        .stream()
        .filter(eventHypothesis1 -> eventHypothesis1.getId().equals(eventHypothesis.getId()))
        .findFirst();

      if (foundEventHypothesisOpt.isEmpty()) {
        logger.warn("EventHypothesis with id:[{}] was not found.  The Overall Preferred EventHypothesis will not be added to the event",
          eventHypothesis.getId());
      }

      return foundEventHypothesisOpt;

    } else {
      return Optional.of(eventHypothesis.toEntityReference());
    }

  }

  /**
   * Returns a {@link List} of {@link EventHypothesis} based on the initial {@link Event.Data},
   * {@link FacetingDefinition} and any provided EventHypotheses that are passed into the method.
   *
   * <p>Searches the bridgedEventHypotheses for objects with the IDs found in `initialEventData::preferredEventHypotheses`</p>
   *
   * @param initialEventData The {@link Event.Data} of interest
   * @param preferredEventHypothesesDefinition The {@link FacetingDefinition} defining which fields to populate
   * @param bridgedEventHypotheses The list of bridged EventHypotheses related to the provided event
   * @return a populated {@link List} of {@link EventHypothesis}
   */
  private List<PreferredEventHypothesis> buildPreferredEventHypothesis(Event.Data initialEventData,
    FacetingDefinition preferredEventHypothesesDefinition, Collection<EventHypothesis> bridgedEventHypotheses) {

    if (!preferredEventHypothesesDefinition.isPopulated()) {
      return initialEventData.getPreferredEventHypothesisByStage().stream().map(preferredEventHypothesis ->
        PreferredEventHypothesis.from(
          preferredEventHypothesis.getStage(),
          preferredEventHypothesis.getPreferredBy(),
          preferredEventHypothesis.getPreferred().toEntityReference())
      ).collect(Collectors.toList());
    }

    List<PreferredEventHypothesis> preferredEventHypotheses = new ArrayList<>();
    initialEventData.getPreferredEventHypothesisByStage().forEach(preferredEventHypothesis -> {
      var facetedEventHypothesis = preferredEventHypothesis.getPreferred();
      var bridgedEventHypothesisOpt = bridgedEventHypotheses
        .stream()
        .filter(eventHypothesis -> eventHypothesis.getId().equals(facetedEventHypothesis.getId()))
        .findFirst();

      bridgedEventHypothesisOpt.ifPresentOrElse(eventHypothesis ->
        preferredEventHypotheses.add(
          PreferredEventHypothesis.from(
            preferredEventHypothesis.getStage(),
            preferredEventHypothesis.getPreferredBy(),
            eventHypothesis)), () ->
        preferredEventHypotheses.add(preferredEventHypothesis));
    });

    return preferredEventHypotheses;
  }

  /**
   * Returns a {@link List} of {@link SignalDetection} based on the initial {@link Event.Data},
   * {@link FacetingDefinition} and {@link WorkflowDefinitionId}
   *
   * @param stageId The {@link WorkflowDefinitionId} of interest
   * @param initialEventData The {@link Event.Data} of interest
   * @param rejectedSignalDetectionAssociationsDefinition The {@link FacetingDefinition} defining which fields to populate
   * @return a populated {@link List} of {@link SignalDetection}
   */
  private List<SignalDetection> retrieveRejectedSignalDetectionAssociations(WorkflowDefinitionId stageId,
    Event.Data initialEventData,
    FacetingDefinition rejectedSignalDetectionAssociationsDefinition) {
    if (!rejectedSignalDetectionAssociationsDefinition.isPopulated()) {
      return
        initialEventData.getRejectedSignalDetectionAssociations().stream().map(
          SignalDetection::toEntityReference).filter(Objects::nonNull).collect(Collectors.toList());
    }
    return
      initialEventData.getRejectedSignalDetectionAssociations().stream().map(
        signalDetection -> signalDetectionFacetingUtility.populateFacets(signalDetection,
          rejectedSignalDetectionAssociationsDefinition,
          stageId)
      ).filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * Encapsulates assertions of state before faceting
   *
   * @param initialObject The {@link Object} to be faceted
   * @param facetingDefinition The {@link FacetingDefinition} declaring how to populate initialObject
   * @param className A {@link String} representing the class of initialObject
   */
  private void facetingNullCheck(Object initialObject, FacetingDefinition facetingDefinition, String className) {
    checkNotNull(initialObject, "Initial %s cannot be null", className);
    checkNotNull(facetingDefinition, "FacetingDefinition for %s cannot be null", className);
    checkState(facetingDefinition.getClassType().equals(className), "FacetingDefinition must be for the %s class", className);
  }

  /**
   * Returns Data based on the {@link FacetingDefinition} that is passed into the method. If the {@link FacetingDefinition}
   * is null, logs a warning and returns the facetingDefinitionNull result. If non-null, returns the FacetingDefinitionNonNull
   * result.
   *
   * @param facetingDefinition The {@link FacetingDefinition} to check for a non-null state
   * @param facetingDefinitionNonNull The function to execute if there is a {@link FacetingDefinition}
   * @param facetingDefinitionNull The function to execute if there is not a {@link FacetingDefinition}
   * @return Data of the type associated with the {@link FacetingDefinition}
   */
  private <T> T getDataUsingNullableFacetingDefinition(FacetingDefinition facetingDefinition,
    Supplier<T> facetingDefinitionNonNull, Supplier<T> facetingDefinitionNull, String className) {

    if (facetingDefinition != null) {
      return facetingDefinitionNonNull.get();
    } else {
      logger.warn("No {} FacetingDefinition provided, returning original {}", className, className);
      return facetingDefinitionNull.get();
    }

  }
}

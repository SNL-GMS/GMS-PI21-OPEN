package gms.shared.event.accessor;

import gms.shared.event.accessor.facet.EventFacetingUtility;
import gms.shared.event.api.EventAccessorInterface;
import gms.shared.event.coi.Event;
import gms.shared.event.coi.EventHypothesis;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.LocationBehavior;
import gms.shared.event.coi.LocationSolution;
import gms.shared.event.coi.MagnitudeType;
import gms.shared.event.coi.featureprediction.FeaturePrediction;
import gms.shared.event.coi.featureprediction.FeaturePredictionContainer;
import gms.shared.event.coi.featureprediction.type.FeaturePredictionType;
import gms.shared.event.coi.featureprediction.value.NumericFeaturePredictionValue;
import gms.shared.signaldetection.api.facet.SignalDetectionFacetingUtility;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.waveform.api.facet.WaveformFacetingUtility;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static gms.shared.event.accessor.facet.FacetingTypes.ASSOCIATED_SDH_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.CHANNEL_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.CHANNEL_SEGMENT_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.CHANNEL_SEGMENT_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.CHANNEL_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.EVENT_HYPOTHESIS_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.EVENT_HYPOTHESIS_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.EVENT_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.FEATURE_MEASUREMENTS_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.FEATURE_MEASUREMENT_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.FEATURE_PREDICTIONS_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.FEATURE_PREDICTION_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.FINAL_EH_HISTORY_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.LOCATION_SOLUTION_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.LOCATION_SOLUTION_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.OVERALL_PREFERRED_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.PREFERRED_EH_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.PREFERRED_EH_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.PREFERRED_LOCATION_SOLUTION_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.REJECTED_SD_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.SDH_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.SD_HYPOTHESES_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.SIGNAL_DETECTION_TYPE;
import static gms.shared.event.accessor.facet.FacetingTypes.STATION_KEY;
import static gms.shared.event.accessor.facet.FacetingTypes.STATION_TYPE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventFacetingUtilityTests {

  private static final FacetingDefinition channelFacetingDefinition = FacetingDefinition.builder()
    .setClassType(CHANNEL_TYPE.getValue())
    .setPopulated(true)
    .build();

  private static final FacetingDefinition channelSegmentFacetingDefinition = FacetingDefinition.builder()
    .setClassType(CHANNEL_SEGMENT_TYPE.getValue())
    .setPopulated(true)
    .build();

  private static final FacetingDefinition featureMeasurementFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FEATURE_MEASUREMENT_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(CHANNEL_KEY.toString(), channelFacetingDefinition)
    .addFacetingDefinitions(CHANNEL_SEGMENT_KEY.toString(), channelSegmentFacetingDefinition)
    .build();

  private static final FacetingDefinition featurePredictionFacetingDefinition = FacetingDefinition.builder()
    .setClassType(FEATURE_PREDICTION_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(CHANNEL_KEY.toString(), channelFacetingDefinition)
    .addFacetingDefinitions(CHANNEL_SEGMENT_KEY.toString(), channelSegmentFacetingDefinition)
    .build();

  private static final FacetingDefinition stationFacetingDefinition = FacetingDefinition.builder()
    .setClassType(STATION_TYPE.getValue())
    .setPopulated(false)
    .build();

  private static final FacetingDefinition signalDetectionHypothesisFacetingDefinition = FacetingDefinition.builder()
    .setClassType(SDH_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(STATION_KEY.toString(), stationFacetingDefinition)
    .addFacetingDefinitions(FEATURE_MEASUREMENTS_KEY.toString(), featureMeasurementFacetingDefinition)
    .build();

  private static final FacetingDefinition rejectedSignalDetectionAssociationsFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(SIGNAL_DETECTION_TYPE.toString())
      .setPopulated(true)
      .addFacetingDefinitions(SD_HYPOTHESES_KEY.toString(), signalDetectionHypothesisFacetingDefinition)
      .addFacetingDefinitions(STATION_KEY.toString(), stationFacetingDefinition)
      .build();

  private static final FacetingDefinition preferredEventHypothesesFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(PREFERRED_EH_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition overallPreferredFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(SIGNAL_DETECTION_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition finalEventHypothesisHistoryFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition defaultHypothesesFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(DEFAULT_FACETED_EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition associatedSignalDetectionHypothesisFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(SDH_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition preferredLocationSolutionFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(LOCATION_SOLUTION_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition locationSolutionFacetingDefinitionWithNullInnerDefinitions =
    FacetingDefinition.builder()
      .setClassType(LOCATION_SOLUTION_TYPE.toString())
      .setPopulated(true)
      .build();

  private static final FacetingDefinition locationSolutionFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(LOCATION_SOLUTION_TYPE.toString())
      .setPopulated(true)
      .addFacetingDefinitions(FEATURE_MEASUREMENTS_KEY.toString(), featureMeasurementFacetingDefinition)
      .addFacetingDefinitions(FEATURE_PREDICTIONS_KEY.toString(), featurePredictionFacetingDefinition)
      .build();

  private static final FacetingDefinition hypothesisFacetingDefinitionPopulateLocationSolution = FacetingDefinition.builder()
    .setClassType(EVENT_HYPOTHESIS_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(ASSOCIATED_SDH_KEY.toString(), associatedSignalDetectionHypothesisFacetingDefinition)
    .addFacetingDefinitions(PREFERRED_LOCATION_SOLUTION_KEY.toString(), preferredLocationSolutionFacetingDefinition)
    .addFacetingDefinitions(LOCATION_SOLUTION_KEY.toString(), locationSolutionFacetingDefinition)
    .build();

  private static final FacetingDefinition hypothesesFacetingDefinition =
    FacetingDefinition.builder()
      .setClassType(EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(true)
      .addFacetingDefinitions(ASSOCIATED_SDH_KEY.toString(), associatedSignalDetectionHypothesisFacetingDefinition)
      .addFacetingDefinitions(PREFERRED_LOCATION_SOLUTION_KEY.toString(), preferredLocationSolutionFacetingDefinition)
      .addFacetingDefinitions(LOCATION_SOLUTION_KEY.toString(), locationSolutionFacetingDefinitionWithNullInnerDefinitions)
      .build();

  private static final FacetingDefinition eventFacetingDefinition = FacetingDefinition.builder()
    .setClassType(EVENT_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(REJECTED_SD_KEY.toString(), rejectedSignalDetectionAssociationsFacetingDefinition)
    .addFacetingDefinitions(EVENT_HYPOTHESIS_KEY.toString(), defaultHypothesesFacetingDefinition)
    .addFacetingDefinitions(PREFERRED_EH_KEY.toString(), preferredEventHypothesesFacetingDefinition)
    .addFacetingDefinitions(OVERALL_PREFERRED_KEY.toString(), overallPreferredFacetingDefinition)
    .addFacetingDefinitions(FINAL_EH_HISTORY_KEY.toString(), finalEventHypothesisHistoryFacetingDefinition)
    .build();

  private static final FacetingDefinition rejectedSignalDetectionAssociationsFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(SIGNAL_DETECTION_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition preferredEventHypothesesFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(PREFERRED_EH_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition overallPreferredFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(SIGNAL_DETECTION_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition finalEventHypothesisHistoryFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition associatedSignalDetectionHypothesisFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(SDH_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition preferredLocationSolutionFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(LOCATION_SOLUTION_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition locationSolutionFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(LOCATION_SOLUTION_TYPE.toString())
      .setPopulated(false)
      .build();

  private static final FacetingDefinition hypothesesFacetingDefinitionNotPopulated =
    FacetingDefinition.builder()
      .setClassType(EVENT_HYPOTHESIS_TYPE.toString())
      .setPopulated(true)
      .addFacetingDefinitions(ASSOCIATED_SDH_KEY.toString(), associatedSignalDetectionHypothesisFacetingDefinitionNotPopulated)
      .addFacetingDefinitions(PREFERRED_LOCATION_SOLUTION_KEY.toString(), preferredLocationSolutionFacetingDefinitionNotPopulated)
      .addFacetingDefinitions(LOCATION_SOLUTION_KEY.toString(), locationSolutionFacetingDefinitionNotPopulated)
      .build();

  private static final FacetingDefinition eventFacetingDefinitionNotPopulated = FacetingDefinition.builder()
    .setClassType(EVENT_TYPE.toString())
    .setPopulated(true)
    .addFacetingDefinitions(REJECTED_SD_KEY.toString(), rejectedSignalDetectionAssociationsFacetingDefinitionNotPopulated)
    .addFacetingDefinitions(EVENT_HYPOTHESIS_KEY.toString(), FacetingDefinition
      .builder().setClassType(EVENT_HYPOTHESIS_TYPE.toString()).setPopulated(false).build())
    .addFacetingDefinitions(PREFERRED_EH_KEY.toString(), preferredEventHypothesesFacetingDefinitionNotPopulated)
    .addFacetingDefinitions(OVERALL_PREFERRED_KEY.toString(), overallPreferredFacetingDefinitionNotPopulated)
    .addFacetingDefinitions(FINAL_EH_HISTORY_KEY.toString(), finalEventHypothesisHistoryFacetingDefinitionNotPopulated)
    .build();

  private static final FacetingDefinition eventFacetingDefinitionEntityOnly = FacetingDefinition.builder()
    .setClassType(EVENT_TYPE.toString())
    .setPopulated(false)
    .build();

  private static final FacetingDefinition eventFacetingDefinitionNullHypothesis = FacetingDefinition.builder()
    .setClassType(EVENT_TYPE.toString())
    .setPopulated(true)
    .build();

  private static final FacetingDefinition hypothesesFacetingDefinitionEntityOnly = FacetingDefinition.builder()
    .setClassType(EVENT_HYPOTHESIS_TYPE.toString())
    .setPopulated(false)
    .build();

  private static EventFacetingUtility eventFacetingUtility;

  @Mock
  private EventAccessorInterface mockEventAccessor;

  @Mock
  private SignalDetectionFacetingUtility mockSignalDetectionFacetingUtility;

  @Mock
  private StationDefinitionFacetingUtility mockStationDefinitionFacetingUtility;

  @Mock
  private WaveformFacetingUtility mockWaveformFacetingUtility;

  @BeforeEach
  public void beforeEach() {
    eventFacetingUtility = new EventFacetingUtility(mockEventAccessor,
      mockSignalDetectionFacetingUtility, mockStationDefinitionFacetingUtility, mockWaveformFacetingUtility);
  }

  @Test
  void testPopulateFacetsForEvents() {

    var stageId = WorkflowDefinitionId.from("AL1");
    var eventId = UUID.randomUUID();
    var dummyEvent = EventTestFixtures.generateDummyEvent(
      eventId,
      stageId,
      "Org",
      "analyst",
      Instant.EPOCH,
      1.0,
      MagnitudeType.MB);

    var signalDetection = SignalDetection.createEntityReference(UUID.randomUUID());
    var event = Event.builder()
      .setId(eventId)
      .setData(dummyEvent.getData().orElseThrow().toBuilder()
        .setRejectedSignalDetectionAssociations(List.of(signalDetection))
        .build())
      .build();

    when(mockSignalDetectionFacetingUtility
      .populateFacets(signalDetection, rejectedSignalDetectionAssociationsFacetingDefinition, stageId))
      .thenReturn(signalDetection);

    assertDoesNotThrow(() -> eventFacetingUtility.populateFacets(event, stageId, eventFacetingDefinitionNullHypothesis));
    assertDoesNotThrow(() -> eventFacetingUtility.populateFacets(event, stageId, eventFacetingDefinition));

    var notPopulatedEvent = eventFacetingUtility.populateFacets(event, stageId, eventFacetingDefinitionNotPopulated);

    assertTrue(notPopulatedEvent.getData().isPresent());
    var notPopulatedEventData = notPopulatedEvent.getData().orElseThrow();
    assertTrue(notPopulatedEventData.getEventHypotheses().stream()
      .allMatch(eventHypothesis1 -> eventHypothesis1.getData().isEmpty()));

    assertTrue(notPopulatedEventData.getFinalEventHypothesisHistory().stream()
      .allMatch(eventHypothesis1 -> eventHypothesis1.getData().isEmpty()));

    assertTrue(notPopulatedEventData.getRejectedSignalDetectionAssociations().stream()
      .allMatch(eventHypothesis1 -> eventHypothesis1.getData().isEmpty()));

    assertTrue(notPopulatedEventData.getOverallPreferred().stream()
      .allMatch(eventHypothesis1 -> eventHypothesis1.getData().isEmpty()));

    assertTrue(notPopulatedEventData.getPreferredEventHypothesisByStage().stream()
      .allMatch(preferredEventHypothesis -> preferredEventHypothesis.getPreferred().getData().isEmpty()));

    assertTrue(eventFacetingUtility.populateFacets(event, stageId, eventFacetingDefinitionEntityOnly).getData().isEmpty());
  }

  @Test
  void testPopulateFacetsForEventHypotheses() {

    var uuid = UUID.randomUUID();
    var eventHypothesisId = EventHypothesis.Id.from(uuid, uuid);
    var dummyEventHypothesis = EventTestFixtures.generateDummyEventHypothesis(
      uuid,
      1.0,
      Instant.EPOCH,
      MagnitudeType.MB,
      DoubleValue.from(2.0, Optional.of(3.0), Units.DEGREES),
      List.of(EventHypothesis.builder()
        .setId(eventHypothesisId)
        .build())
    );

    var dummyEventHypothesisIdOnly = EventHypothesis.builder().setId(eventHypothesisId).build();

    var signalDetectionHypothesis = SignalDetectionHypothesis.builder()
      .setId(SignalDetectionHypothesisId.from(uuid, uuid))
      .setData(SignalDetectionHypothesis.Data.builder().build())
      .build();

    var eventHypothesis = dummyEventHypothesis.toBuilder()
      .setId(eventHypothesisId)
      .setData(dummyEventHypothesis.getData().orElseThrow().toBuilder().setAssociatedSignalDetectionHypotheses(List.of(signalDetectionHypothesis)).build())
      .build();

    when(mockSignalDetectionFacetingUtility
      .populateFacets(signalDetectionHypothesis, associatedSignalDetectionHypothesisFacetingDefinition))
      .thenReturn(signalDetectionHypothesis);

    when(mockEventAccessor.findHypothesesByIds(List.of(dummyEventHypothesisIdOnly.getId())))
      .thenReturn(List.of(eventHypothesis));

    assertDoesNotThrow(() -> eventFacetingUtility.populateFacets(dummyEventHypothesisIdOnly, hypothesesFacetingDefinition));

    assertDoesNotThrow(() -> eventFacetingUtility.populateFacets(eventHypothesis, hypothesesFacetingDefinition));

    when(mockSignalDetectionFacetingUtility
      .populateFacets(signalDetectionHypothesis, associatedSignalDetectionHypothesisFacetingDefinitionNotPopulated))
      .thenReturn(signalDetectionHypothesis.toEntityReference());

    var notPopulatedEventHypothesis = eventFacetingUtility.populateFacets(eventHypothesis, hypothesesFacetingDefinitionNotPopulated);

    assertEquals(1, notPopulatedEventHypothesis.size());
    var notPopulatedEventHypothesisData = notPopulatedEventHypothesis.get(0).getData();
    assertTrue(notPopulatedEventHypothesis.get(0).getData().orElseThrow().getAssociatedSignalDetectionHypotheses().stream()
      .allMatch(associatedSDH -> associatedSDH.getData().isEmpty()));

    assertTrue(notPopulatedEventHypothesisData.orElseThrow().getPreferredLocationSolution().stream()
      .allMatch(preferredLocationSolution -> preferredLocationSolution.getData().isEmpty()));

    assertTrue(notPopulatedEventHypothesisData.orElseThrow().getLocationSolutions().stream()
      .allMatch(locationSolution -> locationSolution.getData().isEmpty()));

    assertEquals(1, eventFacetingUtility.populateFacets(eventHypothesis, hypothesesFacetingDefinitionEntityOnly).size());
    assertTrue(eventFacetingUtility.populateFacets(eventHypothesis, hypothesesFacetingDefinitionEntityOnly).get(0).getData().isEmpty());
  }

  @Test
  void testPopulateFacetsForRejectedEventHypotheses() {

    var uuid = UUID.randomUUID();
    var eventHypothesisId = EventHypothesis.Id.from(uuid, uuid);
    var dummyEventHypothesis = EventTestFixtures.generateDummyEventHypothesis(
      uuid,
      1.0,
      Instant.EPOCH,
      MagnitudeType.MB,
      DoubleValue.from(2.0, Optional.of(3.0), Units.DEGREES),
      List.of(EventHypothesis.builder()
        .setId(eventHypothesisId)
        .build())
    );

    var dummyEventHypothesisIdOnly = EventHypothesis.builder().setId(eventHypothesisId).build();

    var signalDetectionHypothesis = SignalDetectionHypothesis.builder()
      .setId(SignalDetectionHypothesisId.from(uuid, uuid))
      .setData(SignalDetectionHypothesis.Data.builder().build())
      .build();

    var eventHypothesis = dummyEventHypothesis.toBuilder()
      .setId(eventHypothesisId)
      .setData(dummyEventHypothesis.getData().orElseThrow().toBuilder().setAssociatedSignalDetectionHypotheses(List.of(signalDetectionHypothesis)).build())
      .build();

    //add here
    //test rejected EH
    var rejectedEventData = eventHypothesis.getData().orElseThrow().toBuilder()
      .setRejected(true)
      .setLocationSolutions(Set.of())
      .setPreferredLocationSolution(null)
      .build();
    var rejectedEventHypothesis = eventHypothesis.toBuilder().setData(rejectedEventData).build();

    when(mockSignalDetectionFacetingUtility
      .populateFacets(signalDetectionHypothesis, associatedSignalDetectionHypothesisFacetingDefinition))
      .thenReturn(signalDetectionHypothesis);

    when(mockEventAccessor.findHypothesesByIds(List.of(dummyEventHypothesisIdOnly.getId())))
      .thenReturn(List.of(eventHypothesis, rejectedEventHypothesis));

    assertDoesNotThrow(() -> eventFacetingUtility.populateFacets(dummyEventHypothesisIdOnly, hypothesesFacetingDefinition));

    assertDoesNotThrow(() -> eventFacetingUtility.populateFacets(eventHypothesis, hypothesesFacetingDefinition));

    when(mockSignalDetectionFacetingUtility
      .populateFacets(signalDetectionHypothesis, associatedSignalDetectionHypothesisFacetingDefinitionNotPopulated))
      .thenReturn(signalDetectionHypothesis.toEntityReference());
    var eventHypothesisList = eventFacetingUtility
      .populateFacets(dummyEventHypothesisIdOnly, hypothesesFacetingDefinitionNotPopulated);

    assertEquals(2, eventHypothesisList.size());
    var notPopulatedEventHypothesisData = eventHypothesisList.get(0).getData();
    assertTrue(eventHypothesisList.get(0).getData().orElseThrow().getAssociatedSignalDetectionHypotheses().stream()
      .allMatch(associatedSDH -> associatedSDH.getData().isEmpty()));

    assertTrue(notPopulatedEventHypothesisData.orElseThrow().getPreferredLocationSolution().stream()
      .allMatch(preferredLocationSolution -> preferredLocationSolution.getData().isEmpty()));

    assertTrue(notPopulatedEventHypothesisData.orElseThrow().getLocationSolutions().stream()
      .allMatch(locationSolution -> locationSolution.getData().isEmpty()));

    var rejectedEventHypothesisData = eventHypothesisList.get(1).getData().orElseThrow();
    assertTrue(rejectedEventHypothesisData.isRejected());
    assertTrue(rejectedEventHypothesisData.getLocationSolutions().isEmpty());
    assertTrue(rejectedEventHypothesisData.getPreferredLocationSolution().isEmpty());
  }

  @Test
  void testPopulateFacetsForLocationSolution() {

    var uuid = UUID.randomUUID();
    var eventHypothesisId = EventHypothesis.Id.from(uuid, uuid);
    var dummyEventHypothesis = EventTestFixtures.generateDummyEventHypothesis(
      uuid,
      1.0,
      Instant.EPOCH,
      MagnitudeType.MB,
      DoubleValue.from(2.0, Optional.of(3.0), Units.DEGREES),
      List.of(EventHypothesis.builder()
        .setId(eventHypothesisId)
        .build())
    );

    var signalDetectionHypothesis = SignalDetectionHypothesis.builder()
      .setId(SignalDetectionHypothesisId.from(uuid, uuid))
      .setData(SignalDetectionHypothesis.Data.builder().build())
      .build();

    var channelSegment = ChannelSegment.builder()
      .setId(ChannelSegmentDescriptor.from(Channel.builder().setName("test").autoBuild(), Instant.MIN, Instant.MAX, Instant.EPOCH))
      .build();

    var featurePrediction = FeaturePrediction.<NumericFeaturePredictionValue>builder()
      .setPredictionValue(
        NumericFeaturePredictionValue.from(FeatureMeasurementTypes.SLOWNESS,
          EventTestFixtures.NUMERIC_MEASUREMENT_VALUE,
          Map.of(),
          Set.of(EventTestFixtures.FEATURE_PREDICTION_COMPONENT)))
      .setChannel(Optional.of(CHANNEL))
      .setSourceLocation(EventTestFixtures.EVENT_LOCATION)
      .setPhase(PhaseType.P)
      .setPredictionType(FeaturePredictionType.SLOWNESS_PREDICTION_TYPE)
      .setPredictionChannelSegment(Optional.of(channelSegment))
      .setReceiverLocation(CHANNEL.getLocation())
      .setExtrapolated(false)
      .build();

    var featureMeasurement = EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT;

    var locationBehavior = LocationBehavior.from(
      Optional.of(1.0),
      Optional.of(2.0),
      true,
      Optional.of(featurePrediction),
      featureMeasurement
    );

    var locationSolution = LocationSolution.builder()
      .setId(uuid)
      .setData(requireNonNull(EventTestFixtures.LOCATION_SOLUTION_DATA).toBuilder()
        .setLocationBehaviors(List.of(locationBehavior))
        .setFeaturePredictions(FeaturePredictionContainer.of(featurePrediction))
        .build())
      .build();

    var eventHypothesis = dummyEventHypothesis.toBuilder()
      .setId(eventHypothesisId)
      .setData(dummyEventHypothesis.getData().orElseThrow().toBuilder()
        .setAssociatedSignalDetectionHypotheses(List.of(signalDetectionHypothesis))
        .setLocationSolutions(List.of(locationSolution))
        .setPreferredLocationSolution(locationSolution)
        .build())
      .build();

    var featureMeasurementNonArrival = EventTestFixtures.SLOWNESS_FEATURE_MEASUREMENT;

    var locationBehaviorNonArrival = LocationBehavior.from(
      Optional.of(1.0),
      Optional.of(2.0),
      true,
      Optional.of(featurePrediction),
      featureMeasurementNonArrival
    );

    var locationSolutionNonArrival = LocationSolution.builder()
      .setId(uuid)
      .setData(EventTestFixtures.LOCATION_SOLUTION_DATA.toBuilder()
        .setLocationBehaviors(List.of(locationBehaviorNonArrival))
        .setFeaturePredictions(FeaturePredictionContainer.of(featurePrediction))
        .build())
      .build();

    var eventHypothesisNonArrival = dummyEventHypothesis.toBuilder()
      .setId(eventHypothesisId)
      .setData(dummyEventHypothesis.getData().get().toBuilder()
        .setAssociatedSignalDetectionHypotheses(List.of(signalDetectionHypothesis))
        .setLocationSolutions(List.of(locationSolutionNonArrival))
        .setPreferredLocationSolution(locationSolutionNonArrival)
        .build())
      .build();

    when(mockSignalDetectionFacetingUtility
      .populateFacets(any(), eq(associatedSignalDetectionHypothesisFacetingDefinition)))
      .thenReturn(signalDetectionHypothesis);

    when(mockSignalDetectionFacetingUtility.populateFacets(eq(featureMeasurement), any(), any())).thenReturn(featureMeasurement);

    when(mockStationDefinitionFacetingUtility
      .populateFacets(eq(EventTestFixtures.FEATURE_PREDICTION.getChannel().orElseThrow()), any(), any()))
      .thenReturn(EventTestFixtures.FEATURE_PREDICTION.getChannel().orElseThrow());

    doReturn(channelSegment).when(mockWaveformFacetingUtility).populateFacets(eq(channelSegment), any());

    assertDoesNotThrow(() -> eventFacetingUtility
      .populateFacets(eventHypothesis, hypothesisFacetingDefinitionPopulateLocationSolution));

    assertDoesNotThrow(() -> eventFacetingUtility
      .populateFacets(eventHypothesisNonArrival, hypothesisFacetingDefinitionPopulateLocationSolution));

  }

}

package gms.shared.signaldetection.accessor;

import com.google.common.collect.ImmutableList;
import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByIdsRequest;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;
import gms.shared.signaldetection.cache.util.RequestCache;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.EMPTY_SDH_IDS_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.EMPTY_SD_IDS_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.EMPTY_STATIONS_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_END_TIME_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_EXCLUDED_DETECTIONS_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_FACETING_DEFINITION_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_IDS_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_STAGE_ID_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_START_TIME_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.NULL_STATIONS_MESSAGE;
import static gms.shared.signaldetection.accessor.RequestCachingSignalDetectionAccessor.START_AFTER_END_MESSAGE;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.DETECTIONS_WITH_CHANNEL_SEGMENTS;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.REQUEST;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTIONS_WITH_CHANNEL_SEGMENTS1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCachingSignalDetectionAccessorTest {

  private static final List<Station> STATIONS = REQUEST.getStations();
  private static final Instant START_TIME = REQUEST.getStartTime();
  private static final Instant END_TIME = REQUEST.getEndTime();
  private static final WorkflowDefinitionId WORKFLOW_DEFINITION_ID = REQUEST.getStageId();
  private static final List<SignalDetection> EXCLUDED_SIGNAL_DETECTIONS =
    REQUEST.getExcludedSignalDetections();
  private static final List<UUID> SIGNAL_DETECTION_IDS = List.of(SIGNAL_DETECTION.getId());
  private static final List<SignalDetection> SIGNAL_DETECTIONS = List.of(SIGNAL_DETECTION);
  private static final FacetingDefinition FACETING_DEFINITION = FacetingDefinition.builder()
    .setClassType("test")
    .setPopulated(false)
    .build();

  @Mock
  private RequestCache requestCache;

  @Mock
  private SignalDetectionAccessorInterface delegate;

  private RequestCachingSignalDetectionAccessor accessor;

  @BeforeEach
  void setup() {
    accessor = new RequestCachingSignalDetectionAccessor(requestCache, delegate);
  }

  @Test
  void testCreate() {
    RequestCachingSignalDetectionAccessor accessor = assertDoesNotThrow(
      () -> new RequestCachingSignalDetectionAccessor(requestCache, delegate));
    assertNotNull(accessor);
  }

  @ParameterizedTest
  @MethodSource("getFindWithSegmentsByStationsAndTimeArguments")
  void testFindWithSegmentsByStationsAndTimeValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<Station> stations,
    Instant startTime,
    Instant endTime,
    WorkflowDefinitionId stageId,
    List<SignalDetection> excludedSignalDetections) {

    Exception exception = assertThrows(expectedException,
      () -> accessor.findWithSegmentsByStationsAndTime(stations,
        startTime,
        endTime,
        stageId,
        excludedSignalDetections));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindWithSegmentsByStationsAndTimeArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        "Stations cannot be null",
        null,
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS),
      arguments(NullPointerException.class,
        "Start time cannot be null",
        STATIONS,
        null,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS),
      arguments(NullPointerException.class,
        "End time cannot be null",
        STATIONS,
        START_TIME,
        null,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS),
      arguments(NullPointerException.class,
        "Stage ID cannot be null",
        STATIONS,
        START_TIME,
        END_TIME,
        null,
        EXCLUDED_SIGNAL_DETECTIONS),
      arguments(NullPointerException.class,
        "Excluded signal detections cannot be null",
        STATIONS,
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        null),
      arguments(IllegalStateException.class,
        "Cannot find signal detections from an empty station list",
        List.of(),
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS),
      arguments(IllegalStateException.class,
        "Cannot find signal detections in interval where start time is after end time",
        STATIONS,
        END_TIME,
        START_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS));
  }

  @Test
  void testFindSegmentsByStationAndTimeCacheMiss() {
    when(requestCache.retrieve(REQUEST)).thenReturn(Optional.empty());
    when(delegate.findWithSegmentsByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS))
      .thenReturn(DETECTIONS_WITH_CHANNEL_SEGMENTS);

    SignalDetectionsWithChannelSegments actual = accessor.findWithSegmentsByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      List.of());

    assertEquals(DETECTIONS_WITH_CHANNEL_SEGMENTS, actual);
    verify(requestCache).retrieve(REQUEST);
    verify(requestCache).cache(REQUEST, DETECTIONS_WITH_CHANNEL_SEGMENTS);
    verify(delegate).findWithSegmentsByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      List.of());
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @Test
  void testFindSegmentsByStationAndTimeCacheHit() {
    when(requestCache.retrieve(REQUEST)).thenReturn(Optional.of(DETECTIONS_WITH_CHANNEL_SEGMENTS));
    SignalDetectionsWithChannelSegments actual = accessor.findWithSegmentsByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      List.of());

    assertEquals(DETECTIONS_WITH_CHANNEL_SEGMENTS, actual);
    verify(requestCache).retrieve(REQUEST);
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindWithSegmentsByIdsArguments")
  void testFindWithSegmentsByIdsValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<UUID> ids,
    WorkflowDefinitionId stageId) {
    Exception exception = assertThrows(expectedException, () -> accessor.findWithSegmentsByIds(ids, stageId));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindWithSegmentsByIdsArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        "IDs cannot be null",
        null,
        WORKFLOW_DEFINITION_ID),
      arguments(NullPointerException.class,
        "Stage ID cannot be null",
        SIGNAL_DETECTION_IDS,
        null),
      arguments(IllegalStateException.class,
        "Cannot find signal detections from an empty list of IDs",
        List.of(),
        WORKFLOW_DEFINITION_ID));
  }

  @Test
  void testFindWithSegmentsByIdsCacheMiss() {
    when(delegate.findWithSegmentsByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID))
      .thenReturn(SIGNAL_DETECTIONS_WITH_CHANNEL_SEGMENTS1);

    SignalDetectionsWithChannelSegments actual = accessor.findWithSegmentsByIds(SIGNAL_DETECTION_IDS,
      WORKFLOW_DEFINITION_ID);
    assertEquals(SIGNAL_DETECTIONS_WITH_CHANNEL_SEGMENTS1, actual);

    verify(delegate).findWithSegmentsByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID);
    verifyNoMoreInteractions(delegate);
  }

  @Test
  void testFindWithSegmentsByIdsCacheHit() {

    DetectionsWithSegmentsByIdsRequest request = DetectionsWithSegmentsByIdsRequest.create(
      ImmutableList.copyOf(SIGNAL_DETECTION_IDS), WORKFLOW_DEFINITION_ID);
    when(requestCache.retrieve(request)).thenReturn(Optional.of(DETECTIONS_WITH_CHANNEL_SEGMENTS));
    SignalDetectionsWithChannelSegments actual = accessor.findWithSegmentsByIds(
      SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID);

    assertEquals(DETECTIONS_WITH_CHANNEL_SEGMENTS, actual);
    verify(requestCache).retrieve(request);
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindByIdsArguments")
  void testFindByIdsValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<UUID> ids,
    WorkflowDefinitionId stageId) {
    Exception exception = assertThrows(expectedException, () -> accessor.findByIds(ids, stageId));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindByIdsArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        "IDs cannot be null",
        null,
        WORKFLOW_DEFINITION_ID),
      arguments(NullPointerException.class,
        "Stage ID cannot be null",
        SIGNAL_DETECTION_IDS,
        null),
      arguments(IllegalStateException.class,
        "Cannot find signal detections from an empty list of IDs",
        List.of(),
        WORKFLOW_DEFINITION_ID));
  }

  @Test
  void testFindByIds() {
    when(delegate.findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID))
      .thenReturn(SIGNAL_DETECTIONS);

    List<SignalDetection> actual = accessor.findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID);
    assertEquals(SIGNAL_DETECTIONS, actual);

    verify(delegate).findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID);
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindByIdsFacetArguments")
  void testFindByIdsFacetValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<UUID> ids,
    WorkflowDefinitionId stageId,
    FacetingDefinition facetingDefinition) {

    Exception exception = assertThrows(expectedException,
      () -> accessor.findByIds(ids, stageId, facetingDefinition));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindByIdsFacetArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        NULL_IDS_MESSAGE,
        null,
        WORKFLOW_DEFINITION_ID,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_STAGE_ID_MESSAGE,
        SIGNAL_DETECTION_IDS,
        null,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_FACETING_DEFINITION_MESSAGE,
        SIGNAL_DETECTION_IDS,
        WORKFLOW_DEFINITION_ID,
        null),
      arguments(IllegalStateException.class,
        EMPTY_SD_IDS_MESSAGE,
        List.of(),
        WORKFLOW_DEFINITION_ID,
        FACETING_DEFINITION));
  }

  @Test
  void testFindByIdsFacet() {
    when(delegate.findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID, FACETING_DEFINITION))
      .thenReturn(SIGNAL_DETECTIONS);

    List<SignalDetection> signalDetections = accessor.findByIds(SIGNAL_DETECTION_IDS,
      WORKFLOW_DEFINITION_ID,
      FACETING_DEFINITION);
    assertEquals(SIGNAL_DETECTIONS, signalDetections);

    verify(delegate).findByIds(SIGNAL_DETECTION_IDS, WORKFLOW_DEFINITION_ID, FACETING_DEFINITION);
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindHypothesesByIdsArguments")
  void testFindHypothesesByIdsValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<SignalDetectionHypothesisId> ids) {

    Exception exception = assertThrows(expectedException,
      () -> accessor.findHypothesesByIds(ids));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindHypothesesByIdsArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        "IDs cannot be null",
        null),
      arguments(IllegalStateException.class,
        "Cannot find signal detection hypotheses from an empty list of IDs",
        List.of()));
  }

  @Test
  void testFindHypothesesByIds() {
    when(delegate.findHypothesesByIds(List.of(SIGNAL_DETECTION_HYPOTHESIS_ID)))
      .thenReturn(List.of(SIGNAL_DETECTION_HYPOTHESIS));

    List<SignalDetectionHypothesis> actual = accessor.findHypothesesByIds(
      List.of(SIGNAL_DETECTION_HYPOTHESIS_ID));
    assertEquals(List.of(SIGNAL_DETECTION_HYPOTHESIS), actual);

    verify(delegate).findHypothesesByIds(List.of(SIGNAL_DETECTION_HYPOTHESIS_ID));
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindHypothesesByIdsFacetArguments")
  void testFindHypothesesByIdsFacetValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<SignalDetectionHypothesisId> signalDetectionHypothesisIds,
    FacetingDefinition facetingDefinition) {

    Exception exception = assertThrows(expectedException,
      () -> accessor.findHypothesesByIds(signalDetectionHypothesisIds, facetingDefinition));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindHypothesesByIdsFacetArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        NULL_IDS_MESSAGE,
        null,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_FACETING_DEFINITION_MESSAGE,
        List.of(SIGNAL_DETECTION_HYPOTHESIS_ID),
        null),
      arguments(IllegalStateException.class,
        EMPTY_SDH_IDS_MESSAGE,
        List.of(),
        FACETING_DEFINITION));
  }

  @Test
  void testFindHypothesesByIdsFacet() {
    when(delegate.findHypothesesByIds(List.of(SIGNAL_DETECTION_HYPOTHESIS_ID), FACETING_DEFINITION))
      .thenReturn(List.of(SIGNAL_DETECTION_HYPOTHESIS));

    List<SignalDetectionHypothesis> hypotheses = accessor.findHypothesesByIds(List.of(SIGNAL_DETECTION_HYPOTHESIS_ID),
      FACETING_DEFINITION);
    assertEquals(List.of(SIGNAL_DETECTION_HYPOTHESIS), hypotheses);
    verify(delegate).findHypothesesByIds(List.of(SIGNAL_DETECTION_HYPOTHESIS_ID), FACETING_DEFINITION);
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindByStationsAndTimeArguments")
  void testFindByStationsAndTimeValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<Station> stations,
    Instant startTime,
    Instant endTime,
    List<SignalDetection> excludedSignalDetections,
    WorkflowDefinitionId stageId) {

    Exception exception = assertThrows(expectedException,
      () -> accessor.findByStationsAndTime(stations,
        startTime,
        endTime,
        stageId,
        excludedSignalDetections));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindByStationsAndTimeArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        "Stations cannot be null",
        null,
        START_TIME,
        END_TIME,
        EXCLUDED_SIGNAL_DETECTIONS,
        WORKFLOW_DEFINITION_ID),
      arguments(NullPointerException.class,
        "Start time cannot be null",
        STATIONS,
        null,
        END_TIME,
        EXCLUDED_SIGNAL_DETECTIONS,
        WORKFLOW_DEFINITION_ID),
      arguments(NullPointerException.class,
        "End time cannot be null",
        STATIONS,
        START_TIME,
        null,
        EXCLUDED_SIGNAL_DETECTIONS,
        WORKFLOW_DEFINITION_ID),
      arguments(NullPointerException.class,
        "Excluded signal detections cannot be null",
        STATIONS,
        START_TIME,
        END_TIME,
        null,
        WORKFLOW_DEFINITION_ID),
      arguments(NullPointerException.class,
        "Stage ID cannot be null",
        STATIONS,
        START_TIME,
        END_TIME,
        EXCLUDED_SIGNAL_DETECTIONS,
        null),
      arguments(IllegalStateException.class,
        "Cannot find signal detections from an empty station list",
        List.of(),
        START_TIME,
        END_TIME,
        EXCLUDED_SIGNAL_DETECTIONS,
        WORKFLOW_DEFINITION_ID),
      arguments(IllegalStateException.class,
        "Cannot find signal detections in interval where start time is after end time",
        STATIONS,
        END_TIME,
        START_TIME,
        EXCLUDED_SIGNAL_DETECTIONS,
        WORKFLOW_DEFINITION_ID));
  }

  @Test
  void testFindByStationsAndTime() {
    when(delegate.findByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS))
      .thenReturn(SIGNAL_DETECTIONS);

    List<SignalDetection> actual = accessor.findByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS);
    assertEquals(SIGNAL_DETECTIONS, actual);

    verify(delegate).findByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS);
    verifyNoMoreInteractions(requestCache, delegate);
  }

  @ParameterizedTest
  @MethodSource("getFindByStationsAndTimeFacetArguments")
  void testFindByStationsAndTimeFacetValidation(Class<? extends Exception> expectedException,
    String expectedMessage,
    List<Station> stations,
    Instant startTime,
    Instant endTime,
    WorkflowDefinitionId stageId,
    List<SignalDetection> excludedSignalDetections,
    FacetingDefinition facetingDefinition) {

    Exception exception = assertThrows(expectedException,
      () -> accessor.findByStationsAndTime(stations,
        startTime,
        endTime,
        stageId,
        excludedSignalDetections,
        facetingDefinition));
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getFindByStationsAndTimeFacetArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        NULL_STATIONS_MESSAGE,
        null,
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_START_TIME_MESSAGE,
        STATIONS,
        null,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_END_TIME_MESSAGE,
        STATIONS,
        START_TIME,
        null,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_STAGE_ID_MESSAGE,
        STATIONS,
        START_TIME,
        END_TIME,
        null,
        EXCLUDED_SIGNAL_DETECTIONS,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_EXCLUDED_DETECTIONS_MESSAGE,
        STATIONS,
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        null,
        FACETING_DEFINITION),
      arguments(NullPointerException.class,
        NULL_FACETING_DEFINITION_MESSAGE,
        STATIONS,
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS,
        null),
      arguments(IllegalStateException.class,
        EMPTY_STATIONS_MESSAGE,
        List.of(),
        START_TIME,
        END_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS,
        FACETING_DEFINITION),
      arguments(IllegalStateException.class,
        START_AFTER_END_MESSAGE,
        STATIONS,
        END_TIME,
        START_TIME,
        WORKFLOW_DEFINITION_ID,
        EXCLUDED_SIGNAL_DETECTIONS,
        FACETING_DEFINITION));
  }

  @Test
  void testFindByStationsAndTimeFacet() {
    when(delegate.findByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS,
      FACETING_DEFINITION))
      .thenReturn(SIGNAL_DETECTIONS);

    List<SignalDetection> actual = accessor.findByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS,
      FACETING_DEFINITION);

    assertEquals(SIGNAL_DETECTIONS, actual);
    verify(delegate).findByStationsAndTime(STATIONS,
      START_TIME,
      END_TIME,
      WORKFLOW_DEFINITION_ID,
      EXCLUDED_SIGNAL_DETECTIONS,
      FACETING_DEFINITION);
  }
}

package gms.shared.signaldetection.accessor;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByIdsRequest;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByStationsAndTimeRequest;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;
import gms.shared.signaldetection.cache.util.RequestCache;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Qualifier("requestCachingSignalDetectionAccessor")
public class RequestCachingSignalDetectionAccessor implements SignalDetectionAccessorInterface {

  public static final String NULL_STAGE_ID_MESSAGE = "Stage ID cannot be null";
  public static final String NULL_IDS_MESSAGE = "IDs cannot be null";
  public static final String NULL_STATIONS_MESSAGE = "Stations cannot be null";
  public static final String NULL_START_TIME_MESSAGE = "Start time cannot be null";
  public static final String NULL_END_TIME_MESSAGE = "End time cannot be null";
  public static final String NULL_EXCLUDED_DETECTIONS_MESSAGE = "Excluded signal detections cannot be null";
  public static final String EMPTY_STATIONS_MESSAGE = "Cannot find signal detections from an empty station list";
  public static final String START_AFTER_END_MESSAGE = "Cannot find signal detections in interval where start time is after end time";
  public static final String NULL_FACETING_DEFINITION_MESSAGE = "Faceting definition cannot be null";
  public static final String EMPTY_SD_IDS_MESSAGE = "Cannot find signal detections from an empty list of IDs";
  public static final String EMPTY_SDH_IDS_MESSAGE = "Cannot find signal detection hypotheses from an empty list of IDs";

  private final RequestCache requestCache;
  private final SignalDetectionAccessorInterface delegate;

  public RequestCachingSignalDetectionAccessor(@Qualifier("signalDetectionRequestCache") RequestCache requestCache,
    @Qualifier("bridgedSignalDetectionAccessor") SignalDetectionAccessorInterface delegate) {
    this.requestCache = requestCache;
    this.delegate = delegate;
  }

  @Override
  public SignalDetectionsWithChannelSegments findWithSegmentsByIds(List<UUID> signalDetectionIds,
    WorkflowDefinitionId stageId) {
    Objects.requireNonNull(signalDetectionIds, NULL_IDS_MESSAGE);
    Objects.requireNonNull(stageId, NULL_STAGE_ID_MESSAGE);
    Preconditions.checkState(!signalDetectionIds.isEmpty(), EMPTY_SD_IDS_MESSAGE);

    var request = DetectionsWithSegmentsByIdsRequest.create(
      ImmutableList.copyOf(signalDetectionIds), stageId);

    Optional<SignalDetectionsWithChannelSegments> cachedResult = requestCache.retrieve(request);
    if (cachedResult.isEmpty()) {
      SignalDetectionsWithChannelSegments result = delegate.findWithSegmentsByIds(signalDetectionIds, stageId);
      requestCache.cache(request, result);
      return result;
    } else {
      return cachedResult.get();
    }
  }

  @Override
  public SignalDetectionsWithChannelSegments findWithSegmentsByStationsAndTime(List<Station> stations,
    Instant startTime,
    Instant endTime,
    WorkflowDefinitionId stageId,
    List<SignalDetection> excludedSignalDetections) {

    Objects.requireNonNull(stations, NULL_STATIONS_MESSAGE);
    Objects.requireNonNull(startTime, NULL_START_TIME_MESSAGE);
    Objects.requireNonNull(endTime, NULL_END_TIME_MESSAGE);
    Objects.requireNonNull(stageId, NULL_STAGE_ID_MESSAGE);
    Objects.requireNonNull(excludedSignalDetections, NULL_EXCLUDED_DETECTIONS_MESSAGE);

    Preconditions.checkState(!stations.isEmpty(), EMPTY_STATIONS_MESSAGE);
    Preconditions.checkState(!startTime.isAfter(endTime), START_AFTER_END_MESSAGE);

    var request =
      DetectionsWithSegmentsByStationsAndTimeRequest.create(ImmutableList.copyOf(stations),
        startTime,
        endTime,
        stageId,
        ImmutableList.copyOf(excludedSignalDetections));

    Optional<SignalDetectionsWithChannelSegments> cachedResult = requestCache.retrieve(request);
    if (cachedResult.isEmpty()) {
      SignalDetectionsWithChannelSegments result = delegate.findWithSegmentsByStationsAndTime(stations,
        startTime,
        endTime,
        stageId,
        excludedSignalDetections);
      requestCache.cache(request, result);
      return result;
    } else {
      return cachedResult.get();
    }
  }

  @Override
  public List<SignalDetection> findByIds(List<UUID> ids, WorkflowDefinitionId stageId) {
    Objects.requireNonNull(ids, NULL_IDS_MESSAGE);
    Objects.requireNonNull(stageId, NULL_STAGE_ID_MESSAGE);
    Preconditions.checkState(!ids.isEmpty(), EMPTY_SD_IDS_MESSAGE);

    return delegate.findByIds(ids, stageId);
  }

  @Override
  public List<SignalDetection> findByIds(List<UUID> ids, WorkflowDefinitionId stageId,
    FacetingDefinition facetingDefinition) {
    Objects.requireNonNull(ids, NULL_IDS_MESSAGE);
    Objects.requireNonNull(stageId, NULL_STAGE_ID_MESSAGE);
    Objects.requireNonNull(facetingDefinition, NULL_FACETING_DEFINITION_MESSAGE);
    Preconditions.checkState(!ids.isEmpty(), EMPTY_SD_IDS_MESSAGE);

    return delegate.findByIds(ids, stageId, facetingDefinition);
  }

  @Override
  public List<SignalDetectionHypothesis> findHypothesesByIds(List<SignalDetectionHypothesisId> ids) {
    Objects.requireNonNull(ids, NULL_IDS_MESSAGE);

    Preconditions.checkState(!ids.isEmpty(), EMPTY_SDH_IDS_MESSAGE);

    return delegate.findHypothesesByIds(ids);
  }

  @Override
  public List<SignalDetectionHypothesis> findHypothesesByIds(List<SignalDetectionHypothesisId> ids,
    FacetingDefinition facetingDefinition) {

    Objects.requireNonNull(ids, NULL_IDS_MESSAGE);
    Objects.requireNonNull(facetingDefinition, NULL_FACETING_DEFINITION_MESSAGE);
    Preconditions.checkState(!ids.isEmpty(), EMPTY_SDH_IDS_MESSAGE);

    return delegate.findHypothesesByIds(ids, facetingDefinition);
  }

  @Override
  public List<SignalDetection> findByStationsAndTime(List<Station> stations,
    Instant startTime,
    Instant endTime,
    WorkflowDefinitionId stageId,
    List<SignalDetection> excludedSignalDetections) {

    Objects.requireNonNull(stations, NULL_STATIONS_MESSAGE);
    Objects.requireNonNull(startTime, NULL_START_TIME_MESSAGE);
    Objects.requireNonNull(endTime, NULL_END_TIME_MESSAGE);
    Objects.requireNonNull(stageId, NULL_STAGE_ID_MESSAGE);
    Objects.requireNonNull(excludedSignalDetections, NULL_EXCLUDED_DETECTIONS_MESSAGE);

    Preconditions.checkState(!stations.isEmpty(), EMPTY_STATIONS_MESSAGE);
    Preconditions.checkState(!startTime.isAfter(endTime), START_AFTER_END_MESSAGE);

    return delegate.findByStationsAndTime(stations, startTime, endTime, stageId, excludedSignalDetections);
  }

  @Override
  public List<SignalDetection> findByStationsAndTime(List<Station> stations, Instant startTime, Instant endTime,
    WorkflowDefinitionId stageId, List<SignalDetection> excludedSignalDetections,
    FacetingDefinition facetingDefinition) {
    Objects.requireNonNull(stations, NULL_STATIONS_MESSAGE);
    Objects.requireNonNull(startTime, NULL_START_TIME_MESSAGE);
    Objects.requireNonNull(endTime, NULL_END_TIME_MESSAGE);
    Objects.requireNonNull(stageId, NULL_STAGE_ID_MESSAGE);
    Objects.requireNonNull(excludedSignalDetections, NULL_EXCLUDED_DETECTIONS_MESSAGE);
    Objects.requireNonNull(facetingDefinition, NULL_FACETING_DEFINITION_MESSAGE);

    Preconditions.checkState(!stations.isEmpty(), EMPTY_STATIONS_MESSAGE);
    Preconditions.checkState(!startTime.isAfter(endTime), START_AFTER_END_MESSAGE);

    return delegate.findByStationsAndTime(stations,
      startTime,
      endTime,
      stageId,
      excludedSignalDetections,
      facetingDefinition);
  }
}

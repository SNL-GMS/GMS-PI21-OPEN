package gms.shared.signaldetection.api;

import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SignalDetectionRepositoryInterface {

  /**
   * Finds {@link SignalDetection}s for the provided {@link UUID}s and
   * {@link WorkflowDefinitionId}s
   *
   * @param ids list of {@link UUID}s
   * @param stageId stage {@link WorkflowDefinitionId}
   * @return a list of {@link SignalDetection}s
   */
  List<SignalDetection> findByIds(List<UUID> ids, WorkflowDefinitionId stageId);

  /**
   * Finds {@link SignalDetectionHypothesis} for the provided ids
   *
   * @param ids the {@link SignalDetectionHypothesisId}s to find the associated detections for
   * @return a list of {@link SignalDetectionHypothesisId}s
   */
  List<SignalDetectionHypothesis> findHypothesesByIds(List<SignalDetectionHypothesisId> ids);

  /**
   * Retrieves {@link SignalDetection}s for the provided stations and stage, between the provided time range, and excluding any
   * of the excluded {@link SignalDetection}s
   *
   * @param stations The list of {@link Station}s to find {@link SignalDetection}s for
   * @param startTime The start time of the time range to find {@link SignalDetection}s in
   * @param endTime The end time of the time range to find {@link SignalDetection}s in
   * @param excludedSignalDetections The {@link SignalDetection}s to exclude from the results
   * @param stageId The stage id for the {@link SignalDetection}s
   * @return a list of {@link SignalDetection}s
   */
  List<SignalDetection> findByStationsAndTime(
    List<Station> stations,
    Instant startTime,
    Instant endTime,
    WorkflowDefinitionId stageId,
    List<SignalDetection> excludedSignalDetections);
}

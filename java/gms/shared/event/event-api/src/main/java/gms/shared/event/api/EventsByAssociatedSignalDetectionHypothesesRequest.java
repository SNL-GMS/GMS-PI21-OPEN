package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.Collection;

/**
 * Defines the request body for EventManager.findEventsByAssociatedSignalDetectionHypotheses
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EventsByAssociatedSignalDetectionHypothesesRequest {

  /**
   * Gets the collection of {@link SignalDetectionHypothesis} for which to retrieve Events
   *
   * @return Collection of {@link SignalDetectionHypothesis} for which to retrieve Events
   */
  public abstract Collection<SignalDetectionHypothesis> getSignalDetectionHypotheses();

  /**
   * Gets the StageId for which to retrieve Events
   *
   * @return The StageId for which to retrieve Events
   */
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Creates a new EventsByAssociatedSignalDetectionHypothesesRequest instance
   *
   * @param signalDetectionHypotheses A collection of {@link SignalDetectionHypothesis} objects for which to retrieve Events
   * @param stageId The StageId for which to retrieve Events, SignalDetections, and ChannelSegments
   * @return New EventsByAssociatedSignalDetectionHypothesesRequest instance
   */
  @JsonCreator
  public static EventsByAssociatedSignalDetectionHypothesesRequest create(
    @JsonProperty("signalDetectionHypotheses") Collection<SignalDetectionHypothesis> signalDetectionHypotheses,
    @JsonProperty("stageId") WorkflowDefinitionId stageId) {

    return new AutoValue_EventsByAssociatedSignalDetectionHypothesesRequest(signalDetectionHypotheses, stageId);
  }

}

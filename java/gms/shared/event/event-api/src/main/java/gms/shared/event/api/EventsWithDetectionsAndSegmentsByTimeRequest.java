package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;

/**
 * Defines the request body for EventManager.findEventsWithDetectionsAndSegmentsByTime()
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EventsWithDetectionsAndSegmentsByTimeRequest {

  /**
   * Gets the beginning of the time range for which to retrieve Events, SignalDetections, and ChannelSegments
   *
   * @return Instant representing the beginning of the time range for which to retrieve Events, SignalDetections, and
   * ChannelSegments
   */
  public abstract Instant getStartTime();

  /**
   * Gets the end of the time range for which to retrieve Events, SignalDetections, and ChannelSegments
   *
   * @return Instant representing the end of the time range for which to retrieve Events, SignalDetections, and
   * ChannelSegments
   */
  public abstract Instant getEndTime();

  /**
   * Gets the StageId for which to retrieve Events, SignalDetections, and ChannelSegments
   *
   * @return The StageId for which to retrieve Events, SignalDetections, and ChannelSegments
   */
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Creates a new EventsWithDetectionsAndSegmentsByTimeRequest instance
   *
   * @param startTime The beginning of the time range for which to retrieve Events, SignalDetections, and
   * ChannelSegments
   * @param endTime The end of the time range for which to retrieve Events, SignalDetections, and ChannelSegments
   * @param stageId The StageId for which to retrieve Events, SignalDetections, and ChannelSegments
   * @return New EventsWithDetectionsAndSegmentsByTimeRequest instance
   */
  @JsonCreator
  public static EventsWithDetectionsAndSegmentsByTimeRequest create(
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("stageId") WorkflowDefinitionId stageId) {

    return new AutoValue_EventsWithDetectionsAndSegmentsByTimeRequest(startTime, endTime, stageId);
  }

}

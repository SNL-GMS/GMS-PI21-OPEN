package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;
import java.util.Optional;

/**
 * Defines the request body for EventManager.findEventsByTime()
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class EventsByTimeRequest {

  /**
   * Gets the beginning of the time range for which to retrieve Events
   *
   * @return Instant representing the beginning of the time range for which to retrieve Events
   */
  public abstract Instant getStartTime();

  /**
   * Gets the end of the time range for which to retrieve Events
   *
   * @return Instant representing the end of the time range for which to retrieve Events
   */
  public abstract Instant getEndTime();

  /**
   * Gets the StageId for which to retrieve Events
   *
   * @return The StageId for which to retrieve Events
   */
  public abstract WorkflowDefinitionId getStageId();

  /**
   * Gets the {@link Optional} {@link FacetingDefinition} for events
   *
   * @return The FacetingDefinition for which to facet Events
   */
  public abstract Optional<FacetingDefinition> getFacetingDefinition();

  /**
   * Creates a new EventsByTimeRequest instance
   *
   * @param startTime The beginning of the time range for which to retrieve Events
   * @param endTime The end of the time range for which to retrieve Events
   * @param stageId The StageId for which to retrieve Events
   * @return New EventsByTimeRequest instance
   */
  @JsonCreator
  public static EventsByTimeRequest create(
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("stageId") WorkflowDefinitionId stageId,
    @JsonProperty("facetingDefinition") FacetingDefinition facetingDefinition) {

    return new AutoValue_EventsByTimeRequest(startTime, endTime, stageId, Optional.ofNullable(facetingDefinition));
  }

}

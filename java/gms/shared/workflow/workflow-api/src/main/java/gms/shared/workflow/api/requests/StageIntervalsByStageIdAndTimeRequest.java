package gms.shared.workflow.api.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;

/**
 * Request body class for WorkflowManager.findStageIntervalsByStageIdAndTime() endpoint
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StageIntervalsByStageIdAndTimeRequest {

  /**
   * Gets the beginning of the time range for which to retrieve {@link gms.shared.workflow.coi.StageInterval}s, inclusive
   *
   * @return Beginning of the time range for which to retrieve {@link gms.shared.workflow.coi.StageInterval}s, inclusive
   */
  public abstract Instant getStartTime();

  /**
   * Gets the end of the time range for which to retrieve {@link gms.shared.workflow.coi.StageInterval}s, exclusive
   *
   * @return End of the time range for which to retrieve {@link gms.shared.workflow.coi.StageInterval}s, exclusive
   */
  public abstract Instant getEndTime();

  /**
   * Gets the {@link Collection} of {@link WorkflowDefinitionId}s for which to retrieve the corresponding StageIntervals
   *
   * @return {@link Collection} of {@link WorkflowDefinitionId}s for which to retrieve the corresponding StageIntervals
   */
  public abstract Set<WorkflowDefinitionId> getStageIds();

  /**
   * Creates and returns a new StageIntervalsByStageIdAndTimeRequest object.
   *
   * @param startTime Beginning of the time range for which to retrieve {@link gms.shared.workflow.coi.StageInterval}s, inclusive
   * @param endTime End of the time range for which to retrieve StageIntervals, exclusive
   * @param stageIds {@link Collection} of {@link WorkflowDefinitionId}s for which to retrieve the corresponding StageIntervals
   * @return New StageIntervalsByStageIdAndTimeRequest
   */
  @JsonCreator
  public static StageIntervalsByStageIdAndTimeRequest from(
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("stageIds") Collection<WorkflowDefinitionId> stageIds
  ) {
    return new AutoValue_StageIntervalsByStageIdAndTimeRequest(startTime, endTime, ImmutableSet.copyOf(stageIds));
  }
}

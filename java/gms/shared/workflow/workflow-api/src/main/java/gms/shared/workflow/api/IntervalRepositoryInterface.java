package gms.shared.workflow.api;

import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Defines IntervalRepository operations
 */
public interface IntervalRepositoryInterface {

  /**
   * Retrieves {@link StageInterval}s from the database by name and that lie within a given time range
   *
   * @param startTime Beginning of the time range for retrieval, inclusive
   * @param endTime End of the time range for retrieval, exclusive
   * @param stageIds {@link WorkflowDefinitionId} stage ids for which to retrieve StageIntervals
   * @return {@link Map} from StageInterval names to StageIntervals
   */
  Map<String, List<StageInterval>> findStageIntervalsByStageIdAndTime(Instant startTime, Instant endTime,
    Collection<WorkflowDefinitionId> stageIds);
}

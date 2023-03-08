package gms.shared.workflow.api;

import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.Workflow;
import net.jodah.failsafe.RetryPolicy;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Accessor interface of the Manager/Accessor/Repository pattern for the Workflow domain. Responsible for
 * Managing a cache of the {@link Workflow} and all {@link gms.shared.workflow.coi.Interval}s within the operational
 * time period. Delegates storage of intervals to an underlying {@link IntervalRepositoryInterface}
 */
public interface WorkflowAccessorInterface extends IntervalRepositoryInterface {

  /**
   * Initializes the cache by retrieving all relevant intervals within the provided operational time period and
   * caching them, overwriting previously cached values for the respective stage names.
   *
   * @param operationalPeriodStart Start of the operational time period
   * @param operationalPeriodEnd End of the operational time period
   * @param retryPolicy the RetryPolicy of the cache initialization with backoff duration and maxAttempts set
   */
  void initializeCache(Instant operationalPeriodStart, Instant operationalPeriodEnd,
    RetryPolicy<Map<String, List<StageInterval>>> retryPolicy);

  /**
   * Returns the current {@link Workflow}
   *
   * @return The current Workflow
   */
  Workflow getWorkflow();

  /**
   * Returns the time at which the database was last polled for new {@link StageInterval}s
   *
   * @return The time at which the database was last polled for new StageIntervals
   */
  Instant getTimeLastPolled();

  /**
   * Sets the time at which the database was last polled for new {@link StageInterval}s
   *
   * @param timeLastPolled The time at which the database was last polled for new StageIntervals
   */
  void setTimeLastPolled(Instant timeLastPolled);

  /**
   * Retrieves a Optional StageInterval from the cache
   *
   * @param intervalId intervalId
   * @return The stage interval for the interval id, or {@link Optional#empty()} if no interval was found
   */
  Optional<StageInterval> findStageIntervalById(IntervalId intervalId);

  /**
   * Atomically retrieves an Optional StageInterval from the cache and calls the UnaryOperator on it. Method will store the
   * result if the update method returns a non-empty Optional
   *
   * @param stageIntervalId the interval Id to retrieve
   * @param update the update function to perform on the stageInterval
   * @return the Optional<? extends StageInterval> result of the update
   */
  Optional<StageInterval> update(IntervalId stageIntervalId, UnaryOperator<StageInterval> update);

  /**
   * Atomically retrieves an Optional StageInterval from the cache and calls the update function on it. Method will store the
   * result if the update method returns a non-empty Optional
   *
   * @param stageIntervalId the interval Id to retrieve
   * @param update the update function to perform on the stageInterval
   * @return the Optional<? extends StageInterval> result of the update
   */
  Optional<StageInterval> updateIfPresent(IntervalId stageIntervalId,
    Function<StageInterval, Optional<StageInterval>> update);

  /**
   * Inserts or updates the provided {@link StageInterval}s in the cache
   *
   * @param stageIntervals StageIntervals to insert or update in the cache
   */
  void cacheStageIntervals(Collection<? extends StageInterval> stageIntervals);

  /**
   * Removes all values strictly older than the input time from the cache
   *
   * @param olderThan Removes all values strictly older than the this time from the cache
   */
  void pruneStageIntervals(Instant olderThan);

  /**
   * Returns whether or not the {@link StageInterval} with the provided {@link IntervalId} is present in the cache
   *
   * @param stageIntervalId IntervalId to check for in the cache
   * @return Returns true if a StageInterval with the provided IntervalId is in the cache, false if not
   */
  boolean isInCache(IntervalId stageIntervalId);
}

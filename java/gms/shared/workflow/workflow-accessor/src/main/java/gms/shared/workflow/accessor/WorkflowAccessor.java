package gms.shared.workflow.accessor;

import gms.shared.workflow.api.IntervalRepositoryInterface;
import gms.shared.workflow.api.WorkflowAccessorInterface;
import gms.shared.workflow.cache.IntervalCache;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.function.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

/**
 * Accessor implementation of the Manager/Accessor/Repository pattern for the Workflow domain. Responsible for
 * Managing a cache of the {@link Workflow} and all {@link gms.shared.workflow.coi.Interval}s within the operational
 * time period. Delegates storage of intervals to an underlying {@link IntervalRepositoryInterface}
 */
@Component
public class WorkflowAccessor implements WorkflowAccessorInterface {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowAccessor.class);

  private final Workflow workflow;
  private final IntervalRepositoryInterface intervalRepository;
  private final IntervalCache intervalCache;
  private final AtomicReference<Instant> timeLastPolled = new AtomicReference<>();

  /**
   * @param workflow Input workflow definition to be cached
   * @param intervalRepository Delegating interval repository for persistence CRUD operations
   * @param intervalCache See {@link IntervalCache}
   */
  @Autowired
  public WorkflowAccessor(Workflow workflow,
    @Qualifier("interval-repository") IntervalRepositoryInterface intervalRepository,
    IntervalCache intervalCache) {
    this.workflow = workflow;
    this.intervalCache = intervalCache;
    this.intervalRepository = intervalRepository;
    //This defaults to a "min"-like value of 1/1/1900 without the problems converting back and forth from Instant.MIN
    this.timeLastPolled.set(Instant.ofEpochSecond(-2208988800L));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initializeCache(Instant operationalPeriodStart, Instant operationalPeriodEnd,
    RetryPolicy<Map<String, List<StageInterval>>> retryPolicy) {

    logger.info("Initializing WorkflowAccessor...");
    logger.info("Attempting cache retrieval of Intervals for operational period {}:{}", operationalPeriodStart, operationalPeriodEnd);

    var stageIds = workflow.stageIds().collect(toSet());
    var stageNames = stageIds.stream().map(WorkflowDefinitionId::getName).collect(Collectors.toSet());
    var stageIntervals = intervalCache.getAll(stageNames, operationalPeriodStart, operationalPeriodEnd)
      .stream().collect(groupingBy(StageInterval::getName));

    try {
      if (stageIntervals.isEmpty()) {
        logger.info("Cache is empty. Retrieving Intervals for operational period {}:{}", operationalPeriodStart, operationalPeriodEnd);

        var loadCacheRunner = new LoadCacheRunner(operationalPeriodStart, operationalPeriodEnd, stageIds, stageIntervals);

        retryPolicy.handle(List.of(RuntimeException.class))
          .onFailedAttempt(e -> logger.warn("Could not initialize cache, retrying: {}", e))
          .onFailure(e -> logger.warn("Could not initialize cache, retrying: {}", e))
          .onRetriesExceeded(e -> {
            logger.error("Could not initialize cache, workflowAccessor cannot be initialized.");
            throw new IllegalStateException("Could not initialize workflowAccessor");
          });

        Failsafe.with(retryPolicy).run(loadCacheRunner);
        stageIntervals = loadCacheRunner.getStageIntervals();

        logger.info("Interval retrieval successful!  Now caching Intervals...");
        stageIntervals.forEach((s, intervals) -> intervalCache.putAll(intervals));
      }

      logger.debug("Intervals found for {} stages", stageIntervals.keySet().size());
      if (logger.isDebugEnabled()) {
        stageIntervals.forEach((name, intervals) -> logger.debug("{} intervals found for stage {}", intervals.size(), name));
      }

      stageIntervals
        .values()
        .stream()
        .flatMap(List::stream)
        .map(StageInterval::getModificationTime)
        .max(Instant::compareTo)
        .ifPresent(this::setTimeLastPolled);

      logger.info("WorkflowAccessor initialization complete");
    } catch (Exception ex) {
      logger.error("Initialization failed", ex);
    }
  }

  private class LoadCacheRunner implements CheckedRunnable {

    private final Instant operationalPeriodStart;
    private final Instant operationalPeriodEnd;
    private final Set<WorkflowDefinitionId> stageIds;
    private Map<String, List<StageInterval>> stageIntervals;

    public LoadCacheRunner(Instant operationalPeriodStart, Instant operationalPeriodEnd,
      Set<WorkflowDefinitionId> stageIds, Map<String, List<StageInterval>> stageIntervals) {
      this.operationalPeriodEnd = operationalPeriodEnd;
      this.operationalPeriodStart = operationalPeriodStart;
      this.stageIds = stageIds;
      this.stageIntervals = stageIntervals;
    }

    @Override
    public void run() {
      stageIntervals = intervalRepository.findStageIntervalsByStageIdAndTime(
        operationalPeriodStart,
        operationalPeriodEnd,
        stageIds);
    }

    public Map<String, List<StageInterval>> getStageIntervals() {
      return stageIntervals;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Workflow getWorkflow() {
    return workflow;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setTimeLastPolled(Instant timeLastPolled) {
    this.timeLastPolled.set(timeLastPolled);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Instant getTimeLastPolled() {
    return timeLastPolled.get();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<StageInterval> findStageIntervalById(IntervalId intervalId) {
    return intervalCache.get(intervalId);
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  @Override
  public Optional<StageInterval> update(IntervalId stageIntervalId, UnaryOperator<StageInterval> update) {
    return intervalCache.update(stageIntervalId, update);
  }

  /**
   * {@inheritDoc}
   *
   * @return
   */
  @Override
  public Optional<StageInterval> updateIfPresent(IntervalId stageIntervalId,
    Function<StageInterval, Optional<StageInterval>> update) {
    return intervalCache.updateIfPresent(stageIntervalId, update);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void cacheStageIntervals(Collection<? extends StageInterval> stageIntervals) {
    intervalCache.putAll(stageIntervals);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void pruneStageIntervals(Instant olderThan) {
    intervalCache.prune(olderThan);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isInCache(IntervalId intervalId) {
    return intervalCache.get(intervalId).isPresent();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, List<StageInterval>> findStageIntervalsByStageIdAndTime(Instant startTime, Instant endTime,
    Collection<WorkflowDefinitionId> stageIds) {
    return intervalCache.getAll(stageIds.stream().map(WorkflowDefinitionId::getName).collect(toSet()), startTime, endTime)
      .stream().collect(groupingBy(StageInterval::getName));
  }

}

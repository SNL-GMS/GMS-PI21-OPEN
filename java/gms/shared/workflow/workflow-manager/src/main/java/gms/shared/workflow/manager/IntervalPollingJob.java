package gms.shared.workflow.manager;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.system.events.SystemEvent;
import gms.shared.system.events.SystemEventPublisher;
import gms.shared.workflow.accessor.WorkflowAccessor;
import gms.shared.workflow.api.WorkflowAccessorInterface;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.StageMode;
import gms.shared.workflow.repository.BridgedIntervalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toSet;

/**
 * Manages polling the database to update intervals
 */
@Component
public class IntervalPollingJob {

  private static final Logger logger = LoggerFactory.getLogger(IntervalPollingJob.class);

  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";
  private static final String SYSTEM_MESSAGE_EVENT_TYPE = "intervals";

  private final WorkflowAccessorInterface workflowAccessor;
  private final BridgedIntervalRepository bridgedIntervalRepository;
  private final Supplier<Instant> currentTimeSupplier;
  private final Duration operationalPeriodStart;
  private final Duration operationalPeriodEnd;
  private final SystemEventPublisher systemEventPublisher;

  /**
   * @param workflowAccessor {@link WorkflowAccessor} used to get the {@link StageInterval} ids to poll for, cache new
   * StageIntervals, and prune "expired" StageIntervals from the cache
   * @param bridgedIntervalRepository {@link BridgedIntervalRepository} used to query the database for new
   * StageIntervals
   * @param systemEventPublisher {@link SystemEventPublisher} used to publish {@link
   * gms.shared.system.events.SystemEvent}s for new StageIntervals to Kafka
   * @param currentTimeSupplier Supplies the current time
   * @param processingConfig Config to supply operational time period parameters
   */
  @Autowired
  public IntervalPollingJob(
    @Qualifier("workflow-accessor") WorkflowAccessorInterface workflowAccessor,
    BridgedIntervalRepository bridgedIntervalRepository,
    SystemEventPublisher systemEventPublisher,
    Supplier<Instant> currentTimeSupplier,
    ConfigurationConsumerUtility processingConfig
  ) {
    var operationalTimeConfig = processingConfig.resolve(OPERATIONAL_TIME_PERIOD_CONFIG,
      Collections.emptyList());
    var operationalStart = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_START).toString());
    var operationalEnd = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_END).toString());

    this.bridgedIntervalRepository = bridgedIntervalRepository;
    this.workflowAccessor = workflowAccessor;
    this.systemEventPublisher = systemEventPublisher;
    this.currentTimeSupplier = currentTimeSupplier;
    this.operationalPeriodStart = operationalStart;
    this.operationalPeriodEnd = operationalEnd;
  }

  /**
   * <ol>
   *   <li>Prunes "expired" intervals from the cache</li>
   *   <li>Queries the database for new intervals</li>
   *   <li>Adds new intervals to the cache</li>
   * </ol>
   */
  public void updateWorkflowIntervals() {
    logger.debug("Updating Workflow Intervals...");

    var now = currentTimeSupplier.get();
    var operationalTimeStart = now.minus(operationalPeriodStart);
    var operationalTimeEnd = now.minus(operationalPeriodEnd);
    var stageIds = workflowAccessor.getWorkflow().stageIds()
      .collect(toSet());

    logger.debug("Pruning intervals older than {}", operationalTimeStart);
    workflowAccessor.pruneStageIntervals(operationalTimeStart);

    logger.debug("Polling for intervals after {}", workflowAccessor.getTimeLastPolled());
    var stageIntervals = bridgedIntervalRepository.findStageIntervalsByStageIdAndTime(
      operationalTimeStart,
      operationalTimeEnd,
      stageIds,
      workflowAccessor.getTimeLastPolled());

    var filteredIntervals = stageIntervals
      .values().stream()
      .flatMap(List::stream)
      .filter(keepStageInterval(workflowAccessor))
      .collect(toSet());

    logger.debug("{} intervals will be cached", filteredIntervals.size());
    if (!filteredIntervals.isEmpty()) {
      workflowAccessor.cacheStageIntervals(filteredIntervals);
      var systemEvent = SystemEvent.from(SYSTEM_MESSAGE_EVENT_TYPE, filteredIntervals, 0);
      systemEventPublisher.sendSystemEvent(systemEvent);
    }

    filteredIntervals.stream()
      .map(StageInterval::getModificationTime)
      .max(Instant::compareTo)
      .ifPresent(workflowAccessor::setTimeLastPolled);

    logger.debug("Workflow Intervals Update Complete");
  }

  private static Predicate<StageInterval> keepStageInterval(WorkflowAccessorInterface workflowAccessor) {
    return stageInterval -> StageMode.AUTOMATIC.equals(stageInterval.getStageMode())
      || !workflowAccessor.isInCache(stageInterval.getIntervalId());
  }

}

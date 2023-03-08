package gms.shared.workflow.manager;

import gms.shared.frameworks.service.InvalidInputException;
import gms.shared.system.events.SystemEvent;
import gms.shared.system.events.SystemEventPublisher;
import gms.shared.workflow.accessor.configuration.WorkflowAccessorCachingConfig;
import gms.shared.workflow.api.WorkflowAccessorInterface;
import gms.shared.workflow.api.requests.StageIntervalsByStageIdAndTimeRequest;
import gms.shared.workflow.api.requests.UpdateActivityIntervalStatusRequest;
import gms.shared.workflow.api.requests.UpdateInteractiveAnalysisStageIntervalStatusRequest;
import gms.shared.workflow.coi.ActivityInterval;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.Workflow;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

/**
 * WorkflowManager operations allowing for retrieval of {@link Workflow} configuration, retrieval of
 * {@link StageInterval}s, and updates of {@link StageInterval}s.
 */
@RestController("workflow-manager")
@RequestMapping(value = "/workflow-manager",
  produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowManager {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowManager.class);

  private final WorkflowAccessorInterface workflowAccessor;
  private final WorkflowAccessorCachingConfig workflowAccessorCachingConfig;
  private final SystemEventPublisher systemEventPublisher;
  private final IntervalPollingScheduler intervalPollingScheduler;
  private final Supplier<Instant> currentTimeSupplier;
  private static final String SYSTEM_MESSAGE_EVENT_TYPE = "intervals";

  @Autowired
  public WorkflowManager(@Qualifier("workflow-accessor") WorkflowAccessorInterface workflowAccessor,
    WorkflowAccessorCachingConfig workflowAccessorCachingConfig,
    SystemEventPublisher systemEventPublisher,
    IntervalPollingScheduler intervalPollingScheduler,
    Supplier<Instant> currentTimeSupplier) {
    this.workflowAccessor = workflowAccessor;
    this.workflowAccessorCachingConfig = workflowAccessorCachingConfig;
    this.systemEventPublisher = systemEventPublisher;
    this.intervalPollingScheduler = intervalPollingScheduler;
    this.currentTimeSupplier = currentTimeSupplier;
  }

  @PostConstruct
  private void initialize() {
    var now = currentTimeSupplier.get();
    workflowAccessor.initializeCache(now.minus(workflowAccessorCachingConfig.getOperationalStart()),
      now.minus(workflowAccessorCachingConfig.getOperationalEnd()),
      workflowAccessorCachingConfig.getRetryPolicy());
    intervalPollingScheduler.runPollingJob();
  }

  /**
   * Gets the configured {@link Workflow}
   *
   * @param placeholder Placceholder text required by service framework
   * @return The configured Workflow
   */
  @PostMapping(value = "/workflow-definition")
  @Operation(summary = "Get the Workflow definition")
  public Workflow getWorkflowDefinition(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Placeholder request body due to legacy reasons. Optional.")
    @RequestBody(required = false) String placeholder) {
    return workflowAccessor.getWorkflow();
  }

  /**
   * Retrieves and returns {@link StageInterval}s matching the set of {@link gms.shared.workflow.coi.Stage} names and
   * time range in the provided request body.
   *
   * @param request {@link StageIntervalsByStageIdAndTimeRequest} defining request parameters
   * @return {@link Map} from Stage name to StageIntervals
   */
  @PostMapping(value = "/interval/stage/query/ids-timerange")
  @Operation(summary = "Retrieves and returns Stage Intervals matching the set of stage names and time range" +
    " in the provided request body.")
  public Map<String, List<StageInterval>> findStageIntervalsByStageIdAndTime(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Set of stage names and time range to retrieve")
    @RequestBody StageIntervalsByStageIdAndTimeRequest request) {
    return workflowAccessor
      .findStageIntervalsByStageIdAndTime(request.getStartTime(), request.getEndTime(),
        request.getStageIds());
  }

  /**
   * Updates the stage interval matching the provided ID with the provided status
   *
   * @param request {@link UpdateInteractiveAnalysisStageIntervalStatusRequest} defining request parameters
   */
  @PostMapping(value = "/interval/stage/interactive-analysis/update")
  @Operation(summary = "Updates the stage interval matching the provided ID with the provided status.")
  public void updateInteractiveAnalysisStageIntervalStatus(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Stage interval ID and status to update")
    @RequestBody UpdateInteractiveAnalysisStageIntervalStatusRequest request) {
    logger.debug("Handling InteractiveAnalysisStage update for stage {} with status {}",
      request.getStageIntervalId().getDefinitionId().getName(), request.getStatus());

    var updatedInterval = workflowAccessor.updateIfPresent(request.getStageIntervalId(),
      stageInterval -> updateStageStatus(castToInteractiveOrThrow(stageInterval), request.getStatus(), request.getUserName())
        .map(StageInterval.class::cast));

    updatedInterval.ifPresent(interval -> {
      var updatedStageIntervalList = List.of(interval);
      var systemEvent = SystemEvent.from(SYSTEM_MESSAGE_EVENT_TYPE, updatedStageIntervalList, 0);
      logger.debug("Sending {} Stage SystemEvent(s) updates", updatedStageIntervalList.size());
      systemEventPublisher.sendSystemEvent(systemEvent);
    });
  }

  private Optional<InteractiveAnalysisStageInterval> updateStageStatus(
    InteractiveAnalysisStageInterval interactiveStageInterval, IntervalStatus status, String analyst) {
    var interactiveAnalysisStageIntervals = List.of(interactiveStageInterval);
    var modTime = Instant.now();

    List<InteractiveAnalysisStageInterval> updatedStageIntervals;
    if (status.equals(IntervalStatus.IN_PROGRESS)) {
      updatedStageIntervals = IntervalUtility.openInteractiveStageIntervals(interactiveAnalysisStageIntervals, analyst);
    } else if (status.equals(IntervalStatus.NOT_COMPLETE)) {
      updatedStageIntervals = IntervalUtility.closeInteractiveStageIntervals(interactiveAnalysisStageIntervals, analyst);
    } else if (status.equals(IntervalStatus.COMPLETE)) {
      updatedStageIntervals = IntervalUtility.completeInteractiveStageIntervals(interactiveAnalysisStageIntervals);
    } else {
      throw new InvalidInputException(
        format("Attempting to update analysis stage interval to invalid status {%s}", status));
    }

    return updateModTime(
      interactiveStageInterval,
      updatedStageIntervals.get(0),
      modTime
    );
  }

  /**
   * Updates the activity interval whose metadata matches the provided activity ID and stage ID with the provided
   * status
   *
   * @param request {@link UpdateActivityIntervalStatusRequest} defining request parameters
   */
  @PostMapping(value = "/interval/activity/update")
  @Operation(summary = "Updates the activity interval whose metadata matches the provided activity ID and stage ID with the provided status.")
  public void updateActivityIntervalStatus(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Activity interval ID, stage interval ID and status to update")
    @RequestBody UpdateActivityIntervalStatusRequest request) {

    logger.debug("Handling ActivityIntervalStatus update for activity {} with status {}",
      request.getActivityIntervalId(), request.getStatus());

    var updatedIntervalOpt = workflowAccessor.updateIfPresent(request.getStageIntervalId(),
      stageInterval -> updateActivityStatus(castToInteractiveOrThrow(stageInterval),
        request.getActivityIntervalId(), request.getStatus(), request.getUserName())
        .map(StageInterval.class::cast));

    updatedIntervalOpt.ifPresent(stageInterval -> {
      var wrappedStageInterval = List.of(stageInterval);
      var systemEvent = SystemEvent.from(SYSTEM_MESSAGE_EVENT_TYPE, wrappedStageInterval, 0);
      logger.debug("Sending {} Stage SystemEvent(s) updates", wrappedStageInterval.size());
      systemEventPublisher.sendSystemEvent(systemEvent);
    });
  }

  private InteractiveAnalysisStageInterval castToInteractiveOrThrow(StageInterval stageInterval) {
    if (!(stageInterval instanceof InteractiveAnalysisStageInterval)) {
      throw new InvalidInputException(
        format("The requested StageInterval for Id {%s} was an AutomaticProcessingStageInterval",
          stageInterval.getIntervalId()));
    }

    return (InteractiveAnalysisStageInterval) stageInterval;
  }

  private Optional<InteractiveAnalysisStageInterval> updateActivityStatus(
    InteractiveAnalysisStageInterval interactiveStageInterval, IntervalId activityIntervalId,
    IntervalStatus intervalStatus, String analyst) {
    var modTime = Instant.now();

    InteractiveAnalysisStageInterval updatedStageInterval;
    if (intervalStatus.equals(IntervalStatus.IN_PROGRESS)) {
      updatedStageInterval = IntervalUtility.openActivityInterval(interactiveStageInterval, activityIntervalId, analyst);
    } else if (intervalStatus.equals(IntervalStatus.NOT_COMPLETE)) {
      updatedStageInterval = IntervalUtility.closeActivityInterval(interactiveStageInterval, activityIntervalId, analyst);
    } else if (intervalStatus.equals(IntervalStatus.COMPLETE)) {
      updatedStageInterval = IntervalUtility.completeActivityInterval(interactiveStageInterval, activityIntervalId, analyst);
    } else {
      throw new InvalidInputException(
        format("Attempting to update activity interval to status {%s}", intervalStatus));
    }

    return updateModTime(interactiveStageInterval, updatedStageInterval, modTime);
  }

  private Optional<InteractiveAnalysisStageInterval> updateModTime(
    InteractiveAnalysisStageInterval originalStageInterval,
    InteractiveAnalysisStageInterval updatedStageInterval,
    Instant modTime) {

    checkArgument(originalStageInterval.getActivityIntervals().size() == updatedStageInterval.getActivityIntervals().size(),
      "The originalStageInterval and the updatedStageInterval do not have the same number of activities!");

    var originalActivitiesIterator = originalStageInterval.activityIntervals().iterator();
    var updatedActivitiesIterator = updatedStageInterval.activityIntervals().iterator();

    var updatedActivityIntervals = new ArrayList<ActivityInterval>();
    var activitiesUpdated = false;
    var stageIntervalUpdated = false;

    while (originalActivitiesIterator.hasNext() && updatedActivitiesIterator.hasNext()) {
      var activityInterval = originalActivitiesIterator.next();
      var updatedActivity = updatedActivitiesIterator.next();

      if (!activityInterval.equals(updatedActivity)) {
        activitiesUpdated = true;
        updatedActivity = updatedActivity.toBuilder()
          .setModificationTime(modTime)
          .build();
      }

      updatedActivityIntervals.add(updatedActivity);
    }

    //check if the originalStage status got updated
    stageIntervalUpdated = !originalStageInterval.getStatus().equals(updatedStageInterval.getStatus());

    if (activitiesUpdated || stageIntervalUpdated) {
      return Optional.of(
        updatedStageInterval.toBuilder()
          .setModificationTime(modTime)
          .setActivityIntervals(updatedActivityIntervals)
          .build()
      );
    } else {
      return Optional.empty();
    }
  }

}

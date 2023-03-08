package gms.shared.workflow.coi;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class IntervalFixtures {

  private IntervalFixtures() {
    // hide implicit public constructor
  }

  static final String STAGE_NAME = "stage1";
  public static final String ACTIVITY_1_NAME = "activity1";
  static final String ACTIVITY_2_NAME = "activity2";
  public static final String ANALYST_1 = "analyst1";
  public static final String ANALYST_2 = "analyst2";
  static final String COMMENT = "comment";

  public static final Instant START_TIME = Instant.EPOCH;
  static final Instant END_TIME = START_TIME.plusSeconds(1);

  public static final IntervalId activityInterval1Id = IntervalId.from(START_TIME,
    WorkflowDefinitionId.from(ACTIVITY_1_NAME));

  public static final IntervalId activityInterval2Id = IntervalId.from(START_TIME,
    WorkflowDefinitionId.from(ACTIVITY_2_NAME));

  static final ActivityInterval notStartedActivityInterval1 = ActivityInterval.builder()
    .setName(activityInterval1Id.getDefinitionId().getName())
    .setStageName(STAGE_NAME)
    .setStartTime(activityInterval1Id.getStartTime())
    .setEndTime(END_TIME)
    .setStatus(IntervalStatus.NOT_STARTED)
    .setActiveAnalysts(List.of())
    .setPercentAvailable(50.0)
    .setProcessingStartTime(START_TIME)
    .setProcessingEndTime(END_TIME)
    .setModificationTime(START_TIME.plusSeconds(2))
    .setStorageTime(START_TIME.plusSeconds(3))
    .setComment(COMMENT)
    .build();

  public static final ActivityInterval notStartedActivityInterval2 = ActivityInterval.builder()
    .setName(activityInterval2Id.getDefinitionId().getName())
    .setStageName(STAGE_NAME)
    .setStartTime(activityInterval2Id.getStartTime())
    .setEndTime(END_TIME)
    .setStatus(IntervalStatus.NOT_STARTED)
    .setActiveAnalysts(List.of())
    .setPercentAvailable(50.0)
    .setProcessingStartTime(START_TIME)
    .setProcessingEndTime(END_TIME)
    .setModificationTime(START_TIME.plusSeconds(2))
    .setStorageTime(START_TIME.plusSeconds(3))
    .setComment(COMMENT)
    .build();

  public static final ActivityInterval inProgressActivityInterval1 = notStartedActivityInterval1.toBuilder()
    .setStatus(IntervalStatus.IN_PROGRESS)
    .setActiveAnalysts(List.of(ANALYST_1, ANALYST_2))
    .build();

  public static final ActivityInterval inProgressActivityInterval2 = notStartedActivityInterval2.toBuilder()
    .setStatus(IntervalStatus.IN_PROGRESS)
    .setActiveAnalysts(List.of(ANALYST_1))
    .build();

  public static final ActivityInterval notCompleteActivityInterval1 = inProgressActivityInterval1.toBuilder()
    .setStatus(IntervalStatus.NOT_COMPLETE)
    .setActiveAnalysts(List.of())
    .build();

  public static final ActivityInterval notCompleteActivityInterval2 = inProgressActivityInterval2.toBuilder()
    .setStatus(IntervalStatus.NOT_COMPLETE)
    .setActiveAnalysts(List.of())
    .build();

  public static final ActivityInterval completeActivityInterval1 = inProgressActivityInterval1.toBuilder()
    .setStatus(IntervalStatus.COMPLETE)
    .setActiveAnalysts(List.of())
    .build();

  public static final ActivityInterval completeActivityInterval2 = inProgressActivityInterval2.toBuilder()
    .setStatus(IntervalStatus.COMPLETE)
    .setActiveAnalysts(List.of())
    .build();

  public static final InteractiveAnalysisStageInterval notStartedInteractiveAnalysisStageInterval = InteractiveAnalysisStageInterval
    .builder()
    .setName(STAGE_NAME)
    .setStartTime(START_TIME)
    .setEndTime(START_TIME)
    .setStatus(IntervalStatus.NOT_STARTED)
    .setPercentAvailable(100.0)
    .setProcessingStartTime(START_TIME)
    .setProcessingEndTime(START_TIME)
    .setModificationTime(START_TIME)
    .setStorageTime(START_TIME)
    .setActivityIntervals(List.of(notStartedActivityInterval1, notStartedActivityInterval2))
    .setComment(COMMENT)
    .build();

  public static final InteractiveAnalysisStageInterval inProgressInteractiveAnalysisStageInterval = notStartedInteractiveAnalysisStageInterval.toBuilder()
    .setStatus(IntervalStatus.IN_PROGRESS)
    .setActivityIntervals(List.of(inProgressActivityInterval1, inProgressActivityInterval2))
    .build();

  public static final InteractiveAnalysisStageInterval singleActivityInProgressInteractiveAnalysisStageInterval = notStartedInteractiveAnalysisStageInterval.toBuilder()
    .setStatus(IntervalStatus.IN_PROGRESS)
    .setActivityIntervals(List.of(notStartedActivityInterval1, inProgressActivityInterval2))
    .build();

  public static final InteractiveAnalysisStageInterval notCompleteInteractiveAnalysisStageInterval = inProgressInteractiveAnalysisStageInterval.toBuilder()
    .setStatus(IntervalStatus.NOT_COMPLETE)
    .setActivityIntervals(List.of(notCompleteActivityInterval1, notCompleteActivityInterval2))
    .build();

  public static final InteractiveAnalysisStageInterval partiallyCompleteInteractiveAnalysisStageInterval = inProgressInteractiveAnalysisStageInterval.toBuilder()
    .setStatus(IntervalStatus.IN_PROGRESS)
    .setActivityIntervals(List.of(inProgressActivityInterval2, completeActivityInterval2))
    .build();

  public static final InteractiveAnalysisStageInterval completedInteractiveAnalysisStageInterval = inProgressInteractiveAnalysisStageInterval.toBuilder()
    .setStatus(IntervalStatus.COMPLETE)
    .setActivityIntervals(List.of(completeActivityInterval1, completeActivityInterval2))
    .build();

  public static final InteractiveAnalysisStageInterval readyForCompleteInteractiveAnalysisStageInterval = inProgressInteractiveAnalysisStageInterval.toBuilder()
    .setActivityIntervals(List.of(completeActivityInterval1, completeActivityInterval2))
    .build();

  public static final AutomaticProcessingStageInterval emptyAutoProcessingStageInterval = AutomaticProcessingStageInterval.builder()
    .setName(STAGE_NAME)
    .setStartTime(START_TIME)
    .setEndTime(START_TIME)
    .setStatus(IntervalStatus.NOT_STARTED)
    .setPercentAvailable(100.0)
    .setProcessingStartTime(START_TIME)
    .setProcessingEndTime(START_TIME)
    .setModificationTime(START_TIME)
    .setStorageTime(START_TIME)
    .setSequenceIntervals(Collections.emptyList())
    .setComment(COMMENT)
    .build();
}

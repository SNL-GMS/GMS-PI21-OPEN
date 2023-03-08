package gms.shared.workflow.coi;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class MockIntervalData {

  private static final String A_COMMENT = "A comment";
  private static final StageMetrics stageMetrics = StageMetrics.from(3, 3, 1, 4.4);

  private MockIntervalData() {
  }

  public static Map<String, List<StageInterval>> get(Instant startTime,
    Instant endTime, Collection<WorkflowDefinitionId> stageIds) {
    return stageIds.stream()
      .map(WorkflowDefinitionId::getName)
      .map(name -> generateStage(name, startTime, endTime))
      .collect(toMap(StageInterval::getName, List::of));
  }

  private static StageInterval generateStage(String stageName, Instant startTime, Instant endTime) {
    if (stageName.startsWith("Auto")) {
      return generateAuto(stageName, List.of(stageName + " Seq"), startTime, endTime);
    } else {
      return generateInteractive(stageName, List.of("Event Review", "Scan"), startTime, endTime);
    }

  }

  private static AutomaticProcessingStageInterval generateAuto(String stageName, List<String> sequenceNames,
    Instant startTime,
    Instant endTime) {
    List<ProcessingSequenceInterval> sequenceIntervals = sequenceNames.stream()
      .map(sequenceName -> generateSequence(sequenceName, startTime, endTime))
      .collect(toList());

    return AutomaticProcessingStageInterval.builder()
      .setName(stageName)
      .setComment(A_COMMENT)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setModificationTime(endTime)
      .setPercentAvailable(100.0)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setProcessingStartTime(startTime)
      .setProcessingEndTime(endTime)
      .setStorageTime(endTime)
      .setStageMetrics(stageMetrics)
      .setSequenceIntervals(sequenceIntervals)
      .build();
  }

  private static ProcessingSequenceInterval generateSequence(String sequenceName, Instant startTime, Instant endTime) {
    return ProcessingSequenceInterval.builder()
      .setName(sequenceName)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setProcessingStartTime(startTime)
      .setProcessingEndTime(endTime)
      .setStorageTime(endTime)
      .setModificationTime(endTime)
      .setPercentAvailable(100.00)
      .setComment(A_COMMENT)
      .setStageName(sequenceName)
      .setPercentComplete(50.0)
      .setLastExecutedStepName("Step Name")
      .build();
  }

  private static InteractiveAnalysisStageInterval generateInteractive(String stageName, List<String> activityNames,
    Instant startTime, Instant endTime) {

    List<ActivityInterval> activityIntervals = activityNames.stream()
      .map(activityName -> generateActivity(stageName, activityName, startTime, endTime))
      .collect(toList());

    return InteractiveAnalysisStageInterval.builder()
      .setName(stageName)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setProcessingStartTime(startTime)
      .setProcessingEndTime(endTime)
      .setStorageTime(endTime)
      .setModificationTime(endTime)
      .setPercentAvailable(100.0)
      .setComment(A_COMMENT)
      .setStageMetrics(stageMetrics)
      .setActivityIntervals(activityIntervals)
      .build();

  }

  private static ActivityInterval generateActivity(String stageName, String activityName, Instant startTime,
    Instant endTime) {
    return ActivityInterval.builder()
      .setName(activityName)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setProcessingStartTime(startTime)
      .setProcessingEndTime(endTime)
      .setStorageTime(endTime)
      .setModificationTime(endTime)
      .setPercentAvailable(100.00)
      .setComment(A_COMMENT)
      .setStageName(stageName)
      .setActiveAnalysts(List.of("analyst 1", "analyst 2"))
      .build();
  }

}

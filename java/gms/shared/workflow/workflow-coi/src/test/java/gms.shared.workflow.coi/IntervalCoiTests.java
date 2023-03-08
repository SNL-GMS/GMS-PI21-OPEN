package gms.shared.workflow.coi;


import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class IntervalCoiTests {

  private static final String NAME = "name";
  private static final String STAGE_NAME = "stage name";
  private static ActivityInterval activityInterval;
  private static StageMetrics stageMetrics;
  private static ProcessingSequenceInterval processingSequenceInterval;
  private static AutomaticProcessingStageInterval automaticProcessingStageInterval;
  private static InteractiveAnalysisStageInterval interactiveAnalysisStageInterval;

  @BeforeAll
  public static void init() {
    activityInterval = ActivityInterval.builder()
      .setName(NAME)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(Instant.EPOCH)
      .setEndTime(Instant.EPOCH)
      .setProcessingStartTime(Instant.EPOCH)
      .setProcessingEndTime(Instant.EPOCH)
      .setStorageTime(Instant.EPOCH)
      .setModificationTime(Instant.EPOCH)
      .setPercentAvailable(50.0)
      .setComment("comment")
      .setStageName(STAGE_NAME)
      .setActiveAnalysts(List.of("Analyst1", "Analyst2"))
      .build();
    stageMetrics = StageMetrics.from(1, 1, 1, 1.1);
    processingSequenceInterval = ProcessingSequenceInterval.builder()
      .setName(NAME)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(Instant.EPOCH)
      .setEndTime(Instant.EPOCH)
      .setProcessingStartTime(Instant.EPOCH)
      .setProcessingEndTime(Instant.EPOCH)
      .setStorageTime(Instant.EPOCH)
      .setModificationTime(Instant.EPOCH)
      .setPercentAvailable(1.1)
      .setComment("Comment")
      .setStageName(STAGE_NAME)
      .setPercentComplete(1.1)
      .setLastExecutedStepName("StepName")
      .build();
    automaticProcessingStageInterval = AutomaticProcessingStageInterval.builder()
      .setName(NAME)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(Instant.EPOCH)
      .setEndTime(Instant.EPOCH)
      .setProcessingStartTime(Instant.EPOCH)
      .setProcessingEndTime(Instant.EPOCH)
      .setStorageTime(Instant.EPOCH)
      .setModificationTime(Instant.EPOCH)
      .setPercentAvailable(1.1)
      .setComment("Comment")
      .setStageMetrics(stageMetrics)
      .setSequenceIntervals(List.of(processingSequenceInterval))
      .build();
    interactiveAnalysisStageInterval = InteractiveAnalysisStageInterval.builder()
      .setName(NAME)
      .setStatus(IntervalStatus.IN_PROGRESS)
      .setStartTime(Instant.EPOCH)
      .setEndTime(Instant.EPOCH)
      .setProcessingStartTime(Instant.EPOCH)
      .setProcessingEndTime(Instant.EPOCH)
      .setStorageTime(Instant.EPOCH)
      .setModificationTime(Instant.EPOCH)
      .setPercentAvailable(1.1)
      .setComment("Comment")
      .setStageMetrics(stageMetrics)
      .setActivityIntervals(List.of(activityInterval))
      .build();
  }

  @Test
  void testActivityIntervalSerialization() {
    TestUtilities.assertSerializes(activityInterval, ActivityInterval.class);
  }

  @Test
  void testStageMetricsSerialization() {
    TestUtilities.assertSerializes(stageMetrics, StageMetrics.class);
  }

  @Test
  void testProcessingSequenceIntervalSerialization() {
    TestUtilities.assertSerializes(processingSequenceInterval, ProcessingSequenceInterval.class);
  }

  @Test
  void testAutomaticProcessingStageIntervalSerialization() {
    TestUtilities.assertSerializes(automaticProcessingStageInterval, AutomaticProcessingStageInterval.class);
    TestUtilities.assertSerializes(automaticProcessingStageInterval, StageInterval.class);
  }

  @Test
  void testInteractiveAnalysisStageIntervalSerialization() {
    TestUtilities.assertSerializes(interactiveAnalysisStageInterval, InteractiveAnalysisStageInterval.class);
    TestUtilities.assertSerializes(interactiveAnalysisStageInterval, StageInterval.class);
  }
}

package gms.shared.workflow.repository.util;

import gms.shared.workflow.coi.ActivityInterval;
import gms.shared.workflow.coi.AutomaticProcessingStageInterval;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.ProcessingSequenceInterval;
import gms.shared.workflow.coi.StageInterval;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class IntervalShiftingUtility {

  private IntervalShiftingUtility() {
    // Hide implicit public constructor
  }

  public static StageInterval shiftStageInterval(StageInterval stageInterval, Duration timeShift) {
    if (stageInterval instanceof AutomaticProcessingStageInterval) {
      return ((AutomaticProcessingStageInterval) stageInterval).toBuilder()
        .setStartTime(stageInterval.getStartTime().plus(timeShift))
        .setEndTime(stageInterval.getEndTime().plus(timeShift))
        .setProcessingStartTime(stageInterval.getProcessingStartTime().plus(timeShift))
        .setProcessingEndTime(stageInterval.getProcessingEndTime().plus(timeShift))
        .setStorageTime(stageInterval.getStorageTime().plus(timeShift))
        .setModificationTime(stageInterval.getModificationTime().plus(timeShift))
        .setSequenceIntervals(shiftSequenceIntervals(((AutomaticProcessingStageInterval) stageInterval).getSequenceIntervals(), timeShift))
        .build();
    } else if (stageInterval instanceof InteractiveAnalysisStageInterval) {
      return ((InteractiveAnalysisStageInterval) stageInterval).toBuilder()
        .setStartTime(stageInterval.getStartTime().plus(timeShift))
        .setEndTime(stageInterval.getEndTime().plus(timeShift))
        .setProcessingStartTime(stageInterval.getProcessingStartTime().plus(timeShift))
        .setProcessingEndTime(stageInterval.getProcessingEndTime().plus(timeShift))
        .setStorageTime(stageInterval.getStorageTime().plus(timeShift))
        .setModificationTime(stageInterval.getModificationTime().plus(timeShift))
        .setActivityIntervals(shiftActivityIntervals(((InteractiveAnalysisStageInterval) stageInterval).getActivityIntervals(), timeShift))
        .build();
    } else {
      throw new IllegalStateException("Stage interval is of unknown subclass!!!");
    }
  }

  public static List<ActivityInterval> shiftActivityIntervals(List<ActivityInterval> activityIntervals,
    Duration timeShift) {
    return activityIntervals.stream()
      .map(activityInterval -> activityInterval.toBuilder()
        .setStartTime(activityInterval.getStartTime().plus(timeShift))
        .setEndTime(activityInterval.getEndTime().plus(timeShift))
        .setProcessingStartTime(activityInterval.getProcessingStartTime().plus(timeShift))
        .setProcessingEndTime(activityInterval.getProcessingEndTime().plus(timeShift))
        .setStorageTime(activityInterval.getStorageTime().plus(timeShift))
        .setModificationTime(activityInterval.getModificationTime().plus(timeShift))
        .build()
      ).collect(Collectors.toList());
  }

  public static List<ProcessingSequenceInterval> shiftSequenceIntervals(
    List<ProcessingSequenceInterval> sequenceIntervals, Duration timeShift) {
    return sequenceIntervals.stream()
      .map(sequenceInterval -> sequenceInterval.toBuilder()
        .setStartTime(sequenceInterval.getStartTime().plus(timeShift))
        .setEndTime(sequenceInterval.getEndTime().plus(timeShift))
        .setProcessingStartTime(sequenceInterval.getProcessingStartTime().plus(timeShift))
        .setProcessingEndTime(sequenceInterval.getProcessingEndTime().plus(timeShift))
        .setStorageTime(sequenceInterval.getStorageTime().plus(timeShift))
        .setModificationTime(sequenceInterval.getModificationTime().plus(timeShift))
        .build()
      ).collect(Collectors.toList());
  }
}

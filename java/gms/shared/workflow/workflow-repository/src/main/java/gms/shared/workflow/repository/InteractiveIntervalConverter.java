package gms.shared.workflow.repository;

import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;
import gms.shared.workflow.coi.ActivityInterval;
import gms.shared.workflow.coi.InteractiveAnalysisStage;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.dao.IntervalDao;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts legacy {@link IntervalDao}s to an {@link InteractiveAnalysisStageInterval}
 */
class InteractiveIntervalConverter {
  private static final InstantToDoubleConverterPositiveNa instantToDoubleConverter = new InstantToDoubleConverterPositiveNa();

  /**
   * Converts a legacy {@link IntervalDao} to an {@link InteractiveAnalysisStageInterval}
   *
   * @param intervalDao Legacy {@link IntervalDao} to convert
   * @param stage The IntervalDao is converted to this {@link InteractiveAnalysisStage}
   * @return Converted {@link InteractiveAnalysisStageInterval}
   */
  public InteractiveAnalysisStageInterval fromLegacy(IntervalDao intervalDao,
    InteractiveAnalysisStage stage) {

    var name = stage.getName();
    var status = fromLegacyState(intervalDao.getState());
    var startTime = instantToDoubleConverter.convertToEntityAttribute(intervalDao.getTime());
    var endTime = instantToDoubleConverter.convertToEntityAttribute(intervalDao.getEndTime());
    var processingStartTime = intervalDao.getProcessStartDate();
    var processingEndTime = intervalDao.getProcessEndDate();
    var storageTime = intervalDao.getLoadDate();
    var modificationTime = intervalDao.getLastModificationDate();
    var percentAvailable = intervalDao.getPercentAvailable();
    var comment = "";

    var activityIntervals = stage.getActivities().stream().map(activity ->
      ActivityInterval.builder()
        .setName(activity.getName())
        .setStatus(status)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setProcessingStartTime(processingStartTime)
        .setProcessingEndTime(processingEndTime)
        .setStorageTime(storageTime)
        .setModificationTime(modificationTime)
        .setPercentAvailable(percentAvailable)
        .setComment(comment)
        .setStageName(name)
        .setActiveAnalysts(List.of())
        .build()
    ).collect(Collectors.toList());

    return InteractiveAnalysisStageInterval.builder()
      .setName(name)
      .setStatus(status)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setProcessingStartTime(processingStartTime)
      .setProcessingEndTime(processingEndTime)
      .setStorageTime(storageTime)
      .setModificationTime(modificationTime)
      .setPercentAvailable(percentAvailable)
      .setComment(comment)
      .setActivityIntervals(activityIntervals)
      .build();
  }

  private static IntervalStatus fromLegacyState(String legacyState) {
    IntervalStatus intervalStatus;
    if ("pending".equals(legacyState) || "queued".equals(legacyState) || "skipped".equals(legacyState)) {
      intervalStatus = IntervalStatus.NOT_STARTED;
    } else if ("active".equals(legacyState)) {
      intervalStatus = IntervalStatus.IN_PROGRESS;
    } else if ("done".equals(legacyState) || "late-done".equals(legacyState)) {
      intervalStatus = IntervalStatus.COMPLETE;
    } else {
      throw new IllegalArgumentException(String
        .format("Could not convert legacy state {%s} to {%s}", legacyState,
          IntervalStatus.class.getSimpleName()));
    }

    return intervalStatus;
  }
}

package gms.shared.workflow.repository;

import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;
import gms.shared.workflow.coi.AutomaticProcessingStage;
import gms.shared.workflow.coi.AutomaticProcessingStageInterval;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.coi.ProcessingSequence;
import gms.shared.workflow.coi.ProcessingSequenceInterval;
import gms.shared.workflow.coi.ProcessingStep;
import gms.shared.workflow.dao.IntervalDao;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toList;

/**
 * Converts legacy {@link IntervalDao}s to modern {@link AutomaticProcessingStageInterval}s
 */
public class AutomaticIntervalConverter {

  private static final Map<String, String> processToStep = Map.ofEntries(
    entry("assoc", "Association"),
    entry("arrbeamSP", "Arrival Beam SP"),
    entry("conflict", "Conflict Resolution"),
    entry("HAE", "HAE"),
    entry("hydroEDP", "Hydro EDP"),
    entry("LPDet", "Detection LP"),
    entry("LPrecall", "Recall LP"),
    entry("magnitude", "Magnitude"),
    entry("origbeamSP", "Origin Beam SP"),
    entry("origbeamLP", "Origin Beam LP"),
    entry("partproc", "Partial Processing"),
    entry("recall", "Recall")
  );

  private final InstantToDoubleConverterPositiveNa instantToDoubleConverter;

  /**
   * Creates a new AutomaticIntervalConverter
   */
  public AutomaticIntervalConverter() {
    instantToDoubleConverter = new InstantToDoubleConverterPositiveNa();
  }

  /**
   * Converts a legacy {@link IntervalDao} to an {@link AutomaticProcessingStageInterval}
   *
   * @param intervalDao Legacy {@link IntervalDao} to convert
   * @param stage The IntervalDao is converted to this {@link AutomaticProcessingStage}
   * @return AutomaticProcessingStageInterval converted from legacy IntervalDao
   */
  public AutomaticProcessingStageInterval fromLegacy(IntervalDao intervalDao,
    AutomaticProcessingStage stage) {
    return fromLegacy(intervalDao, stage,
      instantToDoubleConverter.convertToEntityAttribute(intervalDao.getTime()),
      instantToDoubleConverter.convertToEntityAttribute(intervalDao.getEndTime()));
  }

  /**
   * Converts a legacy {@link IntervalDao} to an {@link AutomaticProcessingStageInterval}
   *
   * @param intervalDao intervalDao Legacy {@link IntervalDao} to convert
   * @param stage The IntervalDao is converted to this {@link AutomaticProcessingStage}
   * @param startTime Start time of the stage interval
   * @param endTime End time of the stage interval
   * @return AutomaticProcessingStageInterval converted from legacy IntervalDao
   */
  public AutomaticProcessingStageInterval fromLegacy(IntervalDao intervalDao,
    AutomaticProcessingStage stage,
    Instant startTime, Instant endTime) {
    String stageName = stage.getName();
    String state = intervalDao.getState();
    var intervalStatus = parseAutomatedStatus(state);

    ProcessingSequenceInterval templateInterval = buildSequenceTemplate(intervalDao, startTime,
      endTime, stageName, intervalStatus);

    List<ProcessingSequenceInterval> sequenceIntervals = stage.getSequences().stream()
      .map(sequence -> buildSequenceFromTemplate(templateInterval, state, sequence))
      .collect(toList());

    return AutomaticProcessingStageInterval.builder()
      .setName(stageName)
      .setStatus(intervalStatus)
      .setStartTime(startTime)
      .setEndTime(endTime)
      .setProcessingStartTime(intervalDao.getProcessStartDate())
      .setProcessingEndTime(intervalDao.getProcessEndDate())
      .setStorageTime(intervalDao.getLoadDate())
      .setModificationTime(intervalDao.getLastModificationDate())
      .setPercentAvailable(intervalDao.getPercentAvailable())
      .setComment("")
      .setSequenceIntervals(sequenceIntervals)
      .build();
  }

  private static IntervalStatus parseAutomatedStatus(String state) {
    if ("pending".equals(state) || "queued".equals(state) || "skipped".equals(state)) {
      return IntervalStatus.NOT_STARTED;
    } else if (state.endsWith("-start")) {
      return IntervalStatus.IN_PROGRESS;
    } else if ("done".equals(state) || "network-done".equals(state) || "late-done".equals(state)) {
      return IntervalStatus.COMPLETE;
    } else if ("failed".equals(state)) {
      return IntervalStatus.FAILED;
    } else {
      throw new IllegalArgumentException(format("Could not convert legacy state {%s} to {%s}",
        state, IntervalStatus.class.getSimpleName()));
    }
  }

  private static ProcessingSequenceInterval buildSequenceTemplate(IntervalDao interval, Instant start,
    Instant end, String stageName,
    IntervalStatus intervalStatus) {
    return ProcessingSequenceInterval.builder()
      .setName("template")
      .setStatus(intervalStatus)
      .setStartTime(start)
      .setEndTime(end)
      .setProcessingStartTime(interval.getProcessStartDate())
      .setProcessingEndTime(interval.getProcessEndDate())
      .setStorageTime(interval.getLoadDate())
      .setModificationTime(interval.getLastModificationDate())
      .setPercentAvailable(interval.getPercentAvailable())
      .setComment("")
      .setStageName(stageName)
      .setLastExecutedStepName("")
      .setPercentComplete(0.0)
      .build();
  }

  private ProcessingSequenceInterval buildSequenceFromTemplate(
    ProcessingSequenceInterval templateInterval, String state,
    ProcessingSequence sequence) {
    var intervalStatus = templateInterval.getStatus();
    var intervalBuilder = templateInterval.toBuilder()
      .setName(sequence.getName());

    if (IntervalStatus.COMPLETE.equals(intervalStatus)) {
      ProcessingStep lastStep = sequence.getSteps().get(sequence.getSteps().size() - 1);
      intervalBuilder.setLastExecutedStepName(lastStep.getName())
        .setPercentComplete(100.0);
    } else if (IntervalStatus.SKIPPED.equals(intervalStatus) || IntervalStatus.NOT_STARTED
      .equals(intervalStatus)
      || IntervalStatus.FAILED.equals(intervalStatus)) {
      intervalBuilder.setLastExecutedStepName("")
        .setPercentComplete(0.0);
    } else {
      String currentStep = parseStepName(state);
      intervalBuilder
        .setLastExecutedStepName(currentStep)
        .setPercentComplete(calculatePercentComplete(sequence, currentStep));
    }

    return intervalBuilder.build();
  }

  private static String parseStepName(String state) {
    var processName = state.substring(0, state.indexOf("-start"));
    checkArgument(processToStep.containsKey(processName), format("No matching ProcessingStep found for process %s", processName));
    return processToStep.get(processName);
  }

  private static double calculatePercentComplete(ProcessingSequence sequence, String currentStep) {
    List<String> stepNames = sequence.getSteps().stream()
      .map(ProcessingStep::getName)
      .collect(toList());

    int currentStepIndex = stepNames.indexOf(currentStep);
    checkArgument(currentStepIndex > -1, "No step match %s found in processing sequence %s", currentStep, sequence.getName());

    return currentStepIndex == 0 ? 0 : 100.0 * (currentStepIndex - 1) / stepNames.size();
  }
}

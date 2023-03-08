package gms.shared.frameworks.osd.coi.processingcontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Value object holding provenance information identifying where in the processing workflow an
 * automatic, interactive, or interactive initiated processing sequence occurs.  Also includes the
 * storage visibility of any information stored in the OSD as a result of the action.
 */
@AutoValue
public abstract class ProcessingContext {

  public static final String NULL_STORAGE_VISIBILITY = "Can't build ProcessingContext from a null StorageVisibility";

  /**
   * References where automatic processing occurs by stage, sequence, step.  Optional within the
   * ProcessingContext but AnalystActionReference, ProcessingStepReference, or both must exist.
   */
  public abstract Optional<ProcessingStepReference> getProcessingStepReference();

  /**
   * References where an interactive processing actions occurs by stage, activity, analyst. Optional
   * within the ProcessingContext but AnalystActionReference, ProcessingStepReference, or both must
   * exist.
   */
  public abstract Optional<AnalystActionReference> getAnalystActionReference();

  /**
   * Visibility of any objects stored in the OSD as a result of this processing.  Required within
   * the ProcessingContext.
   */
  public abstract StorageVisibility getStorageVisibility();

  /**
   * Constructs a ProcessingContext from the analystActionReference, processingStepReference, and
   * storageVisibility.
   * <p>
   * analystActionReference and processingStepReference are both optional but at least one of them
   * must be present.  The static factory operations in this class enforce this constraint.
   *
   * @param analystActionReference optional AnalystActionReference
   * @param processingStepReference optional ProcessingStepReference
   * @param storageVisibility visibility for objects stored in the OSD, not null
   */
  @JsonCreator
  public static ProcessingContext from(
    @JsonProperty("analystActionReference") Optional<AnalystActionReference> analystActionReference,
    @JsonProperty("processingStepReference") Optional<ProcessingStepReference> processingStepReference,
    @JsonProperty("storageVisibility") StorageVisibility storageVisibility) {

    return new AutoValue_ProcessingContext(processingStepReference, analystActionReference,
      storageVisibility);
  }

  /**
   * Obtains an instance of ProcessingContext for an automatic processing action from the
   * processingStageIntervalId, processingSequenceIntervalId, processingStepId, and
   * storageVisibility.
   * <p>
   * The id parameters are each an id to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingSequenceInterval}, and {@link ProcessingStep}
   * objects forming provenance for where the automatic processing action occurs within the GMS
   * processing workflow.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingSequenceIntervalId id to a ProcessingSequenceInterval, not null
   * @param processingStepId id to a ProcessingStep, not null
   * @param storageVisibility visibility for objects stored in the OSD, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the storageVisibility is null
   */
  public static ProcessingContext createAutomatic(
    UUID processingStageIntervalId,
    UUID processingSequenceIntervalId,
    UUID processingStepId,
    StorageVisibility storageVisibility) {

    Objects.requireNonNull(storageVisibility, NULL_STORAGE_VISIBILITY);

    //Null checks made within the child from methods
    ProcessingStepReference stepRef = ProcessingStepReference
      .from(processingStageIntervalId, processingSequenceIntervalId, processingStepId);

    return ProcessingContext.from(Optional.empty(), Optional.of(stepRef), storageVisibility);
  }

  /**
   * Obtains an instance of ProcessingContext for an interactive processing action from the
   * processingStageIntervalId, processingActivityIntervalId, analystId, and storageVisibility.
   * <p>
   * The id parameters are each an id to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingActivityInterval}, and {@link Analyst} objects
   * forming provenance for where the interactive processing action occurs within the GMS processing
   * workflow.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingActivityIntervalId id to a ProcessingActivityInterval, not null
   * @param analystId id to an Analyst, not null
   * @param storageVisibility visibility for objects stored in the OSD, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the storageVisibility is null
   */
  public static ProcessingContext createInteractive(
    UUID processingStageIntervalId,
    UUID processingActivityIntervalId,
    UUID analystId, StorageVisibility storageVisibility) {

    Objects.requireNonNull(storageVisibility, NULL_STORAGE_VISIBILITY);

    //Null checks made within the child from methods
    AnalystActionReference actionRef = AnalystActionReference
      .from(processingStageIntervalId, processingActivityIntervalId, analystId);

    return ProcessingContext.from(Optional.of(actionRef), Optional.empty(), storageVisibility);
  }

  /**
   * Obtains an instance of ProcessingContext for an interactive initiated processing sequence from
   * the processingStageIntervalId, processingActivityIntervalId, analystId,
   * processingSequenceIntervalId, processingStepId, and storageVisibility.
   * <p>
   * The id parameters are each an id to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingActivityInterval}, {@link Analyst}, {@link
   * ProcessingSequenceInterval}, and {@link ProcessingStep} objects forming provenance for where
   * the interactive processing action occurs within the GMS processing workflow and which
   * processing sequence is initiated.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingActivityIntervalId id to a ProcessingActivityInterval, not null
   * @param analystId id to an Analyst, not null
   * @param processingSequenceIntervalId id to a ProcessingSequenceInterval, not null
   * @param processingStepId id to a ProcessingStep, not null
   * @param storageVisibility visibility for objects stored in the OSD, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the storageVisibility is null
   */
  public static ProcessingContext createInteractiveInitiatedAutomatic(
    UUID processingStageIntervalId,
    UUID processingActivityIntervalId,
    UUID analystId,
    UUID processingSequenceIntervalId,
    UUID processingStepId, StorageVisibility storageVisibility) {

    Objects.requireNonNull(storageVisibility, NULL_STORAGE_VISIBILITY);

    //Null checks made within the child from methods
    AnalystActionReference actionRef = AnalystActionReference
      .from(processingStageIntervalId, processingActivityIntervalId, analystId);

    ProcessingStepReference stepRef = ProcessingStepReference
      .from(processingStageIntervalId, processingSequenceIntervalId, processingStepId);

    return ProcessingContext.from(Optional.of(actionRef), Optional.of(stepRef), storageVisibility);
  }
}

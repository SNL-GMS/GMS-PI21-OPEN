package gms.shared.frameworks.osd.coi.processingcontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

/**
 * Value object holding provenance information identifying where in the processing workflow an
 * automatic processing action occurs.
 */
@AutoValue
public abstract class ProcessingStepReference {

  public abstract UUID getProcessingStageIntervalId();

  public abstract UUID getProcessingSequenceIntervalId();

  public abstract UUID getProcessingStepId();

  /**
   * Obtains an instance from ProcessingStepReference from the processingStageIntervalId,
   * processingSequenceIntervalId, and processingStepId.
   * <p>
   * These parameters are each an id to instances from the {@link
   * ProcessingStageInterval}, {@link ProcessingSequenceInterval}, and {@link ProcessingStep}
   * objects forming the ProcessingStepReference.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingSequenceIntervalId id to a ProcessingSequenceInterval, not null
   * @param processingStepId id to a ProcessingStep, not null
   * @return a ProcessingStepReference, not null
   * @throws IllegalArgumentException if the processingStageIntervalId,
   * processingSequenceIntervalId, or processingStepId are null
   */
  @JsonCreator
  public static ProcessingStepReference from(
    @JsonProperty("processingStageIntervalId") UUID processingStageIntervalId,
    @JsonProperty("processingSequenceIntervalId") UUID processingSequenceIntervalId,
    @JsonProperty("processingStepId") UUID processingStepId) {

    return new AutoValue_ProcessingStepReference(processingStageIntervalId, processingSequenceIntervalId,
      processingStepId);
  }
}

package gms.shared.frameworks.osd.coi.processingcontrol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Objects;
import java.util.UUID;


/**
 * Value object holding provenance information identifying where in the interactive processing
 * workflow an analyst initiated a processing action.
 */
@AutoValue
public abstract class AnalystActionReference {

  public abstract UUID getProcessingStageIntervalId();

  public abstract UUID getProcessingActivityIntervalId();

  public abstract UUID getAnalystId();


  /**
   * Obtains an instance of AnalystActionReference from the processingStageIntervalId,
   * processingActivityIntervalId, and analystId.
   * <p>
   * These parameters are each an {@link UUID} to instances of the {@link
   * ProcessingStageInterval}, {@link ProcessingActivityInterval}, and {@link Analyst} objects
   * forming the AnalystActionReference.
   *
   * @param processingStageIntervalId id to a ProcessingStageInterval, not null
   * @param processingActivityIntervalId id to a ProcessingActivityInterval, not null
   * @param analystId id to an Analyst, not null
   * @return an AnalystActionReference, not null
   * @throws IllegalArgumentException if the processingStageIntervalId,
   * processingActivityIntervalId, or analystId are null
   */
  @JsonCreator
  public static AnalystActionReference from(
    @JsonProperty("processingStageIntervalId") UUID processingStageIntervalId,
    @JsonProperty("processingActivityIntervalId") UUID processingActivityIntervalId,
    @JsonProperty("analystId") UUID analystId) {

    Objects.requireNonNull(processingStageIntervalId,
      "Cannot create AnalystActionReference with null processingStageIntervalId");
    Objects.requireNonNull(processingActivityIntervalId,
      "Cannot create AnalystActionReference with null processingActivityIntervalId");
    Objects.requireNonNull(analystId,
      "Cannot create AnalystActionReference with null analystId");


    return new AutoValue_AnalystActionReference(processingStageIntervalId,
      processingActivityIntervalId, analystId);
  }

}

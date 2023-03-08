package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.workflow.coi.WorkflowDefinitionId;

/**
 * Wraps a preferred {@link EventHypothesis} to include the analyst and stage it was preferred
 */
@AutoValue
public abstract class PreferredEventHypothesis {

  public abstract WorkflowDefinitionId getStage();

  public abstract String getPreferredBy();

  public abstract EventHypothesis getPreferred();

  /**
   * Create an instance of PreferredEventHypothesis
   *
   * @param stage The ID of the processing stage, not null
   * @param preferredBy The name of the analyst marking the hypothesis preferred (should be "System" for automatic stages)
   * @param preferred the single EventHypothesis that is designated as the
   * PreferredEventHypothesis for the Event, across all processing stages. Not null.
   */
  @JsonCreator
  public static PreferredEventHypothesis from(
    @JsonProperty("stage") WorkflowDefinitionId stage,
    @JsonProperty("preferredBy") String preferredBy,
    @JsonProperty("preferred") EventHypothesis preferred) {
    return new AutoValue_PreferredEventHypothesis(stage, preferredBy, preferred);
  }
}

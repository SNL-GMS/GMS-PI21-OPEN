package gms.shared.workflow.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StageMetrics {

  public abstract int getEventCount();

  public abstract int getAssociatedSignalDetectionCount();

  public abstract int getUnassociatedSignalDetectionCount();

  public abstract double getMaxMagnitude();

  @JsonCreator
  public static StageMetrics from(
    @JsonProperty("eventCount") int eventCount,
    @JsonProperty("associatedSignalDetectionCount") int associatedSignalDetectionCount,
    @JsonProperty("unassociatedSignalDetectionCount") int unassociatedSignalDetectionCount,
    @JsonProperty("maxMagnitude") double maxMagnitude) {

    return new AutoValue_StageMetrics(eventCount, associatedSignalDetectionCount,
      unassociatedSignalDetectionCount, maxMagnitude);
  }
}

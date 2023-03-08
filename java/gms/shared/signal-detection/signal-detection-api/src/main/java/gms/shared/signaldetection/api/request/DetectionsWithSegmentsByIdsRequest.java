package gms.shared.signaldetection.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.util.UUID;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DetectionsWithSegmentsByIdsRequest implements Request {
  public abstract ImmutableList<UUID> getDetectionIds();

  @JsonCreator
  public static DetectionsWithSegmentsByIdsRequest create(
    @JsonProperty("detectionIds") ImmutableList<UUID> detectionIds,
    @JsonProperty("stageId") WorkflowDefinitionId stageId
  ) {
    Preconditions.checkState(!detectionIds.isEmpty(),
      "DetectionsWithSegmentsByIdsRequest requires at least 1 Detection Id");

    return new AutoValue_DetectionsWithSegmentsByIdsRequest(stageId, detectionIds);
  }
}

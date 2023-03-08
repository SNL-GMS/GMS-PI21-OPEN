package gms.shared.signaldetection.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Instant;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DetectionsWithSegmentsByStationsAndTimeRequest implements Request {

  public abstract ImmutableList<Station> getStations();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  public abstract ImmutableList<SignalDetection> getExcludedSignalDetections();

  @JsonCreator
  public static DetectionsWithSegmentsByStationsAndTimeRequest create(
    @JsonProperty("stations") ImmutableList<Station> stations,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("stageId") WorkflowDefinitionId stageId,
    @JsonProperty("excludedSignalDetections") ImmutableList<SignalDetection> excludedSignalDetections) {

    Preconditions.checkState(!stations.isEmpty(),
      "DetectionsWithSegmentsByStationsAndTimeRequest requires at least 1 Station");
    Preconditions.checkState(endTime.isAfter(startTime),
      "EndTime must be after startTime");

    return new AutoValue_DetectionsWithSegmentsByStationsAndTimeRequest(stageId,
      stations,
      startTime,
      endTime,
      excludedSignalDetections);
  }

}

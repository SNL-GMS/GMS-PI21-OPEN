package gms.shared.signaldetection.manager;

import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByIdsRequest;
import gms.shared.signaldetection.api.request.DetectionsWithSegmentsByStationsAndTimeRequest;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;
import gms.shared.signaldetection.coi.detection.SignalDetection;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;

@RestController
@RequestMapping(value = "/signal-detection",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
public class SignalDetectionManager {

  private final SignalDetectionAccessorInterface signalDetectionAccessorImpl;

  @Autowired
  public SignalDetectionManager(
    @Qualifier("bridgedSignalDetectionAccessor") SignalDetectionAccessorInterface signalDetectionAccessorImpl) {

    this.signalDetectionAccessorImpl = signalDetectionAccessorImpl;
  }

  /**
   * Retrieves {@link SignalDetectionsWithChannelSegments} based on the stations, time range, stage id and excluded
   * {@link SignalDetection}s in the request.
   *
   * @param request The {@link DetectionsWithSegmentsByStationsAndTimeRequest} defining the request parameters
   * @return The {@link SignalDetectionsWithChannelSegments} satisfying the request parameters
   */
  @PostMapping("/signal-detections-with-channel-segments/query/stations-timerange")
  @Operation(summary = "retrieves all signal detections and associated with channel segments specified by the provided " +
    "stations, time range, stage, and excluding all signal detections having any of the provided signal detection ids")
  public SignalDetectionsWithChannelSegments findDetectionsWithSegmentsByStationsAndTime(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A list of " +
      "stations, a time range, stage id, and signal detection ids to exclude")
    @RequestBody DetectionsWithSegmentsByStationsAndTimeRequest request) {

    return signalDetectionAccessorImpl.findWithSegmentsByStationsAndTime(request.getStations(),
      request.getStartTime(),
      request.getEndTime(),
      request.getStageId(),
      request.getExcludedSignalDetections());
  }

  /**
   * Retrieves {@link SignalDetectionsWithChannelSegments} based on the SignalDetection uuids and stage id in the request
   *
   * @param request The {@link DetectionsWithSegmentsByIdsRequest} defining the request parameters
   * @return The {@link SignalDetectionsWithChannelSegments} satisfying the request parameters
   */
  @PostMapping("/signal-detections-with-channel-segments/query/ids")
  @Operation(summary = "retrieves all signal detections and associated with channel segments specified by the provided " +
    "signal detections ids and stage id")
  public SignalDetectionsWithChannelSegments findDetectionsWithSegmentsByIds(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "A list of signal detection ids and a stage id")
    @RequestBody DetectionsWithSegmentsByIdsRequest request) {

    return signalDetectionAccessorImpl.findWithSegmentsByIds(request.getDetectionIds(),
      request.getStageId());
  }
}

package gms.shared.stationdefinition.api.channel;

import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface ResponseRepositoryInterface {

  /**
   * Finds {@link Response} by response UUIDs and effectiveTime
   *
   * @param responseIds List of UUIDs
   * @param effectiveTime Effective time instant
   * @return List of {@link Response}
   */
  List<Response> findResponsesById(Collection<UUID> responseIds, Instant effectiveTime);

  /**
   * Finds {@link Response} by provided response UUIDs and timeRange
   *
   * @param responseIds Response UUIDs
   * @param startTime The earliest allowable effective time of the responses
   * @param endTime The latest allowable effective time of the responses
   * @return list of {@link Response}s active between provided effective times
   */
  List<Response> findResponsesByIdAndTimeRange(Collection<UUID> responseIds, Instant startTime, Instant endTime);

  /**
   * Finds or creates a {@link Response} using {@link gms.shared.stationdefinition.coi.channel.Calibration}
   * values from a {@link WfdiscDao} record found by the provided ID. If the {@link Response} was previously
   * bridged then this operation returns a version reference to that {@link Response}. If the {@link Response}
   * was not previously bridged then this operation returns the fully populated {@link Response} object.
   *
   * @param wfdiscRecord The provided wfdisc ID
   * @return A {@link Response} based on the provided wfdisc ID
   */
  Response loadResponseFromWfdisc(long wfdiscRecord);

  /**
   * Stores the provided list of {@link Response}s
   *
   * @param responses List of {@link Response}s to store
   */
  default void storeResponses(List<Response> responses) {
    //no op
  }
}

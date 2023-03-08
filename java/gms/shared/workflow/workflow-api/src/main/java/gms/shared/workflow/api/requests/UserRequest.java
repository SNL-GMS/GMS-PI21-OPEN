package gms.shared.workflow.api.requests;

import java.time.Instant;

/**
 * Request body class representing the user who initiated the request and the time at which the request was initiated
 */
public interface UserRequest {

  /**
   * Gets the username of the user who initiated the request
   *
   * @return The username of the user who initiated the request
   */
  String getUserName();

  /**
   * Gets the time at which the request was initiated
   *
   * @return The time at which the request was initiated
   */
  Instant getTime();
}

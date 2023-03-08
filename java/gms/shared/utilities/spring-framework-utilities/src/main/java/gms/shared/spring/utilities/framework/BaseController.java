package gms.shared.spring.utilities.framework;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * BaseController is incorporated into other spring services enabling service check to indicate the service is active
 */
@RestController
public class BaseController {
  /**
   * Returns the current time indicating the service is alive in JSON format. Time is represented in the ISO-8601
   * The format will be:{"aliveAt":"_time_"}
   *
   * @return {@link AliveCheck} representing UTC based current time in the JSON format
   */
  @GetMapping(value = "/alive", consumes = "*/*", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Will return the time this operation was called as JSON.  A HTTP 200 response with current time indicates the service is alive")
  public AliveCheck aliveJson() {
    var alive = AliveCheck.builder();

    return alive.setAliveAt(Instant.now().toString())
      .build();
  }
}

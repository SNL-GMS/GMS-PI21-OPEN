package gms.shared.frameworks.osd.api.performancemonitoring;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface SohStatusChangeRepositoryInterface {

  /**
   * Retrieves unacknowledged soh status changes from osd, if list is empty
   * retrieves all unacknowledged soh status changes from osd
   *
   * @param stationNames
   */
  @Path("/soh/status-change/unacknowledged")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve Unacknowledged Soh Status Changes from the database with specified stations")
  public List<UnacknowledgedSohStatusChange> retrieveUnacknowledgedSohStatusChanges(
    @RequestBody(description = "List of Station Names")
    Collection<String> stationNames);

  /**
   * Save Unacknowledged Soh Status Changes to the database
   *
   * @param unackStatusChanges
   */
  @Path("/soh/status-change/unacknowledged/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Save Unacknowledged Soh Status Changes to the database")
  public void storeUnacknowledgedSohStatusChange(
    @RequestBody(description = "Contains list of UnacknowledgedSohStatusChanges")
    Collection<UnacknowledgedSohStatusChange> unackStatusChanges);

  /**
   * Save Quieted Soh Status Change List to the database
   *
   * @param quietedSohStatusChangeList
   */
  @Path("/soh/status-change/quieted-list")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Save Quieted Soh Status Change List to the database")
  public void storeQuietedSohStatusChangeList(
    @RequestBody(description = "Contains a list of QuietedSohStatusChanges, which includes queitUntil, SohMonitorValueAndStatus, and channelName")
    Collection<QuietedSohStatusChange> quietedSohStatusChangeList);

  /**
   * Save Quieted Soh Status Change List to the database
   *
   * @param currentTime
   */
  @Path("/soh/status-change/quieted-list/retrieve-by-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all Quieted Soh Status Change with quieted-until time later "
    + "than Instant's time from the database")
  public Collection<QuietedSohStatusChange> retrieveQuietedSohStatusChangesByTime(
    @RequestBody(description = "contains an Instant")
    Instant currentTime);

}

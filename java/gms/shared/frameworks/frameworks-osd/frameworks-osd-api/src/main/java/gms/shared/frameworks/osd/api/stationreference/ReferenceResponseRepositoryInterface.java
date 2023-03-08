package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

public interface ReferenceResponseRepositoryInterface {

  /**
   * Retrieves {@link ReferenceResponse}s by the channel names those responses are associated with.
   *
   * @param channelNames the names of the channels to retrieve reference response data for
   * @return list of reference responses; may be empty.
   */
  @Path("/station-reference/reference-responses")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve Reference Responses")
  //TODO: Is it sufficient to only request reference responses by channel name? It seems like the
  // processing version will handle a channel name / time range type request.
  List<ReferenceResponse> retrieveReferenceResponses(
    @RequestBody(description = "Channel Names for ReferenceResponses to retrieve")
    Collection<String> channelNames);

  /**
   * Stores {@link ReferenceResponse}s
   *
   * @param referenceResponses reference response data to store
   */
  @Path("/station-reference/reference-response/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Store Reference Responses")
  void storeReferenceResponses(
    @RequestBody(description = "Collection of ReferenceResponses to store")
    Collection<ReferenceResponse> referenceResponses);

}

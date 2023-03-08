package gms.shared.frameworks.osd.api.instrumentresponse;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ResponseRepositoryInterface {

  @Path("/instrument-response/by-chans")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve Responses by channels")
  Map<String, Response> retrieveResponsesByChannels(
    @RequestBody(description = "Channel names") Set<String> channelNames);

  @Path("/instrument-response/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Store Responses")
  void storeResponses(Collection<Response> responses);
}

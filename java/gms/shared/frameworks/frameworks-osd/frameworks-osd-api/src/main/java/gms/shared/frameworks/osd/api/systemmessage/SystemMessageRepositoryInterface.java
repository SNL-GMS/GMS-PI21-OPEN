package gms.shared.frameworks.osd.api.systemmessage;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;

/**
 * The interface for storing and retrieving {@link SystemMessage} COI objects.
 */
@Path("/smds-service")
public interface SystemMessageRepositoryInterface {

  /**
   * Stores a collection of new {@link SystemMessage} COI objects provided in the request body.
   * Returns a response code but no response body.
   *
   * @param systemMessages The {@link SystemMessage} objects to store.
   */
  @Path("/coi/system-messages/store")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Store SystemMessage objects")
  void storeSystemMessages(
    @RequestBody(description = "SystemMessage objects to store")
    Collection<SystemMessage> systemMessages);
}

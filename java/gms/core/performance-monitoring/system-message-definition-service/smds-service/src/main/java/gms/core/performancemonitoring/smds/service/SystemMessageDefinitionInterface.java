package gms.core.performancemonitoring.smds.service;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageDefinition;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Set;

@Component("system-message-definition-service")
@Path("/smds-service")
public interface SystemMessageDefinitionInterface {

  /**
   * Gets a Set of SystemMessageDefinitions
   *
   * @return {@link Set} of {@link SystemMessageDefinition}s
   */
  @Consumes(ContentType.JSON_NAME)
  @Path("/retrieve-system-message-definitions")
  @POST
  @Operation(description = "Get system message definitions")
  @Produces(ContentType.JSON_NAME)
  Set<SystemMessageDefinition> getSystemMessageDefinitions(String placeholder);

}

package gms.shared.stationdefinition.api.channel;

import gms.shared.frameworks.common.ContentType;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FrequencyAmplitudePhaseRepositoryInterface {

  /**
   * Returns a list of {@link FrequencyAmplitudePhase} objects that corresponds to the list of FrequencyAmplitudePhase Ids passed in.
   * <p>
   * If no ids are passed into this interface, an empty list of FrequencyAmplitudePhases is returned.
   *
   * @param ids List of FrequencyAmplitudePhase Ids to return the list of FrequencyAmplitudePhase Objects for. This list can be empty.
   * @return list of all {@link FrequencyAmplitudePhase} objects for the given set of FrequencyAmplitudePhase ids.
   */
  @Path("/frequencyAmplitudePhase/query/ids")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieves all FrequencyAmplitudePhases specified by a list of FrequencyAmplitudePhase ids")
  List<FrequencyAmplitudePhase> findById(
    @RequestBody(description = "list of FrequencyAmplitudePhase ids", required = true)
    Collection<UUID> ids);

}

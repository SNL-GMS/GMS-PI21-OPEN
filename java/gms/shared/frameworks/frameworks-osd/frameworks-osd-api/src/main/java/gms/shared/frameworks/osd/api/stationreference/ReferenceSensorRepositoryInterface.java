package gms.shared.frameworks.osd.api.stationreference;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReferenceSensorRepositoryInterface {

  /**
   * Retrieves Collection of {@link ReferenceSensor} objects corresponding to the list of
   * sensor ids passed in.
   *
   * @return list of sensors; may be empty.
   */
  @Path("/station-reference/reference-sensors/sensor-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve sensors corresponding to the list of sensor IDs passed in")
  List<ReferenceSensor> retrieveReferenceSensorsById(
    @RequestBody(description = "collection of sensor ids to retrieve")
    Collection<UUID> sensorIds);

  /**
   * Retrieves sensors by the channel they are associated with.
   *
   * @param channelNames the name of the channel whose reference sensors we are retrieving.
   * @return list of sensors; may be empty.
   */
  @Path("/station-reference/reference-sensors/channel-name")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve reference sensors corresponding to the collection of channel names"
    + " passed in")
  Map<String, List<ReferenceSensor>> retrieveSensorsByChannelName(
    @RequestBody(description = "collection of channel names from which we retrieve reference sensors for")
    Collection<String> channelNames);

  /**
   * Stores a collection of {@link ReferenceSensor} objects
   *
   * @param sensors the sensors
   */
  @Path("/station-reference/reference-sensors/new")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "retrieve sensors corresponding to the list of sensor IDs passed in")
  void storeReferenceSensors(
    @RequestBody(description = "collection of reference sensor objects to store.")
    Collection<ReferenceSensor> sensors);

}

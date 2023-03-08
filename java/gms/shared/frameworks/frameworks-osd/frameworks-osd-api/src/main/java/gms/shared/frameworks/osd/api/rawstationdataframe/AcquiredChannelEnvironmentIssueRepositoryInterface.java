package gms.shared.frameworks.osd.api.rawstationdataframe;

import gms.shared.frameworks.common.ContentType;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.api.util.ChannelsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The interface for storing and retrieving COI objects.
 */
public interface AcquiredChannelEnvironmentIssueRepositoryInterface {

  /**
   * Stores a collection of {@link AcquiredChannelEnvironmentIssueBoolean} state of health objects
   * containing boolean values.
   * <p>
   * TODO: Rename to 'store' after deprecated methods are removed.
   *
   * @param aceiUpdates The boolean SOH objects to store.
   */
  @Path("/station-soh/acquired-channel-soh-boolean/store-aceis")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Store (and potentially update) AcquiredChannelSohBoolean objects")
  void syncAceiUpdates(
    @RequestBody(description = "AcquiredChannelEnvironmentIssue<?> objects to remove, store and/or update")
    AceiUpdates aceiUpdates);

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueAnalog} objects for the provided channel
   * created within the provided time range.
   *
   * @param request The collection of channel names and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueAnalog}s retrieved.
   * @return All SOH analog objects that meet the query criteria.
   */
  @Path("/station-soh/acquired-channel-soh-analog/by-channels-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all acquired channel soh analog data for the provided channel name and time range")
  List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByChannelAndTimeRange(
    @RequestBody(description = "Channel name and time range bounding the acquired channel soh "
      + "analog data retrieved")
    ChannelTimeRangeRequest request);

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueAnalog} objects for the provided channel
   * and {@link AcquiredChannelEnvironmentIssueType}, created within the provided time range.
   *
   * @param request The channel name, type, and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueAnalog}s retrieved.
   * @return All SOH analog objects that meet the query criteria.
   */
  @Path("/station-soh/acquired-channel-soh-analog/by-channels-time-type")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all acquired channel soh analog for the provided channel name, " +
    "time range, and type")
  List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByChannelTimeRangeAndType(
    @RequestBody(description = "Channel name, type, and time range bounding the acquired channel "
      +
      "soh analog data retrieved")
    ChannelTimeRangeSohTypeRequest request);

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueBoolean} objects for the provided channel
   * created within the provided time range.
   *
   * @param request The collection of channel names and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueBoolean}s retrieved.
   * @return All SOH boolean objects that meet the query criteria.
   */
  @Path("/station-soh/acquired-channel-soh-boolean/by-channels-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all acquired channel soh boolean data for the provided channel name and time range")
  List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelAndTimeRange(
    @RequestBody(description = "Channel name and time range bounding the acquired channel soh "
      + "boolean data retrieved")
    ChannelTimeRangeRequest request);


  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueBoolean} objects for the provided channel names
   * created within the provided time range.
   *
   * @param request The collection of channel names and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueBoolean}s retrieved.
   * @return All SOH boolean objects that meet the query criteria.
   */
  @Path("/station-soh/acquired-channel-soh-boolean/by-multiple-channels-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all acquired channel soh boolean data for the provided list of channel names and time range")
  List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelsAndTimeRange(
    @RequestBody(description = "Channel names and time range bounding the acquired channel soh "
      + "boolean data retrieved")
    ChannelsTimeRangeRequest request
  );

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueBoolean} objects for the provided channel,
   * {@link AcquiredChannelEnvironmentIssueType}, created within the provided time range.
   *
   * @param request The collection of channel names and time range that will bound the {@link
   * AcquiredChannelEnvironmentIssueBoolean}s retrieved.
   * @return All SOH boolean objects that meet the query criteria.
   */
  @Path("/station-soh/acquired-channel-soh-boolean/by-channels-time-type")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve all acquired channel soh boolean data for the provided channel " +
    "name, time range, and type")
  List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelTimeRangeAndType(
    @RequestBody(description = "Channel name, type, and time range bounding the acquired channel "
      +
      "soh analog data retrieved")
    ChannelTimeRangeSohTypeRequest request);

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueAnalog} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohAnalog has that id.
   *
   * @param compositeId id for the AcquiredChannelSohAnalog, not null
   * @return Optional AcquiredChannelSohAnalog object with the provided id, not null
   */
  @Path("/station-soh/acquired-channel-soh-analog/by-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve acquired channel soh analog data for the provided composite id")
  Optional<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiById(
    @RequestBody(description = "Id for acquired channel environment issue analog data to retrieve")
    AcquiredChannelEnvironmentIssueId compositeId);

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueBoolean} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohBoolean has that id.
   *
   * @param compositeId id for the AcquiredChannelSohBoolean, not null
   * @return Optional AcquiredChannelSohBoolean object with the provided id, not null
   */
  @Path("/station-soh/acquired-channel-soh-boolean/by-id")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve acquired channel soh boolean for the provided composite id")
  Optional<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiById(
    @RequestBody(description = "Id for acquired channel environment issue boolean data to retrieve")
    AcquiredChannelEnvironmentIssueId compositeId);

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueAnalog} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohAnalog has that id.
   *
   * @param request time range request to find AcquiredChannelEnvironmentIssueAnalogs by, not null
   * @return Optional AcquiredChannelSohAnalog object with the provided id, not null
   */
  @Path("/station-soh/acquired-channel-soh-analog/by-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve acquired channel soh analog data for the provided time range")
  List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByTime(
    @RequestBody(description = "Time range for acquired channel environment issue analog data to retrieve")
    TimeRangeRequest request);

  /**
   * Retrieve the {@link AcquiredChannelEnvironmentIssueBoolean} with the provided id.  Returns an
   * empty {@link Optional} if no AcquiredChannelSohBoolean has that id.
   *
   * @param request time range for the AcquiredChannelSohBoolean, not null
   * @return Optional AcquiredChannelSohBoolean object with the provided id, not null
   */
  @Path("/station-soh/acquired-channel-soh-boolean/by-time")
  @POST
  @Consumes(ContentType.JSON_NAME)
  @Produces(ContentType.JSON_NAME)
  @Operation(summary = "Retrieve acquired channel soh boolean for the provided time range")
  List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByTime(
    @RequestBody(description = "Time range for acquired channel environment issue boolean data to retrieve")
    TimeRangeRequest request);

  /**
   * Retrieves all {@link AcquiredChannelEnvironmentIssueBoolean} merge-able with any of the input ACEIS. Mergeable
   * means that their Channel, Type, and status are all equal, and that the time ranges they span are connected within
   * tolerance.
   *
   * @param aceis Boolean Channel issues to find mergeable neighbors for
   * @param tolerance Time tolerance where a channel issue is still connected
   * @return All mergeable channel issues in persistence
   */
  Set<AcquiredChannelEnvironmentIssueBoolean> findMergeable(
    Collection<AcquiredChannelEnvironmentIssueBoolean> aceis, Duration tolerance);
}

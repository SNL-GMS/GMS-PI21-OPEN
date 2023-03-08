package gms.shared.stationdefinition.manager;

import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelsTimeRangeRequest;
import gms.shared.stationdefinition.api.channel.util.ResponseTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ResponseTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationChangeTimesRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeFacetRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeFacetRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRangeRequest;
import gms.shared.stationdefinition.cache.CacheAccessor;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;

import static gms.shared.frameworks.common.ContentType.MSGPACK_NAME;

@RestController
@RequestMapping(value = "/station-definition",
  consumes = MediaType.APPLICATION_JSON_VALUE,
  produces = {MediaType.APPLICATION_JSON_VALUE, MSGPACK_NAME})
public class StationDefinitionManager {

  private static final Logger logger = LoggerFactory.getLogger(StationDefinitionManager.class);

  private final StationDefinitionAccessorInterface stationDefinitionAccessor;
  private final CacheAccessor cacheAccessor;

  private static final String FACETING_ERROR_MESSAGE = "Faceting definition not available";

  @Autowired
  public StationDefinitionManager(
    StationDefinitionAccessorInterface stationDefinitionAccessor,
    CacheAccessor cacheAccessor) {
    this.stationDefinitionAccessor = stationDefinitionAccessor;
    this.cacheAccessor = cacheAccessor;
  }

  @PostConstruct
  private void initialize() {
    // Add a call to the cache to populate on start up
    logger.info("Post construct initialize cache...");

    cacheAccessor.populateCache();
  }


  // Schedule cron for loading the cache on time interval
  @Scheduled(cron = "0 0 * ? * *")
  protected void loadCache() {
    logger.info("Cron scheduled loading cache...");

    cacheAccessor.populateCache();
  }

  /**
   * Retrieves all {@link StationGroup} objects specified by a list of station group names and
   * optional effective time instant and optional faceting definition.
   * If the list is empty, the server will return an empty list.
   *
   * @param request The collections of station groups names, effective time of the groups and faceting definition
   * @return list of {@link StationGroup} objects
   */
  @PostMapping("/station-groups/query/names")
  @Operation(summary = "retrieves all station groups specified by a list of station group names, "
    + "effectiveTime and facetingDefinition")
  public List<StationGroup> findStationGroupsByName(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of station group names, " +
      "effective time and faceting definition")
    @RequestBody StationGroupsTimeFacetRequest request) {

    List<String> stationGroupNames = request.getStationGroupNames();
    Instant effectiveTime = request.getEffectiveTime().orElse(Instant.now());
    if (request.getFacetingDefinition().isPresent()) {
      return stationDefinitionAccessor.findStationGroupsByNameAndTime(stationGroupNames,
        effectiveTime, request.getFacetingDefinition().orElseThrow(() -> new IllegalArgumentException(FACETING_ERROR_MESSAGE)));

    } else {
      return stationDefinitionAccessor.findStationGroupsByNameAndTime(stationGroupNames, effectiveTime);
    }
  }

  /**
   * Retrieves all {@link StationGroup} objects specified by a list of station group names and
   * time range bounding the groups.
   * If the list is empty, the server will return an empty list.
   *
   * @param request The collections of station groups names and time range of the groups
   * @return list of {@link StationGroup} objects
   */
  @PostMapping("/station-groups/query/names-timerange")
  @Operation(summary = "retrieves all station groups specified by a list of station group names and time range")
  public List<StationGroup> findStationGroupsByNameAndTimeRange(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of station group names and time range")
    @RequestBody StationGroupsTimeRangeRequest request) {

    return stationDefinitionAccessor.findStationGroupsByNameAndTimeRange(request.getStationGroupNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());
  }

  /**
   * Returns a list of {@link Station} objects that corresponds to the list of
   * {@link StationsTimeRangeRequest#getStationNames()} passed in with optional
   * effective time and optional faceting definition.
   * If an empty list is provided, this method will return an empty list of stations.
   *
   * @param request {@link StationsTimeFacetRequest} facet request object
   * @return list of all {@link Station} objects for the given set of station names that
   * have an {@link Station#getEffectiveAt()} that is with in the specified time range of the
   * request.
   */
  @PostMapping("/stations/query/names")
  @Operation(description = "returns all stations specified by list of names")
  public List<Station> findStationsByName(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of station names, effective time," +
      " and faceting definition")
    @RequestBody StationsTimeFacetRequest request) {

    List<String> stationNames = request.getStationNames();
    Instant effectiveTime = request.getEffectiveTime().orElse(Instant.now());
    if (request.getFacetingDefinition().isPresent()) {
      return stationDefinitionAccessor.findStationsByNameAndTime(stationNames, effectiveTime,
        request.getFacetingDefinition().orElseThrow(() -> new IllegalArgumentException(FACETING_ERROR_MESSAGE)));
    } else {
      return stationDefinitionAccessor.findStationsByNameAndTime(stationNames, effectiveTime);
    }
  }

  /**
   * Retrieves all {@link Station} objects specified by a list of station names and
   * time range bounding the station.
   * If the list is empty, the server will return an empty list of stations.
   *
   * @param request The collections of station names and time range of the stations
   * @return list of {@link Station} objects
   */
  @PostMapping("/stations/query/names-timerange")
  @Operation(summary = "retrieves all stations specified by a list of station names and time range")
  public List<Station> findStationsByNameAndTimeRange(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of station names and time range")
    @RequestBody StationsTimeRangeRequest request) {

    return stationDefinitionAccessor.findStationsByNameAndTimeRange(request.getStationNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());
  }

  /**
   * Retrieves the times at which the provided {@link Station} and any of its aggregate objects changed, within the
   * provided time range.
   *
   * @param request the {@link Station} and bounding time range
   * @return a collection of {@link Instant}s representing times when new versions of the {@link Station} and/or any
   * of its aggregate objects became effective
   */
  @PostMapping("stations/query/change-times")
  @Operation(summary = "retrieves all times that a station or any of its aggregate members changed")
  public List<Instant> determineStationChangeTimes(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Name of station and start and end time range")
    @RequestBody StationChangeTimesRequest request) {

    return stationDefinitionAccessor.determineStationChangeTimes(request.getStation(),
      request.getStartTime(),
      request.getEndTime());
  }

  /**
   * Retrieves all {@link ChannelGroup} objects specified by a list of channel group names,
   * with optional effective time and optional faceting definition.
   * If the list is empty, the server will return an empty list of channel groups.
   *
   * @param request {@link ChannelGroupsTimeFacetRequest} facet request object
   * @return list of {@link ChannelGroup} objects
   */
  @PostMapping("/channel-groups/query/names")
  @Operation(summary = "retrieves all channel groups specified by a list of channel group names, "
    + "effectiveTime and facetingDefinition")
  public List<ChannelGroup> findChannelGroupsByName(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of channel group names, effective time," +
      " and faceting definition")
    @RequestBody ChannelGroupsTimeFacetRequest request) {

    List<String> channelGroupNames = request.getChannelGroupNames();
    Instant effectiveTime = request.getEffectiveTime().orElse(Instant.now());
    if (request.getFacetingDefinition().isPresent()) {
      return stationDefinitionAccessor.findChannelGroupsByNameAndTime(channelGroupNames, effectiveTime,
        request.getFacetingDefinition().orElseThrow(() -> new IllegalArgumentException(FACETING_ERROR_MESSAGE)));
    } else {
      return stationDefinitionAccessor.findChannelGroupsByNameAndTime(channelGroupNames, effectiveTime);
    }
  }

  /**
   * Retrieves all {@link ChannelGroup} objects specified by a list of channel group names and
   * time range bounding the groups.
   * If the list is empty, the server will return an empty list of channel groups.
   *
   * @param request The collections of channel group names and time range of the channel groups
   * @return list of {@link ChannelGroup} objects
   */
  @PostMapping("/channel-groups/query/names-timerange")
  @Operation(summary = "retrieves all channel groups specified by a list of channel group names, "
    + "effectiveTime and facetingDefinition")
  public List<ChannelGroup> findChannelGroupsByNameAndTimeRange(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of channel group names and time range")
    @RequestBody ChannelGroupsTimeRangeRequest request) {

    return stationDefinitionAccessor.findChannelGroupsByNameAndTimeRange(request.getChannelGroupNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());
  }

  /**
   * Retrieves all {@link Channel} objects specified by a list of channel names,
   * with optional effective time and optional faceting definition.
   * If the list is empty, the server will return an empty list of channels.
   *
   * @param request The {@link ChannelsTimeFacetRequest} containing the query information
   * @return list of {@link Channel} objects
   */
  @PostMapping("/channels/query/names")
  @Operation(summary = "retrieves all channels specified by a list of channel names, and optionally, an"
    + "effectiveTime and facetingDefinition")
  public List<Channel> findChannels(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of channel names, effective time," +
      " and faceting definition")
    @RequestBody ChannelsTimeFacetRequest request) {

    Instant time = request.getEffectiveTime().orElse(Instant.now());
    return request.getFacetingDefinition().isPresent() ?
      stationDefinitionAccessor.findChannelsByNameAndTime(request.getChannelNames(),
        time,
        request.getFacetingDefinition().orElseThrow(() -> new IllegalArgumentException(FACETING_ERROR_MESSAGE))) :
      stationDefinitionAccessor.findChannelsByNameAndTime(request.getChannelNames(), time);
  }

  /**
   * Retrieves all {@link Channel} objects specified by a list of channel names and
   * time range bounding the groups.
   * If the list is empty, the server will return an empty list of channels.
   *
   * @param request The collections of channel names and time range of the channels
   * @return list of {@link Channel} objects
   */
  @PostMapping("/channels/query/names-timerange")
  @Operation(summary = "retrieves all channels specified by a list of channel names and time range")
  public List<Channel> findChannelsByNameAndTimeRange(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of channel names and time range")
    @RequestBody ChannelsTimeRangeRequest request) {

    return stationDefinitionAccessor.findChannelsByNameAndTimeRange(request.getChannelNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());
  }

  /**
   * Returns a list of {@link Response} objects that corresponds to the list of Response Ids passed in.
   * <p>
   * If no ids are passed into this interface, an empty list of Responses is returned.
   * if no time is provided to the optional effectiveTime, the time of request is the default
   * if no faceting definition is provided, FAP is faceted by ID
   *
   * @param request a request object with optional time and optional faceting definition
   * @return list of all {@link Response} objects for the given set of Response ids.
   */
  @PostMapping("/response/query/ids")
  public List<Response> findResponsesById(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of response ids, effective time," +
      " and faceting definition")
    @RequestBody ResponseTimeFacetRequest request) {

    return request.getFacetingDefinition().isPresent() ?
      stationDefinitionAccessor.findResponsesById(request.getResponseIds(),
        request.getEffectiveTime().orElse(Instant.now()),
        request.getFacetingDefinition().orElseThrow(() -> new IllegalArgumentException(FACETING_ERROR_MESSAGE))) :
      stationDefinitionAccessor.findResponsesById(request.getResponseIds(),
        request.getEffectiveTime().orElse(Instant.now()));
  }

  /**
   * Returns a list of {@link Response} objects that correspond to the list of Response ids passed
   * in that lie in the requested time range
   * <p>
   * If no ids are passed into the interface, an empty list of Responses is returned.
   *
   * @param request a {@link ResponseTimeRangeRequest} object with ids and time range
   * @return list of all {@link Response} objects for the given ids and time range
   */
  @PostMapping("/response/query/ids-timerange")
  @Operation(summary = "retrieves all responses specified by a list of response ids and time range")
  public List<Response> findResponsesByIdAndTimeRange(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "List of response ids and time range")
    @RequestBody ResponseTimeRangeRequest request) {

    return stationDefinitionAccessor.findResponsesByIdAndTimeRange(request.getResponseIds(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());
  }
}

package gms.shared.stationdefinition.accessor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.cache.VersionCache;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * Entity caching for retrieving and updating COI object entity ids, times and versions
 * If COI objects don't exist in cache, then we will delegate to the
 * {@link BridgedStationDefinitionAccessor} for return COI objects
 */
@Primary
@Profile("enable-caching")
@Component("entityCacheAccessor")
public class EntityCachingStationDefinitionAccessor implements StationDefinitionAccessorInterface {

  private static final Logger logger = LoggerFactory.getLogger(EntityCachingStationDefinitionAccessor.class);
  // Operational time period config
  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";

  private final ConfigurationConsumerUtility configurationConsumerUtility;
  private final StationDefinitionAccessorInterface delegate;
  private final VersionCache versionCache;
  private final StationDefinitionIdUtility stationDefinitionIdUtility;

  private StationDefinitionFacetingUtility stationDefinitionFacetingUtility;

  private AtomicReference<Range<Instant>> operationalRange;

  @Autowired
  public EntityCachingStationDefinitionAccessor(
    SystemConfig systemConfig,
    ConfigurationConsumerUtility configurationConsumerUtility,
    @Qualifier("bridgedAccessor") StationDefinitionAccessorInterface delegate,
    VersionCache versionCache,
    StationDefinitionIdUtility stationDefinitionIdUtility) {

    this.configurationConsumerUtility = configurationConsumerUtility;
    this.delegate = delegate;
    this.versionCache = versionCache;
    this.stationDefinitionIdUtility = stationDefinitionIdUtility;

    this.operationalRange = new AtomicReference<>();
  }

  @PostConstruct
  public void init() {
    var operationalTimeConfig = configurationConsumerUtility.resolve(
      OPERATIONAL_TIME_PERIOD_CONFIG,
      Collections.emptyList());
    var operationalStart = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_START).toString());
    var operationalEnd = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_END).toString());
    setOperationalRange(Range.closed(Instant.now().minus(operationalStart), Instant.now().minus(operationalEnd)));
    stationDefinitionFacetingUtility = StationDefinitionFacetingUtility.create(this);
  }

  @Override
  public List<StationGroup> findStationGroupsByNameAndTime(List<String> stationGroupNames, Instant effectiveTime) {
    DelegateTimeFunction<StationGroup> delegateFunction = (name, time) -> {
      List<StationGroup> delegateStationGroups = delegate.findStationGroupsByNameAndTime(name, time);

      if (getOperationalRange().contains(time)) {
        storeStationGroups(delegateStationGroups);
      }

      return delegateStationGroups;
    };

    UnaryOperator<StationGroup> facetStationsFunction = stationGroup -> {
      List<String> stationNames = stationGroup.getStations().stream()
        .map(Station::getName)
        .collect(Collectors.toList());

      List<Station> versionReferenceStations = findStationsByNameAndTime(stationNames, effectiveTime).stream()
        .map(station -> station.toBuilder().setData(Optional.empty()).build())
        .collect(Collectors.toList());

      return stationGroup.toBuilder()
        .setData(stationGroup.getData().orElseThrow().toBuilder()
          .setStations(versionReferenceStations)
          .build())
        .build();
    };

    List<StationGroup> cachedStationGroups = findCachedObjects(StationGroup.class,
      stationGroupNames,
      effectiveTime,
      facetStationsFunction,
      delegateFunction,
      StationGroup::getName);

    return sortCachedObjects(cachedStationGroups, StationGroup::getName, StationGroup::getEffectiveAt);
  }

  @Override
  public List<StationGroup> findStationGroupsByNameAndTime(List<String> stationGroupNames, Instant effectiveTime,
    FacetingDefinition facetingDefinition) {
    return findStationGroupsByNameAndTime(stationGroupNames, effectiveTime).stream()
      .map(stationGroup -> stationDefinitionFacetingUtility.populateFacets(stationGroup,
        facetingDefinition,
        effectiveTime))
      .collect(Collectors.toList());
  }

  @Override
  public List<StationGroup> findStationGroupsByNameAndTimeRange(List<String> stationGroupNames, Instant startTime,
    Instant endTime) {
    DelegateRangeFunction<StationGroup> delegateFunction = (stationGroups, start, end) -> {
      List<StationGroup> delegateStationGroups = delegate.findStationGroupsByNameAndTimeRange(stationGroups, start, end);
      storeStationGroups(delegateStationGroups);
      return delegateStationGroups;
    };
    // find station groups by name and time range
    List<StationGroup> cachedStationGroups = findCachedObjectsByIdAndTimeRange(StationGroup.class,
      stationGroupNames,
      startTime,
      endTime,
      UnaryOperator.identity(),
      delegateFunction);

    return sortCachedObjects(cachedStationGroups, StationGroup::getName, StationGroup::getEffectiveAt);
  }

  @Override
  public void storeStationGroups(List<StationGroup> stationGroups) {

    stationGroups.stream()
      .filter(stationGroup -> stationGroup.getEffectiveAt().isPresent())
      .collect(Collectors.groupingBy(StationGroup::getName))
      .forEach((name, value) -> {

        String key = StationGroup.class.getSimpleName().concat(name);

        RangeMap<Instant, Object> stationGroupVersionTimeRanges = value.stream()
          .filter(StationGroup::isPresent)
          .map(stationGroup -> {
            List<Station> entityReferenceStations = stationGroup.getStations().stream()
              .map(Station::toEntityReference)
              .collect(Collectors.toList());

            return stationGroup.toBuilder()
              .setData(stationGroup.getData().get().toBuilder().setStations(entityReferenceStations).build())
              .build();
          })
          .map(stationGroup -> Map.entry(getRange(stationGroup.getEffectiveAt(), true, stationGroup::getEffectiveUntil),
            (Object) stationGroup))
          .filter(entry -> getOperationalRange().isConnected(entry.getKey()))
          .collect(new TreeRangeMapCollector<>());
        versionCache.cacheVersionsByEntityIdAndTime(key, stationGroupVersionTimeRanges);
      });
    delegate.storeStationGroups(stationGroups);
  }

  @Override
  public List<Station> findStationsByNameAndTime(List<String> stationNames, Instant effectiveTime) {

    DelegateTimeFunction<Station> delegateFunction = (stations, effectiveAt) -> {
      List<Station> delegateStations = delegate.findStationsByNameAndTime(stations, effectiveAt);

      if (getOperationalRange().contains(effectiveTime)) {
        storeStations(delegateStations);
      }

      return delegateStations;
    };

    UnaryOperator<Station> childFacetingFunction = station -> {

      List<String> channelNames = station.getAllRawChannels().stream()
        .map(Channel::getName)
        .collect(Collectors.toList());

      List<Channel> versionReferenceChannels = findChannelsByNameAndTime(channelNames, effectiveTime).stream()
        .map(channel -> channel.toBuilder().setData(Optional.empty()).build())
        .collect(Collectors.toList());

      List<String> channelGroupNames = station.getChannelGroups().stream()
        .map(ChannelGroup::getName)
        .collect(Collectors.toList());

      // The faceting for channel groups by time is the same as the faceting for channel groups within stations by time
      List<ChannelGroup> channelGroupFacets = findChannelGroupsByNameAndTime(channelGroupNames, effectiveTime)
        .stream()
        .filter(channelGroup -> versionReferenceChannels.containsAll(channelGroup.getChannels()))
        .collect(Collectors.toList());

      if (channelGroupFacets.isEmpty() || versionReferenceChannels.isEmpty()) {
        return null;
      }

      return station.toBuilder()
        .setData(station.getData().orElseThrow().toBuilder()
          .setAllRawChannels(versionReferenceChannels)
          .setChannelGroups(channelGroupFacets)
          .build())
        .build();
    };

    List<Station> cachedStations = findCachedObjects(Station.class,
      stationNames,
      effectiveTime,
      childFacetingFunction,
      delegateFunction,
      Station::getName);

    return sortCachedObjects(cachedStations, Station::getName, Station::getEffectiveAt);
  }

  @Override
  public List<Station> findStationsByNameAndTime(List<String> stationNames, Instant effectiveTime,
    FacetingDefinition facetingDefinition) {

    return findStationsByNameAndTime(stationNames, effectiveTime).stream()
      .map(station -> stationDefinitionFacetingUtility.populateFacets(station, facetingDefinition, effectiveTime))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  @Override
  public List<Station> findStationsByNameAndTimeRange(List<String> stationNames, Instant startTime, Instant endTime) {
    DelegateRangeFunction<Station> delegateFunction = (stations, start, end) -> {
      List<Station> delegateStations = delegate.findStationsByNameAndTimeRange(stations, start, end);
      storeStations(delegateStations);
      return delegateStations;
    };

    UnaryOperator<Station> childFacetingFunction = station -> {
      List<String> channelGroupNames = station.getChannelGroups().stream()
        .map(ChannelGroup::getName)
        .collect(Collectors.toList());

      List<String> channelNames = station.getAllRawChannels().stream()
        .map(Channel::getName)
        .collect(Collectors.toList());

      List<Channel> entityReferenceChannels = station.getAllRawChannels().stream()
        .map(Channel::toEntityReference)
        .collect(Collectors.toList());

      List<ChannelGroup> facetedChannelGroups = findChannelGroupsByNameAndTime(channelGroupNames,
        station.getEffectiveAt().orElseThrow())
        .stream()
        .filter(ChannelGroup::isPresent)
        .map(channelGroup -> {
          List<Channel> channelGroupEntityReferenceChannels = channelGroup.getChannels()
            .stream()
            .map(Channel::toEntityReference)
            .filter(channel -> channelNames.contains(channel.getName()))
            .collect(Collectors.toList());

          if (channelGroupEntityReferenceChannels.isEmpty()) {
            return null;
          }

          return channelGroup.toBuilder()
            .setData(channelGroup.getData().get().toBuilder()
              .setChannels(channelGroupEntityReferenceChannels)
              .build())
            .build();
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());


      if (facetedChannelGroups.isEmpty() || entityReferenceChannels.isEmpty()) {
        return null;
      }

      return station.toBuilder()
        .setData(station.getData().orElseThrow().toBuilder()
          .setAllRawChannels(entityReferenceChannels)
          .setChannelGroups(facetedChannelGroups)
          .build())
        .build();
    };

    // find stations by name and time range
    List<Station> cachedStations = findCachedObjectsByIdAndTimeRange(Station.class,
      stationNames,
      startTime,
      endTime,
      childFacetingFunction,
      delegateFunction);

    return sortCachedObjects(cachedStations, Station::getName, Station::getEffectiveAt);
  }

  @Override
  public List<Instant> determineStationChangeTimes(Station station,
    Instant startTime,
    Instant endTime) {

    List<Station> stations = findStationsByNameAndTimeRange(List.of(station.getName()),
      startTime, endTime);

    List<ChannelGroup> channelGroups = findChannelGroupsByNameAndTimeRange(stations.stream()
        .map(Station::getChannelGroups)
        .flatMap(Set::stream)
        .map(ChannelGroup::getName)
        .collect(Collectors.toList()),
      startTime, endTime);
    List<Channel> channels = findChannelsByNameAndTimeRange(stations.stream()
        .map(Station::getAllRawChannels)
        .flatMap(Set::stream)
        .map(Channel::getName)
        .collect(Collectors.toList()),
      startTime, endTime);
    List<Response> responses = findResponsesByIdAndTimeRange(channels.stream()
        .map(Channel::getResponse)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Response::getId)
        .collect(Collectors.toList()),
      startTime, endTime);

    return Stream.concat(Stream.concat(stations.stream().map(Station::getEffectiveAt),
          channelGroups.stream().map(ChannelGroup::getEffectiveAt)),
        Stream.concat(channels.stream().map(Channel::getEffectiveAt),
          responses.stream().map(Response::getEffectiveAt)))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .filter(instant -> !instant.isAfter(endTime))
      .sorted(Comparator.reverseOrder())
      .distinct()
      .collect(Collectors.toList());

  }

  @Override
  public void storeStations(List<Station> stations) {

    stations.stream()
      .filter(station -> station.getEffectiveAt().isPresent())
      .collect(Collectors.groupingBy(Station::getName))
      .forEach((name, value) -> {
        String key = Station.class.getSimpleName().concat(name);

        RangeMap<Instant, Object> stationVersionTimeRanges = value.stream()
          .filter(Station::isPresent)
          .map(station -> {
            List<Channel> entityReferenceChannels = station.getAllRawChannels().stream()
              .map(Channel::toEntityReference)
              .collect(Collectors.toList());

            List<ChannelGroup> entityReferenceChannelGroups = station.getChannelGroups().stream()
              .map(ChannelGroup::toEntityReference)
              .collect(Collectors.toList());

            return station.toBuilder()
              .setData(station.getData().get().toBuilder()
                .setAllRawChannels(entityReferenceChannels)
                .setChannelGroups(entityReferenceChannelGroups)
                .build())
              .build();
          })
          .map(station -> Map.entry(getRange(station.getEffectiveAt(), station.isPresent(), station::getEffectiveUntil), (Object) station))
          .filter(entry -> getOperationalRange().isConnected(entry.getKey()))
          .collect(new TreeRangeMapCollector<>());
        versionCache.cacheVersionsByEntityIdAndTime(key, stationVersionTimeRanges);
      });
    delegate.storeStations(stations);
  }

  @Override
  public List<ChannelGroup> findChannelGroupsByNameAndTime(List<String> channelGroupNames, Instant effectiveTime) {

    DelegateTimeFunction<ChannelGroup> delegateFunction = (name, time) -> {
      List<ChannelGroup> delegateChannelGroups = delegate.findChannelGroupsByNameAndTime(name, time);
      storeChannelGroups(delegateChannelGroups);

      return delegateChannelGroups;
    };

    UnaryOperator<ChannelGroup> childFacetingFunction = channelGroup -> {
      List<String> channelNames = channelGroup.getChannels().stream()
        .map(Channel::getName)
        .collect(Collectors.toList());

      List<Channel> versionReferenceChannels = findChannelsByNameAndTime(channelNames, effectiveTime)
        .stream()
        .map(channel -> channel.toBuilder()
          .setData(Optional.empty())
          .build())
        .collect(Collectors.toList());

      if (versionReferenceChannels.isEmpty()) {
        return null;
      }

      return channelGroup.toBuilder()
        .setData(channelGroup.getData().orElseThrow().toBuilder()
          .setChannels(versionReferenceChannels)
          .build())
        .build();
    };

    List<ChannelGroup> cachedChannelGroups = findCachedObjects(ChannelGroup.class,
      channelGroupNames,
      effectiveTime,
      childFacetingFunction,
      delegateFunction,
      ChannelGroup::getName);

    return sortCachedObjects(cachedChannelGroups, ChannelGroup::getName, ChannelGroup::getEffectiveAt);
  }

  @Override
  public List<ChannelGroup> findChannelGroupsByNameAndTime(List<String> channelGroupNames, Instant effectiveTime,
    FacetingDefinition facetingDefinition) {
    return findChannelGroupsByNameAndTime(channelGroupNames, effectiveTime).stream()
      .map(channelGroup -> stationDefinitionFacetingUtility.populateFacets(channelGroup,
        facetingDefinition,
        effectiveTime))
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  @Override
  public List<ChannelGroup> findChannelGroupsByNameAndTimeRange(List<String> channelGroupNames, Instant startTime,
    Instant endTime) {

    DelegateRangeFunction<ChannelGroup> delegateFunction = (channelGroups, start, end) -> {
      List<ChannelGroup> delegateChannelGroups = delegate.findChannelGroupsByNameAndTimeRange(channelGroups, start, end);
      storeChannelGroups(delegateChannelGroups);
      return delegateChannelGroups;
    };

    // find channel groups by name and time range
    List<ChannelGroup> cachedChannelGroups = findCachedObjectsByIdAndTimeRange(ChannelGroup.class,
      channelGroupNames,
      startTime,
      endTime,
      UnaryOperator.identity(),
      delegateFunction);

    return sortCachedObjects(cachedChannelGroups, ChannelGroup::getName, ChannelGroup::getEffectiveAt);
  }

  @Override
  public void storeChannelGroups(List<ChannelGroup> channelGroups) {
    channelGroups.stream()
      .filter(channelGroup -> channelGroup.getEffectiveAt().isPresent())
      .filter(ChannelGroup::isPresent)
      .collect(Collectors.groupingBy(ChannelGroup::getName))
      .forEach((name, value) -> {
        String key = ChannelGroup.class.getSimpleName().concat(name);

        RangeMap<Instant, Object> channelGroupVersionTimeRanges = value.stream()
          .map(channelGroup -> {
            List<Channel> entityReferenceChannels = channelGroup.getChannels().stream()
              .map(Channel::toEntityReference)
              .collect(Collectors.toList());

            return channelGroup.toBuilder()
              .setData(channelGroup.getData().get().toBuilder()
                .setChannels(entityReferenceChannels)
                .build())
              .build();
          })
          .map(channelGroup -> Map.entry(getRange(channelGroup.getEffectiveAt(), channelGroup.isPresent(), channelGroup::getEffectiveUntil),
            (Object) channelGroup))
          .filter(entry -> getOperationalRange().isConnected(entry.getKey()))
          .collect(new TreeRangeMapCollector<>());
        versionCache.cacheVersionsByEntityIdAndTime(key, channelGroupVersionTimeRanges);
      });
    delegate.storeChannelGroups(channelGroups);
  }


  @Override
  public List<Channel> findChannelsByNameAndTime(List<String> channelNames, Instant effectiveTime) {
    DelegateTimeFunction<Channel> delegateFunction = (name, time) -> {
      List<Channel> delegateChannels = delegate.findChannelsByNameAndTime(name, time);

      if (getOperationalRange().contains(effectiveTime)) {
        storeChannels(delegateChannels);
        storeResponses(delegateChannels.stream()
          .map(Channel::getResponse)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .collect(Collectors.toList()));
      }

      return delegateChannels;
    };

    UnaryOperator<Channel> childFacetingFunction = channel -> {
      if (channel.getResponse().isEmpty()) {
        return channel;
      }

      List<Response> facetedResponses = findResponsesById(List.of(channel.getResponse().get().getId()), effectiveTime);
      if (facetedResponses.isEmpty()) {
        return channel;
      }

      return channel.toBuilder()
        .setData(channel.getData().orElseThrow().toBuilder()
          .setResponse(facetedResponses.get(0))
          .build())
        .build();
    };

    List<Channel> cachedChannels = findCachedObjects(Channel.class,
      channelNames,
      effectiveTime,
      childFacetingFunction,
      delegateFunction,
      Channel::getName);

    return sortCachedObjects(cachedChannels, Channel::getName, Channel::getEffectiveAt);
  }

  @Override
  public List<Channel> findChannelsByNameAndTime(List<String> channelNames, Instant effectiveTime,
    FacetingDefinition facetingDefinition) {
    return findChannelsByNameAndTime(channelNames, effectiveTime).stream()
      .map(channel -> stationDefinitionFacetingUtility.populateFacets(channel,
        facetingDefinition,
        effectiveTime))
      .collect(Collectors.toList());
  }

  @Override
  public List<Channel> findChannelsByNameAndTimeRange(List<String> channelNames, Instant startTime, Instant endTime) {
    DelegateRangeFunction<Channel> delegateFunction = (channels, start, end) -> {
      List<Channel> delegateChannels = delegate.findChannelsByNameAndTimeRange(channels, start, end);
      storeChannels(delegateChannels);
      return delegateChannels;
    };

    List<Channel> channels = findCachedObjectsByIdAndTimeRange(Channel.class,
      channelNames,
      startTime,
      endTime,
      UnaryOperator.identity(),
      delegateFunction);

    storeResponses(channels.stream()
      .map(Channel::getResponse)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList()));

    return sortCachedObjects(channels, Channel::getName, Channel::getEffectiveAt);
  }

  @Override
  public Channel loadChannelFromWfdisc(List<Long> wfids, Optional<TagName> associatedRecordType,
    Optional<Long> associatedRecordId, Optional<Long> filterId,
    Instant channelEffectiveTime, Instant channelEndTime) {

    Preconditions.checkState(!wfids.isEmpty(),
      "Attempting to load channel from wfdisc, wfids must be greater than 0");
    Preconditions.checkState(associatedRecordType.isPresent(), "Attempting to load channel from wfdisc, " +
      "associated record type must be present");
    Preconditions.checkState(associatedRecordId.isPresent(), "Attempting to load channel from wfdisc, " +
      "associated record id must be present");

    var channel = stationDefinitionIdUtility.getDerivedChannelForWfidRecordId(associatedRecordType.get(),
      associatedRecordId.get(), wfids.get(0));

    if (channel != null) {

      Instant effectiveAt = channel.getEffectiveAt()
        .orElseThrow(() -> new IllegalStateException("Channel stored in cache is entity reference, version reference is expected."));

      channel = (Channel) versionCache.retrieveVersionsByEntityIdAndTime(Channel.class.getSimpleName().concat(channel.getName()), effectiveAt);

      Optional<Instant> possibleEffectiveAt = channel.getEffectiveAt();
      Optional<Response> possibleResponse = channel.getResponse();

      if (possibleResponse.isPresent() && possibleEffectiveAt.isPresent()) {
        List<Response> responses = findResponsesById(List.of(possibleResponse.get().getId()), possibleEffectiveAt.get());
        if (!responses.isEmpty()) {
          channel.toBuilder()
            .setData(channel.getData().orElseThrow().toBuilder()
              .setResponse(responses.get(0))
              .build())
            .build();
        }
      }
      return channel;
    }

    channel = delegate.loadChannelFromWfdisc(wfids,
      associatedRecordType,
      associatedRecordId,
      Optional.empty(),
      channelEffectiveTime,
      channelEndTime);

    if (getOperationalRange().isConnected(getRange(channel.getEffectiveAt(), channel.isPresent(), channel::getEffectiveUntil))) {

      Optional<Response> possibleResponse = channel.getResponse();
      if (possibleResponse.isPresent()) {
        storeResponses(List.of(possibleResponse.get()));
      }
      storeChannels(List.of(channel));
    }

    return channel;
  }

  @Override
  public void storeChannels(List<Channel> channels) {

    channels.stream()
      .filter(channel -> channel.getEffectiveAt().isPresent())
      .filter(Channel::isPresent)
      .collect(Collectors.groupingBy(Channel::getName))
      .forEach((name, value) -> {
        String key = Channel.class.getSimpleName().concat(name);

        RangeMap<Instant, Object> channelVersionTimeRanges = value.stream()
          .map(channel -> {
            Response entityReferenceResponse = channel.getResponse()
              .map(response -> Response.createEntityReference(response.getId()))
              .orElse(null);
            return channel.toBuilder()
              .setData(channel.getData().get().toBuilder()
                .setResponse(entityReferenceResponse)
                .build())
              .build();
          })
          .map(channel -> Map.entry(getRange(channel.getEffectiveAt(), channel.isPresent(), channel::getEffectiveUntil), (Object) channel))
          .filter(entry -> getOperationalRange().isConnected(entry.getKey()))
          .collect(new TreeRangeMapCollector<>());
        versionCache.cacheVersionsByEntityIdAndTime(key, channelVersionTimeRanges);
      });

    delegate.storeChannels(channels);
  }

  @Override
  public List<Response> findResponsesById(Collection<UUID> responseIds, Instant effectiveTime) {
    List<String> stringIds = responseIds.stream()
      .map(UUID::toString)
      .collect(Collectors.toList());
    DelegateTimeFunction<Response> delegateFunction = (idStrings, time) -> {
      List<UUID> ids = idStrings.stream()
        .map(UUID::fromString)
        .collect(Collectors.toList());
      List<Response> delegateResponses = delegate.findResponsesById(ids, time);
      if (getOperationalRange().contains(effectiveTime)) {
        storeResponses(delegateResponses);
      }

      return delegateResponses;
    };

    return findCachedObjects(Response.class,
      stringIds,
      effectiveTime,
      UnaryOperator.identity(),
      delegateFunction,
      response -> response.getId().toString());
  }

  @Override
  public List<Response> findResponsesById(Collection<UUID> responseIds, Instant effectiveTime,
    FacetingDefinition facetingDefinition) {
    List<String> stringIds = responseIds.stream()
      .map(UUID::toString)
      .collect(Collectors.toList());

    DelegateTimeFunction<Response> delegateFunction =
      (idStrings, effectiveAt) -> {
        List<UUID> ids = idStrings.stream()
          .map(UUID::fromString)
          .collect(Collectors.toList());
        List<Response> delegateResponses = delegate.findResponsesById(ids, effectiveAt, facetingDefinition);

        if (getOperationalRange().contains(effectiveTime)) {
          storeResponses(delegateResponses);
        }

        return delegateResponses;
      };

    List<Response> responses = findCachedObjects(Response.class,
      stringIds,
      effectiveTime,
      UnaryOperator.identity(),
      delegateFunction,
      response -> response.getId().toString());

    // apply faceting to the cached responses
    return responses.stream()
      .map(response -> stationDefinitionFacetingUtility.populateFacets(response,
        facetingDefinition,
        effectiveTime))
      .filter(Objects::nonNull)
      .distinct()
      .collect(Collectors.toList());

  }

  @Override
  public List<Response> findResponsesByIdAndTimeRange(Collection<UUID> responseIds, Instant startTime,
    Instant endTime) {
    DelegateRangeFunction<Response> delegateFunction = (responses, start, end) -> {
      List<UUID> responseUUIDs = responses.stream()
        .map(UUID::fromString)
        .collect(Collectors.toList());
      List<Response> delegateResponses = delegate.findResponsesByIdAndTimeRange(responseUUIDs, start, end);
      storeResponses(delegateResponses);
      return delegateResponses;
    };

    // find responses by name and time range
    List<String> responseIdStrings = responseIds.stream()
      .map(UUID::toString)
      .collect(Collectors.toList());
    return findCachedObjectsByIdAndTimeRange(Response.class,
      responseIdStrings,
      startTime,
      endTime,
      UnaryOperator.identity(),
      delegateFunction);
  }

  @Override
  public Response loadResponseFromWfdisc(long wfdiscRecord) {

    var mappedResponse = stationDefinitionIdUtility.getResponseForWfid(wfdiscRecord);

    if (mappedResponse.isPresent()) {

      var response = mappedResponse.orElseThrow();
      var responseId = response.getId();
      var effectiveAt = response.getEffectiveAt();

      if (effectiveAt.isPresent()) {

        var versionedResponse =
          (Response) versionCache
            .retrieveVersionsByEntityIdAndTime(Response.class.getSimpleName().concat(responseId.toString()),
              effectiveAt.orElseThrow());

        if (versionedResponse != null) {
          return versionedResponse;
        }
      } else {

        var versionedResponseMap =
          versionCache
            .retrieveVersionsByEntityIdAndTimeRangeMap(Response.class.getSimpleName().concat(responseId.toString()));

        if (!versionedResponseMap.asMapOfRanges().isEmpty()) {

          Instant lastTime = versionedResponseMap.span().upperEndpoint();

          var versionedResponse = (Response) versionedResponseMap.get(lastTime);

          if (versionedResponse != null) {
            return versionedResponse;
          }
        }
      }
    }

    var bridgedResponse = delegate.loadResponseFromWfdisc(wfdiscRecord);

    storeResponses(List.of(bridgedResponse));

    return bridgedResponse;
  }

  @Override
  public void storeResponses(List<Response> responses) {

    responses.stream()
      .filter(response -> response.getEffectiveAt().isPresent())
      .collect(Collectors.groupingBy(Response::getId))
      .forEach((id, value) -> {


        String key = Response.class.getSimpleName().concat(id.toString());

        RangeMap<Instant, Object> responseVersionTimeRanges = value.stream()
          .map(response -> Map.entry(getRange(response.getEffectiveAt(), response.isPresent(), response::getEffectiveUntil),
            (Object) response))
          .filter(entry -> getOperationalRange().isConnected(entry.getKey()))
          .collect(new TreeRangeMapCollector<>());
        versionCache.cacheVersionsByEntityIdAndTime(key, responseVersionTimeRanges);
      });

    delegate.storeResponses(responses);
  }

  @Override
  public void cache(List<String> stationGroupNames, Instant startTime, Instant endTime) {
    Objects.requireNonNull(stationGroupNames);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Preconditions.checkState(startTime.isBefore(endTime));

    logger.info("EntityCachingStationDefinitionAccessor::Populating cache");

    setOperationalRange(Range.closed(startTime, endTime));
    versionCache.clear();
    logger.info("Caching station groups");
    List<StationGroup> stationGroups = findStationGroupsByNameAndTimeRange(stationGroupNames, startTime, endTime);

    List<String> stationNames = stationGroups.stream()
      .map(StationGroup::getStations)
      .flatMap(Set::stream)
      .map(Station::getName)
      .collect(Collectors.toList());

    logger.info("Caching stations");
    List<Station> stations = findStationsByNameAndTimeRange(stationNames, startTime, endTime);

    List<String> channelGroupNames = stations.stream()
      .map(Station::getChannelGroups)
      .flatMap(Set::stream)
      .map(ChannelGroup::getName)
      .collect(Collectors.toList());

    logger.info("Caching channel groups");
    List<ChannelGroup> channelGroups = findChannelGroupsByNameAndTimeRange(channelGroupNames, startTime, endTime);

    List<String> channelNames = channelGroups.stream()
      .map(ChannelGroup::getChannels)
      .flatMap(Set::stream)
      .map(Channel::getName)
      .collect(Collectors.toList());

    logger.info("Caching channels");
    List<Channel> channels = findChannelsByNameAndTimeRange(channelNames, startTime, endTime);

    List<UUID> responseIds = channels.stream()
      .map(Channel::getResponse)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .map(Response::getId)
      .collect(Collectors.toList());

    logger.info("Caching responses");
    findResponsesByIdAndTimeRange(responseIds, startTime, endTime);
  }

  /**
   * Retrieve objects from the cache of the desired type, faceting the children appropriately, and delegating any
   * missing values to the delegate {@link StationDefinitionAccessorInterface}
   *
   * @param entityType The entity type to retrieve from the cache
   * @param ids The ids of the entities to retrieve
   * @param effectiveTime the time at which the entities must be effective
   * @param childFacetingFunction the function determining how to facet the children of the retrieved values
   * @param delegateFunction the function for retrieving data from the delegate
   * @param idExtractor function for getting the ids from the retrieved objects
   * @param <T> The COI type of object to retrieve
   * @return The objects from the cache and delegate
   */
  private <T> List<T> findCachedObjects(Class<T> entityType,
    List<String> ids,
    Instant effectiveTime,
    UnaryOperator<T> childFacetingFunction,
    DelegateTimeFunction<T> delegateFunction,
    Function<T, String> idExtractor) {

    List<T> cachedEntities = new ArrayList<>();

    List<String> operatingIds = new ArrayList<>(ids);

    if (getOperationalRange().contains(effectiveTime)) {
      cachedEntities = operatingIds.stream()
        .map(id -> entityType.getSimpleName().concat(id))
        .filter(versionCache::versionsByEntityIdAndTimeHasKey)
        .map(id -> versionCache.retrieveVersionsByEntityIdAndTime(id, effectiveTime))
        .filter(object -> {
          if (object != null) {
            return true;
          }
          logger.info("Attempting to retrieve entities of type {} from the cache, but " +
            "retrieved a null value, ignoring", entityType.toString());
          return false;
        })
        .map(entityType::cast)
        .map(childFacetingFunction)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));

      operatingIds.removeAll(cachedEntities.stream().map(idExtractor).collect(Collectors.toList()));
    }

    if (!operatingIds.isEmpty()) {
      List<T> nonCachedEntities = delegateFunction.apply(operatingIds, effectiveTime).stream()
        .map(childFacetingFunction)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
      cachedEntities.addAll(nonCachedEntities);

    }

    return cachedEntities;
  }

  /**
   * Retrieve cached objects of the provided type by key and time
   * range
   *
   * @param entityType The entity type to retrieve
   * @param coiNames the names of objects to retrieve
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @param childFacetingFunction the function for apply the appropriate faceting to the child objects
   * @param delegateFunction the function for retrieving names not found in the cache via the delegate
   * @param <T> The type of object to return
   * @return The objects from the cache and/or delegate
   */
  private <T> List<T> findCachedObjectsByIdAndTimeRange(Class<T> entityType,
    List<String> coiNames,
    Instant startTime,
    Instant endTime,
    UnaryOperator<T> childFacetingFunction,
    DelegateRangeFunction<T> delegateFunction) {

    // create the time range from start and end
    Range<Instant> timeRange = Range.closed(startTime, endTime);

    // create delegate range set to query for ranges that aren't covered in the cache
    List<T> cachedObjects = new ArrayList<>();
    coiNames.forEach(name -> {

      // create delegate range set to query for ranges that aren't covered in the cache
      RangeSet<Instant> delegateRangeSet = TreeRangeSet.create();
      delegateRangeSet.add(timeRange);

      String key = entityType.getSimpleName().concat(name);
      RangeMap<Instant, Object> cacheRangeMap = versionCache.retrieveVersionsByEntityIdAndTimeRangeMap(key);

      if (cacheRangeMap != null && !cacheRangeMap.asMapOfRanges().isEmpty()) {

        cachedObjects.addAll(cacheRangeMap.asDescendingMapOfRanges().entrySet().stream()
          .map(entry -> {
            Range<Instant> timeRangeEntry = entry.getKey();
            delegateRangeSet.remove(timeRangeEntry);
            return entry;
          })
          .filter(entry -> timeRange.isConnected(entry.getKey()))
          .map(Map.Entry::getValue)
          .distinct()
          .map(entityType::cast)
          .map(childFacetingFunction)
          .collect(Collectors.toList()));
      }

      // query the delegate for the remaining time ranges if they exist
      if (!delegateRangeSet.isEmpty()) {
        List<T> nonCachedEntities = delegateRangeSet.asDescendingSetOfRanges().stream()
          .map(range -> {

              Instant queryStart = range.lowerEndpoint();
              Instant queryEnd = range.upperEndpoint();
              return delegateFunction.apply(List.of(name), queryStart, queryEnd);
            }
          )
          .flatMap(Collection::stream)
          .filter(Objects::nonNull)
          .collect(Collectors.toCollection(ArrayList::new));
        cachedObjects.addAll(nonCachedEntities);
      }
    });

    return cachedObjects.stream()
      .filter(Objects::nonNull)
      .sorted()
      .distinct()
      .collect(Collectors.toList());
  }

  @FunctionalInterface
  private interface DelegateRangeFunction<T> {
    List<T> apply(List<String> names, Instant startTime, Instant endTime);
  }

  @FunctionalInterface
  private interface DelegateTimeFunction<T> {
    List<T> apply(List<String> names, Instant effectiveTime);
  }

  private Range<Instant> getRange(Optional<Instant> effectiveAt, boolean dataPresent,
    Supplier<Optional<Instant>> effectiveUntilSupplier) {

    Instant start = effectiveAt.orElseThrow();
    if (dataPresent && effectiveUntilSupplier.get().isPresent()) {

      Optional<Instant> endTimeOptional = effectiveUntilSupplier.get();
      if (endTimeOptional.isPresent()) {
        return Range.closed(start, endTimeOptional.get());
      }
    }
    return Range.atLeast(start);
  }

  /**
   * Sort the cached station definition objects based on the names and effective times
   *
   * @param cachedObjects list of cached station def objects
   * @return list of T objects
   */
  private static <T> List<T> sortCachedObjects(List<T> cachedObjects, Function<T, String> getNameFunction,
    Function<T, Optional<Instant>> getEffAtFunction) {


    Comparator<T> compareByNameAndTime = comparing(getNameFunction)
      .thenComparing(getEffAtFunction,
        comparing(opt -> opt.orElse(null), nullsFirst(naturalOrder())));

    return cachedObjects.stream()
      .sorted(compareByNameAndTime)
      .collect(Collectors.toList());
  }

  private Range<Instant> getOperationalRange() {
    return operationalRange.get();
  }

  private void setOperationalRange(Range<Instant> operationalRange) {
    this.operationalRange.set(operationalRange);
  }
}

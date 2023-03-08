package gms.dataacquisition.css.stationrefconverter;

import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelFactory;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup.Type;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.Response;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.StatusType;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toList;

public class StationGroupBuilder {

  private static final Logger logger = LoggerFactory.getLogger(StationGroupBuilder.class);
  private final Set<ReferenceNetworkMembership> referenceNetworkMemberships;
  private final Set<ReferenceStationMembership> referenceStationMemberships;
  private final Set<ReferenceSiteMembership> referenceSiteMemberships;
  private final Collection<ReferenceNetwork> referenceNetworks;
  private final Collection<ReferenceStation> referenceStations;
  private final Collection<ReferenceSite> referenceSites;
  private final Collection<ReferenceChannel> referenceChannels;
  private final Collection<ReferenceResponse> referenceResponses;

  public StationGroupBuilder(
    Set<ReferenceNetworkMembership> referenceNetworkMemberships,
    Set<ReferenceStationMembership> referenceStationMemberships,
    Set<ReferenceSiteMembership> referenceSiteMemberships,
    Collection<ReferenceNetwork> referenceNetworks,
    Collection<ReferenceStation> referenceStations,
    Collection<ReferenceSite> referenceSites,
    Collection<ReferenceChannel> referenceChannels,
    Collection<ReferenceResponse> referenceResponses) {
    this.referenceNetworkMemberships = Objects.requireNonNull(referenceNetworkMemberships);
    this.referenceStationMemberships = Objects.requireNonNull(referenceStationMemberships);
    this.referenceSiteMemberships = Objects.requireNonNull(referenceSiteMemberships);
    this.referenceNetworks = Objects.requireNonNull(referenceNetworks);
    this.referenceStations = Objects.requireNonNull(referenceStations);
    this.referenceSites = Objects.requireNonNull(referenceSites);
    this.referenceChannels = Objects.requireNonNull(referenceChannels);
    this.referenceResponses = Objects.requireNonNull(referenceResponses);
  }

  Pair<Set<StationGroup>, Set<Response>> createStationGroupsAndResponses() {
    final Set<Response> responses = new HashSet<>();
    final Map<String, ReferenceResponse> refResponseByChan = getLatestRefResponseByChannel(
      this.referenceResponses);
    // Now build the StationGroup on down using the existing
    // reference entries (i.e. network, station, site and channels)
    Set<StationGroup> stationGroups = new HashSet<>();

    /*Build up the reference objects (network, station, site and channels) to then convert to
      each entry into the corresponding "processing" object
      (station group, station, channel group and channel)
     */

    // For each reference network lookup station references
    for (ReferenceNetwork network : this.referenceNetworks) {
      // Find all the reference stations associated to the network
      Collection<ReferenceNetworkMembership> networkMemberships =
        this.getActiveLatestNetworkMemberships(network);
      // But limit it down to the active, most recent stations only
      Collection<ReferenceStation> refStations = getActiveLatestReferenceStations(
        networkMemberships);

      // Walk the reference stations for each network getting back the site list for each station
      List<Station> stations = new ArrayList<>();
      for (ReferenceStation referenceStation : refStations) {
        Map<String, RelativePosition> relativePositionsByChannel = new HashMap<>();
        List<Channel> stationChannels = new ArrayList<>();
        Collection<ReferenceStationMembership> stationMemberships =
          this.getActiveLatestStationMemberships(referenceStation);
        Collection<ReferenceSite> refSites = this.getActiveLatestReferenceSites(stationMemberships);

        // Walk each site to get back a list of reference channels
        List<ChannelGroup> stationChannelGroups = new ArrayList<>();
        for (ReferenceSite referenceSite : refSites) {
          Collection<ReferenceSiteMembership> siteMemberships =
            this.getActiveLatestSiteMemberships(referenceSite);
          Collection<ReferenceChannel> refChannels = getActiveLatestReferenceChannels(
            siteMemberships);

          // If no reference channels are found warn and skip adding channel group
          if (refChannels.isEmpty()) {
            logger.warn("No reference channels found for reference site: {}",
              referenceSite.getName());
            continue;
          }

          // Convert the Reference Channels for each site to "processing channels"
          Pair<List<Channel>, List<Response>> channelsAndResponses
            = this.createChannelsAndResponses(refChannels, refResponseByChan,
            referenceStation.getName(), referenceSite.getName());

          final List<Channel> channelGroupChannels = channelsAndResponses.getLeft();
          responses.addAll(channelsAndResponses.getRight());

          // Walk the channels to add the Reference Site relative position for each channel
          // to the station's relativePositionsByChannel map
          for (Channel channel : channelGroupChannels) {
            relativePositionsByChannel.put(channel.getName(), referenceSite.getPosition());
          }

          // Add each channel groups channels to station channel list
          stationChannels.addAll(channelGroupChannels);

          makeChannelGroup(referenceSite, channelGroupChannels)
            .ifPresent(stationChannelGroups::add);
        }

        makeStation(referenceStation, stationChannelGroups, stationChannels,
          relativePositionsByChannel)
          .ifPresent(stations::add);
      }
      makeStationGroup(network.getName(), network.getDescription(), stations)
        .ifPresent(stationGroups::add);
    }
    return Pair.of(stationGroups, responses);
  }

  private static Optional<ChannelGroup> makeChannelGroup(ReferenceSite refSite,
    final List<Channel> channels) {

    if (channels.isEmpty()) {
      logger.warn("Failed to create station group for site {} (no channels found)",
        refSite.getName());
      return Optional.empty();
    }
    return Optional.of(ChannelGroup.from(
      refSite.getName(),
      refSite.getDescription(),
      Location.from(
        refSite.getLatitude(),
        refSite.getLongitude(),
        0, // Depth
        refSite.getElevation()),
      Type.SITE_GROUP,
      channels));
  }

  private static Optional<Station> makeStation(ReferenceStation refSta,
    List<ChannelGroup> stationChannelGroups,
    List<Channel> stationChannels,
    Map<String, RelativePosition> relativePositionsByChannel) {

    if (stationChannels.isEmpty()) {
      logger.warn("Failed to add Station {} (no channels found)", refSta.getName());
      return Optional.empty();
    }
    return Optional.of(Station.from(
      refSta.getName(),
      refSta.getStationType(),
      refSta.getDescription(),
      relativePositionsByChannel,
      Location.from(
        refSta.getLatitude(),
        refSta.getLongitude(),
        0, // Depth
        refSta.getElevation()),
      stationChannelGroups,
      stationChannels));
  }

  private static Optional<StationGroup> makeStationGroup(String name, String description,
    List<Station> stations) {

    if (stations.isEmpty()) {
      logger.warn("Failed to add StationGroup name={}, description={}... no stations found",
        name, description);
      return Optional.empty();
    }
    return Optional.of(StationGroup.from(name, description, stations));
  }

  private Pair<List<Channel>, List<Response>> createChannelsAndResponses(
    Collection<ReferenceChannel> referenceChannels,
    Map<String, ReferenceResponse> referenceResponsesByChan,
    String stationName, String siteName) {

    final List<Channel> processingChans = new ArrayList<>();
    final List<Response> processingResponses = new ArrayList<>();
    for (ReferenceChannel referenceChannel : referenceChannels) {
      String channelName = referenceChannel.getName();
      var channel = ChannelFactory.rawFromReferenceChannel(referenceChannel, stationName, siteName);
      processingChans.add(channel);
      if (referenceResponsesByChan.containsKey(channelName)) {
        var refResponse = referenceResponsesByChan.get(channelName);
        processingResponses.add(Response.from(channel.getName(),
          refResponse.getReferenceCalibration().getCalibration(),
          refResponse.getFapResponse().orElse(null)));
      } else {
        logger.warn("Could not find response for raw channel {}", channelName);
      }
    }

    return Pair.of(processingChans, processingResponses);
  }

  private Collection<ReferenceNetworkMembership> getActiveLatestNetworkMemberships(
    ReferenceNetwork referenceNetwork) {
    // Find the latest active list of network memberships
    return this.referenceNetworkMemberships.stream()
      .filter(referenceNetworkMembership -> referenceNetworkMembership.getNetworkId()
        .equals(referenceNetwork.getEntityId()))
      .collect(groupingBy(ReferenceNetworkMembership::getStationId,
        maxBy(activeLatest(ReferenceNetworkMembership::getActualChangeTime,
          isActive(ReferenceNetworkMembership::getStatus)))))
      .values().stream()
      .flatMap(Optional::stream)
      .filter(isActive(ReferenceNetworkMembership::getStatus))
      .collect(toList());
  }

  private List<ReferenceStationMembership> getActiveLatestStationMemberships(
    ReferenceStation referenceStation) {
    // Find the latest active list of station memberships
    return this.referenceStationMemberships.stream()
      .filter(referenceStationMembership -> referenceStationMembership.getStationId()
        .equals(referenceStation.getEntityId()))
      .collect(groupingBy(ReferenceStationMembership::getSiteId,
        maxBy(activeLatest(ReferenceStationMembership::getActualChangeTime,
          isActive(ReferenceStationMembership::getStatus)))))
      .values().stream()
      .flatMap(Optional::stream)
      .filter(isActive(ReferenceStationMembership::getStatus))
      .collect(toList());
  }

  private List<ReferenceSiteMembership> getActiveLatestSiteMemberships(
    ReferenceSite referenceSite) {
    return this.referenceSiteMemberships.stream()
      .filter(referenceSiteMembership -> referenceSiteMembership.getSiteId()
        .equals(referenceSite.getEntityId()))
      .collect(groupingBy(ReferenceSiteMembership::getChannelName,
        maxBy(activeLatest(ReferenceSiteMembership::getActualChangeTime,
          isActive(ReferenceSiteMembership::getStatus)))))
      .values().stream()
      .flatMap(Optional::stream)
      .filter(isActive(ReferenceSiteMembership::getStatus))
      .collect(toList());
  }

  private List<ReferenceStation> getActiveLatestReferenceStations(
    Collection<ReferenceNetworkMembership> memberships) {
    return memberships.stream()
      .map(this::getActiveLatestReferenceStation)
      .flatMap(Optional::stream)
      .collect(toList());
  }

  private Optional<ReferenceStation> getActiveLatestReferenceStation(
    ReferenceNetworkMembership membership) {
    return referenceStations.stream()
      .filter(refStation -> refStation.getEntityId().equals(membership.getStationId()))
      .max(activeLatest(ReferenceStation::getActualChangeTime, ReferenceStation::isActive))
      .filter(ReferenceStation::isActive);
  }

  private List<ReferenceSite> getActiveLatestReferenceSites(
    Collection<ReferenceStationMembership> memberships) {
    return memberships.stream()
      .map(this::getActiveLatestReferenceSite)
      .flatMap(Optional::stream)
      .collect(Collectors.toList());
  }

  private Optional<ReferenceSite> getActiveLatestReferenceSite(
    ReferenceStationMembership membership) {
    return referenceSites.stream()
      .filter(site -> site.getEntityId().equals(membership.getSiteId()))
      .max(activeLatest(ReferenceSite::getActualChangeTime, ReferenceSite::isActive))
      .filter(ReferenceSite::isActive);
  }

  private List<ReferenceChannel> getActiveLatestReferenceChannels(
    Collection<ReferenceSiteMembership> memberships) {
    return memberships.stream()
      .map(this::getActiveLatestReferenceChannel)
      .flatMap(Optional::stream)
      .collect(toList());
  }

  private Optional<ReferenceChannel> getActiveLatestReferenceChannel(
    ReferenceSiteMembership membership) {
    return referenceChannels.stream()
      .filter(channel -> channel.getName().equals(membership.getChannelName()))
      .max(activeLatest(ReferenceChannel::getActualTime, ReferenceChannel::isActive))
      .filter(ReferenceChannel::isActive);
  }

  private Map<String, ReferenceResponse> getLatestRefResponseByChannel(
    Collection<ReferenceResponse> responses) {
    return responses.stream()
      .collect(groupingBy(ReferenceResponse::getChannelName,
        collectingAndThen(maxBy(comparing(ReferenceResponse::getActualTime)),
          Optional::orElseThrow)));
  }

  private static <T> Predicate<T> isActive(Function<T, StatusType> statusExtractor) {
    return t -> statusExtractor.apply(t) == StatusType.ACTIVE;
  }

  private static <T, U extends Comparable<U>> Comparator<T> activeLatest(
    Function<T, U> keyExtractor,
    Predicate<T> isActive) {
    return Comparator.comparing(keyExtractor).thenComparingInt(key -> isActive.test(key) ? 1 : -1);
  }

}

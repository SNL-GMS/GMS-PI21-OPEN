package gms.shared.frameworks.osd.repository;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.channel.ChannelRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.CapabilitySohRollupRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.PerformanceMonitoringRepositoryInterface;
import gms.shared.frameworks.osd.api.performancemonitoring.SohStatusChangeRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.api.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AcquiredChannelEnvironmentIssueRepositoryQueryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.RawStationDataFrameRepositoryQueryInterface;
import gms.shared.frameworks.osd.api.station.StationGroupRepositoryInterface;
import gms.shared.frameworks.osd.api.station.StationRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceChannelRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceNetworkRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceResponseRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceSensorRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceSiteRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.ReferenceStationRepositoryInterface;
import gms.shared.frameworks.osd.api.stationreference.util.NetworkMembershipRequest;
import gms.shared.frameworks.osd.api.stationreference.util.ReferenceSiteMembershipRequest;
import gms.shared.frameworks.osd.api.stationreference.util.ReferenceStationMembershipRequest;
import gms.shared.frameworks.osd.api.systemmessage.SystemMessageRepositoryInterface;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.ChannelTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.api.util.ChannelsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.HistoricalStationSohRequest;
import gms.shared.frameworks.osd.api.util.ReferenceChannelRequest;
import gms.shared.frameworks.osd.api.util.StationTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.StationTimeRangeSohTypeRequest;
import gms.shared.frameworks.osd.api.util.StationsTimeRangeRequest;
import gms.shared.frameworks.osd.api.util.TimeRangeRequest;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ReferenceChannel;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueId;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroupDefinition;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.QuietedSohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetwork;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceNetworkMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceResponse;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSensor;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSite;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceSiteMembership;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStation;
import gms.shared.frameworks.osd.coi.stationreference.ReferenceStationMembership;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.dto.soh.HistoricalAcquiredChannelEnvironmentalIssues;
import gms.shared.frameworks.osd.dto.soh.HistoricalStationSoh;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@AutoValue
public abstract class OsdRepository implements OsdRepositoryInterface {
  public abstract CapabilitySohRollupRepositoryInterface getCapabilitySohRollupRepository();

  public abstract ChannelRepositoryInterface getChannelRepository();

  public abstract PerformanceMonitoringRepositoryInterface getPerformanceMonitoringRepository();

  public abstract RawStationDataFrameRepositoryInterface getRawStationDataFrameRepository();

  public abstract RawStationDataFrameRepositoryQueryInterface getRawStationDataFrameQueryRepository();

  public abstract ReferenceChannelRepositoryInterface getReferenceChannelRepository();

  public abstract ReferenceNetworkRepositoryInterface getReferenceNetworkRepository();

  public abstract ReferenceResponseRepositoryInterface getReferenceResponseRepository();

  public abstract ReferenceSensorRepositoryInterface getReferenceSensorRepository();

  public abstract ReferenceSiteRepositoryInterface getReferenceSiteRepository();

  public abstract ReferenceStationRepositoryInterface getReferenceStationRepository();

  public abstract SohStatusChangeRepositoryInterface getSohStatusChangeRepository();

  public abstract StationGroupRepositoryInterface getStationGroupRepository();

  public abstract StationRepositoryInterface getStationRepository();

  public abstract AcquiredChannelEnvironmentIssueRepositoryInterface getStationSohRepository();

  public abstract AcquiredChannelEnvironmentIssueRepositoryQueryInterface getStationSohQueryViewRepository();

  public abstract SystemMessageRepositoryInterface getSystemMessageRepository();

  public static OsdRepository from(
    CapabilitySohRollupRepositoryInterface capabilitySohRollupRepository,
    ChannelRepositoryInterface channelRepository,
    PerformanceMonitoringRepositoryInterface performanceMonitoringRepository,
    RawStationDataFrameRepositoryInterface rawStationDataFrameRepository,
    RawStationDataFrameRepositoryQueryInterface rawStationDataFrameQueryRepository,
    ReferenceChannelRepositoryInterface referenceChannelRepository,
    ReferenceNetworkRepositoryInterface referenceNetworkRepository,
    ReferenceResponseRepositoryInterface referenceResponseRepository,
    ReferenceSensorRepositoryInterface referenceSensorRepository,
    ReferenceSiteRepositoryInterface referenceSiteRepository,
    ReferenceStationRepositoryInterface referenceStationRepository,
    SohStatusChangeRepositoryInterface sohStatusChangeRepository,
    StationGroupRepositoryInterface stationGroupRepository,
    StationRepositoryInterface stationRepository,
    AcquiredChannelEnvironmentIssueRepositoryInterface stationSohRepository,
    AcquiredChannelEnvironmentIssueRepositoryQueryInterface stationSohQueryViewRepository,
    SystemMessageRepositoryInterface systemMessageRepository) {
    return new AutoValue_OsdRepository.Builder()
      .setCapabilitySohRollupRepository(capabilitySohRollupRepository)
      .setChannelRepository(channelRepository)
      .setPerformanceMonitoringRepository(performanceMonitoringRepository)
      .setRawStationDataFrameRepository(rawStationDataFrameRepository)
      .setRawStationDataFrameQueryRepository(rawStationDataFrameQueryRepository)
      .setReferenceChannelRepository(referenceChannelRepository)
      .setReferenceNetworkRepository(referenceNetworkRepository)
      .setReferenceResponseRepository(referenceResponseRepository)
      .setReferenceSensorRepository(referenceSensorRepository)
      .setReferenceSiteRepository(referenceSiteRepository)
      .setReferenceStationRepository(referenceStationRepository)
      .setSohStatusChangeRepository(sohStatusChangeRepository)
      .setStationGroupRepository(stationGroupRepository)
      .setStationSohRepository(stationSohRepository)
      .setStationSohQueryViewRepository(stationSohQueryViewRepository)
      .setStationRepository(stationRepository)
      .setSystemMessageRepository(systemMessageRepository)
      .setRawStationDataFrameRepository(rawStationDataFrameRepository)
      .build();
  }

  public abstract Builder builder();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setCapabilitySohRollupRepository(CapabilitySohRollupRepositoryInterface i);

    public abstract Builder setChannelRepository(ChannelRepositoryInterface i);

    public abstract Builder setPerformanceMonitoringRepository(
      PerformanceMonitoringRepositoryInterface i);

    public abstract Builder setRawStationDataFrameRepository(
      RawStationDataFrameRepositoryInterface i);

    public abstract Builder setRawStationDataFrameQueryRepository(
      RawStationDataFrameRepositoryQueryInterface i);

    public abstract Builder setSohStatusChangeRepository(SohStatusChangeRepositoryInterface i);

    public abstract Builder setStationGroupRepository(StationGroupRepositoryInterface i);

    public abstract Builder setStationRepository(StationRepositoryInterface i);

    public abstract Builder setStationSohRepository(AcquiredChannelEnvironmentIssueRepositoryInterface i);

    public abstract Builder setStationSohQueryViewRepository(AcquiredChannelEnvironmentIssueRepositoryQueryInterface i);

    public abstract Builder setSystemMessageRepository(SystemMessageRepositoryInterface i);

    public abstract Builder setReferenceChannelRepository(
      ReferenceChannelRepositoryInterface i);

    public abstract Builder setReferenceNetworkRepository(
      ReferenceNetworkRepositoryInterface i);

    public abstract Builder setReferenceResponseRepository(ReferenceResponseRepositoryInterface i);

    public abstract Builder setReferenceSensorRepository(ReferenceSensorRepositoryInterface i);

    public abstract Builder setReferenceSiteRepository(ReferenceSiteRepositoryInterface i);

    public abstract Builder setReferenceStationRepository(
      ReferenceStationRepositoryInterface i);

    public abstract OsdRepository build();
  }

  @Override
  public List<CapabilitySohRollup> retrieveCapabilitySohRollupByStationGroup(
    Collection<String> stationGroups) {
    return getCapabilitySohRollupRepository()
      .retrieveCapabilitySohRollupByStationGroup(stationGroups);
  }

  @Override
  public void storeCapabilitySohRollup(Collection<CapabilitySohRollup> capabilitySohRollups) {
    getCapabilitySohRollupRepository()
      .storeCapabilitySohRollup((capabilitySohRollups));
  }

  @Override
  public List<CapabilitySohRollup> retrieveLatestCapabilitySohRollupByStationGroup(
    Collection<String> stationGroupNames) {
    return getCapabilitySohRollupRepository()
      .retrieveLatestCapabilitySohRollupByStationGroup(stationGroupNames);
  }

  @Override
  public List<Channel> retrieveChannels(Collection<String> channelIds) {
    return getChannelRepository().retrieveChannels(channelIds);
  }

  @Override
  public Set<String> storeChannels(Collection<Channel> channels) {
    return getChannelRepository().storeChannels(channels);
  }

  @Override
  public List<StationSoh> retrieveByStationId(List<String> stationNames) {
    return getPerformanceMonitoringRepository().retrieveByStationId(stationNames);
  }

  @Override
  public List<StationSoh> retrieveByStationsAndTimeRange(StationsTimeRangeRequest stationsTimeRangeRequest) {
    return getPerformanceMonitoringRepository().retrieveByStationsAndTimeRange(stationsTimeRangeRequest);
  }

  @Override
  public List<UUID> storeStationSoh(Collection<StationSoh> stationSohs) {
    return getPerformanceMonitoringRepository().storeStationSoh(stationSohs);
  }

  @Override
  public HistoricalStationSoh retrieveHistoricalStationSoh(HistoricalStationSohRequest request) {
    return getPerformanceMonitoringRepository().retrieveHistoricalStationSoh(request);
  }

  @Override
  public List<StationGroup> retrieveStationGroups(Collection<String> stationGroupNames) {
    return getStationGroupRepository().retrieveStationGroups(stationGroupNames);
  }

  @Override
  public void storeStationGroups(Collection<StationGroup> stationGroups) {
    getStationGroupRepository().storeStationGroups(stationGroups);
  }

  @Override
  public void updateStationGroups(Collection<StationGroupDefinition> stationGroupDefinitions) {
    getStationGroupRepository().updateStationGroups(stationGroupDefinitions);
  }

  @Override
  public List<Station> retrieveAllStations(Collection<String> stationNames) {
    return getStationRepository().retrieveAllStations(stationNames);
  }

  @Override
  public void storeStations(Collection<Station> stations) {
    getStationRepository().storeStations(stations);
  }

  @Override
  public List<ReferenceChannel> retrieveReferenceChannels(
    ReferenceChannelRequest referenceChannelRequest) {
    return getReferenceChannelRepository().retrieveReferenceChannels(referenceChannelRequest);
  }

  @Override
  public void storeReferenceChannels(Collection<ReferenceChannel> channels) {
    getReferenceChannelRepository().storeReferenceChannels(channels);
  }

  @Override
  public List<ReferenceNetwork> retrieveNetworks(Collection<UUID> networkIds) {
    return getReferenceNetworkRepository().retrieveNetworks(networkIds);
  }

  @Override
  public List<ReferenceNetwork> retrieveNetworksByName(List<String> names) {
    return getReferenceNetworkRepository().retrieveNetworksByName(names);
  }

  @Override
  public void storeReferenceNetwork(Collection<ReferenceNetwork> network) {
    getReferenceNetworkRepository().storeReferenceNetwork(network);
  }

  @Override
  public Map<UUID, List<ReferenceNetworkMembership>> retrieveNetworkMembershipsByNetworkId(
    Collection<UUID> networkIds) {
    return getReferenceNetworkRepository().retrieveNetworkMembershipsByNetworkId(networkIds);
  }

  @Override
  public Map<UUID, List<ReferenceNetworkMembership>> retrieveNetworkMembershipsByStationId(
    Collection<UUID> referenceStationIds) {
    return getReferenceNetworkRepository().retrieveNetworkMembershipsByStationId(referenceStationIds);
  }

  @Override
  public List<ReferenceNetworkMembership> retrieveNetworkMembershipsByNetworkAndStationId(
    NetworkMembershipRequest request) {
    return getReferenceNetworkRepository().retrieveNetworkMembershipsByNetworkAndStationId(request);
  }

  @Override
  public void storeNetworkMemberships(Collection<ReferenceNetworkMembership> memberships) {
    getReferenceNetworkRepository().storeNetworkMemberships(memberships);
  }

  @Override
  public List<ReferenceResponse> retrieveReferenceResponses(Collection<String> channelNames) {
    return getReferenceResponseRepository().retrieveReferenceResponses(channelNames);
  }

  @Override
  public void storeReferenceResponses(Collection<ReferenceResponse> referenceResponses) {
    getReferenceResponseRepository().storeReferenceResponses(referenceResponses);
  }

  @Override
  public List<ReferenceSensor> retrieveReferenceSensorsById(Collection<UUID> sensorIds) {
    return getReferenceSensorRepository().retrieveReferenceSensorsById(sensorIds);
  }

  @Override
  public Map<String, List<ReferenceSensor>> retrieveSensorsByChannelName(
    Collection<String> channelNames) {
    return getReferenceSensorRepository().retrieveSensorsByChannelName(channelNames);
  }

  @Override
  public void storeReferenceSensors(Collection<ReferenceSensor> sensors) {
    getReferenceSensorRepository().storeReferenceSensors(sensors);
  }

  @Override
  public List<ReferenceSite> retrieveSites(List<UUID> entityIds) {
    return getReferenceSiteRepository().retrieveSites(entityIds);
  }

  @Override
  public List<ReferenceSite> retrieveSitesByName(List<String> names) {
    return getReferenceSiteRepository().retrieveSitesByName(names);
  }

  @Override
  public void storeReferenceSites(Collection<ReferenceSite> sites) {
    getReferenceSiteRepository().storeReferenceSites(sites);
  }

  @Override
  public Map<UUID, List<ReferenceSiteMembership>> retrieveSiteMembershipsBySiteId(
    List<UUID> siteIds) {
    return getReferenceSiteRepository().retrieveSiteMembershipsBySiteId(siteIds);
  }

  @Override
  public Map<String, List<ReferenceSiteMembership>> retrieveSiteMembershipsByChannelNames(
    List<String> channelNames) {
    return getReferenceSiteRepository().retrieveSiteMembershipsByChannelNames(channelNames);
  }

  @Override
  public List<ReferenceSiteMembership> retrieveSiteMembershipsBySiteIdAndChannelName(
    ReferenceSiteMembershipRequest request) {
    return getReferenceSiteRepository().retrieveSiteMembershipsBySiteIdAndChannelName(request);
  }

  @Override
  public void storeSiteMemberships(Collection<ReferenceSiteMembership> memberships) {
    getReferenceSiteRepository().storeSiteMemberships(memberships);
  }

  @Override
  public List<ReferenceStation> retrieveStations(List<UUID> entityIds) {
    return getReferenceStationRepository().retrieveStations(entityIds);
  }

  @Override
  public List<ReferenceStation> retrieveStationsByVersionIds(Collection<UUID> stationVersionIds) {
    return getReferenceStationRepository().retrieveStationsByVersionIds(stationVersionIds);
  }

  @Override
  public List<ReferenceStation> retrieveStationsByName(List<String> names) {
    return getReferenceStationRepository().retrieveStationsByName(names);
  }

  @Override
  public void storeReferenceStation(Collection<ReferenceStation> stations) {
    getReferenceStationRepository().storeReferenceStation(stations);
  }

  @Override
  public Map<UUID, List<ReferenceStationMembership>> retrieveStationMemberships(List<UUID> ids) {
    return getReferenceStationRepository().retrieveStationMemberships(ids);
  }

  @Override
  public Map<UUID, List<ReferenceStationMembership>> retrieveStationMembershipsByStationId(
    List<UUID> stationIds) {
    return getReferenceStationRepository().retrieveStationMembershipsByStationId(stationIds);
  }

  @Override
  public Map<UUID, List<ReferenceStationMembership>> retrieveStationMembershipsBySiteId(
    List<UUID> siteIds) {
    return getReferenceStationRepository().retrieveStationMembershipsBySiteId(siteIds);
  }

  @Override
  public List<ReferenceStationMembership> retrieveStationMembershipsByStationAndSiteId(
    ReferenceStationMembershipRequest request) {
    return getReferenceStationRepository().retrieveStationMembershipsByStationAndSiteId(request);
  }

  @Override
  public void storeStationMemberships(Collection<ReferenceStationMembership> memberships) {
    getReferenceStationRepository().storeStationMemberships(memberships);
  }

  @Override
  public List<RawStationDataFrame> retrieveRawStationDataFramesByStationAndTime(
    StationTimeRangeRequest request) {
    return getRawStationDataFrameRepository().retrieveRawStationDataFramesByStationAndTime(request);
  }

  @Override
  public List<RawStationDataFrame> retrieveRawStationDataFramesByTime(TimeRangeRequest request) {
    return getRawStationDataFrameRepository().retrieveRawStationDataFramesByTime(request);
  }

  @Override
  public void storeRawStationDataFrames(Collection<RawStationDataFrame> frames) {
    getRawStationDataFrameRepository().storeRawStationDataFrames(frames);
  }

  /**
   * Retrieves all {@link RawStationDataFrameMetadata}, for a station, that have data in the specified time range.
   *
   * @param request The {@link StationTimeRangeRequest}
   * @return {@link RawStationDataFrameMetadata} ordered by time
   */
  @Override
  public List<RawStationDataFrameMetadata> retrieveRawStationDataFrameMetadataByStationAndTime(
    StationTimeRangeRequest request) {
    return getRawStationDataFrameQueryRepository().retrieveRawStationDataFrameMetadataByStationAndTime(request);
  }

  /**
   * Retrieves the latest sample times for the provided channels.
   *
   * @param channelNames The {@link List} of channel names to get latest sample times
   * @return A {@link Map} of the channel time to it's latest sample time
   */
  @Override
  public Map<String, Instant> retrieveLatestSampleTimeByChannel(List<String> channelNames) {
    return getRawStationDataFrameQueryRepository().retrieveLatestSampleTimeByChannel(channelNames);
  }

  @Override
  public void syncAceiUpdates(AceiUpdates aceiUpdates) {
    getStationSohRepository().syncAceiUpdates(aceiUpdates);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByChannelAndTimeRange(
    ChannelTimeRangeRequest request) {
    return getStationSohRepository().findAnalogAceiByChannelAndTimeRange(request);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelAndTimeRange(
    ChannelTimeRangeRequest request) {
    return getStationSohRepository().findBooleanAceiByChannelAndTimeRange(request);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelsAndTimeRange(
    ChannelsTimeRangeRequest request) {
    return getStationSohRepository().findBooleanAceiByChannelsAndTimeRange(request);
  }

  @Override
  public Optional<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiById(
    AcquiredChannelEnvironmentIssueId request) {
    return getStationSohRepository()
      .findAnalogAceiById(request);
  }

  @Override
  public Optional<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiById(
    AcquiredChannelEnvironmentIssueId request) {
    return getStationSohRepository()
      .findBooleanAceiById(request);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByChannelTimeRangeAndType(
    ChannelTimeRangeSohTypeRequest request) {
    return getStationSohRepository().findAnalogAceiByChannelTimeRangeAndType(request);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByChannelTimeRangeAndType(
    ChannelTimeRangeSohTypeRequest request) {
    return getStationSohRepository().findBooleanAceiByChannelTimeRangeAndType(request);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueAnalog> findAnalogAceiByTime(
    TimeRangeRequest request) {
    return getStationSohRepository().findAnalogAceiByTime(request);
  }

  @Override
  public List<AcquiredChannelEnvironmentIssueBoolean> findBooleanAceiByTime(
    TimeRangeRequest request) {
    return getStationSohRepository().findBooleanAceiByTime(request);
  }

  @Override
  public List<UnacknowledgedSohStatusChange> retrieveUnacknowledgedSohStatusChanges(Collection<String> stationNames) {
    return getSohStatusChangeRepository().retrieveUnacknowledgedSohStatusChanges(stationNames);
  }

  @Override
  public void storeUnacknowledgedSohStatusChange(Collection<UnacknowledgedSohStatusChange> unackStatusChanges) {
    getSohStatusChangeRepository().storeUnacknowledgedSohStatusChange(unackStatusChanges);
  }

  @Override
  public void storeQuietedSohStatusChangeList(Collection<QuietedSohStatusChange> quietedSohStatusChangeList) {
    getSohStatusChangeRepository().storeQuietedSohStatusChangeList(quietedSohStatusChangeList);
  }

  @Override
  public List<HistoricalAcquiredChannelEnvironmentalIssues> retrieveAcquiredChannelEnvironmentIssuesByStationTimeRangeAndType(
    StationTimeRangeSohTypeRequest request) {
    return getStationSohQueryViewRepository()
      .retrieveAcquiredChannelEnvironmentIssuesByStationTimeRangeAndType(request);
  }

  @Override
  public Collection<QuietedSohStatusChange> retrieveQuietedSohStatusChangesByTime(Instant currentTime) {
    return getSohStatusChangeRepository().retrieveQuietedSohStatusChangesByTime(currentTime);
  }

  @Override
  public void storeSystemMessages(Collection<SystemMessage> systemMessages) {
    getSystemMessageRepository().storeSystemMessages(systemMessages);
  }

  @Override
  public Set<AcquiredChannelEnvironmentIssueBoolean> findMergeable(
    Collection<AcquiredChannelEnvironmentIssueBoolean> aceis, Duration tolerance) {
    return getStationSohRepository().findMergeable(aceis, tolerance);
  }
}

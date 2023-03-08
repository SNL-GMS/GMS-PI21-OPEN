package gms.shared.stationdefinition.repository;

import com.google.common.base.Functions;
import gms.shared.stationdefinition.api.station.StationRepositoryInterface;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.converter.util.assemblers.StationAssembler;
import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.util.SiteAndSiteChanUtility;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link StationRepositoryInterface} implementation that uses a bridged
 * database to provide {@link Station} instances
 */
@Component("bridgedStationRepository")
public class BridgedStationRepository implements StationRepositoryInterface {

  private final BridgedChannelRepository channelRepository;
  private final BridgedChannelGroupRepository channelGroupRepository;
  private final SiteDatabaseConnector siteDatabaseConnector;
  private final SiteChanDatabaseConnector siteChanDatabaseConnector;
  private final StationAssembler stationAssembler;

  public BridgedStationRepository(BridgedChannelRepository channelRepository,
    BridgedChannelGroupRepository channelGroupRepository,
    SiteDatabaseConnector siteDatabaseConnector,
    SiteChanDatabaseConnector siteChanDatabaseConnector,
    StationAssembler stationAssembler) {
    this.channelRepository = channelRepository;
    this.channelGroupRepository = channelGroupRepository;
    this.siteDatabaseConnector = siteDatabaseConnector;
    this.siteChanDatabaseConnector = siteChanDatabaseConnector;
    this.stationAssembler = stationAssembler;
  }

  @Override
  public List<Station> findStationsByNameAndTime(List<String> stationNames, Instant effectiveAt) {

    // find site daos using surrounding dates
    List<SiteAndSurroundingDates> sitesAndSurroundingDates = siteDatabaseConnector.
      findSitesAndSurroundingDatesByRefStaAndTime(stationNames, effectiveAt);

    List<SiteDao> siteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    Pair<Instant, Instant> minMaxPair = BridgedRepositoryUtils.getMinMaxFromSiteDaos(
      siteDaos, effectiveAt, effectiveAt);

    List<String> stationCodes = siteDaos.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .collect(Collectors.toList());

    // find site chan daos using surrounding dates
    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(stationCodes,
        minMaxPair.getLeft(), minMaxPair.getRight());

    List<SiteChanDao> siteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    if (siteDaos.isEmpty() || siteChanDaos.isEmpty()) {
      return List.of();
    }

    var stationDefinitionDataHolder = new StationDefinitionDataHolder(siteDaos,
      siteChanDaos, null, null, null);

    List<Channel> channels = channelRepository.findChannelsGivenSiteAndSiteChan(
      stationDefinitionDataHolder, minMaxPair.getLeft(), minMaxPair.getRight()).stream()
      .collect(Collectors.toList());

    List<ChannelGroup> channelGroups = channelGroupRepository
      .findChannelGroupsGivenChannelsSitesAndSiteChans(siteDaos, siteChanDaos,
        effectiveAt, channels);

    if (channels.isEmpty() || channelGroups.isEmpty()) {
      return List.of();
    }

    Map<String, SiteChanKey> channelNameSiteChanKeyMap = channels.stream()
      .collect(Collectors.toMap(Channel::getName,
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName,
          Channel::getName), (k1, k2) -> k1));

    return stationAssembler.buildAllForTime(siteDaos, siteChanDaos,
      channelGroups, channels, effectiveAt, channelNameSiteChanKeyMap);
  }

  @Override
  public List<Station> findStationsByNameAndTimeRange(List<String> stationNames,
    Instant startTime, Instant endTime) {

    var sitesAndSurroundingDates = siteDatabaseConnector
      .findSitesAndSurroundingDatesByRefStaAndTimeRange(stationNames, startTime, endTime);

    var siteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    Pair<Instant, Instant> minMaxPair = BridgedRepositoryUtils.getMinMaxFromSiteDaos(
      siteDaos, startTime, endTime);

    List<String> stationCodes = siteDaos.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .collect(Collectors.toList());

    var siteChanAndSurroundingDates = siteChanDatabaseConnector
      .findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(stationCodes, minMaxPair.getLeft(), minMaxPair.getRight());

    var siteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    if (siteDaos.isEmpty() || siteChanDaos.isEmpty()) {
      return List.of();
    }

    var stationDefinitionDataHolder = new StationDefinitionDataHolder(siteDaos,
      siteChanDaos, null, null, null);

    List<Channel> channels = channelRepository.findChannelsGivenSiteAndSiteChan(
      stationDefinitionDataHolder, minMaxPair.getLeft(), minMaxPair.getRight());

    List<ChannelGroup> channelGroups = channelGroupRepository
      .findChannelGroupsGivenChannelsSitesAndSiteChansAndTimeRange(siteDaos,
        siteChanDaos, channels, startTime, endTime);

    if (channels.isEmpty() || channelGroups.isEmpty()) {
      return List.of();
    }

    Map<String, SiteChanKey> channelNameSiteChanKeyMap = channels.stream()
      .collect(Collectors.toMap(Channel::getName,
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName), (k1, k2) -> k1));

    return stationAssembler.buildAllForTimeRange(siteDaos, siteChanDaos,
      channelGroups, channels,
      startTime, endTime, channelNameSiteChanKeyMap);
  }
}

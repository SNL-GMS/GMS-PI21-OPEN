package gms.shared.stationdefinition.repository;

import com.google.common.base.Functions;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.api.channel.ChannelGroupRepositoryInterface;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.converter.util.assemblers.AssemblerUtils;
import gms.shared.stationdefinition.converter.util.assemblers.ChannelGroupAssembler;
import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.stationdefinition.dao.util.SiteAndSiteChanUtility;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A {@link ChannelGroupRepositoryInterface} implementation that uses a bridged
 * database to provide {@link ChannelGroup} instances
 */
@Component("bridgedChannelGroupRepository")
public class BridgedChannelGroupRepository implements ChannelGroupRepositoryInterface {

  private final SiteDatabaseConnector siteDatabaseConnector;
  private final SiteChanDatabaseConnector siteChanDatabaseConnector;
  private final ChannelGroupAssembler channelGroupAssembler;
  private final BridgedChannelRepository channelRepository;

  public BridgedChannelGroupRepository(SiteDatabaseConnector siteDatabaseConnector,
    SiteChanDatabaseConnector siteChanDatabaseConnector,
    ChannelGroupAssembler channelGroupAssembler,
    BridgedChannelRepository channelRepository) {
    this.siteDatabaseConnector = siteDatabaseConnector;
    this.siteChanDatabaseConnector = siteChanDatabaseConnector;
    this.channelGroupAssembler = channelGroupAssembler;
    this.channelRepository = channelRepository;
  }

  @Override
  public List<ChannelGroup> findChannelGroupsByNameAndTime(List<String> channelGroupNames, Instant effectiveAt) {

    // find site daos using surrounding dates
    List<SiteAndSurroundingDates> sitesAndSurroundingDates = siteDatabaseConnector
      .findSitesAndSurroundingDatesByStaCodeAndTime(channelGroupNames, effectiveAt);

    List<SiteDao> siteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    Pair<Instant, Instant> minMaxPair = BridgedRepositoryUtils.getMinMaxFromSiteDaos(
      siteDaos, effectiveAt, effectiveAt);

    siteDaos = siteDaos.stream()
      .filter(siteDao -> Range.closed(siteDao.getId().getOnDate(), siteDao.getOffDate()).contains(effectiveAt))
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    List<String> stationCodes = siteDaos.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .collect(Collectors.toList());

    // find site chan daos using surrounding dates
    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(stationCodes,
        minMaxPair.getLeft(), minMaxPair.getRight());

    var siteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<String> channelNames = siteChanDaos.stream()
      .map(siteChan -> siteChan.getId().getStationCode() + "." + siteChan.getId().getStationCode() + "." + siteChan.getId().getChannelCode())
      .distinct()
      .collect(Collectors.toList());

    if (channelNames.isEmpty()) {
      return new ArrayList<>();
    }

    var stationDefinitiondataHolder = new StationDefinitionDataHolder(siteDaos, siteChanDaos,
      null, null, null);

    List<Channel> channels = channelRepository.findChannelsGivenSiteAndSiteChan(
      stationDefinitiondataHolder, minMaxPair.getLeft(), minMaxPair.getRight());

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan
      = AssemblerUtils.buildVersionTable(
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    return channelGroupAssembler.buildAllForTime(siteDaos, siteChanDaos, effectiveAt, channelsByStaChan);
  }

  public List<ChannelGroup> findChannelGroupsGivenChannelsSitesAndSiteChans(List<SiteDao> siteDaos,
    List<SiteChanDao> siteChanDaos, Instant effectiveAt, List<Channel> channels) {

    var siteDaosForChannelGroup = siteDaos.stream()
      .filter(siteDao -> Range.closed(siteDao.getId().getOnDate(), siteDao.getOffDate()).contains(effectiveAt))
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan
      = AssemblerUtils.buildVersionTable(
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    return channelGroupAssembler.buildAllForTime(siteDaosForChannelGroup, siteChanDaos, effectiveAt, channelsByStaChan);
  }

  @Override
  public List<ChannelGroup> findChannelGroupsByNameAndTimeRange(List<String> channelGroupNames,
    Instant startTime,
    Instant endTime) {

    // find site daos using surrounding dates
    List<SiteAndSurroundingDates> sitesAndSurroundingDates = siteDatabaseConnector
      .findSitesAndSurroundingDatesByStaCodeAndTimeRange(channelGroupNames, startTime, endTime);

    List<SiteDao> siteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    Pair<Instant, Instant> minMaxPair = BridgedRepositoryUtils.getMinMaxFromSiteDaos(
      siteDaos, startTime, endTime);

    List<String> stationCodes = siteDaos.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .distinct()
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates = siteChanDatabaseConnector
      .findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(stationCodes, minMaxPair.getLeft(), minMaxPair.getRight());
    List<SiteChanDao> siteChans = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<String> channelNames = siteChans.stream()
      .map(siteChan -> siteChan.getId().getStationCode() + "." + siteChan.getId().getStationCode() + "." + siteChan.getId().getChannelCode())
      .collect(Collectors.toList());
    if (channelNames.isEmpty()) {
      return new ArrayList<>();
    }

    var stationDefinitiondataHolder = new StationDefinitionDataHolder(siteDaos, siteChans,
      null, null, null);

    List<Channel> channels = channelRepository.findChannelsGivenSiteAndSiteChan(stationDefinitiondataHolder,
      minMaxPair.getLeft(), minMaxPair.getRight());
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan
      = AssemblerUtils.buildVersionTable(
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);
    return channelGroupAssembler.buildAllForTimeRange(siteDaos, siteChans, startTime, endTime, channelsByStaChan);
  }

  public List<ChannelGroup> findChannelGroupsGivenChannelsSitesAndSiteChansAndTimeRange(List<SiteDao> siteDaos,
    List<SiteChanDao> siteChanDaos, List<Channel> channels, Instant startTime, Instant endTime) {

    var siteDaosForChannelGroup = siteDaos.stream()
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan
      = AssemblerUtils.buildVersionTable(
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
        Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
        Functions.compose(Optional::get, Channel::getEffectiveAt),
        channels);

    return channelGroupAssembler.buildAllForTimeRange(siteDaosForChannelGroup, siteChanDaos, startTime, endTime, channelsByStaChan);
  }
}

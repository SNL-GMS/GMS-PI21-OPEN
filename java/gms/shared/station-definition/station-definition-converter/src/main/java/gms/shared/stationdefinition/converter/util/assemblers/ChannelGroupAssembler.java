package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.base.Functions;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.utils.comparator.ChannelGroupComparator;
import gms.shared.stationdefinition.converter.interfaces.ChannelGroupConverter;
import gms.shared.stationdefinition.converter.util.TemporalMap;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class ChannelGroupAssembler {
  private static final Logger logger = LoggerFactory.getLogger(ChannelGroupAssembler.class);
  private final ChannelGroupConverter channelGroupConverter;

  public ChannelGroupAssembler(ChannelGroupConverter channelGroupConverter) {
    this.channelGroupConverter = channelGroupConverter;
  }

  //logic of what changes in site cause a change in station
  BiPredicate<SiteDao, SiteDao> changeOccuredForSite = (SiteDao prev, SiteDao curr) ->{

    if (prev == null) {
      return curr != null;
    }

    return !prev.equals(curr);
  };
  
  //logic of what changes in site chan cause a change in station
  BiPredicate<SiteChanDao, SiteChanDao> changeOccuredForSiteChan = (SiteChanDao prev, SiteChanDao curr) ->{

    if(Objects.isNull(prev) || Objects.isNull(curr)){
      return true;
    }

    return !prev.getId().getStationCode().equals(curr.getId().getStationCode()) ||
      !prev.getId().getChannelCode().equals(curr.getId().getChannelCode()) ||
      prev.getEmplacementDepth() != curr.getEmplacementDepth();

  };

  public List<ChannelGroup> buildAllForTime(List<SiteDao> sites, List<SiteChanDao> siteChans, Instant effectiveAt,
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan) {
    Objects.requireNonNull(sites);
    Objects.requireNonNull(siteChans);
    Objects.requireNonNull(effectiveAt);
    Objects.requireNonNull(channelsByStaChan);

    //we use buildAllForTimeRange since we need to determine the prev/next versions to determine start/end Time for the
    //version we're interested in
    List<ChannelGroup> resultList = createChannelGroupTablesAndMaps(sites, siteChans, Pair.of(effectiveAt, effectiveAt),
      channelsByStaChan, Channel::createVersionReference);

    Map<String, List<ChannelGroup>> channelGroupMap = resultList.stream().collect(groupingBy(ChannelGroup::getName));

    //we need to remove the prev/next versions since they were only used to set the start/end time
    //we also return the version that is effectiveAt the request time or the one previous to it if none exist
    return channelGroupMap.values().stream()
      .map(list -> list.stream()
        .filter(channelGroup -> !channelGroup.getEffectiveAt().get().isAfter(effectiveAt))
        .sorted(new ChannelGroupComparator())
        .sorted(Comparator.reverseOrder())
        .findFirst())
      .filter(Optional::isPresent)
      .map(Optional::get)
      .sorted(new ChannelGroupComparator())
      .collect(Collectors.toList());
  }

  public List<ChannelGroup> buildAllForTimeRange(List<SiteDao> sites, List<SiteChanDao> siteChans,
    Instant startTime, Instant endTime,
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan) {
    Objects.requireNonNull(sites);
    Objects.requireNonNull(siteChans);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Objects.requireNonNull(channelsByStaChan);


    List<ChannelGroup> resultList = createChannelGroupTablesAndMaps(sites, siteChans, Pair.of(startTime, endTime),
      channelsByStaChan, Channel::createEntityReference);

    return resultList.stream()
      .filter(channelGroup -> !channelGroup.getEffectiveUntil().isPresent() ||
        !channelGroup.getEffectiveUntil().get().isBefore(startTime))
      .filter(channelGroup -> !channelGroup.getEffectiveAt().get().isAfter(
        AssemblerUtils.effectiveAtNoonOffset.apply(endTime)))
      .sorted(new ChannelGroupComparator())
      .collect(toList());
  }

  private List<ChannelGroup> createChannelGroupTablesAndMaps(
    List<SiteDao> sites,
    List<SiteChanDao> siteChans,
    Pair<Instant, Instant> startEndTime,
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan,
    UnaryOperator<Channel> channelConverter) {

    Set<String> stationCodes = sites.stream()
      .map(siteDao -> siteDao.getId().getStationCode())
      .collect(Collectors.toSet());

    TemporalMap<String, SiteDao> sitesByStationCode = sites.stream()
      .collect(TemporalMap.collector(Functions.compose(SiteKey::getStationCode, SiteDao::getId),
        Functions.compose(SiteKey::getOnDate, SiteDao::getId)));

    Table<String, String, NavigableMap<Instant, SiteChanDao>> siteChansByStationAndChannel =
      AssemblerUtils.buildVersionTable(Functions.compose(SiteChanKey::getStationCode, SiteChanDao::getId),
        Functions.compose(SiteChanKey::getChannelCode, SiteChanDao::getId),
        Functions.compose(SiteChanKey::getOnDate, SiteChanDao::getId),
        siteChans);

    return stationCodes.stream()
      .flatMap(stationCode ->
        processChannelGroups(startEndTime,
          sitesByStationCode.getVersionMap(stationCode),
          siteChansByStationAndChannel.row(stationCode),
          channelsByStaChan.row(stationCode),
          channelConverter).stream())
      .filter(Objects::nonNull)
      .sorted()
      .collect(toList());
  }

  private List<ChannelGroup> processChannelGroups(Pair<Instant, Instant> startEndTime,
    NavigableMap<Instant, SiteDao> siteNavMap,
    Map<String, NavigableMap<Instant, SiteChanDao>> siteChanNavMap,
    Map<String, NavigableMap<Instant, Channel>> chanCodeChannelNavMap,
    UnaryOperator<Channel> channelFunc) {

    //determine if range or single point in time
    boolean isRange = startEndTime.getLeft().isBefore(startEndTime.getRight());

    //because site and site chan have only an accuracy of nearest day, create range truncated to day for selecting site/sitechan
    Range<Instant> dayRange = Range.open(AssemblerUtils.effectiveAtStartOfDayOffset.apply(startEndTime.getLeft()),
      AssemblerUtils.effectiveUntilEndOfDay.apply(startEndTime.getRight()));

    //check for attribute changes
    NavigableSet<Instant> possibleVersionTimes = getChangeTimes(siteNavMap, siteChanNavMap, chanCodeChannelNavMap);
    SortedSet<Instant> validTimes = AssemblerUtils.getValidTimes(startEndTime, possibleVersionTimes, isRange);

    return processPossibleVersionTimes(new ArrayList<>(validTimes), siteNavMap, siteChanNavMap, chanCodeChannelNavMap, channelFunc);
  }

  private List<ChannelGroup> processPossibleVersionTimes(
    List<Instant> possibleVersionTimes,
    NavigableMap<Instant, SiteDao> sitesForVersion,
    Map<String, NavigableMap<Instant, SiteChanDao>> siteChansForVersion,
    Map<String, NavigableMap<Instant, Channel>> chanCodeChannelNavMap,
    UnaryOperator<Channel> channelFunc) {

    List<ChannelGroup> versionedChannelGroups = new ArrayList<>();
    for (var i = 0; i < possibleVersionTimes.size() - 1; i++) {
      Range<Instant> versionRange = Range.open(possibleVersionTimes.get(i), possibleVersionTimes.get(i + 1));

      Optional<SiteDao> possibleSite = AssemblerUtils.getObjectsForVersionTime(versionRange.lowerEndpoint(), sitesForVersion, SiteDao::getOffDate);
      List<SiteChanDao> versionSiteChanDaos = AssemblerUtils.getObjectsForVersionTime(versionRange.lowerEndpoint(), siteChansForVersion, SiteChanDao::getOffDate);
      List<Channel> activeChannels = AssemblerUtils.getObjectsForVersionTime(versionRange.lowerEndpoint(), chanCodeChannelNavMap,
        chan -> chan.getEffectiveUntil().orElse(Instant.MAX));

      if (activeChannels.isEmpty() || possibleSite.isEmpty()) {
        logger.info("No Active Channels or Site for time range: " + versionRange);
        continue;
      }

      //the endtime of the channel group will be up until the new time (this prevents overlapping)
      var endtime = AssemblerUtils.getImmediatelyBeforeInstant(versionRange.upperEndpoint());

      var curChannelGroup = convertChannelGroup(Range.open(versionRange.lowerEndpoint(), endtime),
        possibleSite, versionSiteChanDaos, activeChannels, channelFunc);

      if (curChannelGroup.isPresent()) {
        versionedChannelGroups.add(curChannelGroup.orElseThrow());
      }
    }
    return versionedChannelGroups;
  }

  private Optional<ChannelGroup> convertChannelGroup(Range<Instant> versionRange,
    Optional<SiteDao> site, List<SiteChanDao> versionSiteChanDaos,
    List<Channel> activeChannels, UnaryOperator<Channel> channelConverter) {
    Optional<ChannelGroup> curChannelGroup = Optional.empty();
    try {
      curChannelGroup = Optional.ofNullable(channelGroupConverter.convert(
        site.orElse(null), versionSiteChanDaos, channelConverter,
        versionRange.lowerEndpoint(), versionRange.upperEndpoint(), activeChannels));
    } catch (Exception ex) {
      logger.warn(ex.getMessage());
      logger.warn("Could not convert channel group with time range {} - {}", versionRange.lowerEndpoint(), versionRange.upperEndpoint());
      logger.warn("Could not convert channel group with sites {}", Stream.of(site.orElse(null))
        .map(SiteDao::getId)
        .map(SiteKey::toString)
        .collect(Collectors.toList()));
      logger.warn("Could not convert channel group with siteChans {}", versionSiteChanDaos.stream()
        .map(SiteChanDao::getId)
        .map(SiteChanKey::toString)
        .collect(Collectors.toList()));
    }
    return curChannelGroup;
  }

  private NavigableSet<Instant> getChangeTimes(
    NavigableMap<Instant, SiteDao> sitesForVersion,
    Map<String, NavigableMap<Instant, SiteChanDao>> siteChansForVersion,
    Map<String, NavigableMap<Instant, Channel>> chanCodeChannelNavMap) {

    //first get times based on channel group and channel
    NavigableSet<Instant> changeTimes = AssemblerUtils.getTimesForObjectChanges(chanCodeChannelNavMap);
    AssemblerUtils.addChangeTimesToListForDaosWithDayAccuracy(changeTimes, siteChansForVersion, changeOccuredForSiteChan, Functions.compose(SiteChanKey::getOnDate,
      SiteChanDao::getId), SiteChanDao::getOffDate);
    AssemblerUtils.addChangeTimesToListForDaosWithDayAccuracy(changeTimes, sitesForVersion, changeOccuredForSite, Functions.compose(SiteKey::getOnDate,
      SiteDao::getId), SiteDao::getOffDate);

    return changeTimes;
  }
}
package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.utils.comparator.ChannelComparator;
import gms.shared.stationdefinition.converter.interfaces.ChannelConverter;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverter;
import gms.shared.stationdefinition.converter.interfaces.ResponseConverterTransform;
import gms.shared.stationdefinition.converter.util.TemporalMap;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SensorKey;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Component
public class ChannelAssembler {

  private static final Logger logger = LoggerFactory.getLogger(ChannelAssembler.class);

  private Instant getEffectiveUntilFromResponse(Response response) {

    if (response.isPresent()) {
      return response.getEffectiveUntil().orElse(Instant.MAX);
    }

    return Instant.MAX;
  }

  private final ChannelConverter channelConverter;
  private final ResponseConverter responseConverter;

  public ChannelAssembler(ChannelConverter channelConverter, ResponseConverter responseConverter) {
    this.channelConverter = channelConverter;
    this.responseConverter = responseConverter;
  }

  BiPredicate<SiteDao, SiteDao> changeOccuredForSite = (SiteDao prev, SiteDao curr) -> {

    if (Objects.isNull(prev) || Objects.isNull(curr)) {
      return true;
    }

    return !prev.getId().getStationCode().equals(curr.getId().getStationCode())
      || prev.getLatitude() != curr.getLatitude()
      || prev.getLongitude() != curr.getLongitude()
      || prev.getElevation() != curr.getElevation()
      || !prev.getReferenceStation().equals(curr.getReferenceStation());

  };

  //logic of what changes in site chan cause a change in station
  BiPredicate<SiteChanDao, SiteChanDao> changeOccuredForSiteChan = (SiteChanDao prev, SiteChanDao curr) -> {

    if (prev == null) {
      return curr != null;
    }

    return !prev.equals(curr);
  };

  /**
   * Build all {@link Channel}s for given query time
   *
   * @param effectiveTime channel query effective time
   * @param sites list of {@link SiteDao}
   * @param siteChans list of {@link SiteChanDao}
   * @param sensors list of {@link SensorDao}
   * @param wfdiscs list of {@link WfdiscDao}
   *
   * @return list of {@link Channel}
   */
  public List<Channel> buildAllForTime(Instant effectiveTime,
    List<SiteDao> sites,
    List<SiteChanDao> siteChans,
    List<SensorDao> sensors,
    List<WfdiscDao> wfdiscs,
    List<Response> responses) {

    Objects.requireNonNull(effectiveTime);
    Objects.requireNonNull(sites);
    Objects.requireNonNull(siteChans);
    Objects.requireNonNull(sensors);
    Objects.requireNonNull(wfdiscs);

    if (sites.isEmpty() || siteChans.isEmpty() || (sensors.isEmpty() && wfdiscs.isEmpty())) {
      return new ArrayList<>();
    }

    List<Channel> resultList = createChannelTablesAndMaps(Pair.of(effectiveTime, effectiveTime), sites, siteChans,
      sensors, wfdiscs, responses, Response::createVersionReference);
    Map<String, List<Channel>> channelMap = resultList.stream().collect(groupingBy(Channel::getName));
    resultList.clear();

    for (Map.Entry<String, List<Channel>> entry : channelMap.entrySet()) {
      NavigableMap<Instant, Channel> channelNavigableMap = new TreeMap<>();
      for (Channel channel : entry.getValue()) {
        channelNavigableMap.put(channel.getEffectiveAt().orElseThrow(), channel);
      }
      Map.Entry<Instant, Channel> floor = channelNavigableMap.floorEntry(effectiveTime);
      if (floor != null) {
        resultList.add(floor.getValue());
      }
    }
    return resultList.stream().sorted(new ChannelComparator()).collect(Collectors.toList());
  }

  /**
   * Build all channels for query time range
   *
   * @param startTime start time for query
   * @param endTime end time for query
   * @param sites list of {@link SiteDao}
   * @param siteChans list of {@link SiteChanDao}
   * @param sensors list of {@link SensorDao}
   * @param wfdiscs list of {@link WfdiscDao}
   *
   * @return list of {@link Channel}
   */
  public List<Channel> buildAllForTimeRange(Instant startTime,
    Instant endTime,
    List<SiteDao> sites,
    List<SiteChanDao> siteChans,
    List<SensorDao> sensors,
    List<WfdiscDao> wfdiscs,
    List<Response> responses) {

    Objects.requireNonNull(startTime);
    Objects.requireNonNull(sites);
    Objects.requireNonNull(siteChans);
    Objects.requireNonNull(sensors);
    Objects.requireNonNull(wfdiscs);

    if (sites.isEmpty() || siteChans.isEmpty() || (sensors.isEmpty() && wfdiscs.isEmpty())) {
      return new ArrayList<>();
    }

    return createChannelTablesAndMaps(
      Pair.of(startTime, endTime), sites, siteChans, sensors, wfdiscs, responses, Response::createEntityReference).stream()
      .filter(channel -> channel.getEffectiveAt().isPresent())
      .filter(channel -> !channel.getEffectiveAt().get().isAfter(AssemblerUtils.effectiveAtNoonOffset.apply(endTime)))
      .filter(channel -> !channel.getEffectiveUntil().isPresent()
      || !channel.getEffectiveUntil().get().isBefore(startTime))
      .distinct()
      .sorted(new ChannelComparator())
      .collect(Collectors.toList());
  }

  /**
   * Build channel from associated record
   *
   * @param processingMetadataMap processing metadata type map for channel
   * @param beamDao {@link BeamDao}
   * @param site {@link SiteDao}
   * @param wfdisc {@link WfdiscDao}
   * @param siteChan {@link SiteChanDao}
   * @param possibleSensor {@link SensorDao}
   * @param channelEffectiveTime channel effective time
   * @param channelEndTime channel end time
   *
   * @return {@link Channel}
   */
  public Channel buildFromAssociatedRecord(Map<ChannelProcessingMetadataType, Object> processingMetadataMap,
    Optional<BeamDao> beamDao,
    SiteDao site,
    WfdiscDao wfdisc,
    SiteChanDao siteChan,
    Optional<SensorDao> possibleSensor,
    Instant channelEffectiveTime,
    Instant channelEndTime) {

    Objects.requireNonNull(processingMetadataMap);
    Objects.requireNonNull(beamDao);
    Objects.requireNonNull(site);
    Objects.requireNonNull(wfdisc);
    Objects.requireNonNull(siteChan);
    Objects.requireNonNull(possibleSensor);
    Objects.requireNonNull(channelEffectiveTime);
    Objects.requireNonNull(channelEndTime);

    Preconditions.checkState(channelEffectiveTime.isBefore(channelEndTime),
      "Channel effective time must be before channel end time.");

    // For arrays, we should be able to tell that a derived channel exists when a
    // reference station (e.g., reference station is ASAR in the site table,
    // sta is ASAR in site table) has a channel in the sitechan (e.g., sta is ASAR
    // and it has a channel SHZ ), whereas for channels in the sitechan that don't
    // match a reference station (e.g., sta in site table is AS01 and its reference
    // station is ASAR, sta in sitechan is AS01 and it has a channel SHZ,) those
    // would be raw channels. 3 component channels tend to be raw channels as we don't
    // beam on 3 component channels
    // Statype is ss (single station) == raw
    if (site.getId().getStationCode().equals(site.getReferenceStation())
      && siteChan.getId().getStationCode().equals(site.getReferenceStation())
      && site.getStaType() == StaType.ARRAY_STATION) {
      return channelConverter.convertToBeamDerived(site,
        siteChan,
        wfdisc,
        channelEffectiveTime,
        channelEndTime,
        beamDao,
        processingMetadataMap);
    } else {
      Preconditions.checkState(possibleSensor.isPresent(),
        "Cannot convert raw channel if sensor is not present");
      SensorDao sensor = possibleSensor.get();
      ResponseConverterTransform responseConverterTransform
        = (wfdiscDao, sensorDao, calibration, frequencyAmplitudePhase) -> responseConverter
          .convertToEntity(wfdiscDao);

      return channelConverter.convert(siteChan, site, sensor, sensor.getInstrument(), wfdisc,
        Range.open(siteChan.getId().getOnDate(), siteChan.getOffDate()), responseConverterTransform);
    }
  }

  /**
   * Build raw channel from usual legacy database tables
   *
   * @param site {@link SiteDao}
   * @param wfdisc {@link WfdiscDao}
   * @param siteChan {@link SiteChanDao}
   * @param possibleSensor {@link SensorDao}
   * @param channelEffectiveTime channel effective time
   * @param channelEndTime channel end time
   *
   * @return raw {@link Channel}
   */
  public Channel buildRawChannel(SiteDao site,
    WfdiscDao wfdisc,
    SiteChanDao siteChan,
    Optional<SensorDao> possibleSensor,
    Instant channelEffectiveTime,
    Instant channelEndTime) {

    Objects.requireNonNull(site);
    Objects.requireNonNull(wfdisc);
    Objects.requireNonNull(siteChan);
    Objects.requireNonNull(possibleSensor);
    Objects.requireNonNull(channelEffectiveTime);
    Objects.requireNonNull(channelEndTime);

    Preconditions.checkState(channelEffectiveTime.isBefore(channelEndTime),
      "Channel effective time must be before channel end time.");

    // For arrays, we should be able to tell that a derived channel exists when a
    // reference station (e.g., reference station is ASAR in the site table,
    // sta is ASAR in site table) has a channel in the sitechan (e.g., sta is ASAR
    // and it has a channel SHZ ), whereas for channels in the sitechan that don't
    // match a reference station (e.g., sta in site table is AS01 and its reference
    // station is ASAR, sta in sitechan is AS01 and it has a channel SHZ,) those
    // would be raw channels. 3 component channels tend to be raw channels as we don't
    // beam on 3 component channels
    // Statype is ss (single station) == raw
    Preconditions.checkState(possibleSensor.isPresent(),
      "Cannot convert raw channel if sensor is not present");
    SensorDao sensor = possibleSensor.get();
    ResponseConverterTransform responseConverterTransform
      = (wfdiscDao, sensorDao, calibration, frequencyAmplitudePhase) -> responseConverter
        .convertToEntity(wfdiscDao);

    return channelConverter.convert(siteChan, site, sensor, sensor.getInstrument(), wfdisc,
      Range.open(siteChan.getId().getOnDate(), siteChan.getOffDate()), responseConverterTransform);
  }

  /**
   * Build list of {@link Channel}s using response converter transform
   *
   * @param startEndTime channel effective time
   * @param sites list of {@link SiteDao}
   * @param siteChans list of {@link SiteChanDao}
   * @param sensors list of {@link SensorDao}
   * @param wfdiscs list of {@link WfdiscDao}
   * @param responses list of {@link Response}
   *
   * @return list of {@link Channel}
   */
  private List<Channel> createChannelTablesAndMaps(
    Pair<Instant, Instant> startEndTime,
    List<SiteDao> sites,
    List<SiteChanDao> siteChans,
    List<SensorDao> sensors,
    List<WfdiscDao> wfdiscs,
    List<Response> responses,
    UnaryOperator<Response> responseFacet) {

    TemporalMap<String, SiteDao> siteVersionsBySta = sites.stream()
      .collect(TemporalMap.collector(Functions.compose(SiteKey::getStationCode, SiteDao::getId),
        Functions.compose(SiteKey::getOnDate, SiteDao::getId)));

    Table<String, String, NavigableMap<Instant, SiteChanDao>> siteChansByStationAndChannel
      = AssemblerUtils.buildVersionTable(Functions.compose(SiteChanKey::getStationCode, SiteChanDao::getId),
        Functions.compose(SiteChanKey::getChannelCode, SiteChanDao::getId),
        Functions.compose(SiteChanKey::getOnDate, SiteChanDao::getId),
        siteChans);

    Table<String, String, NavigableMap<Instant, SensorDao>> sensorVersionsByStaChan
      = AssemblerUtils.buildVersionTable(Functions.compose(SensorKey::getStation, SensorDao::getSensorKey),
        Functions.compose(SensorKey::getChannel, SensorDao::getSensorKey),
        Functions.compose(SensorKey::getTime, SensorDao::getSensorKey),
        sensors);

    Table<String, String, NavigableMap<Instant, WfdiscDao>> wfdiscVersionsByStaChan
      = AssemblerUtils.buildVersionTable(WfdiscDao::getStationCode,
        WfdiscDao::getChannelCode,
        WfdiscDao::getTime,
        wfdiscs);

    TemporalMap<UUID, Response> idToResponseMap = responses.stream()
      .collect(TemporalMap.collector(Response::getId,
        Functions.compose(Optional::get, Response::getEffectiveAt)));

    return siteChansByStationAndChannel.rowKeySet().stream()
      .flatMap(staKey -> {
        var chanNavmap = siteChansByStationAndChannel.row(staKey);
        return chanNavmap.entrySet().stream()
          .flatMap(entry
            -> processChannels(
            startEndTime,
            siteVersionsBySta.getVersionMap(staKey),
            entry.getValue(),
            sensorVersionsByStaChan.row(staKey).get(entry.getKey()),
            wfdiscVersionsByStaChan.row(staKey).get(entry.getKey()),
            idToResponseMap, responseFacet).stream());
      }).filter(Objects::nonNull)
      .sorted()
      .collect(Collectors.toList());
  }

  private List<Channel> processChannels(
    Pair<Instant, Instant> startEndTime,
    NavigableMap<Instant, SiteDao> siteVersions,
    NavigableMap<Instant, SiteChanDao> siteChanVersions,
    NavigableMap<Instant, SensorDao> sensorVersions,
    NavigableMap<Instant, WfdiscDao> wfdiscVersions,
    TemporalMap<UUID, Response> idToResponseMap,
    UnaryOperator<Response> responseFacet) {

    //determine if range or single point in time
    boolean isRange = startEndTime.getLeft().isBefore(startEndTime.getRight());

    NavigableSet<Instant> possibleVersionTimes = getChangeTimes(siteVersions, siteChanVersions, sensorVersions, wfdiscVersions, idToResponseMap);
    SortedSet<Instant> validTimes = AssemblerUtils.getValidTimes(startEndTime, possibleVersionTimes, isRange);

    return processPossibleVersionTimes(new ArrayList<>(validTimes), siteVersions, siteChanVersions, sensorVersions,
      wfdiscVersions, idToResponseMap, responseFacet);
  }

  private List<Channel> processPossibleVersionTimes(
    List<Instant> possibleVersionTimes,
    NavigableMap<Instant, SiteDao> sitesForVersion,
    NavigableMap<Instant, SiteChanDao> siteChansForVersion,
    NavigableMap<Instant, SensorDao> sensorsForVersion,
    NavigableMap<Instant, WfdiscDao> wfdiscsForVersion,
    TemporalMap<UUID, Response> responses,
    UnaryOperator<Response> responseFacet) {

    List<Channel> retChannels = new ArrayList<>();

    for (var i = 0; i < possibleVersionTimes.size() - 1; i++) {
      Range<Instant> versionRange = Range.open(possibleVersionTimes.get(i), possibleVersionTimes.get(i + 1));
      Instant currTime = versionRange.lowerEndpoint();

      var possibleSite = AssemblerUtils.getObjectsForVersionTime(currTime, sitesForVersion, SiteDao::getOffDate);
      var possibleSiteChan = AssemblerUtils.getObjectsForVersionTime(currTime, siteChansForVersion, SiteChanDao::getOffDate);
      var possibleSensorDao = AssemblerUtils.getObjectsForVersionTime(currTime, sensorsForVersion, Functions.compose(SensorKey::getEndTime, SensorDao::getSensorKey));
      var possibleWfdiscDao = AssemblerUtils.getObjectsForVersionTime(currTime, wfdiscsForVersion, WfdiscDao::getEndTime);
      Optional<Response> possibleResponse = possibleWfdiscDao.isPresent()
        ? responses.getVersionFloor(
          UUID.nameUUIDFromBytes((possibleWfdiscDao.get().getStationCode() + possibleWfdiscDao.get().getChannelCode()).getBytes()), currTime)
        : Optional.empty();

      if (possibleResponse.isPresent() && !getEffectiveUntilFromResponse(possibleResponse.get()).isAfter(currTime)) {
        possibleResponse = Optional.empty();
      }

      if (possibleResponse.isPresent()) {
        possibleResponse = Optional.of(responseFacet.apply(possibleResponse.get()));
      }

      var chanRange = Range.open(currTime, AssemblerUtils.getImmediatelyBeforeInstant(versionRange.upperEndpoint()));

      Optional<Channel> curChannel = convertChannel(
        chanRange,
        possibleSiteChan.orElse(null),
        possibleSite.orElse(null),
        possibleSensorDao.orElse(null),
        possibleWfdiscDao.orElse(null),
        possibleResponse);

      if (curChannel.isPresent()) {
        retChannels.add(curChannel.get());
      }
    }

    return retChannels;
  }

  private Optional<Channel> convertChannel(Range<Instant> versionRange,
    SiteChanDao siteChanDao,
    SiteDao siteDao,
    SensorDao sensor,
    WfdiscDao wfdiscDao,
    Optional<Response> response) {
    Optional<Channel> curChannel = Optional.empty();
    try {

      curChannel = Optional.of(channelConverter.convert(siteChanDao, siteDao,
        sensor, sensor != null ? sensor.getInstrument() : null, wfdiscDao, versionRange, response));
    }
    catch (Exception ex) {
      logger.warn(ex.getMessage());
      logger.warn("Could not convert channel with time range {} - {}", versionRange.lowerEndpoint(), versionRange.upperEndpoint());

      if (siteDao != null) {
        logger.warn("Could not convert channel with site {}", siteDao.toString());
      }
      if (siteChanDao != null) {
        logger.warn("Could not convert channel with siteChan {}", siteChanDao.toString());
      }
    }
    return curChannel;
  }

  private NavigableSet<Instant> getChangeTimes(
    NavigableMap<Instant, SiteDao> sitesForRange,
    NavigableMap<Instant, SiteChanDao> siteChansForRange,
    NavigableMap<Instant, SensorDao> sensorsForRange,
    NavigableMap<Instant, WfdiscDao> wfdiscsForRange,
    TemporalMap<UUID, Response> idToResponseMap) {

    NavigableSet<Instant> changeTimes = getChangeTimesForWfdiscSensorAndResponse(sensorsForRange, wfdiscsForRange, idToResponseMap);
    AssemblerUtils.addChangeTimesToListForDaosWithDayAccuracy(changeTimes, siteChansForRange, changeOccuredForSiteChan,
      Functions.compose(SiteChanKey::getOnDate, SiteChanDao::getId), SiteChanDao::getOffDate);
    AssemblerUtils.addChangeTimesToListForDaosWithDayAccuracy(changeTimes, sitesForRange, changeOccuredForSite,
      Functions.compose(SiteKey::getOnDate, SiteDao::getId), SiteDao::getOffDate);

    return changeTimes;
  }

  private NavigableSet<Instant> getChangeTimesForWfdiscSensorAndResponse(
    NavigableMap<Instant, SensorDao> sensorsForRange,
    NavigableMap<Instant, WfdiscDao> wfdiscsForRange,
    TemporalMap<UUID, Response> idToResponseMap) {

    var changeTimes = new TreeSet<Instant>();

    NavigableSet<Instant> possibleTimes = getPossibleTimes(wfdiscsForRange, WfdiscDao::getTime, WfdiscDao::getEndTime);
    possibleTimes.addAll(getPossibleTimes(sensorsForRange, Functions.compose(SensorKey::getTime, SensorDao::getSensorKey),
      Functions.compose(SensorKey::getEndTime, SensorDao::getSensorKey)));

    Iterator<Instant> versionTimeIterator = possibleTimes.iterator();

    Optional<WfdiscDao> currWfdisc;
    Optional<SensorDao> currSensor;
    Optional<Response> prevResponse = Optional.empty();
    Optional<Response> currResponse;
    Double prevSampleRate = null;
    Double currSampleRate;

    while (versionTimeIterator.hasNext()) {

      var currInstant = versionTimeIterator.next();
      currWfdisc = AssemblerUtils.getObjectsForVersionTime(currInstant, wfdiscsForRange, WfdiscDao::getEndTime);
      currSensor = AssemblerUtils.getObjectsForVersionTime(currInstant, sensorsForRange,
        Functions.compose(SensorKey::getEndTime, SensorDao::getSensorKey));

      currSampleRate = getCurrSampleRate(currWfdisc, currSensor);
      currResponse = getCurrResponse(currWfdisc, idToResponseMap, currInstant);

      //if sample rate changes
      if ((prevSampleRate == null && currSampleRate != null) || (prevSampleRate != null && currSampleRate == null)
        || ((prevSampleRate != null && currSampleRate != null) && (!prevSampleRate.equals(currSampleRate)))
        //or response changes add time
        || (prevResponse.isEmpty() && currResponse.isPresent()) || (prevResponse.isPresent() && currResponse.isEmpty())
        || (prevResponse.isPresent() && currResponse.isPresent()) && !(prevResponse.get().getId().equals(currResponse.get().getId()))) {

        changeTimes.add(currInstant);
      }

      prevSampleRate = currSampleRate;
      prevResponse = currResponse;
    }
    return changeTimes;
  }

  private Double getCurrSampleRate(Optional<WfdiscDao> currWfdisc, Optional<SensorDao> currSensor) {

    Double currSampleRate = null;
    if (currSensor.isPresent()) {
      var ins = currSensor.get().getInstrument();
      currSampleRate = ins.getSampleRate();
    } else if (currWfdisc.isPresent()) {
      currSampleRate = currWfdisc.get().getSampRate();
    }

    return currSampleRate;
  }

  private Optional<Response> getCurrResponse(Optional<WfdiscDao> currWfdisc,
    TemporalMap<UUID, Response> idToResponseMap, Instant currInstant) {
    Optional<Response> currResponse = currWfdisc.isPresent()
      ? idToResponseMap.getVersionFloor(
        UUID.nameUUIDFromBytes((currWfdisc.get().getStationCode() + currWfdisc.get().getChannelCode()).getBytes()), currInstant)
      : Optional.empty();

    if (currResponse.isPresent() && !getEffectiveUntilFromResponse(currResponse.get()).isAfter(currInstant)) {
      currResponse = Optional.empty();
    }
    return currResponse;
  }

  private <V> NavigableSet<Instant> getPossibleTimes(
    NavigableMap<Instant, V> daosForRange,
    Function<V, Instant> startTimeExtractor,
    Function<V, Instant> endTimeExtractor) {

    if (daosForRange == null) {
      return new TreeSet<>();
    }

    var possibleVersionTimes = new TreeSet<Instant>();
    Instant prevTime = null;

    for (Map.Entry<Instant, V> dao : daosForRange.entrySet()) {
      Instant effectiveAt = startTimeExtractor.apply(dao.getValue());
      possibleVersionTimes.add(effectiveAt);

      if (prevTime != null && !AssemblerUtils.fullTimePrecisionObjectAdjacent(prevTime, effectiveAt)) {
        possibleVersionTimes.add(AssemblerUtils.getImmediatelyAfterInstant(prevTime));
      }
      prevTime = endTimeExtractor.apply(dao.getValue());
    }
    if (prevTime != null) {
      possibleVersionTimes.add(AssemblerUtils.getImmediatelyAfterInstant(prevTime));
    }
    return possibleVersionTimes;
  }
}

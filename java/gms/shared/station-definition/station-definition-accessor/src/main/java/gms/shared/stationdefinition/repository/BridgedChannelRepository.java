package gms.shared.stationdefinition.repository;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import gms.shared.stationdefinition.api.channel.ChannelAccessorInterface;
import gms.shared.stationdefinition.api.channel.ChannelRepositoryInterface;
import gms.shared.stationdefinition.cache.VersionCache;
import gms.shared.stationdefinition.coi.channel.BeamType;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.converter.util.assemblers.ChannelAssembler;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.StationChannelTimeKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.repository.util.CssCoiConverterUtility;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.BEAM_TYPE;
import static gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType.BRIDGED;

/**
 * A {@link ChannelAccessorInterface} implementation that uses a bridged
 * database
 */
@Component("bridgedChannelRepository")
public class BridgedChannelRepository implements ChannelRepositoryInterface {

  private static final Logger logger = LoggerFactory.getLogger(BridgedChannelRepository.class);

  private static final String SITECHAN_ERR = "No sitechan from which to load channel";
  private static final String SITE_ERR = "No site from which to load channel";
  private static final String WFDISC_ERR = "No wfdisc from which to load channel";

  private final BeamDatabaseConnector beamDatabaseConnector;
  private final SiteDatabaseConnector siteDatabaseConnector;
  private final SiteChanDatabaseConnector siteChanDatabaseConnector;
  private final SensorDatabaseConnector sensorDatabaseConnector;
  private final WfdiscDatabaseConnector wfdiscDatabaseConnector;
  private final BridgedResponseRepository responseRepository;
  private final ChannelAssembler channelAssembler;
  private final StationDefinitionIdUtility stationDefinitionIdUtility;
  private final VersionCache versionCache;

  public BridgedChannelRepository(BeamDatabaseConnector beamDatabaseConnector,
    SiteDatabaseConnector siteDatabaseConnector,
    SiteChanDatabaseConnector siteChanDatabaseConnector,
    SensorDatabaseConnector sensorDatabaseConnector,
    WfdiscDatabaseConnector wfdiscDatabaseConnector,
    ChannelAssembler channelAssembler,
    StationDefinitionIdUtility stationDefinitionIdUtility,
    VersionCache versionCache,
    BridgedResponseRepository responseRepository) {

    this.beamDatabaseConnector = beamDatabaseConnector;
    this.siteDatabaseConnector = siteDatabaseConnector;
    this.siteChanDatabaseConnector = siteChanDatabaseConnector;
    this.sensorDatabaseConnector = sensorDatabaseConnector;
    this.wfdiscDatabaseConnector = wfdiscDatabaseConnector;
    this.channelAssembler = channelAssembler;
    this.stationDefinitionIdUtility = stationDefinitionIdUtility;
    this.versionCache = versionCache;
    this.responseRepository = responseRepository;
  }

  @Override
  public List<Channel> findChannelsByNameAndTime(List<String> channelNames, Instant effectiveAt) {
    Objects.requireNonNull(channelNames, "channelNames must not be null");
    Preconditions.checkState(!channelNames.isEmpty());

    List<SiteChanKey> siteChanKeys = CssCoiConverterUtility.getSiteChanKeysFromChannelNames(channelNames);

    StationDefinitionDataHolder data = BridgedRepositoryUtils.findDataByTime(
      siteChanKeys, effectiveAt, siteDatabaseConnector, siteChanDatabaseConnector,
      sensorDatabaseConnector, wfdiscDatabaseConnector);

    Pair<Instant, Instant> minMaxTimes
      = BridgedRepositoryUtils.getMinMaxFromSiteChanDaos(data.getSiteChanDaos(), effectiveAt, effectiveAt);

    List<Response> responses = responseRepository.findResponsesGivenSensorAndWfdisc(data, minMaxTimes.getLeft(),
      minMaxTimes.getRight() == Instant.MAX ? Instant.now() : minMaxTimes.getRight());

    List<Channel> channels = channelAssembler.buildAllForTime(
      effectiveAt, data.getSiteDaos(), data.getSiteChanDaos(), data.getSensorDaos(), data.getWfdiscVersions(), responses);

    cacheResponseIds(channels);

    return channels;
  }

  @Override
  public List<Channel> findChannelsByNameAndTimeRange(List<String> channelNames, Instant startTime, Instant endTime) {
    Objects.requireNonNull(channelNames, "channelNames must not be null");
    Preconditions.checkState(!channelNames.isEmpty());
    Preconditions.checkState(startTime.isBefore(endTime));
    List<SiteChanKey> siteChanKeys = CssCoiConverterUtility.getSiteChanKeysFromChannelNames(channelNames);

    StationDefinitionDataHolder data = BridgedRepositoryUtils.findDataByTimeRange(
      siteChanKeys, startTime, endTime, siteDatabaseConnector, siteChanDatabaseConnector,
      sensorDatabaseConnector, wfdiscDatabaseConnector);

    Pair<Instant, Instant> minMaxTimes
      = BridgedRepositoryUtils.getMinMaxFromSiteChanDaos(data.getSiteChanDaos(), startTime, endTime);

    List<Response> responses = responseRepository.findResponsesGivenSensorAndWfdisc(data, minMaxTimes.getLeft(),
      minMaxTimes.getRight() == Instant.MAX ? Instant.now() : minMaxTimes.getRight());

    List<Channel> channels = channelAssembler.buildAllForTimeRange(
      startTime, endTime, data.getSiteDaos(), data.getSiteChanDaos(), data.getSensorDaos(), data.getWfdiscVersions(), responses);

    cacheResponseIds(channels);

    return channels;
  }

  public List<Channel> findChannelsGivenSiteAndSiteChan(StationDefinitionDataHolder stationDefinitionDataHolder,
    Instant startTime, Instant endTime) {

    Objects.requireNonNull(stationDefinitionDataHolder.getSiteDaos());
    Objects.requireNonNull(stationDefinitionDataHolder.getSiteChanDaos());

    var data = BridgedRepositoryUtils.getSensorAndWfdiscData(stationDefinitionDataHolder,
      sensorDatabaseConnector, wfdiscDatabaseConnector);

    List<Response> responses = responseRepository.findResponsesGivenSensorAndWfdisc(data, startTime, endTime);
    List<Channel> channels = channelAssembler.buildAllForTimeRange(
      startTime, endTime, data.getSiteDaos(), data.getSiteChanDaos(), data.getSensorDaos(), data.getWfdiscVersions(), responses);

    cacheResponseIds(channels);
    return channels;
  }

  @Override
  public Channel loadChannelFromWfdisc(List<Long> wfids, Optional<TagName> associatedRecordType,
    Optional<Long> associatedRecordId, Optional<Long> filterId,
    Instant channelEffectiveTime, Instant channelEndTime) {

    // create the raw channel
    Preconditions.checkState(channelEffectiveTime.isBefore(channelEndTime),
      "Attempting to load channel from wfdisc, channel effective time must be before channel end time");

    if (associatedRecordType.isEmpty() && associatedRecordId.isEmpty() && filterId.isEmpty()) {
      Preconditions.checkState(!wfids.isEmpty(),
        "Attempting to load channel from wfdisc, wfids must be greater than 0");

      // create raw channel using wfids
      logger.info("Calling Create Raw Channel with wfids.");
      return createRawChannel(wfids, channelEffectiveTime, channelEndTime);
    } else if (associatedRecordType.isPresent() && associatedRecordId.isPresent()) {
      // create channel using Event or FK Beam

      // ensure that we have exactly one wfid
      Preconditions.checkState(wfids.size() == 1,
        "Attempting to load channel from wfdisc, must contain a single wfid for derived channels");
      var wfid = wfids.get(0);

      var assocRecordType = associatedRecordType.get();
      var assocRecordId = associatedRecordId.get();
      switch (assocRecordType) {
        case EVID:
          return createEventBeamDerivedChannel(wfid, assocRecordType, assocRecordId,
            channelEffectiveTime, channelEndTime);
        case ARID:
          return createFkBeamDerivedChannel(wfid, assocRecordType, assocRecordId,
            channelEffectiveTime, channelEndTime);
        default:
          throw new IllegalArgumentException("Illegal arguments for loading channel from wfdisc, "
            + assocRecordType.getName() + " record type not supported");
      }
    } else {
      throw new IllegalArgumentException("Illegal arguments for loading channel from wfdisc");
    }
  }

  private void cacheResponseIds(List<Channel> channels) {
    channels.forEach(channel -> channel.getResponse()
      .map(Response::getId)
      .ifPresent(id -> stationDefinitionIdUtility.storeResponseIdChannelNameMapping(id, channel.getName())));
  }

  /**
   * Create raw channel using list of wfids, channel effective time and end time
   *
   * @param wfids list of wfids
   * @param channelEffectiveTime channel query effective time
   * @param channelEndTime channel query end time
   *
   * @return raw {@link Channel}
   */
  private Channel createRawChannel(List<Long> wfids, Instant channelEffectiveTime, Instant channelEndTime) {

    // create wfdiscs and compare against site dao refsta
    List<WfdiscDao> wfdiscDaoList = wfdiscDatabaseConnector.findWfdiscsByWfids(wfids);

    // create station channel time keys from the wfdisc daos
    List<StationChannelTimeKey> stationChannelTimeKeys = wfdiscDaoList.stream()
      .map(wfdiscDao -> new StationChannelTimeKey(wfdiscDao.getStationCode(),
      wfdiscDao.getChannelCode(), channelEffectiveTime))
      .collect(Collectors.toList());

    var siteChanKeys = stationChannelTimeKeys.stream()
      .map(key -> new SiteChanKey(key.getStationCode(),
      key.getChannelCode(), channelEffectiveTime))
      .collect(Collectors.toList());

    List<SiteChanDao> siteChanDaoList = siteChanDatabaseConnector.findSiteChansByKeyAndTime(siteChanKeys, channelEffectiveTime);
    Optional<SiteChanDao> possibleSiteChanDao = siteChanDaoList.stream().findFirst();
    var siteChanDao = possibleSiteChanDao.orElseThrow(() -> new IllegalStateException(SITECHAN_ERR));

    // find all site daos using the station channel time keys
    List<SiteDao> siteDaoList = stationChannelTimeKeys.stream()
      .flatMap(key -> siteDatabaseConnector.findSitesByStationCodesAndStartTime(
      List.of(key.getStationCode()), key.getTime()).stream())
      .collect(Collectors.toList());

    List<String> refStations = siteDaoList.stream().filter(
      site -> (site.getStaType() == StaType.ARRAY_STATION || site.getStaType() == StaType.UNKNOWN)).map(
        SiteDao::getReferenceStation).collect(Collectors.toList());

    // find sensor daos using site chan keys
    List<SensorDao> sensorDaos = siteChanKeys.stream()
      .map(key -> sensorDatabaseConnector.findSensorByKeyInRange(key.getStationCode(),
      key.getChannelCode(), channelEffectiveTime, channelEndTime))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
    Optional<SensorDao> possibleSensorDao = sensorDaos.stream().findFirst();

    //there should only be one site existing at a certain time
    Optional<SiteDao> possibleSiteDao = siteDaoList.stream()
      .filter(site -> Range.closedOpen(site.getId().getOnDate(), site.getOffDate()).contains(siteChanDao.getId().getOnDate()))
      .findFirst();
    var siteDao = possibleSiteDao.orElseThrow(() -> new IllegalStateException(SITE_ERR));

    // check that the wfdiscs don't have cha property that contains site refsta property, filter those for arrays
    // unless it is a single instrument station, in which case sta = refsta... so don't filter those.
    List<WfdiscDao> wfdiscDaos;
    wfdiscDaos = wfdiscDaoList.stream()
      .filter(dao -> (!refStations.contains(dao.getStationCode())))
      .collect(Collectors.toList());

    //wfid is a pk, guaranteed exactly 1 wfdisc
    Optional<WfdiscDao> possbleWfdiscDao = wfdiscDaos.stream().findFirst();
    var wfdiscDao = possbleWfdiscDao.orElseThrow(() -> new IllegalStateException(WFDISC_ERR));

    // ensure wfids dont have assctd beamdaos
    List<BeamDao> beamDaos = beamDatabaseConnector.findBeamsByWfid(wfids);
    Preconditions.checkState(beamDaos.isEmpty(),
      "Attempting to load channel from wfdisc, wfids mustn't have associated beam records");

    return channelAssembler
      .buildRawChannel(
        siteDao,
        wfdiscDao,
        siteChanDao,
        possibleSensorDao,
        channelEffectiveTime,
        channelEndTime);
  }

  /**
   * Create event beam derived channel
   *
   * @param wfid wfid to query derived channel
   * @param associatedRecordType record type for input beam type
   * @param associatedRecordId record id for derived channel
   * @param channelEffectiveTime channel query effective time
   * @param channelEndTime channel query end time
   *
   * @return derived {@link Channel}
   */
  private Channel createEventBeamDerivedChannel(Long wfid, TagName associatedRecordType,
    Long associatedRecordId, Instant channelEffectiveTime, Instant channelEndTime) {

    // processing metadata type for event beam type
    EnumMap<ChannelProcessingMetadataType, Object> processingMetadataMap = new EnumMap<>(ChannelProcessingMetadataType.class);
    processingMetadataMap.put(BRIDGED, "/bridged," + associatedRecordType + ":" + associatedRecordId);
    processingMetadataMap.put(BEAM_TYPE, BeamType.EVENT);

    return createAndCacheDerivedChannel(wfid, associatedRecordType, associatedRecordId,
      channelEffectiveTime, channelEndTime, processingMetadataMap);
  }

  /**
   * Create fk beam derived channel
   *
   * @param wfid wfid to query derived channel
   * @param associatedRecordType record type for input beam type
   * @param associatedRecordId record id for derived channel
   * @param channelEffectiveTime channel query effective time
   * @param channelEndTime channel query end time
   *
   * @return derived {@link Channel}
   */
  private Channel createFkBeamDerivedChannel(Long wfid, TagName associatedRecordType,
    Long associatedRecordId, Instant channelEffectiveTime, Instant channelEndTime) {

    // processing metadata type for fk beam type
    EnumMap<ChannelProcessingMetadataType, Object> processingMetadataMap = new EnumMap<>(ChannelProcessingMetadataType.class);
    processingMetadataMap.put(BRIDGED, "/bridged," + associatedRecordType + ":" + associatedRecordId);
    processingMetadataMap.put(BEAM_TYPE, BeamType.FK);

    return createAndCacheDerivedChannel(wfid, associatedRecordType, associatedRecordId,
      channelEffectiveTime, channelEndTime, processingMetadataMap);
  }

  /**
   * Create and cache the derived channel
   *
   * @param wfid wfid to query derived channel
   * @param associatedRecordType record type for input beam type
   * @param associatedRecordId record id for derived channel
   * @param channelEffectiveTime channel query effective time
   * @param channelEndTime channel query end time
   * @param processingMetadataMap processing meta map for derived channel type
   *
   * @return derived {@link Channel}
   */
  private Channel createAndCacheDerivedChannel(Long wfid, TagName associatedRecordType,
    Long associatedRecordId, Instant channelEffectiveTime, Instant channelEndTime,
    EnumMap<ChannelProcessingMetadataType, Object> processingMetadataMap) {

    // check if derived channel exists in cache
    var channel = stationDefinitionIdUtility.getDerivedChannelForWfidRecordId(associatedRecordType,
      associatedRecordId,
      wfid);

    // populate channel if it exists
    if (channel != null) {
      Channel populated = (Channel) versionCache.retrieveVersionsByEntityIdAndTime(
        Channel.class.getSimpleName().concat(channel.getName()),
        channel.getEffectiveAt().orElseThrow());
      if (populated != null) {
        return populated;
      }
    }

    // query list of wfdisc daos using the given wfid
    List<WfdiscDao> wfdiscDaoList = wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(wfid));

    //wfid is a pk, guaranteed exactly 1 wfdisc
    Optional<WfdiscDao> possbleWfdiscDao = wfdiscDaoList.stream().findFirst();
    var wfdiscDao = possbleWfdiscDao.orElseThrow(() -> new IllegalStateException(WFDISC_ERR));

    // create StationChannelTimeKey for site dao query
    var stationChannelTimeKey = new StationChannelTimeKey(wfdiscDao.getStationCode(), wfdiscDao.getChannelCode(), channelEffectiveTime);
    List<SiteDao> siteDaoList = siteDatabaseConnector.findSitesByStationCodesAndStartTime(List.of(stationChannelTimeKey.getStationCode()), stationChannelTimeKey.getTime());

    // create SiteChanKey for site dao query
    var siteChanKey = new SiteChanKey(stationChannelTimeKey.getStationCode(), stationChannelTimeKey.getChannelCode(), channelEffectiveTime);
    List<SiteChanDao> siteChanDaoList = siteChanDatabaseConnector.findSiteChansByKeyAndTime(List.of(siteChanKey), channelEffectiveTime);
    Optional<SiteChanDao> possibleSiteChanDao = siteChanDaoList.stream().findFirst();
    var siteChanDao = possibleSiteChanDao.orElse(null);
    if (siteChanDao == null) {
      logger.debug(SITECHAN_ERR);
      return null;
    }

    // query beam dao using the given wfid
    Optional<BeamDao> beamDao = beamDatabaseConnector.findBeamForWfid(wfid);
    Optional<SensorDao> sensorDao = sensorDatabaseConnector.findSensorByKeyInRange(siteChanKey.getStationCode(),
      siteChanKey.getChannelCode(),
      channelEffectiveTime,
      channelEndTime);

    //there should only be one site existing at a certain time
    Optional<SiteDao> possibleSiteDao = siteDaoList.stream()
      .filter(site -> Range.closedOpen(site.getId().getOnDate(), site.getOffDate()).contains(siteChanDao.getId().getOnDate()))
      .findFirst();
    var siteDao = possibleSiteDao.orElse(null);
    if (siteDao == null) {
      logger.debug(SITE_ERR);
      return null;
    }

    // build the derived channel using channel assembler and converter
    var built = channelAssembler
      .buildFromAssociatedRecord(processingMetadataMap,
        beamDao,
        siteDao,
        wfdiscDao,
        siteChanDao,
        sensorDao,
        channelEffectiveTime,
        channelEndTime);

    // store the built derived channel using record type, record id and wfid
    stationDefinitionIdUtility.storeWfidRecordIdChannelMapping(associatedRecordType, associatedRecordId, wfid, built);

    String key = Channel.class.getSimpleName().concat(built.getName());
    NavigableSet<Instant> versionTimes = versionCache.retrieveVersionEffectiveTimesByEntityId(key);
    if (versionTimes == null) {
      versionTimes = new TreeSet<>();
    }

    // cache versions times using built dervied channel
    versionTimes.add(built.getEffectiveAt().orElseThrow());
    versionCache.cacheVersionEffectiveTimesByEntityId(key, versionTimes);

    RangeMap<Instant, Object> versions = versionCache.retrieveVersionsByEntityIdAndTimeRangeMap(key);

    if (versions == null) {
      versions = TreeRangeMap.create();
    }

    // cache derived channel versions
    Range<Instant> range = built.getEffectiveUntil().isPresent()
      ? Range.closedOpen(built.getEffectiveAt().orElseThrow(), built.getEffectiveUntil().orElseThrow())
      : Range.atLeast(built.getEffectiveAt().orElseThrow());
    versions.put(range, built);
    versionCache.cacheVersionsByEntityIdAndTime(key, versions);

    return built;
  }
}

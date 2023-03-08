package gms.shared.stationdefinition.testfixtures;

import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelBandType;
import gms.shared.stationdefinition.coi.channel.ChannelDataType;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.ChannelInstrumentType;
import gms.shared.stationdefinition.coi.channel.ChannelNameUtilities;
import gms.shared.stationdefinition.coi.channel.ChannelOrientationType;
import gms.shared.stationdefinition.coi.channel.ChannelProcessingMetadataType;
import gms.shared.stationdefinition.coi.channel.ChannelTypes;
import gms.shared.stationdefinition.coi.channel.ChannelTypesParser;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.channel.Orientation;
import gms.shared.stationdefinition.coi.channel.RelativePosition;
import gms.shared.stationdefinition.coi.channel.RelativePositionChannelPair;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.NetworkStationTimeKey;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SensorKey;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.Band;
import gms.shared.stationdefinition.dao.css.enums.ChannelType;
import gms.shared.stationdefinition.dao.css.enums.Digital;
import gms.shared.stationdefinition.dao.css.enums.StaType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultCoiTestFixtures {

  private DefaultCoiTestFixtures() {
  }

  public static final String STA1 = "STA01";
  public static final String STA_1 = "STA1";
  public static final String CHAN_1 = "BHZ";
  public static final String CHAN1 = "BHE";
  public static final Instant START = Instant.parse("2008-11-10T17:26:44Z");
  public static final Instant START_2 = Instant.parse("2000-04-01T00:00:00Z");
  public static final Instant GAP_1 = Instant.parse("2010-12-11T15:32:33Z");
  public static final Instant BETWEEN = Instant.parse("2012-12-11T15:32:33Z");
  public static final Instant GAP_2 = Instant.parse("2016-12-11T15:32:33Z");
  public static final Instant END = Instant.parse("2019-07-23T10:42:29Z");
  public static final Instant END_2 = Instant.parse("2020-04-01T00:00:00Z");
  public static final Instant LDATE = Instant.parse("2015-04-11T12:00:00Z");
  public static final long DEFAULT_CHANID = 2342;
  public static final long DEFAULT_INID = 562;

  public static final long DEFAULT_WFID = 908;
  public static final String DEFAULT_STATION_NAME = "Hakha,_Myanmar";
  public static final StaType DEFAULT_STATION_TYPE = StaType.SINGLE_STATION;
  public static final ChannelType DEFAULT_CHANNEL_TYPE = ChannelType.B;
  public static final String DEFAULT_REF_STA = "REFSTA1";
  public static final Double DEFAULT_DOUBLE = 834.0;
  public static final int DEFAULT_INT = 56;
  public static final String DEFAULT_STRING = "a default String";
  public static final Band DEFAULT_BAND = Band.SHORT_BAND;
  public static final Digital DEFAULT_DIGITAL = Digital.DIGITAL;

  public static SiteDao getDefaultSiteDao() {
    var defaultSiteKey = new SiteKey(STA_1, START);
    return new SiteDao(defaultSiteKey,
      END, DEFAULT_DOUBLE, DEFAULT_DOUBLE, DEFAULT_DOUBLE,
      DEFAULT_STATION_NAME, DEFAULT_STATION_TYPE,
      DEFAULT_REF_STA, DEFAULT_DOUBLE, DEFAULT_DOUBLE, START);
  }

  public static SiteChanDao getDefaultSiteChanDao() {
    var defaultSiteChanKey = new SiteChanKey(STA_1, CHAN_1, START);
    var siteChanDao = new SiteChanDao();
    siteChanDao.setId(defaultSiteChanKey);
    siteChanDao.setChannelId(DEFAULT_CHANID);
    siteChanDao.setOffDate(END);
    siteChanDao.setChannelType(DEFAULT_CHANNEL_TYPE);
    siteChanDao.setEmplacementDepth(DEFAULT_DOUBLE);
    siteChanDao.setHorizontalAngle(DEFAULT_DOUBLE);
    siteChanDao.setVerticalAngle(DEFAULT_DOUBLE);
    siteChanDao.setChannelDescription(DEFAULT_STRING);
    siteChanDao.setLoadDate(START);

    return siteChanDao;
  }

  public static SiteChanDao getDefaultSiteChanDaoFromSiteDao(SiteDao siteDao) {

    var defaultSiteChanKey = new SiteChanKey(siteDao.getId().getStationCode(), CHAN_1, siteDao.getId().getOnDate());
    var siteChanDao = new SiteChanDao();
    siteChanDao.setId(defaultSiteChanKey);
    siteChanDao.setChannelId(DEFAULT_CHANID);
    siteChanDao.setOffDate(siteDao.getOffDate());
    siteChanDao.setChannelType(DEFAULT_CHANNEL_TYPE);
    siteChanDao.setEmplacementDepth(DEFAULT_DOUBLE);
    siteChanDao.setHorizontalAngle(DEFAULT_DOUBLE);
    siteChanDao.setVerticalAngle(DEFAULT_DOUBLE);
    siteChanDao.setChannelDescription(DEFAULT_STRING);
    siteChanDao.setLoadDate(siteDao.getOffDate());

    return siteChanDao;
  }

  public static InstrumentDao getDefaultInstrumentDao() {
    var instrumentDao = new InstrumentDao();
    instrumentDao.setInstrumentId(DEFAULT_INID);
    instrumentDao.setInstrumentName(DEFAULT_STRING);
    instrumentDao.setBand(DEFAULT_BAND);
    instrumentDao.setDigital(DEFAULT_DIGITAL);
    instrumentDao.setSampleRate(DEFAULT_DOUBLE);
    instrumentDao.setNominalCalibrationFactor(DEFAULT_DOUBLE);
    instrumentDao.setNominalCalibrationPeriod(DEFAULT_DOUBLE);
    instrumentDao.setDirectory(DEFAULT_STRING);
    instrumentDao.setDataFile(DEFAULT_STRING);
    instrumentDao.setResponseType(DEFAULT_STRING);
    instrumentDao.setLoadDate(START);

    return instrumentDao;
  }

  public static SensorDao getDefaultSensorDao() {

    var defaultSensorKey = new SensorKey(STA_1, CHAN_1, START, END);
    var sensor = new SensorDao();
    sensor.setSensorKey(defaultSensorKey);
    sensor.setEndTime(END);
    sensor.setInstrument(getDefaultInstrumentDao());
    sensor.setChannelId(DEFAULT_CHANID);
    sensor.setjDate(START);
    sensor.setCalibrationRatio(DEFAULT_DOUBLE);
    sensor.setCalibrationPeriod(DEFAULT_DOUBLE);
    sensor.settShift(DEFAULT_DOUBLE);
    sensor.setSnapshotIndicator(DEFAULT_STRING);
    sensor.setLoadDate(START);
    return sensor;
  }

  public static WfdiscDao getDefaultWfdisc() {

    var wfdisc = new WfdiscDao();
    wfdisc.setId(DEFAULT_WFID);
    wfdisc.setStationCode(STA_1);
    wfdisc.setChannelCode(CHAN_1);
    wfdisc.setTime(START);
    wfdisc.setChannelId(DEFAULT_CHANID);
    wfdisc.setjDate(START);
    wfdisc.setEndTime(END);
    wfdisc.setNsamp(DEFAULT_INT);
    wfdisc.setSampRate(DEFAULT_DOUBLE);
    wfdisc.setCalib(DEFAULT_DOUBLE);
    wfdisc.setCalper(DEFAULT_DOUBLE);
    wfdisc.setInsType(DEFAULT_STRING);
    return wfdisc;
  }

  public static Channel getDefaultChannelFromSiteAndSiteChan(SiteDao siteDao, SiteChanDao siteChanDao) {

    var chanName = siteChanDao.getId().getChannelCode();
    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(chanName);

    if (channelTypesOptional.isEmpty()) {
      throw new IllegalStateException("Could create test channel for Dao, chanName provided can not be parsed");
    }

    var channelTypes = channelTypesOptional.orElse(null);
    var orientation = Orientation.from(siteChanDao.getHorizontalAngle(),
      siteChanDao.getVerticalAngle());
    var groupName = siteChanDao.getId().getStationCode();

    var data = Channel.Data.builder()
      .setCanonicalName(DEFAULT_STRING)
      .setEffectiveUntil(siteChanDao.getOffDate())
      .setDescription(DEFAULT_STRING)
      .setStation(getDefaultStationForSite(siteDao))
      .setChannelDataType(channelTypes.getDataType())
      .setChannelBandType(channelTypes.getBandType())
      .setChannelInstrumentType(channelTypes.getInstrumentType())
      .setChannelOrientationType(channelTypes.getOrientationType())
      .setChannelOrientationCode(channelTypes.getOrientationCode())
      .setUnits(Units.determineUnits(channelTypes.getDataType()))
      .setNominalSampleRateHz(DEFAULT_DOUBLE)
      .setLocation(getDefaultLocation())
      .setOrientationAngles(orientation)
      .setConfiguredInputs(List.of())
      .setProcessingDefinition(Map.of())
      .setProcessingMetadata(Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, groupName))
      .setResponse(Optional.empty())
      .build();


    return Channel.builder()
      .setName(ChannelNameUtilities.createShortName(siteDao.getReferenceStation(),
        siteDao.getId().getStationCode(),
        siteChanDao.getId().getChannelCode()))
      .setEffectiveAt(siteChanDao.getId().getOnDate())
      .setData(data)
      .build();
  }

  public static ChannelGroup getDefaultChannelGroupFromSiteAndChannel(SiteDao siteDao, Channel channel) {

    var data = ChannelGroup.Data.builder()
      .setChannels(List.of(channel))
      .setEffectiveUntil(siteDao.getOffDate())
      .setDescription(DEFAULT_STRING)
      .setLocation(getDefaultLocation())
      .setType(ChannelGroup.ChannelGroupType.PROCESSING_GROUP)
      .setStation(getDefaultStationForSite(siteDao))
      .setEffectiveAtUpdatedByResponse(Optional.of(false))
      .setEffectiveUntilUpdatedByResponse(Optional.of(false))
      .build();

    return ChannelGroup.builder()
      .setName(siteDao.getId().getStationCode())
      .setEffectiveAt(siteDao.getId().getOnDate())
      .setData(data)
      .build();
  }

  public static Station getDefaultStationFromChannelGroup(ChannelGroup channelGroup, SiteDao siteDao) {

    List<RelativePositionChannelPair> pairs = channelGroup.getChannels().stream()
      .map(channel -> RelativePositionChannelPair.create(RelativePosition.from(DEFAULT_DOUBLE, DEFAULT_DOUBLE, DEFAULT_DOUBLE), channel))
      .collect(Collectors.toList());

    var data = Station.Data.builder()
      .setType(StationType.HYDROACOUSTIC)
      .setDescription(DEFAULT_STRING)
      .setRelativePositionChannelPairs(pairs)
      .setLocation(getDefaultLocation())
      .setEffectiveUntil(channelGroup.getEffectiveUntil())
      .setChannelGroups(List.of(channelGroup))
      .setAllRawChannels(channelGroup.getChannels())
      .build();

    var startTime = siteDao.getId().getOnDate();
    var optionalEffectiveAt = channelGroup.getEffectiveAt();
    if (optionalEffectiveAt.isPresent()) {
      startTime = optionalEffectiveAt.get();
    }

    return Station.builder()
      .setName(siteDao.getReferenceStation())
      .setEffectiveAt(startTime)
      .setData(data)
      .build();
  }

  public static Station getDefaultStationForSite(SiteDao siteDao) {

    return Station.builder()
      .setName(siteDao.getReferenceStation())
      .setEffectiveAt(siteDao.getId().getOnDate())
      .build();
  }

  public static Station getDefaultStation() {


    return Station.builder()
      .setName(DEFAULT_STRING)
      .setEffectiveAt(START)
      .build();
  }

  public static Station getDefaultStation(String name, Instant effectiveAt) {

    var channel = getDefaultChannel();
    List<RelativePositionChannelPair> pairs = List.of(
      RelativePositionChannelPair.create(RelativePosition.from(DEFAULT_DOUBLE, DEFAULT_DOUBLE, DEFAULT_DOUBLE), channel));

    var data = Station.Data.builder()
      .setType(StationType.HYDROACOUSTIC)
      .setDescription(DEFAULT_STRING)
      .setRelativePositionChannelPairs(pairs)
      .setLocation(getDefaultLocation())
      .setEffectiveUntil(Instant.MAX)
      .setChannelGroups(List.of(getDefaultChannelGroupFromSiteAndChannel(getDefaultSiteDao(), channel)))
      .setAllRawChannels(List.of(channel))
      .build();

    return Station.builder()
      .setName(name)
      .setEffectiveAt(effectiveAt)
      .setData(data)
      .build();
  }

  public static Location getDefaultLocation() {

    return Location.from(DEFAULT_DOUBLE,
      DEFAULT_DOUBLE,
      DEFAULT_DOUBLE,
      DEFAULT_DOUBLE);
  }

  public static Channel getDefaultChannel(){

    var data = Channel.Data.builder()
      .setCanonicalName(DEFAULT_STRING)
      .setDescription(DEFAULT_STRING)
      .setStation(Station.createEntityReference(getDefaultStation().getName()))
      .setChannelDataType(ChannelDataType.DIAGNOSTIC_SOH)
      .setChannelBandType(ChannelBandType.BROADBAND)
      .setChannelInstrumentType(ChannelInstrumentType.HIGH_GAIN_SEISMOMETER)
      .setChannelOrientationType(ChannelOrientationType.VERTICAL)
      .setChannelOrientationCode('Z')
      .setUnits(Units.NANOMETERS)
      .setNominalSampleRateHz(DEFAULT_DOUBLE)
      .setLocation(getDefaultLocation())
      .setOrientationAngles(Orientation.from(
        DEFAULT_DOUBLE,
        DEFAULT_DOUBLE))
      .setConfiguredInputs(List.of())
      .setProcessingDefinition(Map.of())
      .setProcessingMetadata(Map.of(ChannelProcessingMetadataType.CHANNEL_GROUP, DEFAULT_STRING))
      .setResponse(Optional.empty())
      .setEffectiveUntil(END)
      .build();

    return Channel.builder()
      .setName(DEFAULT_STRING)
      .setEffectiveAt(START)
      .setData(data)
      .build();
  }

  public static AffiliationDao getDefaultAffiliationDao(String net, String sta, Instant time, Instant endTime){

    var networkStationTimeKey = new NetworkStationTimeKey(net, sta, time);
    var affiliationDao = new AffiliationDao();
    affiliationDao.setNetworkStationTimeKey(networkStationTimeKey);
    affiliationDao.setEndTime(endTime);

    return affiliationDao;
  }
}

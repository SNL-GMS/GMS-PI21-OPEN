package gms.shared.stationdefinition.converter.util.assemblers;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedSet;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.Location;
import gms.shared.stationdefinition.coi.channel.RelativePosition;
import gms.shared.stationdefinition.coi.channel.RelativePositionChannelPair;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationType;
import gms.shared.stationdefinition.converter.DaoStationConverter;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters;
import gms.shared.stationdefinition.testfixtures.DefaultCoiTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_REF_11;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_REF_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_REF_11;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createSiteChanDao;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.createSiteDao;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getDeepCopySiteDao;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHANID_1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.CHAN_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE2;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.REFERENCE_STATION;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STA1_PARAM_MAP;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.START_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.STATION_TYPE_1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP_STA01;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_TWO;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STA01_STA01_BHE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.createTestChannelGroupDataTwo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@ExtendWith(MockitoExtension.class)
class StationAssemblerTest {

  @Mock
  private DaoStationConverter stationConverter;

  private StationAssembler assembler;

  List<Channel> channelsByStaChan = List.of(CHANNEL.toBuilder()
    .setName(STA01_STA01_BHE)
    .setEffectiveAt(SITE_DAO_REF_11.getId().getOnDate()).build());

  List<ChannelGroup> channelGroupsByStaChan = List.of(CHANNEL_GROUP_STA01.toBuilder()
    .setEffectiveAt(SITE_DAO_REF_11.getId().getOnDate()).build());

  Map<String, SiteChanKey> channelNameSiteChanKeyMap = channelsByStaChan.stream()
    .collect(Collectors.toMap(Channel::getName, Functions.compose(ChannelGroupAssemblerTest::getCssKeyFromName, Channel::getName)));

  @BeforeEach
  public void beforeEach() {
    assembler = new StationAssembler(stationConverter);
  }

  @Test
  void testBuildAll() {
    List<SiteDao> siteList = List.of(SITE_DAO_REF_1, SITE_DAO_REF_11);
    List<SiteChanDao> siteChanList = List.of(SITE_CHAN_DAO_REF_11);

    Station stationWithOnDateEffectiveAt = STATION.toBuilder().setEffectiveAt(
      AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE)).build();

    doReturn(stationWithOnDateEffectiveAt).when(stationConverter).convert(
      Mockito.any(), Mockito.any(), eq(siteList),
      eq(siteChanList), Mockito.any(), Mockito.any());

    //createTestChannelGroupStaData
    List<Station> stations = assembler.buildAllForTime(
      siteList, siteChanList, channelGroupsByStaChan, channelsByStaChan,
      AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE), channelNameSiteChanKeyMap);
    assertNotNull(stations);
    assertEquals(1, stations.size());
    assertTrue(stations.contains(stationWithOnDateEffectiveAt));

    verify(stationConverter, times(1)).convert(Mockito.any(), Mockito.any(), eq(siteList),
      eq(siteChanList), Mockito.any(), Mockito.any());
    verifyNoMoreInteractions(stationConverter);
  }

  @Test
  void testBuildAllTimeRange() {

    SiteDao testSiteDao = getDeepCopySiteDao(SITE_DAO_REF_11);
    testSiteDao.setOffDate(Instant.MAX);
    List<SiteDao> siteList = List.of(SITE_DAO_REF_1, testSiteDao);
    List<SiteChanDao> siteChanList = List.of(SITE_CHAN_DAO_REF_11);
    List<String> stationNames = siteList.stream()
      .map(SiteDao::getReferenceStation)
      .distinct()
      .collect(Collectors.toList());

    Station stationWithOnDateEffectiveAt = STATION.toBuilder()
      .setEffectiveAt(AssemblerUtils.effectiveAtNoonOffset.apply(ONDATE)).build();

    doReturn(stationWithOnDateEffectiveAt).when(stationConverter).convert(
      Mockito.any(), Mockito.any(), eq(siteList),
      eq(siteChanList), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(siteList, siteChanList, channelGroupsByStaChan, channelsByStaChan,
      ONDATE, OFFDATE, channelNameSiteChanKeyMap);
    assertNotNull(stations);
    assertEquals(1, stations.size());

    //stationWithOnDateEffectiveAt has version refs for rawChannels, so just compare versionRef of station
    assertEquals(Station.createVersionReference(
        stationWithOnDateEffectiveAt.getName(), stationWithOnDateEffectiveAt.getEffectiveAt().get()),
      (Station.createVersionReference(stations.get(0).getName(), stations.get(0).getEffectiveAt().get())));

    verify(stationConverter, times(1)).convert(
      Mockito.any(), Mockito.any(), eq(siteList), eq(siteChanList), Mockito.any(), Mockito.any());
    //verifyNoMoreInteractions(stationConverter);
  }

  @Test
  void testBuildAllTimeRangeOutOfBounds() {
    List<SiteDao> siteList = List.of(SITE_DAO_REF_1, SITE_DAO_REF_11);
    List<SiteChanDao> siteChanList = List.of(SITE_CHAN_DAO_REF_11);
    List<String> stationNames = siteList.stream()
      .map(SiteDao::getReferenceStation)
      .distinct()
      .collect(Collectors.toList());

    List<Station> stations = assembler.buildAllForTimeRange(siteList, siteChanList, channelGroupsByStaChan, channelsByStaChan,
      START_TIME, START_TIME.plusSeconds(30), channelNameSiteChanKeyMap);
    assertNotNull(stations);
    assertEquals(0, stations.size());
  }

  @Test
  void testBuildAllTimeRange_convertFailed() {
    List<SiteDao> siteList = List.of(createSiteDao(STA1, ONDATE, OFFDATE2,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION));
    List<SiteChanDao> siteChanList = List.of(SITE_CHAN_DAO_REF_11);

    doThrow(new IllegalStateException("bad converter")).when(stationConverter).convert(eq(ONDATE), eq(OFFDATE),
      eq(siteList), eq(siteChanList), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(siteList, siteChanList, channelGroupsByStaChan, channelsByStaChan,
      ONDATE, OFFDATE2, channelNameSiteChanKeyMap);
    assertNotNull(stations);
    assertEquals(0, stations.size());
  }

  @Test
  void testNewChannelsChannelGroupsTriggeringNewVersion() {

    var siteDao = DefaultCoiTestFixtures.getDefaultSiteDao();
    var siteChanDao = DefaultCoiTestFixtures.getDefaultSiteChanDaoFromSiteDao(siteDao);
    var newChannel = DefaultCoiTestFixtures.getDefaultChannelFromSiteAndSiteChan(siteDao, siteChanDao);
    newChannel = (Channel) newChannel.setEffectiveAt(DefaultCoiTestFixtures.BETWEEN);
    var newChannelGroup = DefaultCoiTestFixtures.getDefaultChannelGroupFromSiteAndChannel(siteDao, newChannel);
    newChannelGroup.setEffectiveAt(DefaultCoiTestFixtures.BETWEEN);

    var newStation = DefaultCoiTestFixtures.getDefaultStationFromChannelGroup(newChannelGroup, siteDao);
    HashMap<String, SiteChanKey> mapChannelNameSiteChanKey = new HashMap();
    mapChannelNameSiteChanKey.put(newChannel.getName(), siteChanDao.getId());

    doReturn(newStation).when(stationConverter).convert(eq(DefaultCoiTestFixtures.BETWEEN),
      eq(DefaultCoiTestFixtures.END),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(List.of(siteDao), List.of(siteChanDao),
      List.of(newChannelGroup), List.of(newChannel),
      DefaultCoiTestFixtures.START, DefaultCoiTestFixtures.END, mapChannelNameSiteChanKey);
    assertNotNull(stations);
    assertEquals(1, stations.size());
  }

  @Test
  void testSiteAndSiteChansStartWithGap() {

    var siteDao = DefaultCoiTestFixtures.getDefaultSiteDao();
    siteDao.setOffDate(DefaultCoiTestFixtures.GAP_1);
    var siteChanDao = DefaultCoiTestFixtures.getDefaultSiteChanDaoFromSiteDao(siteDao);

    var siteDao2 = DefaultCoiTestFixtures.getDefaultSiteDao();
    siteDao2.getId().setOnDate(DefaultCoiTestFixtures.GAP_2);
    var siteChanDao2 = DefaultCoiTestFixtures.getDefaultSiteChanDaoFromSiteDao(siteDao2);

    var newChannel = DefaultCoiTestFixtures.getDefaultChannelFromSiteAndSiteChan(siteDao, siteChanDao);
    newChannel = (Channel) newChannel.setEffectiveAt(DefaultCoiTestFixtures.START);
    newChannel = (Channel) newChannel.setEffectiveUntil(DefaultCoiTestFixtures.END);

    var newChannelGroup = DefaultCoiTestFixtures.getDefaultChannelGroupFromSiteAndChannel(siteDao, newChannel);
    newChannelGroup = (ChannelGroup) newChannelGroup.setEffectiveAt(DefaultCoiTestFixtures.START);
    newChannelGroup = (ChannelGroup) newChannelGroup.setEffectiveUntil(DefaultCoiTestFixtures.END);

    var newStation = DefaultCoiTestFixtures.getDefaultStationFromChannelGroup(newChannelGroup, siteDao);
    var newStation2 = DefaultCoiTestFixtures.getDefaultStationFromChannelGroup(newChannelGroup, siteDao);
    newStation.setEffectiveAt(DefaultCoiTestFixtures.START);
    newStation.setEffectiveUntil(DefaultCoiTestFixtures.BETWEEN);
    newStation2.setEffectiveAt(DefaultCoiTestFixtures.BETWEEN);
    newStation2.setEffectiveUntil(DefaultCoiTestFixtures.END);

    HashMap<String, SiteChanKey> mapChannelNameSiteChanKey = new HashMap();
    mapChannelNameSiteChanKey.put(newChannel.getName(), siteChanDao.getId());

    doReturn(newStation).when(stationConverter).convert(eq(DefaultCoiTestFixtures.START),
      eq(DefaultCoiTestFixtures.GAP_1), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    doReturn(newStation2).when(stationConverter).convert(eq(DefaultCoiTestFixtures.GAP_2),
      eq(DefaultCoiTestFixtures.END),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(List.of(siteDao, siteDao2), List.of(siteChanDao, siteChanDao2),
      List.of(newChannelGroup), List.of(newChannel),
      DefaultCoiTestFixtures.START, DefaultCoiTestFixtures.END, mapChannelNameSiteChanKey);
    assertNotNull(stations);
    assertEquals(2, stations.size());
  }

  @Test
  void testSiteAndSiteChansStartOutsideTimeRange() {

    var siteDao = DefaultCoiTestFixtures.getDefaultSiteDao();
    siteDao.setOffDate(DefaultCoiTestFixtures.BETWEEN);
    siteDao.setDegreesEast(1337);
    var siteChanDao = DefaultCoiTestFixtures.getDefaultSiteChanDaoFromSiteDao(siteDao);

    var siteDao2 = DefaultCoiTestFixtures.getDefaultSiteDao();
    siteDao2.getId().setOnDate(DefaultCoiTestFixtures.BETWEEN);
    var siteChanDao2 = DefaultCoiTestFixtures.getDefaultSiteChanDaoFromSiteDao(siteDao2);

    var newChannel = DefaultCoiTestFixtures.getDefaultChannelFromSiteAndSiteChan(siteDao, siteChanDao);
    newChannel = (Channel) newChannel.setEffectiveAt(DefaultCoiTestFixtures.START);
    newChannel = (Channel) newChannel.setEffectiveUntil(DefaultCoiTestFixtures.END);

    var newChannelGroup = DefaultCoiTestFixtures.getDefaultChannelGroupFromSiteAndChannel(siteDao, newChannel);
    newChannelGroup = (ChannelGroup) newChannelGroup.setEffectiveAt(DefaultCoiTestFixtures.START);
    newChannelGroup = (ChannelGroup) newChannelGroup.setEffectiveUntil(DefaultCoiTestFixtures.END);

    var newStation = DefaultCoiTestFixtures.getDefaultStationFromChannelGroup(newChannelGroup, siteDao);
    var newStation2 = DefaultCoiTestFixtures.getDefaultStationFromChannelGroup(newChannelGroup, siteDao);
    newStation.setEffectiveAt(DefaultCoiTestFixtures.START);
    newStation.setEffectiveUntil(DefaultCoiTestFixtures.BETWEEN);
    newStation2.setEffectiveAt(DefaultCoiTestFixtures.BETWEEN);
    newStation2.setEffectiveUntil(DefaultCoiTestFixtures.END);

    HashMap<String, SiteChanKey> mapChannelNameSiteChanKey = new HashMap();
    mapChannelNameSiteChanKey.put(newChannel.getName(), siteChanDao.getId());

    doReturn(newStation).when(stationConverter).convert(eq(DefaultCoiTestFixtures.START),
      eq(AssemblerUtils.effectiveAtNoonOffset.apply(DefaultCoiTestFixtures.BETWEEN).minusMillis(1)),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    doReturn(newStation2).when(stationConverter).convert(
      eq(AssemblerUtils.effectiveAtNoonOffset.apply(DefaultCoiTestFixtures.BETWEEN)),
      eq(DefaultCoiTestFixtures.END),
      Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(List.of(siteDao, siteDao2), List.of(siteChanDao, siteChanDao2),
      List.of(newChannelGroup), List.of(newChannel),
      DefaultCoiTestFixtures.BETWEEN.minus(Duration.ofDays(300)), DefaultCoiTestFixtures.END, mapChannelNameSiteChanKey);
    assertNotNull(stations);
    assertEquals(2, stations.size());
  }

  @Test
  @Disabled("needs to be reworked after refactor")
  void testBuildAllTimeRange_ChannelGroupTriggeringNewVersion() {
    SiteDao siteDao = createSiteDao(STA1, ONDATE, CssDaoAndCoiParameters.OFFDATE2,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    List<SiteDao> siteList = List.of(siteDao);
    var SITE_CHAN_DAO_DIFFERENT_CHANNEL_GROUP = createSiteChanDao(STA1, CHAN2, ONDATE, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_1);
    List<SiteChanDao> siteChanList = List.of(SITE_CHAN_DAO_1, SITE_CHAN_DAO_DIFFERENT_CHANNEL_GROUP);

    List<String> stationNames = siteList.stream()
      .map(SiteDao::getReferenceStation)
      .distinct()
      .collect(Collectors.toList());

    var stationWithOnDateEffectiveAt = STATION.toBuilder().setEffectiveAt(ONDATE).build();

    doReturn(stationWithOnDateEffectiveAt).when(stationConverter).convert(eq(ONDATE), eq(OFFDATE), eq(siteList),
      Mockito.any(), Mockito.any(), Mockito.any());
    doReturn(getStationTwo()).when(stationConverter).convert(eq(OFFDATE), eq(OFFDATE2), eq(siteList),
      Mockito.any(), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(siteList, siteChanList, channelGroupsByStaChan, channelsByStaChan,
      ONDATE, OFFDATE2, channelNameSiteChanKeyMap);
    assertNotNull(stations);
    assertEquals(2, stations.size());
  }

  @Test
  @Disabled("needs to be reworked after refactor")
  void testBuildAllTimeRangeWithRawChannelsTriggeringNewVersion() {
    SiteDao siteDao = createSiteDao(STA1, ONDATE, CssDaoAndCoiParameters.OFFDATE2,
      STA1_PARAM_MAP, STATION_TYPE_1, REFERENCE_STATION);
    List<SiteDao> siteList = List.of(siteDao);
    var SITE_CHAN_DAO_DIFFERENT_CHANNEL_GROUP = createSiteChanDao(STA1, CHAN2, ONDATE, OFFDATE2, CHAN_PARAM_MAP,
      CHANID_1);
    List<SiteChanDao> siteChanList = List.of(SITE_CHAN_DAO_1, SITE_CHAN_DAO_DIFFERENT_CHANNEL_GROUP);

    List<String> stationNames = siteList.stream()
      .map(SiteDao::getReferenceStation)
      .distinct()
      .collect(Collectors.toList());

    var stationWithOnDateEffectiveAt = STATION.toBuilder().setEffectiveAt(ONDATE).build();
    var stationWithDifferentRawChannels = STATION.toBuilder().setEffectiveAt(ONDATE).setData(
        Station.Data.builder()
          .setType(StationType.HYDROACOUSTIC)
          .setDescription("This is a test station")
          .setRelativePositionChannelPairs(STATION.getRelativePositionsByChannel().entrySet().stream()
            .map(entry -> RelativePositionChannelPair.create(entry.getValue(), entry.getKey()))
            .collect(Collectors.toList()))
          .setLocation(STATION.getLocation())
          .setEffectiveUntil(CssDaoAndCoiParameters.END_TIME)
          .setChannelGroups(ImmutableSortedSet.of(CHANNEL_GROUP))
          .setAllRawChannels(ImmutableSortedSet.of(CHANNEL))
          .build())
      .build();

    doReturn(stationWithOnDateEffectiveAt).when(stationConverter).convert(eq(ONDATE), eq(OFFDATE), eq(siteList),
      Mockito.any(), Mockito.any(), Mockito.any());
    doReturn(stationWithDifferentRawChannels).when(stationConverter).convert(eq(OFFDATE), eq(OFFDATE2), eq(siteList),
      Mockito.any(), Mockito.any(), Mockito.any());

    List<Station> stations = assembler.buildAllForTimeRange(siteList,
      siteChanList, channelGroupsByStaChan, channelsByStaChan, ONDATE, OFFDATE2, channelNameSiteChanKeyMap);
    assertNotNull(stations);
    assertEquals(2, stations.size());
  }

  private Station getStationTwo() {
    return Station.builder()
      .setName(REFERENCE_STATION)
      .setEffectiveAt(OFFDATE)
      .setData(Station.Data.builder()
        .setType(StationType.HYDROACOUSTIC)
        .setDescription("This is a test station")
        .setRelativePositionChannelPairs(List.of(
          RelativePositionChannelPair.create(RelativePosition.from(50.0, 5.0, 10.0), CHANNEL),
          RelativePositionChannelPair.create(RelativePosition.from(50.0, 5.0, 10.0), CHANNEL_TWO)))
        .setLocation(Location.from(35.647, 100.0, 50.0, 10.0))
        .setEffectiveUntil(CssDaoAndCoiParameters.END_TIME)
        .setChannelGroups(ImmutableSortedSet.of(ChannelGroup.builder()
          .setName("Test Channel Group Two")
          .setEffectiveAt(Instant.EPOCH)
          .setData(createTestChannelGroupDataTwo())
          .build()))
        .setAllRawChannels(ImmutableSortedSet.of(CHANNEL_TWO))
        .build())
      .build();
  }
}

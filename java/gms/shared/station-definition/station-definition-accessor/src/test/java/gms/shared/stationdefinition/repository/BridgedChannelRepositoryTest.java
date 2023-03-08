package gms.shared.stationdefinition.repository;

import gms.shared.stationdefinition.cache.VersionCache;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.converter.util.StationDefinitionDataHolder;
import gms.shared.stationdefinition.converter.util.assemblers.ChannelAssembler;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.dao.util.SiteAndSiteChanUtility;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_CHAN_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.SITE_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.WFDISC_TEST_DAO_1;
import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestSiteAndSurroundingDates;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.EVENT_BEAM_CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.FK_BEAM_CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RAW_CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.RESPONSE_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedChannelRepositoryTest {

  private final List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
  private final List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
  private final List<SensorDao> sensorDaos = CSSDaoTestFixtures.getTestSensorDaos();
  private final List<WfdiscDao> wfdiscDaos = CSSDaoTestFixtures.getTestWfdiscDaos();

  @Mock
  private BeamDatabaseConnector beamDatabaseConnector;

  @Mock
  private SiteDatabaseConnector siteDatabaseConnector;

  @Mock
  private SiteChanDatabaseConnector siteChanDatabaseConnector;

  @Mock
  private SensorDatabaseConnector sensorDatabaseConnector;

  @Mock
  private InstrumentDatabaseConnector instrumentDatabaseConnector;

  @Mock
  private WfdiscDatabaseConnector wfdiscDatabaseConnector;

  @Mock
  private ChannelAssembler channelAssembler;

  @Mock
  private StationDefinitionIdUtility stationDefinitionIdUtility;

  @Mock
  private VersionCache versionCache;

  @Mock
  private BridgedResponseRepository responseRepository;

  private BridgedChannelRepository repository;
  private List<String> channelNames;

  @BeforeEach
  void setUp() {
    repository = new BridgedChannelRepository(
      beamDatabaseConnector,
      siteDatabaseConnector,
      siteChanDatabaseConnector,
      sensorDatabaseConnector,
      wfdiscDatabaseConnector,
      channelAssembler,
      stationDefinitionIdUtility,
      versionCache,
      responseRepository);

    channelNames = List
      .of("REF.STA.NO1", "REF.STA.NO2", "REF.STA.NO3", "REF.STA.NO4", "REF.STA.NO5",
        "REF.STA.NO6");
  }

  private void verifyNoMoreMockInteractions() {
    verifyNoMoreInteractions(siteChanDatabaseConnector, siteDatabaseConnector, sensorDatabaseConnector,
      instrumentDatabaseConnector, wfdiscDatabaseConnector, channelAssembler);
  }

  @Test
  void testFindChannelsByNameAndTime() {
    Instant startTime = Instant.now();
    Instant endTime = startTime.plusSeconds(1);

    Pair<List<SiteDao>, List<SiteChanDao>> siteAndSiteChans = mockConnectorRepositoryInterfactionTime();
    var updatedSiteDaos = siteAndSiteChans.getLeft();
    var updatedSiteChanDaos = siteAndSiteChans.getRight();

    Instant queryEndTime = updatedSiteChanDaos.stream()
      .map(SiteChanDao::getOffDate)
      .max(Instant::compareTo)
      .orElse(Instant.now());

    when(wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(any(), any(), any()))
      .thenReturn(wfdiscDaos);

    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(any(), any(), any())).thenReturn(sensorDaos);

    when(channelAssembler
      .buildAllForTime(any(), Mockito.argThat(t -> t.containsAll(updatedSiteDaos)),
        Mockito.argThat(t -> t.containsAll(updatedSiteChanDaos)), Mockito.argThat(t -> t.containsAll(sensorDaos)),
        any(),
        any()))
      .thenReturn(
        List.of(
          CHANNEL.toBuilder().setName(channelNames.get(0)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(1)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(2)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(3)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(4)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(5)).build())
      );
    final List<Channel> result = repository.findChannelsByNameAndTime(channelNames, startTime);

    assertNotNull(result);
    assertEquals(channelNames.size(), result.size());
    verify(siteDatabaseConnector, times(1)).findSitesAndSurroundingDatesByStaCodeAndTimeRange(any(), any(), any());
    verify(siteChanDatabaseConnector, times(1))
      .findSiteChansAndSurroundingDatesByKeysAndTime(any(), any());
    verify(wfdiscDatabaseConnector, times(1)).findWfdiscsByNameAndTimeRange(any(), any(), any());
    verify(sensorDatabaseConnector, times(1)).findSensorsByKeyAndTimeRange(any(), eq(startTime), eq(queryEndTime));
    verify(channelAssembler, times(1))
      .buildAllForTime(eq(startTime), Mockito.argThat(t -> t.containsAll(updatedSiteDaos)),
        Mockito.argThat(t -> t.containsAll(updatedSiteChanDaos)), Mockito.argThat(t -> t.containsAll(sensorDaos)),
        any(),
        any());
    verifyNoMoreMockInteractions();
  }

  @Test
  void testFindChannelsByNameAndTimeRange() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plus(Duration.ofSeconds(5));

    Pair<List<SiteDao>, List<SiteChanDao>> siteAndSiteChans = mockConnectorRepositoryInterfactionTimeRange();
    var updatedSiteDaos = siteAndSiteChans.getLeft();
    var updatedSiteChanDaos = siteAndSiteChans.getRight();

    when(wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(any(), any(), any()))
      .thenReturn(wfdiscDaos);
    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(any(), any(), any())).thenReturn(sensorDaos);
    when(channelAssembler
      .buildAllForTimeRange(eq(startTime), eq(endTime), Mockito.argThat(t -> t.containsAll(updatedSiteDaos)),
        Mockito.argThat(t -> t.containsAll(updatedSiteChanDaos)), Mockito.argThat(t -> t.containsAll(sensorDaos)),
        any(),
        any()))
      .thenReturn(
        List.of(
          CHANNEL.toBuilder().setName(channelNames.get(0)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(1)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(2)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(3)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(4)).build(),
          CHANNEL.toBuilder().setName(channelNames.get(5)).build())
      );
    final List<Channel> result = repository.findChannelsByNameAndTimeRange(channelNames, startTime, endTime);

    assertNotNull(result);
    assertEquals(channelNames.size(), result.size());
    verify(siteDatabaseConnector, times(1)).findSitesAndSurroundingDatesByStaCodeAndTimeRange(any(), any(), any());
    verify(siteChanDatabaseConnector, times(1))
      .findSiteChansAndSurroundingDatesByKeysAndTimeRange(any(), eq(startTime), eq(endTime));
    verify(wfdiscDatabaseConnector, times(1)).findWfdiscsByNameAndTimeRange(any(), any(), any());
    verify(sensorDatabaseConnector, times(1)).findSensorsByKeyAndTimeRange(any(), any(), any());
    verify(channelAssembler, times(1))
      .buildAllForTimeRange(eq(startTime), eq(endTime), Mockito.argThat(t -> t.containsAll(updatedSiteDaos)),
        Mockito.argThat(t -> t.containsAll(updatedSiteChanDaos)), Mockito.argThat(t -> t.containsAll(sensorDaos)),
        any(),
        any());
    verifyNoMoreMockInteractions();
  }

  @Test
  void testFindChannelsGivenSiteAndSiteChan() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plus(Duration.ofSeconds(5));

    var stationDefinitionDataHolder
      = new StationDefinitionDataHolder(siteDaos, siteChanDaos, null, null, null);
    var stationDefintionDataHolder2
      = new StationDefinitionDataHolder(siteDaos, siteChanDaos, sensorDaos, null, wfdiscDaos);
    var responses = List.of(RESPONSE_1);
    var channels = List.of(
      CHANNEL.toBuilder().setName(channelNames.get(0)).build(),
      CHANNEL.toBuilder().setName(channelNames.get(1)).build(),
      CHANNEL.toBuilder().setName(channelNames.get(2)).build(),
      CHANNEL.toBuilder().setName(channelNames.get(3)).build(),
      CHANNEL.toBuilder().setName(channelNames.get(4)).build(),
      CHANNEL.toBuilder().setName(channelNames.get(5)).build());
    List<SiteChanKey> listOfSiteChanKeys = siteChanDaos.stream()
      .map(SiteChanDao::getId)
      .collect(Collectors.toList());

    when(sensorDatabaseConnector.findSensorsByKeyAndTimeRange(eq(listOfSiteChanKeys), any(), any())).thenReturn(sensorDaos);
    when(wfdiscDatabaseConnector.findWfdiscsByNameAndTimeRange(eq(listOfSiteChanKeys), any(), any())).thenReturn(wfdiscDaos);

    when(responseRepository.findResponsesGivenSensorAndWfdisc(any(), eq(startTime), eq(endTime)))
      .thenReturn(responses);
    when(channelAssembler.buildAllForTimeRange(eq(startTime), eq(endTime), eq(stationDefintionDataHolder2.getSiteDaos()),
      eq(stationDefintionDataHolder2.getSiteChanDaos()), eq(stationDefintionDataHolder2.getSensorDaos()),
      any(), eq(responses)))
      .thenReturn(channels);

    final List<Channel> result = repository.findChannelsGivenSiteAndSiteChan(stationDefinitionDataHolder, startTime, endTime);

    assertNotNull(result);
    assertEquals(channels.size(), result.size());
    verify(channelAssembler, times(1))
      .buildAllForTimeRange(eq(startTime), eq(endTime),
        Mockito.argThat(t -> t.containsAll(stationDefintionDataHolder2.getSiteDaos())),
        Mockito.argThat(t -> t.containsAll(stationDefintionDataHolder2.getSiteChanDaos())),
        Mockito.argThat(t -> t.containsAll(stationDefintionDataHolder2.getSensorDaos())),
        any(),
        any());
    verifyNoMoreMockInteractions();
  }

  @Test
  void testChannelEffectiveTimeBeforeChannelEndTime_throwsIllegalState() {
    List<Long> wfids = Collections.singletonList(1L);
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.of(1L);
    Optional<TagName> associatedRecordType = Optional.of(TagName.ARID);
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalStateException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEndTime, channelEffectiveTime));
  }

  @Test
  void testAssociatedRecordTypePresent_throwsIllegalState() {
    List<Long> wfids = Collections.singletonList(1L);
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.empty();
    Optional<TagName> associatedRecordType = Optional.of(TagName.ARID);
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalArgumentException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime));
  }

  @Test
  void testAssociatedRecordIdPresent_throwsIllegalState() {
    List<Long> wfids = Collections.singletonList(1L);
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.of(2L);
    Optional<TagName> associatedRecordType = Optional.empty();
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalArgumentException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime));
  }

  @Test
  void testAssociatedRecordsPresentWfidsEmpty_throwsIllegalState() {
    List<Long> wfids = List.of();
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.of(2L);
    Optional<TagName> associatedRecordType = Optional.of(TagName.ARID);
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalStateException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime));
  }

  @Test
  void testAssociatedRecordsPresentMultipleWfids_throwsIllegalState() {
    List<Long> wfids = List.of(1L, 2L);
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.of(2L);
    Optional<TagName> associatedRecordType = Optional.of(TagName.ARID);
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalStateException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime));
  }

  @Test
  void testAssociatedRecordsEmptyFilterIdPresent_throwsIllegalState() {
    List<Long> wfids = Collections.singletonList(1L);
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.empty();
    Optional<TagName> associatedRecordType = Optional.empty();
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalArgumentException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime));
  }

  @Test
  void testLoadChannelsEmptyInputs_emptyWfids() {
    List<Long> wfids = List.of();
    Optional<Long> filterId = Optional.empty();
    Optional<Long> associatedRecordId = Optional.empty();
    Optional<TagName> associatedRecordType = Optional.empty();
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(5));
    assertThrows(IllegalStateException.class, () -> repository.loadChannelFromWfdisc(wfids, associatedRecordType,
      associatedRecordId, filterId, channelEffectiveTime, channelEndTime));
  }

  @Test
  void testLoadChannelFromWfdisc_rawChannel_array_bad() {
    SiteDao siteDao = SITE_DAO_1;
    siteDao.getId().setStationCode(SITE_DAO_1.getReferenceStation());
    siteDao.setStaType(StaType.ARRAY_STATION);

    WfdiscDao wfdiscDao = WFDISC_TEST_DAO_1;
    wfdiscDao.setStationCode(siteDao.getId().getStationCode());
    //Sonarlint complains if there are multiple exceptions that could result, so have to define these outside of
    //assertThrows to limit it to the single exception, which is IllegalState, because there is no wfdisc.
    Optional<TagName> emptyOne = Optional.empty();
    Optional<Long> emptyTwo = Optional.empty();
    List<Long> wfids = List.of(wfdiscDao.getId());
    Instant epochInst = Instant.EPOCH;
    Instant epochPlus = epochInst.plusSeconds(60);

    assertThrows(IllegalStateException.class, () -> repository.loadChannelFromWfdisc(wfids,
      emptyOne,
      emptyTwo,
      emptyTwo,
      epochInst,
      epochPlus));
  }

  @Test
  void testLoadChannelFromWfdisc_rawChannel_singleStation() {
    SiteDao siteDao = SITE_DAO_1;
    siteDao.setStaType(StaType.SINGLE_STATION);
    siteDao.setReferenceStation(siteDao.getId().getStationCode());

    WfdiscDao wfdiscDao = WFDISC_TEST_DAO_1;
    wfdiscDao.setStationCode(siteDao.getId().getStationCode());

    SiteChanDao siteChanDao = SITE_CHAN_DAO_1;
    SiteChanKey siteChanKey = siteChanDao.getId();
    siteChanKey.setStationCode(siteDao.getReferenceStation());
    siteChanDao.setId(siteChanKey);

    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(wfdiscDao.getId())))
      .thenReturn(List.of(wfdiscDao));
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(siteDao));
    when(siteChanDatabaseConnector.findSiteChansByKeyAndTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(siteChanDao));
    when(beamDatabaseConnector.findBeamsByWfid(List.of(wfdiscDao.getId()))).thenReturn(List.of());
    when(sensorDatabaseConnector.findSensorByKeyInRange(any(),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(Optional.empty());

    when(channelAssembler.buildRawChannel(eq(siteDao),
      eq(wfdiscDao),
      eq(siteChanDao),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(RAW_CHANNEL);

    Channel channel = repository.loadChannelFromWfdisc(List.of(wfdiscDao.getId()),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Instant.EPOCH,
      Instant.EPOCH.plusSeconds(60));

    assertEquals(RAW_CHANNEL, channel);
  }

  @Test
  void testLoadChannelFromWfdisc_rawChannel_array() {
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(WFDISC_TEST_DAO_1.getId())))
      .thenReturn(List.of(WFDISC_TEST_DAO_1));
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_DAO_1));
    when(siteChanDatabaseConnector.findSiteChansByKeyAndTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_CHAN_DAO_1));
    when(beamDatabaseConnector.findBeamsByWfid(List.of(WFDISC_TEST_DAO_1.getId()))).thenReturn(List.of());
    when(sensorDatabaseConnector.findSensorByKeyInRange(any(),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(Optional.empty());
    SiteDao siteDaoArray = SITE_DAO_1;
    siteDaoArray.setStaType(StaType.ARRAY_STATION);

    when(channelAssembler.buildRawChannel(eq(siteDaoArray),
      eq(WFDISC_TEST_DAO_1),
      eq(SITE_CHAN_DAO_1),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(RAW_CHANNEL);

    Channel channel = repository.loadChannelFromWfdisc(List.of(WFDISC_TEST_DAO_1.getId()),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Instant.EPOCH,
      Instant.EPOCH.plusSeconds(60));

    assertEquals(RAW_CHANNEL, channel);
  }

  @Test
  void testLoadChannelFromWfdisc_eventBeam() {
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(WFDISC_TEST_DAO_1.getId())))
      .thenReturn(List.of(WFDISC_TEST_DAO_1));
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_DAO_1));
    when(siteChanDatabaseConnector.findSiteChansByKeyAndTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_CHAN_DAO_1));
    when(beamDatabaseConnector.findBeamForWfid(WFDISC_TEST_DAO_1.getId())).thenReturn(Optional.empty());
    when(sensorDatabaseConnector.findSensorByKeyInRange(any(),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(Optional.empty());

    when(channelAssembler.buildFromAssociatedRecord(anyMap(),
      any(),
      eq(SITE_DAO_1),
      eq(WFDISC_TEST_DAO_1),
      eq(SITE_CHAN_DAO_1),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(EVENT_BEAM_CHANNEL);

    Channel channel = repository.loadChannelFromWfdisc(List.of(WFDISC_TEST_DAO_1.getId()),
      Optional.of(TagName.EVID),
      Optional.of(3L),
      Optional.empty(),
      Instant.EPOCH,
      Instant.EPOCH.plusSeconds(60));

    assertEquals(EVENT_BEAM_CHANNEL, channel);
  }

  @Test
  void testLoadChannelFromWfdisc_fkBeam() {
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(WFDISC_TEST_DAO_1.getId())))
      .thenReturn(List.of(WFDISC_TEST_DAO_1));
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_DAO_1));
    when(siteChanDatabaseConnector.findSiteChansByKeyAndTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_CHAN_DAO_1));
    when(beamDatabaseConnector.findBeamForWfid(WFDISC_TEST_DAO_1.getId())).thenReturn(Optional.empty());
    when(sensorDatabaseConnector.findSensorByKeyInRange(any(),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(Optional.empty());

    when(channelAssembler.buildFromAssociatedRecord(anyMap(),
      any(),
      eq(SITE_DAO_1),
      eq(WFDISC_TEST_DAO_1),
      eq(SITE_CHAN_DAO_1),
      any(),
      eq(Instant.EPOCH),
      eq(Instant.EPOCH.plusSeconds(60))))
      .thenReturn(FK_BEAM_CHANNEL);

    Channel channel = repository.loadChannelFromWfdisc(List.of(WFDISC_TEST_DAO_1.getId()),
      Optional.of(TagName.ARID),
      Optional.of(3L),
      Optional.empty(),
      Instant.EPOCH,
      Instant.EPOCH.plusSeconds(60));

    assertEquals(FK_BEAM_CHANNEL, channel);
  }

  @Test
  void testLoadChannelFromWfdisc_fkBeam_emptySiteChans() {
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(WFDISC_TEST_DAO_1.getId())))
      .thenReturn(List.of(WFDISC_TEST_DAO_1));
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_DAO_1));
    when(siteChanDatabaseConnector.findSiteChansByKeyAndTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of());

    Channel channel = repository.loadChannelFromWfdisc(List.of(WFDISC_TEST_DAO_1.getId()),
      Optional.of(TagName.ARID),
      Optional.of(3L),
      Optional.empty(),
      Instant.EPOCH,
      Instant.EPOCH.plusSeconds(60));

    assertNull(channel);
  }

  @Test
  void testLoadChannelFromWfdisc_fkBeam_emptySites() {
    when(wfdiscDatabaseConnector.findWfdiscsByWfids(List.of(WFDISC_TEST_DAO_1.getId())))
      .thenReturn(List.of(WFDISC_TEST_DAO_1));
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of());
    when(siteChanDatabaseConnector.findSiteChansByKeyAndTime(anyList(), eq(Instant.EPOCH)))
      .thenReturn(List.of(SITE_CHAN_DAO_1));

    Channel channel = repository.loadChannelFromWfdisc(List.of(WFDISC_TEST_DAO_1.getId()),
      Optional.of(TagName.ARID),
      Optional.of(3L),
      Optional.empty(),
      Instant.EPOCH,
      Instant.EPOCH.plusSeconds(60));

    assertNull(channel);
  }

  @Test
  void testLoadChannelFromWfdisc_null() {
    List<Long> wfids = List.of(WFDISC_TEST_DAO_1.getId());
    Optional<Long> filterId = Optional.of(1L);
    Optional<Long> associatedRecordId = Optional.of(3L);
    Optional<TagName> associatedRecordType = Optional.of(TagName.CLUSTAID);
    Instant channelEffectiveTime = Instant.EPOCH;
    Instant channelEndTime = channelEffectiveTime.plus(Duration.ofSeconds(60));

    assertThrows(IllegalArgumentException.class, () -> repository
      .loadChannelFromWfdisc(wfids,
        associatedRecordType,
        associatedRecordId,
        filterId,
        channelEffectiveTime,
        channelEndTime));
  }

  Pair<List<SiteDao>, List<SiteChanDao>> mockConnectorRepositoryInterfactionTime() {

    List<SiteAndSurroundingDates> sitesAndSurroundingDates = getTestSiteAndSurroundingDates();
    when(siteDatabaseConnector.findSitesAndSurroundingDatesByStaCodeAndTimeRange(
      any(), any(), any()))
      .thenReturn(sitesAndSurroundingDates);
    List<SiteDao> updatedSiteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();
    when(siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByKeysAndTime(
      any(), any()))
      .thenReturn(siteChanAndSurroundingDates);

    List<SiteChanDao> updatedSiteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    return Pair.of(updatedSiteDaos, updatedSiteChanDaos);
  }

  Pair<List<SiteDao>, List<SiteChanDao>> mockConnectorRepositoryInterfactionTimeRange() {

    List<SiteAndSurroundingDates> sitesAndSurroundingDates = getTestSiteAndSurroundingDates();
    when(siteDatabaseConnector.findSitesAndSurroundingDatesByStaCodeAndTimeRange(
      any(), any(), any()))
      .thenReturn(sitesAndSurroundingDates);
    List<SiteDao> updatedSiteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();
    when(siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByKeysAndTimeRange(
      any(), any(), any()))
      .thenReturn(siteChanAndSurroundingDates);

    List<SiteChanDao> updatedSiteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    return Pair.of(updatedSiteDaos, updatedSiteChanDaos);
  }
}

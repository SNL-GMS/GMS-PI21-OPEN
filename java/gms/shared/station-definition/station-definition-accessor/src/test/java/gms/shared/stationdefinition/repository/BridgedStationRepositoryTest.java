package gms.shared.stationdefinition.repository;

import com.google.common.base.Functions;
import gms.shared.stationdefinition.api.station.util.StationsTimeFacetRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationsTimeRequest;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.converter.util.assemblers.StationAssembler;
import gms.shared.stationdefinition.dao.css.SiteAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanAndSurroundingDates;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteChanKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.SiteKey;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.FacetingDefintionsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestSiteAndSurroundingDates;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.MAX_END_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.MIN_START_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.CHANNEL_GROUP1;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedStationRepositoryTest {

  private static final Object NULL_OBJECT = null;
  private final List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
  private final List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();

  private List<String> stationNames;
  private List<Channel> channelList = List.of(CHANNEL.toBuilder().setName("Channel.Name.One").build());
  private List<ChannelGroup> channelGroupList = List.of(CHANNEL_GROUP1);

  @Mock
  private SiteDatabaseConnector siteDatabaseConnector;

  @Mock
  private SiteChanDatabaseConnector siteChanDatabaseConnector;

  @Mock
  private StationAssembler stationAssembler;

  private BridgedStationRepository repository;

  @Mock
  private BridgedChannelRepository channelRepository;

  @Mock
  private BridgedChannelGroupRepository channelGroupRepository;

  @BeforeEach
  void setUp() {
    repository = new BridgedStationRepository(channelRepository, channelGroupRepository,
      siteDatabaseConnector,
      siteChanDatabaseConnector,
      stationAssembler);

    stationNames = siteDaos.stream()
      .map(SiteDao::getReferenceStation)
      .distinct()
      .collect(Collectors.toList());
  }

  @Test
  void testFindStationsByName() {
    Instant now = Instant.now();
    mockConnectorRepositoryInteraction(stationNames, now);

    final List<Station> result = repository.findStationsByNameAndTime(stationNames, now);

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());

    verifyConnectorRepositoryInteraction(now);
  }

  @Test
  void testFindStationsByNameAndTime() {
    final StationsTimeRequest request = StationsTimeRequest.builder()
      .setStationNames(stationNames)
      .setEffectiveTime(ONDATE)
      .build();

    mockConnectorRepositoryInteraction(stationNames, request.getEffectiveTime());

    final List<Station> result = repository.findStationsByNameAndTime(request.getStationNames(),
      request.getEffectiveTime());

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());
    verifyConnectorRepositoryInteraction(request.getEffectiveTime());
  }

  @Test
  void testFindStationsByNameAndTimeRange() {
    final StationsTimeRangeRequest request = StationsTimeRangeRequest.builder()
      .setStationNames(stationNames)
      .setTimeRange(
        TimeRangeRequest.builder().setStartTime(ONDATE.minusSeconds(10)).setEndTime(ONDATE).build())
      .build();

    Instant startTime = request.getTimeRange().getStartTime();
    Instant endTime = request.getTimeRange().getEndTime();

    mockJpaRepositoryTimeRangeInteraction(stationNames, startTime, endTime);

    when(stationAssembler.buildAllForTimeRange(any(), any(), any(),
      any(), eq(startTime), eq(endTime), any()))
      .thenReturn(
        stationNames.stream()
          .map(name -> STATION.toBuilder().setName(name).build())
          .collect(Collectors.toList())
      );

    final List<Station> result = repository.findStationsByNameAndTimeRange(request.getStationNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());
   // verifyJpaRepositoryTimeRangeInteraction(startTime, endTime);
  }

  @Test
  void testFindStationsByNameAndTime_FacetRequest_populated() {
    final StationsTimeFacetRequest request = StationsTimeFacetRequest.builder()
      .setStationNames(stationNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATION_POPULATED_FULL)
      .build();

    mockConnectorRepositoryInteraction(stationNames, request.getEffectiveTime().orElseThrow());

    final List<Station> result = repository.findStationsByNameAndTime(request.getStationNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());
    assertTrue(result.stream().allMatch(Station::isPresent));
    verifyConnectorRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  @Test
  void testFindStationsByNameAndTime_FacetRequest_populated_partial() {
    final StationsTimeFacetRequest request = StationsTimeFacetRequest.builder()
      .setStationNames(stationNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATION_POPULATED_PARTIAL)
      .build();

    mockConnectorRepositoryInteraction(stationNames, request.getEffectiveTime().orElseThrow());

    final List<Station> result = repository.findStationsByNameAndTime(request.getStationNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());
    assertTrue(result.stream().allMatch(Station::isPresent));
    verifyConnectorRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  @Test
  void testFindStationsByNameAndTime_FacetRequest_populated_emptyFacets() {
    final StationsTimeFacetRequest request = StationsTimeFacetRequest.builder()
      .setStationNames(stationNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATION_POPULATED_EMPTYFACETS)
      .build();

    mockConnectorRepositoryInteraction(stationNames, request.getEffectiveTime().orElseThrow());

    final List<Station> result = repository.findStationsByNameAndTime(request.getStationNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());
    assertTrue(result.stream().allMatch(Station::isPresent));
    verifyConnectorRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  @Test
  @Disabled("pending rewrite/redesign")
  void testFindStationsByNameAndTime_FacetRequest_notPopulated() {
    final StationsTimeFacetRequest request = StationsTimeFacetRequest.builder()
      .setStationNames(stationNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATION_NOTPOPULATED_EMPTYFACETS)
      .build();

    mockConnectorRepositoryInteraction(stationNames, request.getEffectiveTime().orElseThrow());

    final List<Station> result = repository.findStationsByNameAndTime(request.getStationNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(stationNames.size(), result.size());
    assertFalse(result.stream().allMatch(Station::isPresent));
    verifyConnectorRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  public void mockConnectorRepositoryInteraction(List<String> stationNames, Instant effectiveTime) {
    List<SiteAndSurroundingDates> sitesAndSurroundingDates = getTestSiteAndSurroundingDates();
    when(siteDatabaseConnector.findSitesAndSurroundingDatesByRefStaAndTime(
      stationNames, effectiveTime))
      .thenReturn(sitesAndSurroundingDates);

    List<String> stationCodes = siteDaos.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();
    when(siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(
      eq(stationCodes), any(), any()))
      .thenReturn(siteChanAndSurroundingDates);

    when(channelRepository.findChannelsGivenSiteAndSiteChan(
      any(), eq(MIN_START_TIME), eq(MAX_END_TIME))).thenReturn(channelList);
    when(channelGroupRepository.findChannelGroupsGivenChannelsSitesAndSiteChans(
      any(), any(), eq(effectiveTime), eq(channelList))).thenReturn(channelGroupList);

    when(stationAssembler.buildAllForTime(any(), any(), any(), any(), any(), any()))
      .thenReturn(stationNames.stream().map(name
        -> STATION.toBuilder().setName(name).build()).collect(Collectors.toList()));
  }

  public void mockJpaRepositoryTimeRangeInteraction(List<String> stationNames, Instant startTime, Instant endTime) {

    var sitesAndSurroundingDates = getTestSiteAndSurroundingDates();
    var siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();

    when(siteDatabaseConnector
      .findSitesAndSurroundingDatesByRefStaAndTimeRange(any(), eq(startTime), eq(endTime))).
      thenReturn(sitesAndSurroundingDates);
    when(siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(
      any(), any(), any())).
      thenReturn(siteChanAndSurroundingDates);

    when(channelRepository.findChannelsGivenSiteAndSiteChan(
      any(), any(), any())).thenReturn(channelList);
    when(channelGroupRepository.findChannelGroupsGivenChannelsSitesAndSiteChansAndTimeRange(
      any(), any(), eq(channelList), eq(startTime), eq(endTime))).thenReturn(channelGroupList);
  }

  void verifyConnectorRepositoryInteraction(Instant effectiveTime) {
    verify(siteDatabaseConnector, times(1)).findSitesAndSurroundingDatesByRefStaAndTime(any(), eq(effectiveTime));
    verify(siteChanDatabaseConnector, times(1)).findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(
      any(), any(), any());
    verifyNoMoreMockInteractions();
  }

  void verifyJpaRepositoryTimeRangeInteraction(Instant startTime, Instant endTime) {
    verify(siteDatabaseConnector, times(1)).findSitesByReferenceStationAndTimeRange(any(), eq(startTime), eq(endTime));
    verify(siteChanDatabaseConnector, times(1)).findSiteChansByStationCodeAndTimeRange(
      any(), eq(ONDATE.minusSeconds(1)), eq(OFFDATE.plusSeconds(1)));
    verifyNoMoreInteractions(siteChanDatabaseConnector, siteDatabaseConnector, stationAssembler);
  }

  private void verifyNoMoreMockInteractions() {
    verifyNoMoreInteractions(siteChanDatabaseConnector, siteDatabaseConnector, stationAssembler);
  }
}

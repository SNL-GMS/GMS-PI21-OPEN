package gms.shared.stationdefinition.repository;

import com.google.common.base.Functions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeFacetRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.channel.util.ChannelGroupsTimeRequest;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
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
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.shared.stationdefinition.testfixtures.FacetingDefintionsTestFixtures;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;

import static gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures.getTestSiteAndSurroundingDates;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.MAX_END_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.MIN_START_TIME;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.OFFDATE;
import static gms.shared.stationdefinition.testfixtures.CssDaoAndCoiParameters.ONDATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedChannelGroupRepositoryTest {

  private static final Object NULL_OBJECT = null;
  private final List<SiteChanDao> siteChanDaos = CSSDaoTestFixtures.getTestSiteChanDaos();
  private final static List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
  private final static List<Channel> channels = CSSDaoTestFixtures.getTestChannels();
  private List<String> channelGroupNames;
  private final static SetMultimap<String, String> EXPECTED_MULTIMAP;

  static {
    EXPECTED_MULTIMAP = siteDaos.stream()
      .collect(HashMultimap::create, (m, i) -> m.put(i.getReferenceStation(), i.getId().getStationCode()), Multimap::putAll);
  }

  @Mock
  private SiteDatabaseConnector siteDatabaseConnector;

  @Mock
  private SiteChanDatabaseConnector siteChanDatabaseConnector;

  @Mock
  private ChannelGroupAssembler channelGroupAssembler;

  @Mock
  private BridgedChannelRepository channelRepository;

  private BridgedChannelGroupRepository repository;

  @BeforeEach
  void setUp() {
    repository = new BridgedChannelGroupRepository(siteDatabaseConnector,
      siteChanDatabaseConnector,
      channelGroupAssembler,
      channelRepository);

    channelGroupNames = siteChanDaos.stream()
      .map(siteChanDao -> siteChanDao.getId().getStationCode())
      .distinct()
      .collect(Collectors.toList());
  }

  @Test
  void testFindChannelGroupsByNameAndTime() {
    final ChannelGroupsTimeRequest request = ChannelGroupsTimeRequest.builder()
      .setChannelGroupNames(channelGroupNames)
      .setEffectiveTime(ONDATE)
      .build();

    mockConnectorRepositoryInterfaction(request.getChannelGroupNames(), request.getEffectiveTime());
    final List<ChannelGroup> result = repository.findChannelGroupsByNameAndTime(request.getChannelGroupNames(),
      request.getEffectiveTime());

    assertNotNull(result);
    assertEquals(channelGroupNames.size(), result.size());
    verifyConnectorRepositoryInteraction(request.getEffectiveTime());
  }

  @Test
  void testFindChannelGroupsByNameAndTimeFacet() {
    final ChannelGroupsTimeFacetRequest request = ChannelGroupsTimeFacetRequest.builder()
      .setChannelGroupNames(channelGroupNames)
      .setEffectiveTime(ONDATE)
      .setFacetingDefinition(FacetingDefinition.builder()
        .setClassType("ChannelGroup")
        .setPopulated(true)
        .build())
      .build();

    mockConnectorRepositoryInterfaction(request.getChannelGroupNames(),
      request.getEffectiveTime().orElseThrow());
    final List<ChannelGroup> result = repository.findChannelGroupsByNameAndTime(channelGroupNames,
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(channelGroupNames.size(), result.size());
    verifyConnectorRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  @Test
  void testFindChannelGroupsGivenChannelsSitesAndSiteChans() {

    // filter site daos to their associated channel group
    var siteDaosForChannelGroup = siteDaos.stream()
      .filter(siteDao -> Range.closed(siteDao.getId().getOnDate(), siteDao.getOffDate()).contains(ONDATE))
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    // create navigable version table for channels
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan = getChannelVersionTable();

    when(channelGroupAssembler
      .buildAllForTime(siteDaosForChannelGroup, siteChanDaos, ONDATE, channelsByStaChan))
      .thenReturn(
        UtilsTestFixtures.getListOfChannelGroupsForDaos()
      );

    List<ChannelGroup> channelGroups = repository.findChannelGroupsGivenChannelsSitesAndSiteChans(
      siteDaos, siteChanDaos, ONDATE, channels);

    assertNotNull(channelGroups);
    assertEquals(channelGroupNames.size(), channelGroups.size());
    verify(channelGroupAssembler, times(1))
      .buildAllForTime(siteDaosForChannelGroup, siteChanDaos, ONDATE, channelsByStaChan);
    verifyNoMoreInteractions(channelGroupAssembler);
  }

  @Test
  void testFindChannelGroupsByNameAndTimeRange() {
    Instant startTime = ONDATE;
    Instant endTime = OFFDATE;

    final ChannelGroupsTimeRangeRequest request = ChannelGroupsTimeRangeRequest.builder()
      .setChannelGroupNames(channelGroupNames)
      .setTimeRange(TimeRangeRequest.builder()
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build())
      .build();
   
    mockConnectorRepositoryInterfactionRange(channelGroupNames, startTime, endTime);

    final List<ChannelGroup> result = repository.findChannelGroupsByNameAndTimeRange(request.getChannelGroupNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());

    assertNotNull(result);
    assertEquals(channelGroupNames.size(), result.size());
    verify(siteDatabaseConnector, times(1))
      .findSitesAndSurroundingDatesByStaCodeAndTimeRange(any(), eq(request.getTimeRange().getStartTime()),
        eq(request.getTimeRange().getEndTime()));
    verify(siteChanDatabaseConnector, times(1)).findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(any(), any(), any());
    verify(channelGroupAssembler, times(1))
      .buildAllForTimeRange(any(), any(), any(), any(), any());
    verifyNoMoreMockInteractions();
  }

  @Test
  void testFindChannelGroupsGivenChannelsSitesAndSiteChansAndTimeRange() {

    // filter site daos to their associated channel group
    var siteDaosForChannelGroup = siteDaos.stream()
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    // create navigable version table for channels
    Table<String, String, NavigableMap<Instant, Channel>> channelsByStaChan = getChannelVersionTable();

    when(channelGroupAssembler
      .buildAllForTimeRange(siteDaosForChannelGroup, siteChanDaos, ONDATE, OFFDATE, channelsByStaChan))
      .thenReturn(UtilsTestFixtures.getListOfChannelGroupsForDaos());

    List<ChannelGroup> channelGroups = repository.findChannelGroupsGivenChannelsSitesAndSiteChansAndTimeRange(
      siteDaos, siteChanDaos, channels, ONDATE, OFFDATE);

    assertNotNull(channelGroups);
    assertEquals(channelGroupNames.size(), channelGroups.size());
    verify(channelGroupAssembler, times(1))
      .buildAllForTimeRange(siteDaosForChannelGroup, siteChanDaos, ONDATE, OFFDATE, channelsByStaChan);
    verifyNoMoreInteractions(channelGroupAssembler);

  }

  private void verifyNoMoreMockInteractions() {
    verifyNoMoreInteractions(siteChanDatabaseConnector,
      siteDatabaseConnector,
      channelGroupAssembler);
  }

  @Test
  void testFindChannelGroupsByNameAndTime_FacetRequest_populated() {
    final ChannelGroupsTimeFacetRequest request = ChannelGroupsTimeFacetRequest.builder()
      .setChannelGroupNames(channelGroupNames)
      .setEffectiveTime(ONDATE)
      .setFacetingDefinition(FacetingDefintionsTestFixtures.CHANNELGROUP_POPULATED_FULL)
      .build();

    mockConnectorRepositoryInterfaction(request.getChannelGroupNames(),
      request.getEffectiveTime().orElseThrow());

    final List<ChannelGroup> result = repository.findChannelGroupsByNameAndTime(request.getChannelGroupNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(channelGroupNames.size(), result.size());
    assertTrue(result.stream().allMatch(ChannelGroup::isPresent));
    verifyConnectorRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  private Table<String, String, NavigableMap<Instant, Channel>> getChannelVersionTable() {
    return AssemblerUtils.buildVersionTable(
      Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getStationCode),
      Functions.compose(StationDefinitionIdUtility::getCssKeyFromName, Channel::getName).andThen(SiteChanKey::getChannelCode),
      Functions.compose(Optional::get, Channel::getEffectiveAt),
      channels);
  }

  void mockConnectorRepositoryInterfaction(List<String> channelGroupNames, Instant effectiveTime) {
    List<SiteAndSurroundingDates> sitesAndSurroundingDates
      = getTestSiteAndSurroundingDates();
    when(siteDatabaseConnector.findSitesAndSurroundingDatesByStaCodeAndTime(
      channelGroupNames, effectiveTime))
      .thenReturn(sitesAndSurroundingDates);

    List<SiteDao> updatedSiteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<SiteDao> siteDaosFiltered = updatedSiteDaos.stream()
      .filter(siteDao -> Range.closed(siteDao.getId().getOnDate(), siteDao.getOffDate()).contains(effectiveTime))
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    List<String> stationCodes = siteDaosFiltered.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();
    when(siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(
      eq(stationCodes), any(), any()))
      .thenReturn(siteChanAndSurroundingDates);

    List<SiteChanDao> updatedSiteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    when(channelRepository.findChannelsGivenSiteAndSiteChan(any(), eq(MIN_START_TIME), eq(MAX_END_TIME)))
      .thenReturn(List.of());

    when(channelGroupAssembler
      .buildAllForTime(siteDaosFiltered, updatedSiteChanDaos, effectiveTime, HashBasedTable.create()))
      .thenReturn(
        UtilsTestFixtures.getListOfChannelGroupsForDaos()
      );
  }

  void mockConnectorRepositoryInterfactionRange(List<String> channelGroupNames,
    Instant startTime, Instant endTime) {
    
    List<SiteAndSurroundingDates> sitesAndSurroundingDates
      = getTestSiteAndSurroundingDates();
    when(siteDatabaseConnector
      .findSitesAndSurroundingDatesByStaCodeAndTimeRange(channelGroupNames, startTime, endTime))
      .thenReturn(sitesAndSurroundingDates);

    List<SiteDao> updatedSiteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<SiteDao> siteDaosFiltered = updatedSiteDaos.stream()
      .filter(siteDao -> Range.closed(siteDao.getId().getOnDate(), siteDao.getOffDate()).isConnected(Range.open(startTime, endTime)))
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    List<String> stationCodes = siteDaosFiltered.stream()
      .map(SiteDao::getId)
      .map(SiteKey::getStationCode)
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();
    when(siteChanDatabaseConnector.findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(
      eq(stationCodes), any(), any()))
      .thenReturn(siteChanAndSurroundingDates);

    List<SiteChanDao> updatedSiteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    when(channelRepository.findChannelsGivenSiteAndSiteChan(any(), eq(MIN_START_TIME), eq(MAX_END_TIME)))
      .thenReturn(List.of());

    when(channelGroupAssembler
      .buildAllForTimeRange(eq(siteDaosFiltered), eq(updatedSiteChanDaos), 
        eq(startTime), eq(endTime), any()))
      .thenReturn(UtilsTestFixtures.getListOfChannelGroupsForDaos());
  }

  void mockJpaRepositoryInteraction(Instant effectiveTime) {
    when(siteDatabaseConnector.findSitesByStationCodesAndStartTime(List.of("STA01", "STA02"), effectiveTime))
      .thenReturn(siteDaos);
    when(siteChanDatabaseConnector.findSiteChansByStationCodeAndTimeRange(
      any(), eq(ONDATE.minusSeconds(1)), eq(OFFDATE.plusSeconds(1))))
      .thenReturn(siteChanDaos);
    List<SiteDao> siteDaosFiltered = siteDaos.stream()
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    when(channelRepository.findChannelsGivenSiteAndSiteChan(any(), eq(ONDATE.minusSeconds(1)), eq(OFFDATE.plusSeconds(1))))
      .thenReturn(List.of());

    when(channelGroupAssembler
      .buildAllForTime(siteDaosFiltered, siteChanDaos, effectiveTime, HashBasedTable.create()))
      .thenReturn(
        UtilsTestFixtures.getListOfChannelGroupsForDaos()
      );
  }

  void verifyConnectorRepositoryInteraction(Instant effectiveTime) {
    verify(siteDatabaseConnector, times(1))
      .findSitesAndSurroundingDatesByStaCodeAndTime(any(), eq(effectiveTime));
    verify(siteChanDatabaseConnector, times(1))
      .findSiteChansAndSurroundingDatesByStationCodeAndTimeRange(any(), any(), any());

    List<SiteAndSurroundingDates> sitesAndSurroundingDates
      = getTestSiteAndSurroundingDates();

    List<SiteDao> updatedSiteDaos = sitesAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteDaoOnAndOffDates)
      .collect(Collectors.toList());

    List<SiteDao> siteDaosFiltered = updatedSiteDaos.stream()
      .filter(siteDao -> Range.closed(siteDao.getId().getOnDate(), siteDao.getOffDate()).contains(effectiveTime))
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());

    List<SiteChanAndSurroundingDates> siteChanAndSurroundingDates
      = CSSDaoTestFixtures.getTestSiteChanAndSurroundingDates();

    List<SiteChanDao> updatedSiteChanDaos = siteChanAndSurroundingDates.stream()
      .map(SiteAndSiteChanUtility::updateSiteChanDaoOnAndOffDates)
      .collect(Collectors.toList());

    verify(channelGroupAssembler, times(1))
      .buildAllForTime(siteDaosFiltered, updatedSiteChanDaos, effectiveTime, HashBasedTable.create());
    verifyNoMoreMockInteractions();
  }

  void verifyJpaRepositoryInteraction(Instant effectiveTime) {
    verify(siteDatabaseConnector, times(1))
      .findSitesByStationCodesAndStartTime(any(), eq(effectiveTime));
    verify(siteChanDatabaseConnector, times(1))
      .findSiteChansByStationCodeAndTimeRange(any(), eq(ONDATE.minusSeconds(1)), eq(OFFDATE.plusSeconds(1)));

    List<SiteDao> siteDaosFiltered = siteDaos.stream()
      .filter(siteDao -> !(siteDao.getId().getStationCode().equalsIgnoreCase(siteDao.getReferenceStation())
      && siteDao.getStaType() == StaType.ARRAY_STATION))
      .collect(Collectors.toList());
    verify(channelGroupAssembler, times(1))
      .buildAllForTime(siteDaosFiltered, siteChanDaos, effectiveTime, HashBasedTable.create());
    verifyNoMoreMockInteractions();
  }
}

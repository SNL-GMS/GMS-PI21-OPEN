package gms.shared.stationdefinition.repository;

import com.google.common.base.Functions;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeFacetRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRangeRequest;
import gms.shared.stationdefinition.api.station.util.StationGroupsTimeRequest;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.converter.util.assemblers.StationGroupAssembler;
import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.NetworkStationTimeKey;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.database.connector.AffiliationDatabaseConnector;
import gms.shared.stationdefinition.database.connector.NetworkDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
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
import java.util.stream.Collectors;

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
class BridgedStationGroupRepositoryTest {
  private static final Object NULL_OBJECT = null;
  private final List<SiteDao> siteDaos = CSSDaoTestFixtures.getTestSiteDaos();
  private final List<Station> stations = siteDaos.stream()
    .map(sitedao -> Station.createVersionReference(sitedao.getReferenceStation(), sitedao.getId().getOnDate()))
    .collect(Collectors.toList());
  private final List<AffiliationDao> affiliationDaos = CSSDaoTestFixtures
    .getAffiliationDaosForBridged();
  private final List<AffiliationDao> nextAffiliationDaos = CSSDaoTestFixtures.getNextAffiliationDaosForBridged();
  private final List<NetworkDao> networkDaos = CSSDaoTestFixtures.getNetworkDaosForBridged();
  private List<String> stationGroupNames;

  @Mock
  private NetworkDatabaseConnector networkDatabaseConnector;

  @Mock
  private AffiliationDatabaseConnector affiliationDatabaseConnector;

  @Mock
  private SiteDatabaseConnector siteDatabaseConnector;

  @Mock
  private StationGroupAssembler stationGroupAssembler;

  @Mock
  private BridgedStationRepository stationRepository;

  private BridgedStationGroupRepository repository;

  @BeforeEach
  void setUp() {
    repository = new BridgedStationGroupRepository(stationRepository, networkDatabaseConnector,
      affiliationDatabaseConnector,
      siteDatabaseConnector,
      stationGroupAssembler);

    stationGroupNames = affiliationDaos.stream()
      .map(Functions
        .compose(NetworkStationTimeKey::getNetwork, AffiliationDao::getNetworkStationTimeKey))
      .distinct()
      .collect(Collectors.toList());
  }

  @Test
  void testFindStationGroupsByName() {
    when(networkDatabaseConnector.findNetworks(any())).thenReturn(networkDaos);
    when(affiliationDatabaseConnector.findAffiliationsByNameAndTime(any(), any())).thenReturn(affiliationDaos);
    when(stationGroupAssembler.buildAllForTime(any(), any(), any(), any(), any())).thenReturn(
      List.of(
        UtilsTestFixtures.STATION_GROUP.toBuilder().setName(stationGroupNames.get(0)).build(),
        UtilsTestFixtures.STATION_GROUP.toBuilder().setName(stationGroupNames.get(1)).build()
      ));
    final List<StationGroup> result = repository.findStationGroupsByNameAndTime(stationGroupNames, Instant.now());

    assertNotNull(result);
    assertEquals(stationGroupNames.size(), result.size());
    verify(networkDatabaseConnector, times(1)).findNetworks(any());
    verify(affiliationDatabaseConnector, times(1)).findAffiliationsByNameAndTime(any(), any());
    verify(affiliationDatabaseConnector, times(1)).findNextAffiliationByNameAfterTime(any(), any());
    verifyNoMoreInteractions(networkDatabaseConnector, affiliationDatabaseConnector, siteDatabaseConnector, stationGroupAssembler);
  }

  @Test
  void testFindStationGroupsByNameAndTime() {
    final StationGroupsTimeRequest request = StationGroupsTimeRequest.builder()
      .setStationGroupNames(stationGroupNames)
      .setEffectiveTime(Instant.now())
      .build();


    mockJpaRepositoryInteraction(stationGroupNames, request.getEffectiveTime());
    final List<StationGroup> result = repository.findStationGroupsByNameAndTime(request.getStationGroupNames(), request.getEffectiveTime());

    assertNotNull(result);
    assertEquals(stationGroupNames.size(), result.size());
    verifyJpaRepositoryInteraction(request.getEffectiveTime());
  }

  @Test
  void testFindStationGroupsByNameAndTimeRange() {
    Instant startTime = Instant.now();
    Instant endTime = Instant.MAX;
    final StationGroupsTimeRangeRequest request = StationGroupsTimeRangeRequest.builder()
      .setStationGroupNames(stationGroupNames)
      .setTimeRange(TimeRangeRequest.builder()
        .setStartTime(startTime)
        .setEndTime(endTime)
        .build())
      .build();

    when(networkDatabaseConnector.findNetworks(stationGroupNames)).thenReturn(networkDaos);
    when(affiliationDatabaseConnector.findAffiliationsByNameAndTimeRange(stationGroupNames,
      startTime,
      endTime))
      .thenReturn(affiliationDaos);
    when(affiliationDatabaseConnector.findNextAffiliationByNameAfterTime(stationGroupNames, endTime))
      .thenReturn(nextAffiliationDaos);
    List<String> referenceStations = affiliationDaos.stream()
      .map(AffiliationDao::getNetworkStationTimeKey)
      .map(NetworkStationTimeKey::getStation)
      .distinct()
      .collect(Collectors.toList());
    when(stationRepository.findStationsByNameAndTimeRange(referenceStations, startTime, endTime)).thenReturn(stations);
    when(stationGroupAssembler.buildAllForTimeRange(Range.closed(startTime, endTime), networkDaos, affiliationDaos,
      nextAffiliationDaos, stations))
      .thenReturn(List.of(
        UtilsTestFixtures.STATION_GROUP.toBuilder().setName(stationGroupNames.get(0)).build(),
        UtilsTestFixtures.STATION_GROUP.toBuilder().setName(stationGroupNames.get(1)).build()
      ));

    final List<StationGroup> result = repository.findStationGroupsByNameAndTimeRange(request.getStationGroupNames(),
      request.getTimeRange().getStartTime(), request.getTimeRange().getEndTime());

    assertNotNull(result);
    assertEquals(stationGroupNames.size(), result.size());

    verify(stationGroupAssembler, times(1)).buildAllForTimeRange(Range.closed(startTime, endTime),
      networkDaos, affiliationDaos, nextAffiliationDaos, stations);
    verifyNoMoreMockInteractions();
  }

  @Test
  void testFindStationGroupsByNameAndTime_FacetRequest_populated() {
    final StationGroupsTimeFacetRequest request = StationGroupsTimeFacetRequest.builder()
      .setStationGroupNames(stationGroupNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATIONGROUP_POPULATED_FULL)
      .build();

    mockJpaRepositoryInteraction(stationGroupNames, request.getEffectiveTime().orElseThrow());

    final List<StationGroup> result = repository.findStationGroupsByNameAndTime(request.getStationGroupNames(),
      request.getEffectiveTime().get());

    assertNotNull(result);
    assertEquals(stationGroupNames.size(), result.size());
    assertTrue(result.stream().allMatch(StationGroup::isPresent));
    verifyJpaRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  @Test
  void testFindStationGroupsByNameAndTime_FacetRequest_populated_partial() {
    final StationGroupsTimeFacetRequest request = StationGroupsTimeFacetRequest.builder()
      .setStationGroupNames(stationGroupNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATIONGROUP_POPULATED_PARTIAL)
      .build();

    mockJpaRepositoryInteraction(stationGroupNames, request.getEffectiveTime().orElseThrow());

    final List<StationGroup> result = repository.findStationGroupsByNameAndTime(request.getStationGroupNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(stationGroupNames.size(), result.size());
    assertTrue(result.stream().allMatch(StationGroup::isPresent));
    verifyJpaRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  @Test
  void testFindStationGroupsByNameAndTime_FacetRequest_populated_emptyFacets() {
    final StationGroupsTimeFacetRequest request = StationGroupsTimeFacetRequest.builder()
      .setStationGroupNames(stationGroupNames)
      .setEffectiveTime(Instant.now())
      .setFacetingDefinition(FacetingDefintionsTestFixtures.STATIONGROUP_POPULATED_EMPTYFACETS)
      .build();

    mockJpaRepositoryInteraction(stationGroupNames, request.getEffectiveTime().orElseThrow());

    final List<StationGroup> result = repository.findStationGroupsByNameAndTime(request.getStationGroupNames(),
      request.getEffectiveTime().orElseThrow());

    assertNotNull(result);
    assertEquals(stationGroupNames.size(), result.size());
    assertTrue(result.stream().allMatch(StationGroup::isPresent));
    verifyJpaRepositoryInteraction(request.getEffectiveTime().orElseThrow());
  }

  void mockJpaRepositoryInteraction(List<String> stationGroupNames, Instant effectiveTime) {
    when(networkDatabaseConnector.findNetworks(any())).thenReturn(networkDaos);
    when(affiliationDatabaseConnector.findAffiliationsByNameAndTime(any(), eq(effectiveTime))).thenReturn(affiliationDaos);
    when(stationGroupAssembler.buildAllForTime(eq(effectiveTime), any(), any(), any(), any())).thenReturn(
      List.of(
        UtilsTestFixtures.STATION_GROUP.toBuilder().setName(stationGroupNames.get(0)).build(),
        UtilsTestFixtures.STATION_GROUP.toBuilder().setName(stationGroupNames.get(1)).build()
      ));
  }

  void verifyJpaRepositoryInteraction(Instant effectiveTime) {
    verify(networkDatabaseConnector, times(1)).findNetworks(any());
    verify(affiliationDatabaseConnector, times(1)).findAffiliationsByNameAndTime(any(), eq(effectiveTime));
    verify(affiliationDatabaseConnector, times(1)).findNextAffiliationByNameAfterTime(any(), eq(effectiveTime));
    verifyNoMoreInteractions(networkDatabaseConnector, affiliationDatabaseConnector, stationGroupAssembler);
  }

  private void verifyNoMoreMockInteractions() {
    verifyNoMoreInteractions(networkDatabaseConnector, affiliationDatabaseConnector, stationGroupAssembler);
  }
}
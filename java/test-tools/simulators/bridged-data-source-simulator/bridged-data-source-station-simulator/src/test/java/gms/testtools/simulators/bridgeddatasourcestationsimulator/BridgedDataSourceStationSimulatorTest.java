package gms.testtools.simulators.bridgeddatasourcestationsimulator;

import gms.shared.stationdefinition.dao.css.AffiliationDao;
import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.NetworkDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.database.connector.AffiliationDatabaseConnector;
import gms.shared.stationdefinition.database.connector.InstrumentDatabaseConnector;
import gms.shared.stationdefinition.database.connector.NetworkDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SensorDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteChanDatabaseConnector;
import gms.shared.stationdefinition.database.connector.SiteDatabaseConnector;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.Site;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.SiteChan;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceStationRepositoryJpa;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BridgedDataSourceStationSimulatorTest {

  public static final String PLACEHOLDER = "placeholder";
  @Mock
  private BridgedDataSourceStationRepositoryJpa bridgedDataSourceRepositoryJpa;
  @Mock
  private NetworkDatabaseConnector networkRepositoryInstance;
  @Mock
  private AffiliationDatabaseConnector affiliationRepositoryInstance;
  @Mock
  private SiteDatabaseConnector siteRepositoryInstance;
  @Mock
  private SiteChanDatabaseConnector siteChanRepositoryInstance;
  @Mock
  private SensorDatabaseConnector sensorRepositoryInstance;
  @Mock
  private InstrumentDatabaseConnector instrumentRepositoryInstance;
  @Mock
  private BridgedDataSourceSimulatorSpec simulatorSpec;
  private BridgedDataSourceStationSimulator stationSimulator;
  @Captor
  private ArgumentCaptor<List<SiteChanDao>> recordCaptor;
  @Captor
  private ArgumentCaptor<List<SiteDao>> recordCaptorSites;
  private static final Instant NEW_ONDATE = Instant.parse("2021-05-28T03:20:17.207962Z");
  private static final Instant NEW_OFFDATE = Instant.parse("2024-07-30T03:20:17.207962Z");

  @BeforeEach
  public void testSetup() {
    stationSimulator = BridgedDataSourceStationSimulator.create(
      networkRepositoryInstance,
      affiliationRepositoryInstance,
      siteRepositoryInstance,
      siteChanRepositoryInstance,
      sensorRepositoryInstance,
      instrumentRepositoryInstance,
      bridgedDataSourceRepositoryJpa);
  }

  @ParameterizedTest
  @MethodSource("constructorValidationCases")
  void testConstructorValidation(
    Supplier<NetworkDatabaseConnector> networkRepositorySupplier,
    Supplier<AffiliationDatabaseConnector> affiliationRepositorySupplier,
    Supplier<SiteDatabaseConnector> siteRepositorySupplier,
    Supplier<SiteChanDatabaseConnector> siteChanRepositorySupplier,
    Supplier<SensorDatabaseConnector> sensorRepositorySupplier,
    Supplier<InstrumentDatabaseConnector> InstrumentRepositorySupplier,
    Supplier<BridgedDataSourceStationRepositoryJpa> bridgedDataSourceRepositorySupplier) {
    NetworkDatabaseConnector networkRepository = networkRepositorySupplier.get();
    AffiliationDatabaseConnector affiliationRepository = affiliationRepositorySupplier.get();
    SiteDatabaseConnector siteRepository = siteRepositorySupplier.get();
    SiteChanDatabaseConnector siteChanRepository = siteChanRepositorySupplier.get();
    SensorDatabaseConnector sensorRepository = sensorRepositorySupplier.get();
    InstrumentDatabaseConnector instrumentRepository = InstrumentRepositorySupplier.get();
    BridgedDataSourceStationRepositoryJpa bridgedDataSourceRepository = bridgedDataSourceRepositorySupplier.get();
    assertThrows(NullPointerException.class,
      () -> BridgedDataSourceStationSimulator.create(
        networkRepository,
        affiliationRepository,
        siteRepository,
        siteChanRepository,
        sensorRepository,
        instrumentRepository,
        bridgedDataSourceRepository));
  }

  private static Stream<Arguments> constructorValidationCases() {
    return Stream.of(
      Arguments.arguments(
        getNullSupplier(NetworkDatabaseConnector.class),
        getMockSupplier(AffiliationDatabaseConnector.class),
        getMockSupplier(SiteDatabaseConnector.class),
        getMockSupplier(SiteChanDatabaseConnector.class),
        getMockSupplier(SensorDatabaseConnector.class),
        getMockSupplier(InstrumentDatabaseConnector.class),
        getMockSupplier(BridgedDataSourceStationRepositoryJpa.class)
      ),
      Arguments.arguments(
        getMockSupplier(NetworkDatabaseConnector.class),
        getNullSupplier(AffiliationDatabaseConnector.class),
        getMockSupplier(SiteDatabaseConnector.class),
        getMockSupplier(SiteChanDatabaseConnector.class),
        getMockSupplier(SensorDatabaseConnector.class),
        getMockSupplier(InstrumentDatabaseConnector.class),
        getMockSupplier(BridgedDataSourceStationRepositoryJpa.class)
      ),
      Arguments.arguments(
        getMockSupplier(NetworkDatabaseConnector.class),
        getMockSupplier(AffiliationDatabaseConnector.class),
        getNullSupplier(SiteDatabaseConnector.class),
        getMockSupplier(SiteChanDatabaseConnector.class),
        getMockSupplier(SensorDatabaseConnector.class),
        getMockSupplier(InstrumentDatabaseConnector.class),
        getMockSupplier(BridgedDataSourceStationRepositoryJpa.class)
      ),
      Arguments.arguments(
        getMockSupplier(NetworkDatabaseConnector.class),
        getMockSupplier(AffiliationDatabaseConnector.class),
        getMockSupplier(SiteDatabaseConnector.class),
        getNullSupplier(SiteChanDatabaseConnector.class),
        getMockSupplier(SensorDatabaseConnector.class),
        getMockSupplier(InstrumentDatabaseConnector.class),
        getMockSupplier(BridgedDataSourceStationRepositoryJpa.class)
      ),
      Arguments.arguments(
        getMockSupplier(NetworkDatabaseConnector.class),
        getMockSupplier(AffiliationDatabaseConnector.class),
        getMockSupplier(SiteDatabaseConnector.class),
        getMockSupplier(SiteChanDatabaseConnector.class),
        getNullSupplier(SensorDatabaseConnector.class),
        getMockSupplier(InstrumentDatabaseConnector.class),
        getMockSupplier(BridgedDataSourceStationRepositoryJpa.class)
      ),
      Arguments.arguments(
        getMockSupplier(NetworkDatabaseConnector.class),
        getMockSupplier(AffiliationDatabaseConnector.class),
        getMockSupplier(SiteDatabaseConnector.class),
        getMockSupplier(SiteChanDatabaseConnector.class),
        getMockSupplier(SensorDatabaseConnector.class),
        getNullSupplier(InstrumentDatabaseConnector.class),
        getMockSupplier(BridgedDataSourceStationRepositoryJpa.class)
      ),
      Arguments.arguments(
        getMockSupplier(NetworkDatabaseConnector.class),
        getMockSupplier(AffiliationDatabaseConnector.class),
        getMockSupplier(SiteDatabaseConnector.class),
        getMockSupplier(SiteChanDatabaseConnector.class),
        getMockSupplier(SensorDatabaseConnector.class),
        getMockSupplier(InstrumentDatabaseConnector.class),
        getNullSupplier(BridgedDataSourceStationRepositoryJpa.class)
      )
    );
  }

  private static <T> Supplier<T> getMockSupplier(Class<T> cls) {
    return () -> mock(cls);
  }

  private static <T> Supplier<T> getNullSupplier(Class<T> cls) {
    Validate.notNull(cls);
    return () -> null;
  }

  @Test
  void testInitialize() {
    final List<NetworkDao> networkDaos = List
      .of(CSSDaoTestFixtures.NETWORK_DAO_1, CSSDaoTestFixtures.NETWORK_DAO_1_1);
    final List<AffiliationDao> affiliationDaos = List
      .of(CSSDaoTestFixtures.AFFILIATION_DAO_2, CSSDaoTestFixtures.AFFILIATION_DAO_3);
    final List<SiteDao> siteDaos = List
      .of(CSSDaoTestFixtures.SITE_DAO_4, CSSDaoTestFixtures.SITE_DAO_5);
    final List<SiteChanDao> siteChanDaos = List
      .of(CSSDaoTestFixtures.SITE_CHAN_DAO_1, CSSDaoTestFixtures.SITE_CHAN_DAO_1_1);
    final List<SensorDao> sensorDaos = List
      .of(CSSDaoTestFixtures.SENSOR_DAO_1, CSSDaoTestFixtures.SENSOR_DAO_1_1);
    final List<InstrumentDao> instrumentDaos = List
      .of(CSSDaoTestFixtures.INSTRUMENT_DAO_1, CSSDaoTestFixtures.INSTRUMENT_DAO_1_1);
    when(simulatorSpec.getSeedDataStartTime()).thenReturn(Instant.now().minusSeconds(8));
    when(simulatorSpec.getSeedDataEndTime()).thenReturn(Instant.now().minusSeconds(1));
    when(simulatorSpec.getOperationalTimePeriod()).thenReturn(Duration.ofSeconds(10));
    when(networkRepositoryInstance
      .findNetworks(any())).thenReturn(networkDaos);
    when(affiliationRepositoryInstance
      .findAffiliationsByTimeRange(simulatorSpec.getSeedDataStartTime(),
        simulatorSpec.getSeedDataEndTime())).thenReturn(affiliationDaos);
    when(siteRepositoryInstance
      .findSitesByTimeRange(simulatorSpec.getSeedDataStartTime(),
        simulatorSpec.getSeedDataEndTime())).thenReturn(siteDaos);
    when(siteChanRepositoryInstance
      .findSiteChansByTimeRange(simulatorSpec.getSeedDataStartTime(),
        simulatorSpec.getSeedDataEndTime())).thenReturn(siteChanDaos);
    when(sensorRepositoryInstance
      .findSensorsByChannelIdAndTimeRange(any(), any(), any())).thenReturn(sensorDaos);
    when(instrumentRepositoryInstance
      .findInstruments(anyList())).thenReturn(instrumentDaos);
    assertDoesNotThrow(() -> stationSimulator.initialize(simulatorSpec));

    verify(networkRepositoryInstance, times(1)).findNetworks(any());
    verify(affiliationRepositoryInstance, times(1)).findAffiliationsByTimeRange(simulatorSpec.getSeedDataStartTime(), simulatorSpec.getSeedDataEndTime());
    verify(siteRepositoryInstance, times(1)).findSitesByTimeRange(simulatorSpec.getSeedDataStartTime(), simulatorSpec.getSeedDataEndTime());
    verify(siteChanRepositoryInstance, times(1)).findSiteChansByTimeRange(simulatorSpec.getSeedDataStartTime(), simulatorSpec.getSeedDataEndTime());
    verify(sensorRepositoryInstance, times(1)).findSensorsByChannelIdAndTimeRange(any(), any(), any());
    verify(instrumentRepositoryInstance, times(1)).findInstruments(anyList());
    verify(bridgedDataSourceRepositoryJpa, times(6)).store(any());

    verifyNoMoreInteractions(networkRepositoryInstance,
      affiliationRepositoryInstance,
      siteRepositoryInstance,
      siteChanRepositoryInstance,
      sensorRepositoryInstance,
      instrumentRepositoryInstance,
      bridgedDataSourceRepositoryJpa);
  }


  @Test
  void testStart() {
    assertDoesNotThrow(() -> stationSimulator.start(PLACEHOLDER));

    verifyNoMoreInteractions(networkRepositoryInstance,
      affiliationRepositoryInstance,
      siteRepositoryInstance,
      siteChanRepositoryInstance,
      sensorRepositoryInstance,
      instrumentRepositoryInstance,
      bridgedDataSourceRepositoryJpa);
  }


  @Test
  void testStop() {
    assertDoesNotThrow(() -> stationSimulator.stop(PLACEHOLDER));

    verifyNoMoreInteractions(networkRepositoryInstance,
      affiliationRepositoryInstance,
      siteRepositoryInstance,
      siteChanRepositoryInstance,
      sensorRepositoryInstance,
      instrumentRepositoryInstance,
      bridgedDataSourceRepositoryJpa);
  }


  @Test
  void testStoreNewChannelVersions() {

    SiteChanDao siteChanDao1 = CSSDaoTestFixtures.SITE_CHAN_DAO_1;
    SiteChanDao siteChanDao3 = CSSDaoTestFixtures.SITE_CHAN_DAO_2;
    SiteChanDao siteChanDao2 = CSSDaoTestFixtures.SITE_CHAN_DAO_3;

    final List<SiteChanDao> siteChanDaos = List
      .of(siteChanDao1, siteChanDao2, siteChanDao3);

    when(simulatorSpec.getSeedDataStartTime()).thenReturn(Instant.now().minusSeconds(8));
    when(simulatorSpec.getSeedDataEndTime()).thenReturn(Instant.now().minusSeconds(1));
    when(simulatorSpec.getOperationalTimePeriod()).thenReturn(Duration.ofSeconds(10));

    when(networkRepositoryInstance.findNetworks(any())).thenReturn(Collections.emptyList());
    when(affiliationRepositoryInstance.findAffiliationsByTimeRange(any(), any())).thenReturn(Collections.emptyList());
    when(siteRepositoryInstance.findSitesByTimeRange(any(), any())).thenReturn(Collections.emptyList());
    when(sensorRepositoryInstance.findSensorsByChannelIdAndTimeRange(any(), any(), any())).thenReturn(Collections.emptyList());
    when(siteChanRepositoryInstance.findSiteChansByTimeRange(any(), any())).thenReturn(siteChanDaos);

    assertDoesNotThrow(() -> stationSimulator.initialize(simulatorSpec));

    SiteChan siteChan1 = siteChanFromDao(siteChanDao1).build();
    SiteChan siteChan2 = siteChanFromDao(siteChanDao3).build();


    assertDoesNotThrow(() -> stationSimulator.storeNewChannelVersions(List.of(siteChan1, siteChan2, siteChan1)));
    verify(bridgedDataSourceRepositoryJpa, times(6)).store(any());
    verify(bridgedDataSourceRepositoryJpa, times(1)).updateAndStoreSiteChans(recordCaptor.capture());

    final List<SiteChanDao> storedSiteChans = recordCaptor.getValue();

    assertEquals(2, storedSiteChans.size());
  }


  @Test
  void testStoreNewSiteVersions() {

    SiteDao siteDao1 = CSSDaoTestFixtures.SITE_DAO_REF_11;
    SiteDao siteDao2 = CSSDaoTestFixtures.SITE_DAO_REF_21;


    final List<SiteDao> siteDaos = List.of(siteDao1, siteDao2);

    when(simulatorSpec.getSeedDataStartTime()).thenReturn(Instant.now().minusSeconds(8));
    when(simulatorSpec.getSeedDataEndTime()).thenReturn(Instant.now().minusSeconds(1));
    when(simulatorSpec.getOperationalTimePeriod()).thenReturn(Duration.ofSeconds(10));

    when(networkRepositoryInstance.findNetworks(any())).thenReturn(Collections.emptyList());
    when(affiliationRepositoryInstance.findAffiliationsByTimeRange(any(), any())).thenReturn(Collections.emptyList());
    when(siteRepositoryInstance.findSitesByTimeRange(any(), any())).thenReturn(siteDaos);
    when(sensorRepositoryInstance.findSensorsByChannelIdAndTimeRange(any(), any(), any())).thenReturn(Collections.emptyList());
    when(instrumentRepositoryInstance.findInstruments(anyList())).thenReturn(Collections.emptyList());
    when(instrumentRepositoryInstance.findInstruments(anyList())).thenReturn(Collections.emptyList());
    when(siteChanRepositoryInstance.findSiteChansByTimeRange(any(), any())).thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> stationSimulator.initialize(simulatorSpec));

    Site site1 = siteFromDao(siteDao1).build();
    Site site2 = siteFromDao(siteDao2).build();


    assertDoesNotThrow(() -> stationSimulator.storeNewSiteVersions(List.of(site1, site2, site1)));
    verify(bridgedDataSourceRepositoryJpa, times(6)).store(any());
    verify(bridgedDataSourceRepositoryJpa, times(1)).updateAndStoreSites(recordCaptorSites.capture());

    final List<SiteDao> storedSites = recordCaptorSites.getValue();

    assertEquals(2, storedSites.size());
  }


  @Test
  void testCleanup_callsRepositoryToRunCleanup() {
    assertDoesNotThrow(() -> stationSimulator.cleanup(PLACEHOLDER));

    verify(bridgedDataSourceRepositoryJpa, times(1)).cleanupData();
    verifyNoMoreInteractions(networkRepositoryInstance,
      affiliationRepositoryInstance,
      siteRepositoryInstance,
      siteChanRepositoryInstance,
      sensorRepositoryInstance,
      instrumentRepositoryInstance,
      bridgedDataSourceRepositoryJpa);
  }

  private SiteChan.Builder siteChanFromDao(SiteChanDao siteChanDao) {

    return SiteChan.builder()
      .setStationCode(siteChanDao.getId().getStationCode())
      .setChannelCode(siteChanDao.getId().getChannelCode())
      .setOnDate(NEW_ONDATE)
      .setOffDate(NEW_OFFDATE)
      .setChannelType(siteChanDao.getChannelType())
      .setChannelDescription(siteChanDao.getChannelDescription())
      .setEmplacementDepth(siteChanDao.getEmplacementDepth())
      .setHorizontalAngle(siteChanDao.getHorizontalAngle())
      .setVerticalAngle(siteChanDao.getVerticalAngle())
      .setLoadDate(Instant.now());
  }

  private Site.Builder siteFromDao(SiteDao siteDao) {

    return Site.builder()
      .setStationCode(siteDao.getId().getStationCode())
      .setOnDate(NEW_ONDATE)
      .setOffDate(NEW_OFFDATE)
      .setLatitude(siteDao.getLatitude())
      .setLongitude(siteDao.getLongitude())
      .setElevation(siteDao.getElevation())
      .setStationName(siteDao.getStationName())
      .setStationType(siteDao.getStaType())
      .setDegreesNorth(siteDao.getDegreesNorth())
      .setDegreesEast(siteDao.getDegreesEast())
      .setReferenceStation(siteDao.getReferenceStation())
      .setLoadDate(siteDao.getLoadDate());
  }


}
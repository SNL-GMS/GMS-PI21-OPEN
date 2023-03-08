package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.event.dao.LatLonDepthTimeKey;
import gms.shared.event.dao.MagnitudeIdAmplitudeIdStationNameKey;
import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OriginDataSimulatorTest {

  @Mock
  OriginSimulatorDatabaseConnector originSimulatorDatabaseConnector;
  @Mock
  EventDatabaseConnector eventDatabaseConnector;
  @Mock
  OriginErrDatabaseConnector originErrDatabaseConnector;
  @Mock
  BridgedDataSourceRepository bridgedDataSourceRepository;

  private Map<String, BridgedDataSourceRepository> bridgedDataSourceOriginRepositoryMap;
  private Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap;
  private Map<String, EventDatabaseConnector> eventDatabaseConnectorMap;
  private Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap;
  private Set<String> orderedStages = new HashSet<>();

  @Captor
  ArgumentCaptor<List<?>> listArgumentCaptor;

  private static final String STAGE_1 = "stage1";
  private static final String STAGE_2 = "stage2";
  private static final String STAGE_3 = "stage3";
  private static final String STAGE_4 = "stage4";

  @BeforeEach
  public void testSetup() {
    // init bridged data source origin repo
    bridgedDataSourceOriginRepositoryMap = Map.of(
      STAGE_1, bridgedDataSourceRepository,
      STAGE_2, bridgedDataSourceRepository,
      STAGE_3, bridgedDataSourceRepository,
      STAGE_4, bridgedDataSourceRepository);

    // init original sim db connector
    originSimulatorDatabaseConnectorMap = Map.of(
      STAGE_1, originSimulatorDatabaseConnector,
      STAGE_2, originSimulatorDatabaseConnector,
      STAGE_3, originSimulatorDatabaseConnector,
      STAGE_4, originSimulatorDatabaseConnector);

    // init event db connector map
    eventDatabaseConnectorMap = Map.of(
      STAGE_1, eventDatabaseConnector,
      STAGE_2, eventDatabaseConnector,
      STAGE_3, eventDatabaseConnector,
      STAGE_4, eventDatabaseConnector);

    // init origin err db connector map
    originErrDatabaseConnectorMap = Map.of(
      STAGE_1, originErrDatabaseConnector,
      STAGE_2, originErrDatabaseConnector,
      STAGE_3, originErrDatabaseConnector,
      STAGE_4, originErrDatabaseConnector);

    orderedStages = originErrDatabaseConnectorMap.keySet();
  }

  @Test
  void testLoadDataForOrigin() {

    Mockito.when(originSimulatorDatabaseConnector.findOriginDaosByPreciseTime(
      any(Instant.class), any(Instant.class)
    )).thenReturn(List.of(OriginDataSimulatorTestFixture.happyOriginDaoBuilder
      .build()));

    Mockito.when(originSimulatorDatabaseConnector
        .retrieveEventControlDaoByEventIdAndOriginId(any()))
      .thenReturn(Optional.of(OriginDataSimulatorTestFixture.happyEventControlDao.build()));

    Mockito.when(originSimulatorDatabaseConnector
        .retrieveAssocDaoListFromOriginId(anyLong()))
      .thenReturn(List.of(OriginDataSimulatorTestFixture.happyAssocDaoBuilder.build()));

    Mockito.when(originSimulatorDatabaseConnector
        .retrieveArInfoDaoListForOriginId(anyLong()))
      .thenReturn(List.of(OriginDataSimulatorTestFixture.happyArInfoDaoBuilder.build()));

    Mockito.when(originSimulatorDatabaseConnector
        .retrieveNetMagDaoListForOriginId(anyLong()))
      .thenReturn(List.of(OriginDataSimulatorTestFixture.happyNetMagDaoBuilder.build()));

    Mockito.when(originSimulatorDatabaseConnector
        .retrieveStamagDaoListForOriginId(anyLong()))
      .thenReturn(List.of(OriginDataSimulatorTestFixture.happyStaMagDaoBuilder.build()));

    Mockito.when(eventDatabaseConnector.findEventById(anyLong()))
      .thenReturn(Optional.of(OriginDataSimulatorTestFixture.happyEventDaoBuilder.build()));

    Mockito.when(originErrDatabaseConnector.findByIds(anyList()))
      .thenReturn(Set.of(OriginDataSimulatorTestFixture.happyOrigerrDaoBuilder.build()));

    var simulator = OriginDataSimulator.create(
      originSimulatorDatabaseConnectorMap,
      eventDatabaseConnectorMap,
      originErrDatabaseConnectorMap,
      bridgedDataSourceOriginRepositoryMap,
      new AnalysisDataIdMapper()
    );

    orderedStages.forEach(stage -> simulator.loadData(stage, Instant.ofEpochSecond(1),
      Instant.ofEpochSecond(2), Duration.ofSeconds(1)));

    verify(bridgedDataSourceRepository, times(32))
      .store(listArgumentCaptor.capture());

    var daoTypes = listArgumentCaptor.getAllValues();

    //As the DAOs are modified as they are stored, use the builder to make the base
    // version, the happy builder, and then create the modified

    assertEquals(OriginDataSimulatorTestFixture.happyModifiedOriginDaoBuilder.build(),
      daoTypes.get(0).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyEventDaoBuilder.build(),
      daoTypes.get(1).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyOrigerrDaoBuilder.build(),
      daoTypes.get(2).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyEventControlDao.build(),
      daoTypes.get(3).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyAssocDaoBuilder.build(),
      daoTypes.get(4).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyArInfoDaoBuilder.build(),
      daoTypes.get(5).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyNetMagDaoBuilder.build(),
      daoTypes.get(6).get(0));

    assertEquals(OriginDataSimulatorTestFixture.happyStaMagDaoBuilder
      .withMagnitudeIdAmplitudeIdStationNameKey(new MagnitudeIdAmplitudeIdStationNameKey.Builder()
        .withMagnitudeId(1)
        .withAmplitudeId(1)
        .withStationName("AA")
        .build())
      .withArrivalId(2)
      .build(), daoTypes.get(7).get(0));

  }

  @Test
  void testGetOriginDaoFlux() {

    long time1Seconds = 10000000L;
    long time2Seconds = 20000000L;

    var timeJulian1 = OriginDataSimulatorTestFixture.julianDate(time1Seconds);
    var timeJulian2 = OriginDataSimulatorTestFixture.julianDate(time2Seconds);

    var time1SecondsShifted = 10000000L + 90000;
    var time2SecondsShifted = 20000000L + 90000;

    var timeJulian1Shifted = OriginDataSimulatorTestFixture.julianDate(time1SecondsShifted);
    var timeJulian2Shifted = OriginDataSimulatorTestFixture.julianDate(time2SecondsShifted);

    var inputOriginDaoList = List.of(

      OriginDataSimulatorTestFixture.happyOriginDaoBuilder
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(10)
            .withLongitude(10)
            .withDepth(10)
            .withTime(time1Seconds)
            .build()
        )
        .withJulianDate(timeJulian1)
        .build(),

      OriginDataSimulatorTestFixture.happyOriginDaoBuilder
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(10)
            .withLongitude(10)
            .withDepth(10)
            .withTime(time2Seconds)
            .build()
        )
        .withJulianDate(timeJulian2)
        .build()
    );

    var expectedResult = List.of(

      OriginDataSimulatorTestFixture.happyModifiedOriginDaoBuilder
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(10)
            .withLongitude(10)
            .withDepth(10)
            .withTime(time1SecondsShifted)
            .build()
        )
        .withJulianDate(timeJulian1Shifted)
        .build(),

      OriginDataSimulatorTestFixture.happyModifiedOriginDaoBuilder
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(10)
            .withLongitude(10)
            .withDepth(10)
            .withTime(time2SecondsShifted)
            .build()
        )
        .withJulianDate(timeJulian2Shifted)
        .build()
    );

    var result = OriginDataSimulator.getOriginDaoFlux(
      inputOriginDaoList,
      Duration.ofSeconds(90000)
    ).collectList().block();

    assertEquals(expectedResult, result);

    System.out.println(result);
  }
}

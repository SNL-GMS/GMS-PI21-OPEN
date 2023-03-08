package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.testfixtures.CSSDaoTestFixtures;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums.AnalysisIdTag;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaveformDataSimulatorTest {

  @Mock
  private BridgedDataSourceRepository bridgedDataSourceRepositoryJpa;
  @Mock
  private WfdiscDatabaseConnector wfdiscDatabaseConnectorInstance;
  @Mock
  private BeamDatabaseConnector beamDatabaseConnector;
  @Mock
  private AnalysisDataIdMapper idMapper;
  @Captor
  private ArgumentCaptor<List<Object>> recordCaptor;
  @Captor
  private ArgumentCaptor<List<Object>> recordCaptor2;

  private static final String STAGE_TEST = "stageTest";
  private WaveformDataSimulator waveformDataSimulator;

  @BeforeEach
  public void testSetup() {
    waveformDataSimulator = WaveformDataSimulator.create(wfdiscDatabaseConnectorInstance,
      beamDatabaseConnector, bridgedDataSourceRepositoryJpa, idMapper);
  }

  @Test
  void testLoadDataAndCleanup() {

    Instant startTime = Instant.now().minusSeconds(3600);
    Instant endTime = Instant.now();
    Duration copiedDataTimeShift = Duration.ofDays(2);
    final long defaultId = 314159;
    final Instant defaultEndTime = startTime.minusSeconds(3600);

    WfdiscDao wfdiscDao1 = new WfdiscDao(CSSDaoTestFixtures.WFDISC_DAO_1);
    wfdiscDao1.setEndTime(defaultEndTime);
    WfdiscDao wfdiscDao2 = new WfdiscDao(CSSDaoTestFixtures.WFDISC_DAO_2);
    wfdiscDao2.setEndTime(defaultEndTime);
    WfdiscDao wfdiscDao3 = new WfdiscDao(CSSDaoTestFixtures.WFDISC_DAO_3);
    wfdiscDao3.setEndTime(defaultEndTime);
    WfdiscDao wfdiscDao4 = new WfdiscDao(CSSDaoTestFixtures.WFDISC_DAO_4);
    wfdiscDao4.setEndTime(defaultEndTime);
    List<WfdiscDao> wfdiscDaoList = List.of(wfdiscDao1, wfdiscDao2, wfdiscDao3, wfdiscDao4);

    BeamDao beamDao1 = CSSDaoTestFixtures.createBeamDao(wfdiscDao1.getId());
    BeamDao beamDao2 = CSSDaoTestFixtures.createBeamDao(wfdiscDao2.getId());
    BeamDao beamDao3 = CSSDaoTestFixtures.createBeamDao(wfdiscDao3.getId());
    BeamDao beamDao4 = CSSDaoTestFixtures.createBeamDao(wfdiscDao4.getId());
    List<BeamDao> beamDaoList = List.of(beamDao1, beamDao2, beamDao3, beamDao4);

    when(wfdiscDatabaseConnectorInstance.findWfdiscsByTimeRange(any(), any()))
      .thenReturn(wfdiscDaoList);
    when(beamDatabaseConnector.findBeamsByWfid(any()))
      .thenReturn(beamDaoList);
    when(idMapper.getOrGenerate(any(AnalysisIdTag.class), anyLong(), anyLong())).thenReturn(
      defaultId);

    waveformDataSimulator.loadData("stageTest", startTime, endTime, copiedDataTimeShift);
    verify(wfdiscDatabaseConnectorInstance, times(1))
      .findWfdiscsByTimeRange(startTime, endTime);
    verify(beamDatabaseConnector, times(1))
      .findBeamsByWfid(any());
    verify(bridgedDataSourceRepositoryJpa, times(2)).store(recordCaptor.capture());

    List<List<Object>> storedValues = recordCaptor.getAllValues();
    List<WfdiscDao> storedWfdiscs = getListOfValuesOfClass(storedValues, WfdiscDao.class, 1);
    List<BeamDao> storedBeamDaos = getListOfValuesOfClass(storedValues, BeamDao.class, 1);

    assertEquals(wfdiscDaoList.size(), storedWfdiscs.size());
    assertEquals(beamDaoList.size(), storedBeamDaos.size());

    for (WfdiscDao wfdiscDao : storedWfdiscs) {
      assertEquals(defaultId, wfdiscDao.getId());
      assertEquals(defaultEndTime.plus(copiedDataTimeShift), wfdiscDao.getEndTime());
    }

    waveformDataSimulator.cleanup();
    verify(bridgedDataSourceRepositoryJpa, times(1)).cleanupData();

    List<WfdiscDao> wfdiscDaoList2 = List.of(wfdiscDao1, wfdiscDao2);
    List<BeamDao> beamDaoList2 = List.of(beamDao1, beamDao2);
    when(wfdiscDatabaseConnectorInstance.findWfdiscsByTimeRange(any(), any()))
      .thenReturn(wfdiscDaoList2);
    when(beamDatabaseConnector.findBeamsByWfid(any()))
      .thenReturn(beamDaoList2);

    waveformDataSimulator.loadData(STAGE_TEST, startTime, endTime, copiedDataTimeShift);
    verify(wfdiscDatabaseConnectorInstance, times(2))
      .findWfdiscsByTimeRange(startTime, endTime);
    verify(beamDatabaseConnector, times(2))
      .findBeamsByWfid(any());
    verify(bridgedDataSourceRepositoryJpa, times(4)).store(recordCaptor2.capture());
    storedValues = recordCaptor2.getAllValues();
    storedWfdiscs = getListOfValuesOfClass(storedValues, WfdiscDao.class, 2);
    storedBeamDaos = getListOfValuesOfClass(storedValues, BeamDao.class, 2);
    assertEquals(wfdiscDaoList2.size(), storedWfdiscs.size());
    assertEquals(beamDaoList2.size(), storedBeamDaos.size());
  }

  @Test
  void testPreload() {

    Instant endTime = Instant.parse("2000-04-01T05:00:00Z");
    Instant startTime = endTime.minus(Duration.ofHours(5));
    Instant simulationStartTime = Instant.parse("2021-08-12T05:00:00Z");
    Duration operationalTimePeriod = Duration.ofDays(2);
    Duration calibUpdateFrequency = Duration.ofHours(7);
    int calibUpdatePercentage = 12;

    WfdiscDao wfdiscDao1 = new WfdiscDao(CSSDaoTestFixtures.WFDISC_DAO_1);
    wfdiscDao1.setTime(startTime);
    wfdiscDao1.setjDate(startTime);
    wfdiscDao1.setEndTime(endTime);
    WfdiscDao wfdiscDao2 = new WfdiscDao(CSSDaoTestFixtures.WFDISC_DAO_2);
    wfdiscDao2.setTime(startTime);
    wfdiscDao2.setjDate(startTime);
    wfdiscDao2.setEndTime(endTime);
    List<WfdiscDao> wfdiscDaoList = List.of(wfdiscDao1, wfdiscDao2);

    when(wfdiscDatabaseConnectorInstance.findWfdiscsByTimeRange(any(), any()))
      .thenReturn(wfdiscDaoList);

    waveformDataSimulator.preloadData(startTime, endTime, simulationStartTime,
      operationalTimePeriod, calibUpdateFrequency, calibUpdatePercentage);

    verify(wfdiscDatabaseConnectorInstance, times(1))
      .findWfdiscsByTimeRange(startTime, endTime);
    verify(bridgedDataSourceRepositoryJpa, times(1)).store(recordCaptor.capture());
    List<List<Object>> storedValues = recordCaptor.getAllValues();
    List<WfdiscDao> storedWfdiscs = getListOfValuesOfClass(storedValues, WfdiscDao.class, 1);

    assertEquals(18, storedWfdiscs.size());
  }

  private <T> List<T> getListOfValuesOfClass(List<List<Object>> listOfObjects, Class<T> clazz, int times) {

    int i = 0;

    for (List<Object> storedList : listOfObjects) {

      if (!storedList.isEmpty() && clazz.isInstance(storedList.get(0))) {
        i++;
        if (times == i) {
          return (List<T>) storedList;
        }
      }
    }

    return null;
  }
}
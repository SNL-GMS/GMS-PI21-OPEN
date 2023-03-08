package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.stationdefinition.dao.css.WfTagDao;
import gms.shared.stationdefinition.dao.css.enums.TagName;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArrivalDataSimulatorTest {

  @Mock
  private BridgedDataSourceRepository bridgedDataSourceRepositoryJpa;
  @Mock
  private ArrivalDatabaseConnector arrivalDatabaseConnector;
  @Mock
  private AmplitudeDatabaseConnector amplitudeDatabaseConnector;
  @Mock
  private WftagDatabaseConnector wftagDatabaseConnector;
  @Mock
  private AnalysisDataIdMapper idMapper;
  @Captor
  private ArgumentCaptor<List<Object>> recordCaptor;
  @Captor
  private ArgumentCaptor<List<Object>> recordCaptor2;

  private ArrivalDataSimulator arrivalDataSimulator;
  private Set<String> orderedStages = new HashSet<>();
  private static final String STAGE_1 = "stage1";
  private static final String STAGE_2 = "stage2";
  private static final String STAGE_3 = "stage3";
  private static final String STAGE_4 = "stage4";

  @BeforeEach
  public void testSetup() {

    // init signal detection repo map
    Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap = Map.of(
      STAGE_1, bridgedDataSourceRepositoryJpa,
      STAGE_2, bridgedDataSourceRepositoryJpa,
      STAGE_3, bridgedDataSourceRepositoryJpa,
      STAGE_4, bridgedDataSourceRepositoryJpa);

    // init arrival db connector map
    Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap = Map.of(
      STAGE_1, arrivalDatabaseConnector,
      STAGE_2, arrivalDatabaseConnector,
      STAGE_3, arrivalDatabaseConnector,
      STAGE_4, arrivalDatabaseConnector);

    // init amplitude db connector map
    Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap = Map.of(
      STAGE_1, amplitudeDatabaseConnector,
      STAGE_2, amplitudeDatabaseConnector,
      STAGE_3, amplitudeDatabaseConnector,
      STAGE_4, amplitudeDatabaseConnector);

    // create the arrival data simulator from the connector maps and repos
    arrivalDataSimulator = ArrivalDataSimulator.create(arrivalDatabaseConnectorMap,
      amplitudeDatabaseConnectorMap,
      wftagDatabaseConnector,
      signalDetectionBridgedDataSourceRepositoryMap,
      bridgedDataSourceRepositoryJpa,
      idMapper);

    orderedStages = signalDetectionBridgedDataSourceRepositoryMap.keySet();
  }

  //TODO: Need to update this to fix the wftag dao problems
  @Test
  void testLoadDataAndCleanup() {
    final long defaultId = 314159;
    final Instant startTime = Instant.now().minusSeconds(3600);
    final Instant endTime = Instant.now();
    final Instant defaultTime = startTime.minusSeconds(3600);
    final Duration copiedDataTimeShift = Duration.ofDays(2);
    final List<ArrivalDao> arrivalDaoList = new ArrayList<>();
    final List<WfTagDao> wfTagDaoList = new ArrayList<>();
    final List<WfTagDao> originalWfTagDaoList1 = new ArrayList<>();
    final List<AmplitudeDao> amplitudeDaoList = new ArrayList<>();

    ArrivalDao arrivalDao1 = new ArrivalDao(CSSDaoTestFixtures.ARRIVAL_DAO_1);
    arrivalDao1.getArrivalKey().setTime(defaultTime);
    arrivalDao1.setjDate(defaultTime);
    ArrivalDao arrivalDao2 = new ArrivalDao(CSSDaoTestFixtures.ARRIVAL_DAO_2);
    arrivalDao2.getArrivalKey().setTime(defaultTime);
    arrivalDao2.setjDate(defaultTime);
    ArrivalDao arrivalDao3 = new ArrivalDao(CSSDaoTestFixtures.ARRIVAL_DAO_3);
    arrivalDao3.getArrivalKey().setTime(defaultTime);
    arrivalDao3.setjDate(defaultTime);
    arrivalDaoList.add(arrivalDao1);
    arrivalDaoList.add(arrivalDao2);
    arrivalDaoList.add(arrivalDao3);

    long wfid = 326;
    long ampid = 55;
    for (ArrivalDao arrivalDao : arrivalDaoList) {
      wfTagDaoList.add(CSSDaoTestFixtures.createWfTagDao(arrivalDao.getId(), wfid, TagName.ARID));
      originalWfTagDaoList1.add(CSSDaoTestFixtures.createWfTagDao(arrivalDao.getId(), wfid, TagName.ARID));
      amplitudeDaoList.add(CSSDaoTestFixtures.createAmplitudeDao(ampid, arrivalDao));
      wfid++;
      ampid++;
    }

    when(arrivalDatabaseConnector.findArrivalsByTimeRange(any(), any()))
      .thenReturn(arrivalDaoList);
    when(wftagDatabaseConnector.findWftagsByTagIds(any()))
      .thenReturn(wfTagDaoList);
    when(amplitudeDatabaseConnector.findAmplitudesByArids(any()))
      .thenReturn(amplitudeDaoList);
    when(idMapper.getOrGenerate(any(AnalysisIdTag.class), anyLong(), anyLong())).thenReturn(
      defaultId);

    orderedStages.forEach(stage -> arrivalDataSimulator.loadData(stage, startTime, endTime, copiedDataTimeShift));
    verify(arrivalDatabaseConnector, times(4))
      .findArrivalsByTimeRange(startTime, endTime);
    verify(wftagDatabaseConnector, times(4))
      .findWftagsByTagIds(any());
    verify(bridgedDataSourceRepositoryJpa, times(12)).store(recordCaptor.capture());

    List<List<Object>> storedValues = recordCaptor.getAllValues();
    List<ArrivalDao> storedArrivals = getListOfValuesOfClass(storedValues, ArrivalDao.class, 1);
    List<WfTagDao> storedWftags = getListOfValuesOfClass(storedValues, WfTagDao.class, 1);
    List<AmplitudeDao> storedAmplitudes = getListOfValuesOfClass(storedValues, AmplitudeDao.class, 1);

    assertNotNull(storedArrivals);
    assertNotNull(storedWftags);
    assertNotNull(storedAmplitudes);
    assertEquals(arrivalDaoList.size(), storedArrivals.size());
    assertEquals(amplitudeDaoList.size(), storedAmplitudes.size());
    assertEquals(originalWfTagDaoList1.size(), storedWftags.size());

    for (ArrivalDao arrivalDao : storedArrivals) {
      assertEquals(defaultId, arrivalDao.getId());
      assertEquals(defaultTime.plus(copiedDataTimeShift), arrivalDao.getArrivalKey().getTime());
      assertEquals(defaultTime.plus(copiedDataTimeShift), arrivalDao.getjDate());
    }

    for (AmplitudeDao amplitudeDao : storedAmplitudes) {
      assertEquals(defaultId, amplitudeDao.getId());
      assertEquals(defaultTime.plus(copiedDataTimeShift), amplitudeDao.getTime());
      assertEquals(defaultTime.plus(copiedDataTimeShift), amplitudeDao.getAmplitudeTime());
    }

    arrivalDataSimulator.cleanup();

    verify(bridgedDataSourceRepositoryJpa, times(5)).cleanupData();

    when(arrivalDatabaseConnector.findArrivalsByTimeRange(any(), any()))
      .thenReturn(arrivalDaoList);
    when(wftagDatabaseConnector.findWftagsByTagIds(any()))
      .thenReturn(originalWfTagDaoList1);

    orderedStages.forEach(stage -> arrivalDataSimulator.loadData(stage, startTime, endTime, copiedDataTimeShift));

    verify(arrivalDatabaseConnector, times(8))
      .findArrivalsByTimeRange(startTime, endTime);
    verify(wftagDatabaseConnector, times(8))
      .findWftagsByTagIds(any());
    verify(bridgedDataSourceRepositoryJpa, times(24))
      .store(recordCaptor2.capture());

    storedValues = recordCaptor2.getAllValues();
    storedArrivals = getListOfValuesOfClass(storedValues, ArrivalDao.class, 2);
    storedWftags = getListOfValuesOfClass(storedValues, WfTagDao.class, 2);
    assertNotNull(storedArrivals);
    assertNull(storedWftags);
    assertEquals(arrivalDaoList.size(), storedArrivals.size());
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

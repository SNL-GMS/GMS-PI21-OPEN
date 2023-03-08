package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.stationdefinition.dao.css.WfTagDao;
import gms.shared.stationdefinition.dao.css.WfTagKey;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums.AnalysisIdTag;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The ArrivalDataSimulator is responsible for loading Arrival, Wftag, and Amplitude records into the simulation
 * database and removing these records from the simulation database.
 */
public class ArrivalDataSimulator implements AnalysisDataSimulator {

  private Map<String, ArrivalSeedData> arrivalSeedDataMap;
  private final Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap;
  private final Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap;
  private final WftagDatabaseConnector wftagDatabaseConnector;
  private final Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap;
  private final BridgedDataSourceRepository wftagBridgedDataSourceRepository;
  private final AnalysisDataIdMapper analysisDataIdMapper;
  private final Set<WfTagKey> wftagKeys;
  private static final long NA_VALUE_PARID = -1;

  private static final Logger logger = LoggerFactory.getLogger(ArrivalDataSimulator.class);

  private ArrivalDataSimulator(Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap,
    Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap,
    WftagDatabaseConnector wftagDatabaseConnector,
    Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap,
    BridgedDataSourceRepository wftagBridgedDataSourceRepository,
    AnalysisDataIdMapper analysisDataIdMapper) {
    this.arrivalDatabaseConnectorMap = arrivalDatabaseConnectorMap;
    this.amplitudeDatabaseConnectorMap = amplitudeDatabaseConnectorMap;
    this.wftagDatabaseConnector = wftagDatabaseConnector;
    this.signalDetectionBridgedDataSourceRepositoryMap = signalDetectionBridgedDataSourceRepositoryMap;
    this.wftagBridgedDataSourceRepository = wftagBridgedDataSourceRepository;
    this.analysisDataIdMapper = analysisDataIdMapper;
    wftagKeys = new HashSet<>();

    initializeArrivalSeedDataMap(arrivalDatabaseConnectorMap.keySet());
  }

  public static ArrivalDataSimulator create(Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap,
    Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap,
    WftagDatabaseConnector wftagDatabaseConnector,
    Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap,
    BridgedDataSourceRepository wftagBridgedDataSourceRepository,
    AnalysisDataIdMapper analysisDataIdMapper) {

    return new ArrivalDataSimulator(arrivalDatabaseConnectorMap,
      amplitudeDatabaseConnectorMap,
      wftagDatabaseConnector,
      signalDetectionBridgedDataSourceRepositoryMap,
      wftagBridgedDataSourceRepository,
      analysisDataIdMapper);
  }

  /**
   * loads Arrival, Wftag, and Amplitude records into the simulation database
   *
   * @param stageId - stageId string for querying particular stage
   * @param seedDataStartTime - start time of seed data to retrieve from bridged database
   * @param seedDataEndTime - end time of seed data to retrieve from bridged database
   * @param copiedDataTimeShift - amount of time to shift data by
   * to new ids generated for storing in the database
   */
  @Override
  public void loadData(String stageId, Instant seedDataStartTime, Instant seedDataEndTime,
    Duration copiedDataTimeShift) {

    // update arrival seed data map before iterating and loading
    updateSeedDataMap(stageId, seedDataStartTime, seedDataEndTime);

    // iterate through the arrival seed data map and store values to repository
    var arrivalSeedData = arrivalSeedDataMap.get(stageId);
    var dataSourceRepo = signalDetectionBridgedDataSourceRepositoryMap.get(stageId);

    List<ArrivalDao> arrivalDaos = arrivalSeedData.getArrivals().stream()
      .map(ArrivalDao::new)
      .map(arrivalDao -> {
        long oldArid = arrivalDao.getId();
        long newArid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldArid, -1);
        updateAridAndTime(arrivalDao, copiedDataTimeShift, newArid);
        return arrivalDao;
      })
      .collect(Collectors.toList());

    List<WfTagDao> wfTagDaos = arrivalSeedData.getWfTags().stream()
      .map(WfTagDao::new)
      .map(wfTagDao -> {
        long oldWfid = wfTagDao.getWfTagKey().getWfId();
        long oldArid = wfTagDao.getWfTagKey().getId();
        long newWfid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.WFID, oldWfid, -1);
        long newArid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldArid, -1);

        wfTagDao.getWfTagKey().setWfId(newWfid);
        wfTagDao.getWfTagKey().setId(newArid);

        return wfTagDao;
      })
      .collect(Collectors.toList());

    List<AmplitudeDao> amplitudeDaos = arrivalSeedData.getAmplitudeRecords().stream()
      .map(AmplitudeDao::new)
      .map(amplitudeDao -> {

        long oldAmpid = amplitudeDao.getId();
        long oldArid = amplitudeDao.getArrivalId();

        long oldParid = amplitudeDao.getPredictedArrivalId();
        long newParid = oldParid;

        //check if parid is not N/A value
        if (oldParid != NA_VALUE_PARID) {
          newParid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldParid, -1);
        }
        long newAmpid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.AMPID, oldAmpid, -1);
        long newArid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.ARID, oldArid, -1);

        amplitudeDao.setId(newAmpid);
        amplitudeDao.setArrivalId(newArid);
        amplitudeDao.setPredictedArrivalId(newParid);
        updateAmplitudeTime(amplitudeDao, copiedDataTimeShift);

        return amplitudeDao;
      })
      .collect(Collectors.toList());

    dataSourceRepo.store(arrivalDaos);
    dataSourceRepo.store(amplitudeDaos);
    wftagBridgedDataSourceRepository.store(wfTagDaos);
  }

  /**
   * removes Arrival, Wftag, and Amplitude records from simulator database
   */
  @Override
  public void cleanup() {
    arrivalSeedDataMap.clear();
    signalDetectionBridgedDataSourceRepositoryMap.values().forEach(BridgedDataSourceRepository::cleanupData);
    wftagBridgedDataSourceRepository.cleanupData();
  }

  /**
   * Initialize the {@link ArrivalSeedData} hashmap for each stageId
   *
   * @param stageKeys set of stage keys from the {@link ArrivalDatabaseConnector} map
   */
  private void initializeArrivalSeedDataMap(Set<String> stageKeys) {
    arrivalSeedDataMap = new HashMap<>();
    stageKeys.forEach(key -> arrivalSeedDataMap.put(key, null));
  }

  /**
   * Update {@link ArrivalSeedData} hashmap with updated daos for each workflow stage
   *
   * @param stageId - stageId string for updating arrival seed data
   * @param seedDataStartTime seed data start
   * @param seedDataEndTime seed data end
   */
  private void updateSeedDataMap(String stageId, Instant seedDataStartTime, Instant seedDataEndTime) {

    // iterate through the arrivalSeedDataMap by stage id and query for stage daos
    var arrivalSeedData = arrivalSeedDataMap.get(stageId);
    if (arrivalSeedData == null || !arrivalSeedData.getTimeRange().lowerEndpoint().equals(seedDataStartTime) ||
      !arrivalSeedData.getTimeRange().upperEndpoint().equals(seedDataEndTime)) {

      // stream through all stage arrival connectors and query initial arrivals
      List<ArrivalDao> initialArrivalSeedData = arrivalDatabaseConnectorMap.get(stageId)
        .findArrivalsByTimeRange(seedDataStartTime, seedDataEndTime);

      logger.info("Arrival seed data size for stage {}: {}", stageId, initialArrivalSeedData.size());

      List<Long> arids = initialArrivalSeedData.stream()
        .map(ArrivalDao::getId)
        .distinct()
        .collect(Collectors.toList());

      // query for current stage wftags and remove any duplicates
      List<WfTagDao> initialWftagData = wftagDatabaseConnector.findWftagsByTagIds(arids);
      initialWftagData.removeIf(dao -> wftagKeys.contains(dao.getWfTagKey()));
      wftagKeys.addAll(initialWftagData.stream()
        .map(WfTagDao::getWfTagKey)
        .collect(Collectors.toSet()));

      logger.info("Wftag seed data size for stage {}: {}", stageId, initialWftagData.size());

      List<AmplitudeDao> initialAmplitudeData = amplitudeDatabaseConnectorMap.get(stageId)
        .findAmplitudesByArids(arids);

      logger.info("Amplitude seed data size for stage {}: {}", stageId, initialAmplitudeData.size());

      // update the current entry with new ArrivalSeedData object
      arrivalSeedDataMap.put(stageId, (ArrivalSeedData.create(initialArrivalSeedData,
        initialAmplitudeData,
        initialWftagData,
        seedDataStartTime,
        seedDataEndTime)));
    }
  }

  private void updateAridAndTime(ArrivalDao arrivalDao, Duration copiedDataTimeShift, long newArid) {
    arrivalDao.getArrivalKey().setTime(arrivalDao.getArrivalKey().getTime().plus(copiedDataTimeShift));
    arrivalDao.setjDate(arrivalDao.getjDate().plus(copiedDataTimeShift));
    arrivalDao.setId(newArid);
  }

  private void updateAmplitudeTime(AmplitudeDao amplitudeDao, Duration copiedDataTimeShift) {
    amplitudeDao.setTime(amplitudeDao.getTime().plus(copiedDataTimeShift));
    amplitudeDao.setAmplitudeTime(amplitudeDao.getAmplitudeTime().plus(copiedDataTimeShift));
  }
}

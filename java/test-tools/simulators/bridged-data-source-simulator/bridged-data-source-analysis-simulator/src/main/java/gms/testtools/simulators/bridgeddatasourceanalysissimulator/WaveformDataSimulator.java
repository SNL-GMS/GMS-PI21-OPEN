package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.enums.AnalysisIdTag;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The WaveformDataSimulator is responsible for loading Wfdisc and Beam records into the simulation database
 * and removing these records from the simulation database.
 */
public class WaveformDataSimulator implements AnalysisDataSimulator {

  private static final long MAX_WFID = (long) 1E18;

  private WaveformSeedData waveformSeedData;
  private final WfdiscDatabaseConnector wfdiscDatabaseConnector;
  private final BeamDatabaseConnector beamDatabaseConnector;
  private final BridgedDataSourceRepository bridgedDataSourceRepository;
  private final AnalysisDataIdMapper analysisDataIdMapper;
  //Calib changes are spread out over seedDataLength*MAX_INCREMENT period of time, instead of happening all at the same time
  private static final int MAX_INCREMENT = 5;

  private static final Logger logger = LoggerFactory.getLogger(WaveformDataSimulator.class);

  private WaveformDataSimulator(WfdiscDatabaseConnector wfdiscDatabaseConnector,
    BeamDatabaseConnector beamDatabaseConnector, BridgedDataSourceRepository bridgedDataSourceRepository,
    AnalysisDataIdMapper analysisDataIdMapper) {
    this.wfdiscDatabaseConnector = wfdiscDatabaseConnector;
    this.beamDatabaseConnector = beamDatabaseConnector;
    this.bridgedDataSourceRepository = bridgedDataSourceRepository;
    this.analysisDataIdMapper = analysisDataIdMapper;
  }

  public static WaveformDataSimulator create(WfdiscDatabaseConnector wfdiscDatabaseConnector,
    BeamDatabaseConnector beamDatabaseConnector, BridgedDataSourceRepository bridgedDataSourceRepository,
    AnalysisDataIdMapper analysisDataIdMapper) {
    return new WaveformDataSimulator(wfdiscDatabaseConnector, beamDatabaseConnector, bridgedDataSourceRepository, analysisDataIdMapper);
  }

  public void preloadData(Instant seedDataStartTime, Instant seedDataEndTime, Instant simulationStartTime,
    Duration operationalTimePeriod, Duration calibUpdateFrequency, double calibUpdatePercentage) {

    updateSeedData(seedDataStartTime, seedDataEndTime);
    var idMapper = new AnalysisDataIdMapper();

    var seedDataSetLength = Duration.between(seedDataStartTime, seedDataEndTime);
    Instant timeCursor = simulationStartTime.minus(seedDataSetLength);

    List<WfdiscDao> wfdiscDaosToStore = new ArrayList<>();

    while (timeCursor.isAfter(simulationStartTime.minus(operationalTimePeriod)) ||
      timeCursor.equals(simulationStartTime.minus(operationalTimePeriod))) {

      var copiedDataTimeShift = Duration.between(seedDataStartTime, timeCursor);

      var timeOffset = 0;
      for (WfdiscDao wfdiscDao : waveformSeedData.getWfdiscSeedData()) {

        var wfdiscToStore = new WfdiscDao(wfdiscDao);
        var timeElapsed =
          Duration.between(timeCursor, simulationStartTime.minus(seedDataSetLength.multipliedBy(timeOffset)));

        long numUpdates = timeElapsed.dividedBy(calibUpdateFrequency);
        numUpdates = numUpdates < 0 ? 0 : numUpdates;

        //update calib
        double calibUpdateVal = Math.pow(((100.0 + calibUpdatePercentage) / 100.0), numUpdates);
        wfdiscToStore.setCalib(wfdiscToStore.getCalib() * calibUpdateVal);

        long oldWfid = wfdiscToStore.getId();
        // Per architecture, we should avoid overlapping ids by simulating from opposite ends of the valid data
        // range for wfids.  It's known and accepted that on a long enough simulation, the preload and loaded data
        // may have overlapping wfids, producing a uniqueness constraint violation exception
        long newWfid = MAX_WFID - idMapper.getOrGenerate(AnalysisIdTag.WFID, oldWfid, -1);
        updateWfidAndTime(wfdiscToStore, copiedDataTimeShift, newWfid);
        wfdiscDaosToStore.add(wfdiscToStore);

        timeOffset = timeOffset == MAX_INCREMENT ? 0 : timeOffset + 1;
      }

      timeCursor = timeCursor.minus(seedDataSetLength);
      idMapper.clear();
    }

    logger.info("Operational period start time {}", timeCursor.plus(seedDataSetLength));

    bridgedDataSourceRepository.store(wfdiscDaosToStore);
    logger.info("{} Wfdiscs preloaded into simulation database", wfdiscDaosToStore.size());
  }

  /**
   * loads Wfdisc and Beam records into the simulation database
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

    updateSeedData(seedDataStartTime, seedDataEndTime);
    List<WfdiscDao> wfdiscDaosToStore = waveformSeedData.getWfdiscSeedData().stream()
      .map(WfdiscDao::new)
      .map(wfdiscDao -> {
        long oldWfid = wfdiscDao.getId();
        long newWfid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.WFID, oldWfid, -1);
        updateWfidAndTime(wfdiscDao, copiedDataTimeShift, newWfid);
        return wfdiscDao;
      })
      .collect(Collectors.toList());

    List<BeamDao> beamDaosToStore = waveformSeedData.getBeamSeedData().stream()
      .map(BeamDao::new)
      .map(beamDao -> {
        long oldWfid = beamDao.getWfId();
        long newWfid = analysisDataIdMapper.getOrGenerate(AnalysisIdTag.WFID, oldWfid, -1);
        beamDao.setWfId(newWfid);
        return beamDao;
      })
      .collect(Collectors.toList());

    bridgedDataSourceRepository.store(wfdiscDaosToStore);
    bridgedDataSourceRepository.store(beamDaosToStore);
  }

  /**
   * removes Wfdisc and Beam records from simulator database
   */
  @Override
  public void cleanup() {
    waveformSeedData = null;
    bridgedDataSourceRepository.cleanupData();
  }

  private void updateSeedData(Instant seedDataStartTime, Instant seedDataEndTime) {

    if (waveformSeedData == null || !waveformSeedData.getTimeRange().lowerEndpoint().equals(seedDataStartTime) || !waveformSeedData.getTimeRange().upperEndpoint().equals(seedDataEndTime)) {
      List<WfdiscDao> initialWfdiscSeedData = wfdiscDatabaseConnector.findWfdiscsByTimeRange(seedDataStartTime, seedDataEndTime);

      List<Long> wfids = initialWfdiscSeedData.stream()
        .map(WfdiscDao::getId)
        .distinct()
        .collect(Collectors.toList());

      Map<Long, WfdiscDao> wfdiscDaoMap = new HashMap<>();

      initialWfdiscSeedData.stream()
        .forEach(wfdiscDao -> {

          long wfid = wfdiscDao.getId();
          if (wfdiscDaoMap.put(wfid, wfdiscDao) != null) {
            logger.info("DUPLICATE WFID: {} for STA {} CHAN {}",
              wfdiscDao.getId(), wfdiscDao.getStationCode(), wfdiscDao.getChannelCode());
          }

        });
      logger.info("Wfdisc seed data size: {}", initialWfdiscSeedData.size());

      List<BeamDao> initialBeamData = beamDatabaseConnector.findBeamsByWfid(wfids);
      logger.info("Beam seed data size: {}", initialBeamData.size());

      waveformSeedData = WaveformSeedData.create(initialWfdiscSeedData, initialBeamData, seedDataStartTime, seedDataEndTime);
    }
  }


  private void updateWfidAndTime(WfdiscDao wfdiscDao, Duration copiedDataTimeShift, long newWfid) {

    wfdiscDao.setTime(wfdiscDao.getTime().plus(copiedDataTimeShift));
    if (!copiedDataTimeShift.isNegative() && Instant.MAX.minus(copiedDataTimeShift).isBefore(wfdiscDao.getEndTime())) {
      wfdiscDao.setEndTime(Instant.MAX);
    } else {
      wfdiscDao.setEndTime(wfdiscDao.getEndTime().plus(copiedDataTimeShift));
    }
    wfdiscDao.setjDate(wfdiscDao.getTime());
    wfdiscDao.setId(newWfid);
  }
}

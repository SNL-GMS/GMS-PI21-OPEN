package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.stationdefinition.database.connector.BeamDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WfdiscDatabaseConnector;
import gms.shared.stationdefinition.database.connector.WftagDatabaseConnector;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.util.BridgedDataSourceAnalysisSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Bridged Data Source Analysis Data Simulator is responsible for loading analysis data -
 * including waveforms, arrivals, origins, etc. - into the simulation database for a specified
 * simulation by copying and modifying records from the pre-installed seed data set into the
 * simulation data set. The Simulator loads an initial copy of the specified analysis data from the
 * seed set when the simulation is initialized. Once the simulation is started, the Simulator
 * periodically loads additional copies of the specified analysis data from the seed data set to
 * simulate ongoing data processing and storage.
 */
public class BridgedDataSourceAnalysisDataSimulator implements BridgedDataSourceDataSimulator {

  private static final Logger logger = LoggerFactory.getLogger(BridgedDataSourceAnalysisDataSimulator.class);

  private final Map<String, ArrivalDatabaseConnector> arrivalDatabaseConnectorMap;
  private final Map<String, AmplitudeDatabaseConnector> amplitudeDatabaseConnectorMap;
  private final BeamDatabaseConnector beamDatabaseConnector;
  private final Map<String, BridgedDataSourceRepository> originBridgedDataSourceRepositoryMap;
  private final Map<String, EventDatabaseConnector> eventDatabaseConnectorMap;
  private final Map<String, OriginErrDatabaseConnector> originErrDatabaseConnectorMap;
  private final Map<String, OriginSimulatorDatabaseConnector> originSimulatorDatabaseConnectorMap;
  private final WfdiscDatabaseConnector wfdiscDatabaseConnector;
  private final WftagDatabaseConnector wftagDatabaseConnector;
  private final Map<String, BridgedDataSourceRepository> signalDetectionBridgedDataSourceRepositoryMap;
  private final BridgedDataSourceRepository wftagBridgedDataSourceRepository;
  private final BridgedDataSourceRepository waveformBridgedDataSourceRepository;
  private final double calibUpdatePercentage;
  private final Set<String> orderedStages;
  private List<AnalysisDataSimulator> analysisDataSimulators;
  private WaveformDataSimulator waveformDataSimulator;
  private AnalysisDataIdMapper analysisDataIdMapper;
  private Instant lastDataLoadEndTime;

  private BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec;
  private Disposable loadingDisposable;

  private BridgedDataSourceAnalysisDataSimulator(BridgedDataSourceAnalysisSimulatorSpec analysisSimulatorSpec,
    int calibDelta) {
    this.arrivalDatabaseConnectorMap = analysisSimulatorSpec.getArrivalDatabaseConnectorMap();
    this.amplitudeDatabaseConnectorMap = analysisSimulatorSpec.getAmplitudeDatabaseConnectorMap();
    this.wfdiscDatabaseConnector = analysisSimulatorSpec.getWfdiscDatabaseConnector();
    this.wftagDatabaseConnector = analysisSimulatorSpec.getWftagDatabaseConnector();
    this.beamDatabaseConnector = analysisSimulatorSpec.getBeamDatabaseConnector();
    this.eventDatabaseConnectorMap = analysisSimulatorSpec.getEventDatabaseConnectorMap();
    this.originErrDatabaseConnectorMap = analysisSimulatorSpec.getOriginErrDatabaseConnectorMap();
    this.originSimulatorDatabaseConnectorMap = analysisSimulatorSpec.getOriginSimulatorDatabaseConnectorMap();
    this.signalDetectionBridgedDataSourceRepositoryMap = analysisSimulatorSpec.getSignalDetectionBridgedDataSourceRepositoryMap();
    this.wftagBridgedDataSourceRepository = analysisSimulatorSpec.getWftagBridgedDataSourceRepository();
    this.waveformBridgedDataSourceRepository = analysisSimulatorSpec.getWaveformBridgedDataSourceRepository();
    this.originBridgedDataSourceRepositoryMap = analysisSimulatorSpec.getOriginBridgedDataSourceRepositoryMap();
    this.calibUpdatePercentage = calibDelta;
    this.loadingDisposable = null;
    orderedStages = signalDetectionBridgedDataSourceRepositoryMap.keySet();
  }

  /**
   * Creates a {@link BridgedDataSourceAnalysisDataSimulator} given the required, non null
   * components.
   *
   * @param analysisSimulatorSpec - the {@link BridgedDataSourceAnalysisSimulatorSpec} builder
   * @param calibDeltaValue -       the configurable delta value for the calibration update shift delta
   * @return {@link BridgedDataSourceAnalysisDataSimulator}
   */
  public static BridgedDataSourceAnalysisDataSimulator create(
    BridgedDataSourceAnalysisSimulatorSpec analysisSimulatorSpec,
    int calibDeltaValue) {
    Validate.notNull(analysisSimulatorSpec);
    Validate.validState(calibDeltaValue != 0);

    return new BridgedDataSourceAnalysisDataSimulator(analysisSimulatorSpec,
      calibDeltaValue);
  }

  /**
   * Initialize analysis data - including waveforms, arrivals, origins, etc. - for the specified
   * simulation based on the provided spec, copying and modifying data from the seed data set.
   *
   * @param bridgeSimulatorSpec - An {@link BridgedDataSourceSimulatorSpec} to provided the
   * simulation specification details.
   */
  @Override
  public void initialize(BridgedDataSourceSimulatorSpec bridgeSimulatorSpec) {

    analysisDataSimulators = new ArrayList<>();
    analysisDataIdMapper = new AnalysisDataIdMapper();

    //List ordering is important, WaveformDataSimulator must be added before ArrivalDataSimulator
    waveformDataSimulator = WaveformDataSimulator.create(wfdiscDatabaseConnector, beamDatabaseConnector,
      waveformBridgedDataSourceRepository, analysisDataIdMapper);

    analysisDataSimulators.add(
      ArrivalDataSimulator.create(arrivalDatabaseConnectorMap, amplitudeDatabaseConnectorMap,
        wftagDatabaseConnector, signalDetectionBridgedDataSourceRepositoryMap,
        wftagBridgedDataSourceRepository, analysisDataIdMapper));

    analysisDataSimulators.add(
      OriginDataSimulator.create(originSimulatorDatabaseConnectorMap,
        eventDatabaseConnectorMap,
        originErrDatabaseConnectorMap,
        originBridgedDataSourceRepositoryMap,
        analysisDataIdMapper));

    logger.info("Initializing BridgedDataSourceAnalysisDataSimulator...");
    bridgedDataSourceSimulatorSpec = bridgeSimulatorSpec;

    final Instant seedDataStartTime = bridgedDataSourceSimulatorSpec.getSeedDataStartTime();
    final Instant seedDataEndTime = bridgedDataSourceSimulatorSpec.getSeedDataEndTime();
    final Instant simulationStartTime = bridgeSimulatorSpec.getSimulationStartTime();
    final Duration operationalTimePeriod = bridgeSimulatorSpec.getOperationalTimePeriod();

    final var seedDataSetLength = Duration.between(seedDataStartTime, seedDataEndTime);
    final Duration calibUpdateFrequency = bridgeSimulatorSpec.getCalibUpdateFrequency();
    final var start = Instant.now();

    logger.info("Seed data start time: {} ({})", seedDataStartTime,
      seedDataStartTime.toEpochMilli());
    logger.info("Seed data end time: {} ({})", seedDataEndTime, seedDataEndTime.toEpochMilli());
    logger.info("Seed data duration: {}ms (={}s ={}m ={}h)", seedDataSetLength.toMillis(),
      seedDataSetLength.toSeconds(), seedDataSetLength.toMinutes(), seedDataSetLength.toHours());
    logger.info("Simulation start time: {} ({})", simulationStartTime,
      simulationStartTime.toEpochMilli());
    logger.info("Operational time period: {}ms (={}s ={}m ={}h)", operationalTimePeriod.toMillis(),
      operationalTimePeriod.toSeconds(), operationalTimePeriod.toMinutes(),
      operationalTimePeriod.toHours());
    logger.info("Calibration update frequency: {}ms (={}s ={}m ={}h)",
      calibUpdateFrequency.toMillis(),
      calibUpdateFrequency.toSeconds(), calibUpdateFrequency.toMinutes(),
      calibUpdateFrequency.toHours());

    logger.info("Preloading Wfdiscs.");
    waveformDataSimulator.preloadData(seedDataStartTime, seedDataEndTime, simulationStartTime,
      operationalTimePeriod, calibUpdateFrequency, calibUpdatePercentage);

    logger.info("Preload of Wfdiscs into simulation database completed.");

    Instant timeCursor = simulationStartTime;

    logger.info("Loading analysis data up to current wall clock time.");
    while (timeCursor.isBefore(Instant.now())) {
      loadData(seedDataStartTime, seedDataEndTime, timeCursor);
      timeCursor = timeCursor.plus(seedDataSetLength);
    }

    logger.info("Loaded analysis data up to current wall clock time.");
    final var end = Instant.now();
    final var totalTime = Duration.between(start, end);
    logger.info("Initialization completed in {}ms", totalTime.toMillis());
  }

  @Override
  public void load(String placeholder) {
//    function not needed as it is handled by the controller but due to interface inheritance is
//    required to be in this class as well
  }

  /**
   * initialize a timer to periodically load additional analysis data (waveforms, arrivals, origins,
   * etc.) into the simulation database from the seed data set.
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be *
   * ignored.
   */
  @Override
  public void start(String placeholder) {

    Instant seedDataStartTime = bridgedDataSourceSimulatorSpec.getSeedDataStartTime();
    Instant seedDataEndTime = bridgedDataSourceSimulatorSpec.getSeedDataEndTime();

    final var seedDataSetLength =
      Duration.between(seedDataStartTime, seedDataEndTime);

    var waitBeforeFiring = Duration.between(Instant.now(), lastDataLoadEndTime);

    if (waitBeforeFiring.isNegative()) {
      waitBeforeFiring = Duration.ZERO;
    }

    loadingDisposable = Flux.interval(waitBeforeFiring, seedDataSetLength)
      .subscribe(value ->
      {
        logger.info("Next load time for entry {} will be at {}",
          value + 2, lastDataLoadEndTime.plusSeconds(
            Duration.between(
              seedDataStartTime, seedDataEndTime).toSeconds()));
        loadData(seedDataStartTime, seedDataEndTime, Instant.now());
      });
  }

  /**
   * Cancel the timer that periodically loads additional simulation data into the simulation
   * database from the seed data set.
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be *
   * ignored.
   */
  @Override
  public void stop(String placeholder) {

    if (loadingDisposable != null) {
      loadingDisposable.dispose();
      loadingDisposable = null;
    }
  }

  /**
   * loads Analysis record data into simulator database based on the given parameters.
   *
   * @param seedDataStartTime - start time of seed data to retrieve from bridged database
   * @param seedDataEndTime - end time of seed data to retrieve from bridged database
   * @param copiedDataStartTime - the new start time for the analysis data that will be loaded into
   * the database
   */
  public void loadData(Instant seedDataStartTime, Instant seedDataEndTime,
    Instant copiedDataStartTime) {
    var copiedDataTimeShift = Duration.between(seedDataStartTime, copiedDataStartTime);

    // load data for new analysis simulators
    waveformDataSimulator.loadData("", seedDataStartTime, seedDataEndTime, copiedDataTimeShift);
    for (String stageId : orderedStages) {
      analysisDataSimulators.forEach(analysisDataSimulator ->
        analysisDataSimulator.loadData(stageId, seedDataStartTime, seedDataEndTime, copiedDataTimeShift));
    }

    lastDataLoadEndTime = copiedDataStartTime.plus(
      Duration.between(bridgedDataSourceSimulatorSpec.getSeedDataStartTime(),
        bridgedDataSourceSimulatorSpec.getSeedDataEndTime()));

    logger.info("Copied seed data interval [{} to {}] to updated interval [{} to {}]",
      seedDataStartTime, seedDataEndTime, copiedDataStartTime, lastDataLoadEndTime);

    analysisDataIdMapper.clear();
  }

  /**
   * clear the current BridgedDataSourceSimulationSpec and deletes the simulation analysis data
   * (waveforms, arrivals, origins, etc.) from the simulation database (the seed data set is
   * unaffected).
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be *
   * ignored.
   */
  @Override
  public void cleanup(String placeholder) {

    if (analysisDataSimulators != null) {
      analysisDataSimulators.forEach(AnalysisDataSimulator::cleanup);
    }
    if (waveformDataSimulator != null) {
      waveformDataSimulator.cleanup();
    }

    this.waveformDataSimulator = null;
    this.analysisDataSimulators = null;
    this.analysisDataIdMapper = null;
  }
}

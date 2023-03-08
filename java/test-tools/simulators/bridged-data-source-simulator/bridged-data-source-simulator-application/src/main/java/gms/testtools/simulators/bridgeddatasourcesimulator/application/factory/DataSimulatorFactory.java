package gms.testtools.simulators.bridgeddatasourcesimulator.application.factory;

import gms.shared.event.repository.connector.EventDatabaseConnector;
import gms.shared.event.repository.connector.OriginErrDatabaseConnector;
import gms.shared.event.repository.connector.OriginSimulatorDatabaseConnector;
import gms.shared.event.repository.factory.OriginSimulatorDatabaseConnectorFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.signaldetection.database.connector.AmplitudeDatabaseConnector;
import gms.shared.signaldetection.database.connector.ArrivalDatabaseConnector;
import gms.shared.signaldetection.database.connector.factory.SignalDetectionDatabaseConnectorFactory;
import gms.shared.stationdefinition.database.connector.factory.StationDefinitionDatabaseConnectorFactory;
import gms.shared.utilities.bridge.database.BridgedEntityManagerFactoryProvider;
import gms.shared.workflow.repository.IntervalDatabaseConnectorFactory;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.BridgedDataSourceAnalysisDataSimulator;
import gms.testtools.simulators.bridgeddatasourceanalysissimulator.util.BridgedDataSourceAnalysisSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourceintervalsimulator.BridgedDataSourceIntervalSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.application.BridgedDataSourceSimulatorController;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceIntervalRepositoryJpa;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceOriginRepositoryJpa;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceRepository;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceSignalDetectionRepositoryJpa;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceStationRepositoryJpa;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceWaveformRepositoryJpa;
import gms.testtools.simulators.bridgeddatasourcesimulator.repository.BridgedDataSourceWftagRepositoryJpa;
import gms.testtools.simulators.bridgeddatasourcestationsimulator.BridgedDataSourceStationSimulator;
import org.apache.commons.lang3.Validate;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a utility class for building the dependencies of a Bridged Data Source Simulator Service
 * in singleton patterns. This will help simplify the initialization of a {@link
 * BridgedDataSourceSimulatorController}.
 */
public class DataSimulatorFactory {

  private static final String SOCCPRO_KEY = "soccpro";
  private static final String AL1_KEY = "al1";
  private static final String AL2_KEY = "al2";

  private static final String ANALYSIS_SIMULATION_PERSISTENCE_UNIT = "gms_analysis_simulation";
  private static final String ORIGIN_PERSISTENCE_UNIT = "gms_origin_seed";
  private static final String ORIGIN_SIMULATION_PERSISTENCE_UNIT = "gms_origin_simulation";
  private static final String SIGNAL_SEED_PERSISTENCE_UNIT = "gms_signal_seed";
  private static final String SIGNAL_SIMULATION_PERSISTENCE_UNIT = "gms_signal_simulation";
  private static final String STATION_DEFINITION_PERSISTENCE_UNIT = "gms_station_definition_seed";
  private static final String STATION_DEFINITION_SIMULATION_PERSISTENCE_UNIT = "gms_station_definition_simulation";
  private static final String WORKFLOW_PERSISTENCE_UNIT = "workflow-dao-seed";
  private static final String WORKFLOW_SIMULATION_PERSISTENCE_UNIT = "workflow-dao-simulation";

  private static final String SEED_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.seed";
  private static final String SIMULATION_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.simulation";
  private static final String SIMULATION_SOCCPRO_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.sim-soccpro";
  private static final String SEED_SOCCPRO_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.seed-soccpro";
  private static final String SIMULATION_AL1_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.sim-al1";
  private static final String SEED_AL1_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.seed-al1";
  private static final String SIMULATION_AL2_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.sim-al2";
  private static final String SEED_AL2_SYSTEM_CONFIG_ROOT = "bridged-data-source-simulator.seed-al2";
  private final SystemConfig seedConfig;
  private final SystemConfig simulationConfig;

  // seed data signal detection system configs
  private final SystemConfig seedSignalSoccproConfig;
  private final SystemConfig seedSignalAl1Config;
  private final SystemConfig seedSignalAl2Config;

  // seed data origin system configs
  private final SystemConfig seedOriginSoccproConfig;
  private final SystemConfig seedOriginAl1Config;
  private final SystemConfig seedOriginAl2Config;

  // simulation signal detection system configs
  private final SystemConfig simulationSignalSoccproConfig;
  private final SystemConfig simulationSignalAl1Config;
  private final SystemConfig simulationSignalAl2Config;

  // simulation origin system configs
  private final SystemConfig simulationOriginSoccproConfig;
  private final SystemConfig simulationOriginAl1Config;
  private final SystemConfig simulationOriginAl2Config;

  private BridgedDataSourceStationSimulator bridgedDataSourceStationSimulatorInstance;
  private BridgedDataSourceAnalysisDataSimulator bridgedDataSourceAnalysisDataSimulatorInstance;
  private BridgedDataSourceIntervalSimulator bridgedDataSourceIntervalSimulatorInstance;

  private final BridgedEntityManagerFactoryProvider seedDataBridgedEntityManagerFactoryProvider;
  private final BridgedEntityManagerFactoryProvider simulationBridgedEntityManagerFactoryProvider;

  private final List<Runnable> cleanupHookList = new ArrayList<>();

  private final int calibDeltaValue;

  private DataSimulatorFactory(
    BridgedEntityManagerFactoryProvider seedDataBridgedEntityManagerFactoryProvider,
    BridgedEntityManagerFactoryProvider simulationBridgedEntityManagerFactoryProvider,
    SystemConfig systemConfig,
    int calibDeltaValue
  ) {
    this.seedDataBridgedEntityManagerFactoryProvider = seedDataBridgedEntityManagerFactoryProvider;
    this.simulationBridgedEntityManagerFactoryProvider = simulationBridgedEntityManagerFactoryProvider;

    this.seedConfig = systemConfig;
    this.simulationConfig = systemConfig;
    this.simulationSignalSoccproConfig = systemConfig;
    this.simulationSignalAl1Config = systemConfig;
    this.simulationSignalAl2Config = systemConfig;
    this.seedSignalSoccproConfig = systemConfig;
    this.seedSignalAl1Config = systemConfig;
    this.seedSignalAl2Config = systemConfig;
    this.seedOriginSoccproConfig = systemConfig;
    this.seedOriginAl1Config = systemConfig;
    this.seedOriginAl2Config = systemConfig;
    this.simulationOriginSoccproConfig = systemConfig;
    this.simulationOriginAl1Config = systemConfig;
    this.simulationOriginAl2Config = systemConfig;
    this.calibDeltaValue = calibDeltaValue;
  }

  private DataSimulatorFactory(
    BridgedEntityManagerFactoryProvider seedDataBridgedEntityManagerFactoryProvider,
    BridgedEntityManagerFactoryProvider simulationBridgedEntityManagerFactoryProvider,
    int calibDeltaValue
  ) {
    this.seedDataBridgedEntityManagerFactoryProvider = seedDataBridgedEntityManagerFactoryProvider;
    this.simulationBridgedEntityManagerFactoryProvider = simulationBridgedEntityManagerFactoryProvider;

    // create the global seed and simulation system configs
    this.seedConfig = SystemConfig.create(SEED_SYSTEM_CONFIG_ROOT);
    this.simulationConfig = SystemConfig.create(SIMULATION_SYSTEM_CONFIG_ROOT);

    // create the simulation signal multiple stage system configs
    this.simulationSignalSoccproConfig = SystemConfig.create(SIMULATION_SOCCPRO_SYSTEM_CONFIG_ROOT);
    this.simulationSignalAl1Config = SystemConfig.create(SIMULATION_AL1_SYSTEM_CONFIG_ROOT);
    this.simulationSignalAl2Config = SystemConfig.create(SIMULATION_AL2_SYSTEM_CONFIG_ROOT);

    // create the seed signal multiple stage system configs
    this.seedSignalSoccproConfig = SystemConfig.create(SEED_SOCCPRO_SYSTEM_CONFIG_ROOT);
    this.seedSignalAl1Config = SystemConfig.create(SEED_AL1_SYSTEM_CONFIG_ROOT);
    this.seedSignalAl2Config = SystemConfig.create(SEED_AL2_SYSTEM_CONFIG_ROOT);

    // create the simulation origin multiple stage system configs
    this.simulationOriginSoccproConfig = SystemConfig.create(SIMULATION_SOCCPRO_SYSTEM_CONFIG_ROOT);
    this.simulationOriginAl1Config = SystemConfig.create(SIMULATION_AL1_SYSTEM_CONFIG_ROOT);
    this.simulationOriginAl2Config = SystemConfig.create(SIMULATION_AL2_SYSTEM_CONFIG_ROOT);

    // create the seed origin multiple stage system configs
    this.seedOriginSoccproConfig = SystemConfig.create(SEED_SOCCPRO_SYSTEM_CONFIG_ROOT);
    this.seedOriginAl1Config = SystemConfig.create(SEED_AL1_SYSTEM_CONFIG_ROOT);
    this.seedOriginAl2Config = SystemConfig.create(SEED_AL2_SYSTEM_CONFIG_ROOT);

    this.calibDeltaValue = calibDeltaValue;
  }

  /**
   * Initializes a factory to manage the creation of items in the dependency graph of a Bridged Data
   * Source Simulator Service. This is only used for testing.
   *
   * @param seedDataBridgedEntityManagerFactoryProvider BridgedEntityManagerFactoryProvider for seed
   * data
   * @param simulationBridgedEntityManagerFactoryProvider BridgedEntityManagerFactoryProvider for the
   * simulated data.
   * @param systemConfig system config to use, likely to ba a mock
   * @param calibDeltaValue the percent by which to update the calibration value
   */
  static DataSimulatorFactory create(
    BridgedEntityManagerFactoryProvider seedDataBridgedEntityManagerFactoryProvider,
    BridgedEntityManagerFactoryProvider simulationBridgedEntityManagerFactoryProvider,
    SystemConfig systemConfig,
    int calibDeltaValue
  ) {
    Validate.notNull(seedDataBridgedEntityManagerFactoryProvider);
    Validate.notNull(simulationBridgedEntityManagerFactoryProvider);
    Validate.notNull(systemConfig);

    return new DataSimulatorFactory(
      seedDataBridgedEntityManagerFactoryProvider,
      simulationBridgedEntityManagerFactoryProvider,
      systemConfig, calibDeltaValue
    );
  }

  /**
   * Initializes a factory to manage the creation of items in the dependency graph of a Bridged Data
   * Source Simulator Service.
   *
   * @param seedDataBridgedEntityManagerFactoryProvider BridgedEntityManagerFactoryProvider for seed
   * data
   * @param simulationBridgedEntityManagerFactoryProvider BridgedEntityManagerFactoryProvider for the
   * simulated data.
   * @param calibDeltaValue the percent by which to update the calibration value
   */
  public static DataSimulatorFactory create(
    BridgedEntityManagerFactoryProvider seedDataBridgedEntityManagerFactoryProvider,
    BridgedEntityManagerFactoryProvider simulationBridgedEntityManagerFactoryProvider,
    int calibDeltaValue
  ) {
    Validate.notNull(seedDataBridgedEntityManagerFactoryProvider);
    Validate.notNull(simulationBridgedEntityManagerFactoryProvider);

    return new DataSimulatorFactory(
      seedDataBridgedEntityManagerFactoryProvider,
      simulationBridgedEntityManagerFactoryProvider,
      calibDeltaValue
    );
  }


  /**
   * Initializes or returns a {@link BridgedDataSourceStationSimulator} using a singleton pattern.
   *
   * @return {@link DataSimulatorFactory#bridgedDataSourceStationSimulatorInstance}
   */
  public BridgedDataSourceStationSimulator getBridgedDataSourceStationSimulatorInstance() {
    if (bridgedDataSourceStationSimulatorInstance == null) {

      var stationDefinitionSeedDataJpaRepositoryFactory =
        StationDefinitionDatabaseConnectorFactory.create(
          addToCleanupHooks(
            seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
              STATION_DEFINITION_PERSISTENCE_UNIT, seedConfig
            )
          )
        );

      bridgedDataSourceStationSimulatorInstance = BridgedDataSourceStationSimulator
        .create(
          stationDefinitionSeedDataJpaRepositoryFactory.getNetworkDatabaseConnectorInstance(),
          stationDefinitionSeedDataJpaRepositoryFactory
            .getAffiliationDatabaseConnectorInstance(),
          stationDefinitionSeedDataJpaRepositoryFactory.getSiteDatabaseConnectorInstance(),
          stationDefinitionSeedDataJpaRepositoryFactory.getSiteChanDatabaseConnectorInstance(),
          stationDefinitionSeedDataJpaRepositoryFactory.getSensorDatabaseConnectorInstance(),
          stationDefinitionSeedDataJpaRepositoryFactory
            .getInstrumentDatabaseConnectorInstance(),

          BridgedDataSourceStationRepositoryJpa.create(
            addToCleanupHooks(
              simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
                STATION_DEFINITION_SIMULATION_PERSISTENCE_UNIT, simulationConfig
              )
            )
          )
        );
    }
    return bridgedDataSourceStationSimulatorInstance;
  }

  /**
   * Initializes or returns a {@link BridgedDataSourceAnalysisDataSimulator} using a singleton pattern.
   *
   * @return {@link DataSimulatorFactory#bridgedDataSourceAnalysisDataSimulatorInstance}
   */
  public BridgedDataSourceAnalysisDataSimulator getBridgedDataSourceAnalysisSimulatorInstance() {
    if (bridgedDataSourceAnalysisDataSimulatorInstance == null) {

      var stationDefinitionDatabaseConnectorFactory =
        StationDefinitionDatabaseConnectorFactory.create(
          addToCleanupHooks(
            seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
              STATION_DEFINITION_PERSISTENCE_UNIT, seedConfig
            )
          )
        );

      // create stage to signal database connectory factory map
      //      seedSignalDetproConfig
      var signalDatabaseConnectorFactoryMap =
        createSignalDatabaseConnectorFactoryMap(seedSignalSoccproConfig,
          seedSignalAl1Config, seedSignalAl2Config);

      //      seedOriginDetproConfig
      var originDatabaseConnectorFactoryMap =
        createOriginDatabaseConnectorFactoryMap(seedOriginSoccproConfig,
          seedOriginAl1Config, seedOriginAl2Config);

      // create stage to arrival database connectors
      var arrivalDatabaseConnectorMap =
        createArrivalDatabaseConnectorMap(signalDatabaseConnectorFactoryMap);

      // create stage to amplitude database connectors
      var amplitudeDatabaseConnectorMap =
        createAmplitudeDatabaseConnectorMap(signalDatabaseConnectorFactoryMap);

      // create stage to event database connectors
      var eventDatabaseConnectorMap =
        createEventDatabaseConnectorMap(originDatabaseConnectorFactoryMap);

      // create stage to origin err db connectors
      var originErrDatabaseConnectorMap =
        createOriginErrDatabaseConnectorMap(originDatabaseConnectorFactoryMap);

      // create stage to origin sim db connectors
      var originSimulatorDatabaseConnectorMap =
        createOriginSimulatorDatabaseConnectorMap(originDatabaseConnectorFactoryMap);

      var analysisSimulatorSpec = BridgedDataSourceAnalysisSimulatorSpec.builder()
        .setArrivalDatabaseConnectorMap(arrivalDatabaseConnectorMap)
        .setAmplitudeDatabaseConnectorMap(amplitudeDatabaseConnectorMap)
        .setEventDatabaseConnectorMap(eventDatabaseConnectorMap)
        .setOriginErrDatabaseConnectorMap(originErrDatabaseConnectorMap)
        .setOriginSimulatorDatabaseConnectorMap(originSimulatorDatabaseConnectorMap)
        .setWfdiscDatabaseConnector(stationDefinitionDatabaseConnectorFactory.getWfdiscDatabaseConnectorInstance())
        .setWftagDatabaseConnector(stationDefinitionDatabaseConnectorFactory.getWftagfDatabaseConnectorInstance())
        .setBeamDatabaseConnector(stationDefinitionDatabaseConnectorFactory.getBeamDatabaseConnectorInstance())
        .setSignalDetectionBridgedDataSourceRepositoryMap(createSignalDetectionBridgedDataSourceRepositoryMap(
          simulationSignalSoccproConfig, simulationSignalAl1Config, simulationSignalAl2Config))
        .setOriginBridgedDataSourceRepositoryMap(createOriginBridgedDataSourceRepositoryMap(
          simulationOriginSoccproConfig, simulationOriginAl1Config, simulationOriginAl2Config))
        .setWaveformBridgedDataSourceRepository(BridgedDataSourceWaveformRepositoryJpa.create(
          addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
            ANALYSIS_SIMULATION_PERSISTENCE_UNIT, simulationConfig))))
        .setWftagBridgedDataSourceRepository(BridgedDataSourceWftagRepositoryJpa.create(
          addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
            ANALYSIS_SIMULATION_PERSISTENCE_UNIT, simulationConfig))))
        .build();

      // create bridged datasource analysis data simulator from nested connectors
      bridgedDataSourceAnalysisDataSimulatorInstance = BridgedDataSourceAnalysisDataSimulator
        .create(analysisSimulatorSpec, calibDeltaValue);
    }

    return bridgedDataSourceAnalysisDataSimulatorInstance;
  }

  /**
   * Initializes or returns a {@link BridgedDataSourceIntervalSimulator} using a singleton pattern.
   *
   * @return {@link DataSimulatorFactory#bridgedDataSourceIntervalSimulatorInstance}
   */
  public BridgedDataSourceIntervalSimulator getBridgedDataSourceIntervalSimulatorInstance() {
    if (bridgedDataSourceIntervalSimulatorInstance == null) {
      bridgedDataSourceIntervalSimulatorInstance = BridgedDataSourceIntervalSimulator
        .create(
          IntervalDatabaseConnectorFactory
            .create(
              addToCleanupHooks(
                seedDataBridgedEntityManagerFactoryProvider
                  .getEntityManagerFactory(WORKFLOW_PERSISTENCE_UNIT, seedConfig))
            )
            .getIntervalDatabaseConnectorInstance(),
          BridgedDataSourceIntervalRepositoryJpa.create(
            addToCleanupHooks(
              simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
                WORKFLOW_SIMULATION_PERSISTENCE_UNIT, simulationConfig
              )
            )
          )
        );
    }
    return bridgedDataSourceIntervalSimulatorInstance;
  }

  /**
   * Perform clean up, such as closing all of the entity menager factories.
   */
  public void cleanup() {
    cleanupHookList.forEach(Runnable::run);
  }

  private EntityManagerFactory addToCleanupHooks(EntityManagerFactory entityManagerFactory) {
    cleanupHookList.add(entityManagerFactory::close);
    return entityManagerFactory;
  }

  /**
   * Create map of stages to their corresponding {@link SignalDetectionDatabaseConnectorFactory}
   *
   * @param soccproConfig {@link SystemConfig} for soccpro account
   * @param al1Config {@link SystemConfig} for al1 account
   * @param al2Config {@link SystemConfig} for al2 account
   * @return map of stage to signal detection database connector factories
   */
  private Map<String, SignalDetectionDatabaseConnectorFactory> createSignalDatabaseConnectorFactoryMap(
    SystemConfig soccproConfig, SystemConfig al1Config, SystemConfig al2Config) {

    return Map.of(
      SOCCPRO_KEY, SignalDetectionDatabaseConnectorFactory.create(
        addToCleanupHooks(seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          SIGNAL_SEED_PERSISTENCE_UNIT, soccproConfig))),
      AL1_KEY, SignalDetectionDatabaseConnectorFactory.create(
        addToCleanupHooks(seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          SIGNAL_SEED_PERSISTENCE_UNIT, al1Config))),
      AL2_KEY, SignalDetectionDatabaseConnectorFactory.create(
        addToCleanupHooks(seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          SIGNAL_SEED_PERSISTENCE_UNIT, al2Config)))
    );
  }

  /**
   * Create map of stages to their corresponding {@link OriginSimulatorDatabaseConnectorFactory}
   *
   * @param soccproConfig {@link SystemConfig} for soccpro account
   * @param al1Config {@link SystemConfig} for al1 account
   * @param al2Config {@link SystemConfig} for al2 account
   * @return map of stage to origin database connector factories
   */
  private Map<String, OriginSimulatorDatabaseConnectorFactory> createOriginDatabaseConnectorFactoryMap(
    SystemConfig soccproConfig, SystemConfig al1Config, SystemConfig al2Config) {

    return Map.of(
      SOCCPRO_KEY, OriginSimulatorDatabaseConnectorFactory.create(
        addToCleanupHooks(seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          ORIGIN_PERSISTENCE_UNIT, soccproConfig))),
      AL1_KEY, OriginSimulatorDatabaseConnectorFactory.create(
        addToCleanupHooks(seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          ORIGIN_PERSISTENCE_UNIT, al1Config))),
      AL2_KEY, OriginSimulatorDatabaseConnectorFactory.create(
        addToCleanupHooks(seedDataBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          ORIGIN_PERSISTENCE_UNIT, al2Config)))
    );
  }

  /**
   * Create map of stages to their corresponding {@link BridgedDataSourceRepository}
   *
   * @param soccproConfig {@link SystemConfig} for soccpro account
   * @param al1Config {@link SystemConfig} for al1 account
   * @param al2Config {@link SystemConfig} for al2 account
   * @return map of stage ids to bridged data source repositories
   */
  private Map<String, BridgedDataSourceRepository> createSignalDetectionBridgedDataSourceRepositoryMap(
    SystemConfig soccproConfig, SystemConfig al1Config, SystemConfig al2Config) {

    return Map.of(
      SOCCPRO_KEY, BridgedDataSourceSignalDetectionRepositoryJpa.create(
        addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          SIGNAL_SIMULATION_PERSISTENCE_UNIT, soccproConfig))),
      AL1_KEY, BridgedDataSourceSignalDetectionRepositoryJpa.create(
        addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          SIGNAL_SIMULATION_PERSISTENCE_UNIT, al1Config))),
      AL2_KEY, BridgedDataSourceSignalDetectionRepositoryJpa.create(
        addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          SIGNAL_SIMULATION_PERSISTENCE_UNIT, al2Config)))
    );
  }

  /**
   * Create stage to corresponding origin bridged data source repository map
   *
   * @param soccproConfig system config for soccpro account
   * @param al1Config system config for al1 account
   * @param al2Config system config for al2 account
   * @return map of stage to {@link BridgedDataSourceRepository}
   */
  private Map<String, BridgedDataSourceRepository> createOriginBridgedDataSourceRepositoryMap(
    SystemConfig soccproConfig, SystemConfig al1Config, SystemConfig al2Config) {

    return Map.of(
      SOCCPRO_KEY, BridgedDataSourceOriginRepositoryJpa.create(
        addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          ORIGIN_SIMULATION_PERSISTENCE_UNIT, soccproConfig))),
      AL1_KEY, BridgedDataSourceOriginRepositoryJpa.create(
        addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          ORIGIN_SIMULATION_PERSISTENCE_UNIT, al1Config))),
      AL2_KEY, BridgedDataSourceOriginRepositoryJpa.create(
        addToCleanupHooks(simulationBridgedEntityManagerFactoryProvider.getEntityManagerFactory(
          ORIGIN_SIMULATION_PERSISTENCE_UNIT, al2Config)))
    );
  }

  /**
   * Create map of stages to their corresponding {@link ArrivalDatabaseConnector}
   *
   * @param signalDatabaseConnectorFactoryMap {@link SignalDetectionDatabaseConnectorFactory} map containing
   * {@link ArrivalDatabaseConnector}
   * @return map of stage to {@link ArrivalDatabaseConnector}s
   */
  private Map<String, ArrivalDatabaseConnector> createArrivalDatabaseConnectorMap(
    Map<String, SignalDetectionDatabaseConnectorFactory> signalDatabaseConnectorFactoryMap) {
    return signalDatabaseConnectorFactoryMap.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getArrivalDatabaseConnectorInstance()));
  }

  /**
   * Create map of stages to their corresponding {@link AmplitudeDatabaseConnector}
   *
   * @param signalDatabaseConnectorFactoryMap {@link SignalDetectionDatabaseConnectorFactory} map containing
   * {@link AmplitudeDatabaseConnector}
   * @return map of stage to {@link AmplitudeDatabaseConnector}s
   */
  private Map<String, AmplitudeDatabaseConnector> createAmplitudeDatabaseConnectorMap(
    Map<String, SignalDetectionDatabaseConnectorFactory> signalDatabaseConnectorFactoryMap) {
    return signalDatabaseConnectorFactoryMap.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAmplitudeDatabaseConnectorInstance()));
  }

  /**
   * Create map of stage to {@link EventDatabaseConnector}s
   *
   * @param originSimulatorDatabaseConnectorFactoryMap factory map needed for stage connector
   * @return map of stage to {@link EventDatabaseConnector}
   */
  private Map<String, EventDatabaseConnector> createEventDatabaseConnectorMap(
    Map<String, OriginSimulatorDatabaseConnectorFactory> originSimulatorDatabaseConnectorFactoryMap) {
    return originSimulatorDatabaseConnectorFactoryMap.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getEventDatabaseConnectorInstance()));
  }

  /**
   * Create map of stage to {@link OriginErrDatabaseConnector}s
   *
   * @param originSimulatorDatabaseConnectorFactoryMap factory map needed for stage connector
   * @return map of stage to {@link OriginErrDatabaseConnector}
   */
  private Map<String, OriginErrDatabaseConnector> createOriginErrDatabaseConnectorMap(
    Map<String, OriginSimulatorDatabaseConnectorFactory> originSimulatorDatabaseConnectorFactoryMap) {
    return originSimulatorDatabaseConnectorFactoryMap.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getOrigErrDatabaseConnectorInstance()));
  }


  /**
   * Create map of stage to {@link OriginSimulatorDatabaseConnector}s
   *
   * @param originSimulatorDatabaseConnectorFactoryMap factory map needed for stage connector
   * @return map of stage to {@link OriginSimulatorDatabaseConnector}
   */
  private Map<String, OriginSimulatorDatabaseConnector> createOriginSimulatorDatabaseConnectorMap(
    Map<String, OriginSimulatorDatabaseConnectorFactory> originSimulatorDatabaseConnectorFactoryMap) {
    return originSimulatorDatabaseConnectorFactoryMap.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getOriginDatabaseConnectorInstance()));
  }
}

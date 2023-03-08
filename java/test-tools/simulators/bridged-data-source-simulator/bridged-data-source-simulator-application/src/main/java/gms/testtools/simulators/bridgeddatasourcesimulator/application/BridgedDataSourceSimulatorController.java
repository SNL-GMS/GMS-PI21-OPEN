package gms.testtools.simulators.bridgeddatasourcesimulator.application;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.utilities.bridge.database.BridgedEntityManagerFactoryProvider;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import gms.shared.utilities.javautilities.objectmapper.OracleLivenessCheck;
import gms.testtools.simulators.bridgeddatasourceintervalsimulator.BridgedDataSourceIntervalSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceDataSimulator;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorService;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.ExceptionSummary;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.Site;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.coi.SiteChan;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.SourceInterval;
import gms.testtools.simulators.bridgeddatasourcesimulator.application.factory.DataSimulatorFactory;
import gms.testtools.simulators.bridgeddatasourcestationsimulator.BridgedDataSourceStationSimulator;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is the backing implementation of the restful api defined in {@link
 * BridgedDataSourceSimulatorService} and is used the start the Bridged Data Source Simulator
 * Service in {@link BridgedDataSourceSimulatorApplication}.
 */
public class BridgedDataSourceSimulatorController implements BridgedDataSourceSimulatorService {

  private static final Logger logger = LoggerFactory.getLogger(BridgedDataSourceSimulatorController.class);

  private final Map<String, List<ExceptionSummary>> simulatorErrors = new ConcurrentHashMap<>();
  private final BridgedDataSourceSimulatorStateMachine stateMachine;
  private final List<BridgedDataSourceDataSimulator> dataSimulators;

  private static final String SIMULATOR_BRIDGED_DATA_SOURCE_CONFIG = "simulator.bridged-data-source-config";
  private static final String DEFAULT_SCHEMA_CONFIG_KEY = "default_schema";
  private static final String SIMULATION_SCHEMA_CONFIG_KEY = "simulation_schema";
  private static final String CALIB_DELTA = "calib_delta";


  private BridgedDataSourceSimulatorController(BridgedDataSourceSimulatorStateMachine stateMachine,
    List<BridgedDataSourceDataSimulator> dataSimulators) {
    this.stateMachine = stateMachine;
    this.dataSimulators = dataSimulators;
  }

  /**
   * Initializes a {@link BridgedDataSourceSimulatorController} by providing a Processing Config
   * {@link ControlContext}
   *
   * @param context - the context used to retrieve processing config.
   * @return an initialized {@link BridgedDataSourceSimulatorController}
   */
  public static BridgedDataSourceSimulatorController create(ControlContext context) {
    Validate.notNull(context, "ControlContext");

    //get config values
    var configurationConsumerUtility = context
      .getProcessingConfigurationConsumerUtility();

    Map<String, Object> processingConfig = configurationConsumerUtility
      .resolve(SIMULATOR_BRIDGED_DATA_SOURCE_CONFIG, List.of());

    var defaultSchemaConfigStringValue = getProcessingConfigStringValue(processingConfig,
      DEFAULT_SCHEMA_CONFIG_KEY, "Default Schema");
    var simulationSchemaConfigStringValue = getProcessingConfigStringValue(
      processingConfig,
      SIMULATION_SCHEMA_CONFIG_KEY, "Simulation Schema");

    var calibDeltaValue = (Integer) (processingConfig.getOrDefault(
      CALIB_DELTA, 5));

    DatabaseLivenessCheck simDataLivenessCheck = OracleLivenessCheck.create(
      context.getSystemConfig());
    if (!simDataLivenessCheck.isLive()) {
      logger.error("Could not establish database liveness.  Exiting");
      System.exit(1);
    }

    //create database connections
    var seedDataEntityManagerFactoryProvider = BridgedEntityManagerFactoryProvider
      .create(defaultSchemaConfigStringValue);
    var simulationDataEntityManagerFactoryProvider = BridgedEntityManagerFactoryProvider
      .create(simulationSchemaConfigStringValue);

    var dataSimulatorFactory = DataSimulatorFactory
      .create(
        seedDataEntityManagerFactoryProvider,
        simulationDataEntityManagerFactoryProvider,
        calibDeltaValue
      );

    Runtime.getRuntime().addShutdownHook(new Thread(dataSimulatorFactory::cleanup));

    var bridgedDataSourceStationSimulator = dataSimulatorFactory
      .getBridgedDataSourceStationSimulatorInstance();

    var bridgedDataSourceAnalysisSimulator = dataSimulatorFactory
      .getBridgedDataSourceAnalysisSimulatorInstance();

    var bridgedDataSourceIntervalSimulator = dataSimulatorFactory
      .getBridgedDataSourceIntervalSimulatorInstance();

    List<BridgedDataSourceDataSimulator> dataSimulators = List
      .of(
        bridgedDataSourceStationSimulator,
        bridgedDataSourceAnalysisSimulator,
        bridgedDataSourceIntervalSimulator
      );

    return BridgedDataSourceSimulatorController.create(
      BridgedDataSourceSimulatorStateMachine.create(),
      dataSimulators);
  }

  protected static BridgedDataSourceSimulatorController create(
    BridgedDataSourceSimulatorStateMachine stateMachine,
    List<BridgedDataSourceDataSimulator> dataSimulators) {
    Validate.notNull(stateMachine, "stateMaching cannot be null");
    Validate.notNull(dataSimulators, "dataSimulators must be provided");
    Validate.notEmpty(dataSimulators, "dataSimulators must be provided");
    Validate.isTrue(dataSimulators.stream().noneMatch(Objects::isNull),
      "no null dataSimulators are allowed");

    return new BridgedDataSourceSimulatorController(stateMachine, dataSimulators);
  }

  private static String getProcessingConfigStringValue(Map<String, Object> processingConfig,
    String cacheKey, final String dataTypeString) {
    final var configValueObject = processingConfig.get(cacheKey);
    final var errorMessage = String
      .format("No %s were found in processing config for simulation config.", dataTypeString);
    Validate.notNull(configValueObject, errorMessage);
    final var configStringValue = String.valueOf(configValueObject);
    Validate.isTrue(!configStringValue.isBlank(), errorMessage);
    return configStringValue;
  }

  /**
   * Verifies that the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#INITIALIZED} state using the {@link
   * BridgedDataSourceSimulatorTransition#INITIALIZE} transition.
   * BridgedDataSourceSimulatorStateMachine}.
   * <p>
   * If the transition is allowed, then {@link BridgedDataSourceDataSimulator#initialize(BridgedDataSourceSimulatorSpec)}
   * is called on each {@link BridgedDataSourceSimulatorController#dataSimulators}
   *
   * @param bridgedDataSourceSimulatorSpec - An {@link BridgedDataSourceSimulatorSpec} to provided
   * the simulation specification details.
   */
  @Override
  public void initialize(BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec) {
    var start = Instant.now();
    logger.info("starting initialization at {}", start);
    stateMachine.verifyInitializeTransition(bridgedDataSourceSimulatorSpec);

    logger.info("Valid Status Transition Detected. Initializing Simulation...");
    stateMachine.initialize(bridgedDataSourceSimulatorSpec);
    stateMachine.verifyLoadTransition();

    Function<BridgedDataSourceDataSimulator, DataSimulatorConsumerResult> initializationFunction =
      ErrorCapturingFunction.create(simulator -> simulator.initialize(bridgedDataSourceSimulatorSpec));
    Consumer<DataSimulatorConsumerResult> resultConsumer = ErrorTrackingConsumer.create(simulatorErrors);
    getAsyncSimulatorFlux(initializationFunction)
      .subscribe(resultConsumer,
        error -> {
          logger.error("Error during initialization", error);
          load("");
        },
        () -> {
          load("");
          logger.info("Simulation Initialized.");
          var end = Instant.now();
          logger.info("Simulation Initialization completed at {}", end);
        });
  }

  @Override
  public void load(String placeholder) {

    stateMachine.load("");

  }

  /**
   * Verifies that the the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#STARTED} state using the {@link
   * BridgedDataSourceSimulatorTransition#START} transition.
   * <p>
   * If the transition is allowed, then {@link BridgedDataSourceDataSimulator#start(String)} is
   * called on each {@link BridgedDataSourceSimulatorController#dataSimulators}
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void start(String placeholder) {
    stateMachine.verifyStartTransition();
    logger.info("Valid Status Transition Detected. Starting Simulation...");
    runFlux(simulator -> {
      simulator.start(placeholder);
      return placeholder;
    });

    stateMachine.start(placeholder);
    logger.info("Simulation Started.");
  }

  /**
   * Verifies that the the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#STOPPED} state using the {@link
   * BridgedDataSourceSimulatorTransition#STOP} transition.
   * <p>
   * If the transition is allowed, then {@link BridgedDataSourceDataSimulator#stop(String)} is
   * called on each {@link BridgedDataSourceSimulatorController#dataSimulators}
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void stop(String placeholder) {
    stateMachine.verifyStopTransition();
    logger.info("Valid Status Transition Detected. Stopping Simulation...");
    runFlux(simulator -> {
      simulator.stop(placeholder);
      return placeholder;
    });
    stateMachine.stop(placeholder);
    logger.info("Simulation Stopped.");
  }


  /**
   * Verifies that the the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#UNINITIALIZED} state using the {@link
   * BridgedDataSourceSimulatorTransition#CLEANUP} transition.
   * <p>
   * If the transition is allowed, then {@link BridgedDataSourceDataSimulator#cleanup(String)} is
   * called on each {@link BridgedDataSourceSimulatorController#dataSimulators}
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void cleanup(String placeholder) {
    stateMachine.verifyCleanupTransition();
    logger.info("Valid Status Transition Detected. Cleaning Up Simulation...");
    runFlux(simulator -> {
      simulator.cleanup(placeholder);
      return placeholder;
    });
    stateMachine.cleanup(placeholder);
    logger.info("Simulation Uninitialized.");
  }

  @VisibleForTesting
  Flux<DataSimulatorConsumerResult> getAsyncSimulatorFlux(
    Function<BridgedDataSourceDataSimulator, DataSimulatorConsumerResult> mapFunction) {
    return Flux.fromIterable(dataSimulators)
      .subscribeOn(Schedulers.boundedElastic())
      .parallel()
      .map(mapFunction)
      .sequential();
  }

  /**
   * Verifies that simulator is not in {@link BridgedDataSourceSimulatorStatus#UNINITIALIZED} state
   * so that {@link BridgedDataSourceSimulatorController#storeNewChannelVersions(Collection<SiteChan>)}
   * can be called
   * <p>
   * - @param chans - A collections of SiteChan.
   */
  @Override
  public void storeNewChannelVersions(Collection<SiteChan> chans) {

    Preconditions.checkState(stateMachine.status("status") != BridgedDataSourceSimulatorStatus.UNINITIALIZED,
      "Cannot store new channel versions if simulator is uninitialized.");
    Objects.requireNonNull(chans, "Cannot store null channel versions");
    Validate.isTrue(!chans.isEmpty(), "Cannot store empty channel versions");

    Flux.fromIterable(dataSimulators)
      .parallel()
      .runOn(Schedulers.boundedElastic())
      .doOnNext(bridgedDataSourceDataSimulator -> {
        if (bridgedDataSourceDataSimulator instanceof BridgedDataSourceStationSimulator) {
          ((BridgedDataSourceStationSimulator) bridgedDataSourceDataSimulator).storeNewChannelVersions(chans);
        }
      })
      .sequential()
      .blockLast();

    logger.info("New Channel Versions stored.");
  }

  /**
   * Verifies that simulator is not in {@link BridgedDataSourceSimulatorStatus#UNINITIALIZED} state so that
   * {@link BridgedDataSourceSimulatorController#storeNewSiteVersions(Collection<Site>)} can be called
   * <p>
   * - @param sites - A collections of Sites.
   */
  @Override
  public void storeNewSiteVersions(Collection<Site> sites) {

    Preconditions.checkState(stateMachine.status("status").ordinal()
        >= BridgedDataSourceSimulatorStatus.INITIALIZED.ordinal(),
      "Cannot store new site versions if simulator is uninitialized.");
    Objects.requireNonNull(sites, "Cannot store null sites");
    Validate.isTrue(!sites.isEmpty(), "Cannot store empty sites");

    Flux.fromIterable(dataSimulators)
      .parallel()
      .runOn(Schedulers.boundedElastic())
      .doOnNext(bridgedDataSourceDataSimulator -> {
        if (bridgedDataSourceDataSimulator instanceof BridgedDataSourceStationSimulator) {
          ((BridgedDataSourceStationSimulator) bridgedDataSourceDataSimulator).storeNewSiteVersions(
            sites);
        }
      })
      .sequential()
      .blockLast();

    logger.info("New Site Versions stored.");
  }

  @Override
  public BridgedDataSourceSimulatorStatus status(String placeholder) {
    logger.info("Simulation Status Requested.");
    return stateMachine.status(placeholder);
  }

  @Override
  public void storeIntervals(List<SourceInterval> intervalList) {
    runFlux(simulator -> {
      if (simulator instanceof BridgedDataSourceIntervalSimulator) {
        ((BridgedDataSourceIntervalSimulator) simulator).storeIntervals(intervalList);
      }
      return true;
    });
  }

  protected <T> void runFlux(Function<BridgedDataSourceDataSimulator, T> simulatorConsumer) {
    Flux.fromIterable(dataSimulators)
      .parallel()
      .runOn(Schedulers.boundedElastic())
      .map(simulatorConsumer)
      .sequential()
      .blockLast();
  }

  @Override
  public Map<String, Exception> errorLog(String placeholder) {
    return null;
  }
}

package gms.testtools.simulators.bridgeddatasourcesimulator.api;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BridgedDataSourceSimulatorStateMachineTest {

  private static final String CURRENT_STATE_IS_UNINITIALIZED_ERROR = "Invalid Status Transition Detected. The current status is UNINITIALIZED. The next valid status transitions are [INITIALIZE].";
  private static final String CURRENT_STATE_IS_INITIALIZING_ERROR = "Invalid Status Transition Detected. The current status is INITIALIZING. The next valid status transitions are [LOAD].";
  private static final String CURRENT_STATE_IS_INITIALIZED_ERROR = "Invalid Status Transition Detected. The current status is INITIALIZED. The next valid status transitions are [START, CLEANUP].";
  private static final String CURRENT_STATE_IS_STARTED_ERROR = "Invalid Status Transition Detected. The current status is STARTED. The next valid status transitions are [STOP].";
  private static final String CURRENT_STATE_IS_STOPPED_ERROR = "Invalid Status Transition Detected. The current status is STOPPED. The next valid status transitions are [START, CLEANUP].";
  private static final String STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR = "Status Transition was not verified before attempting to execute transition.";
  public static final String PLACEHOLDER = "placeholder";
  private final BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec = BridgedDataSourceSimulatorSpec.builder()
    .setSeedDataStartTime(Instant.parse("2010-05-20T16:00:00.00Z"))
    .setSeedDataEndTime(Instant.parse("2010-05-20T18:00:00.00Z"))
    .setSimulationStartTime(Instant.now())
    .setOperationalTimePeriod(Duration.ofDays(45L))
    .setCalibUpdateFrequency(Duration.ofDays(4L)).build();

  private BridgedDataSourceSimulatorStateMachine simulatorManager;

  @BeforeEach
  void testSetup() {
    simulatorManager = BridgedDataSourceSimulatorStateMachine.create();
  }

  @Test
  void testInitialize() {
    assertDoesNotThrow(this::runValidInitialize);
  }

  @Test
  void testCreate() {
    BridgedDataSourceSimulatorStateMachine stateMachine =
      assertDoesNotThrow(() -> BridgedDataSourceSimulatorStateMachine.create());
    assertNotNull(stateMachine);
  }

  @Test
  void testGetBridgeSimulatorSpec_beforeInitialize_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      simulatorManager::getBridgeSimulatorSpec);

    assertEquals(
      "Initialize was not called yet. Initialize must be called before other interactions.",
      error.getMessage());
  }

  @Test
  void testGetBridgeSimulatorSpec_afterInitialize() {
    runValidInitialize();

    final BridgedDataSourceSimulatorSpec result = assertDoesNotThrow(
      () -> simulatorManager.getBridgeSimulatorSpec());

    assertEquals(bridgedDataSourceSimulatorSpec, result);
  }

  @Test
  void testStart_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.start(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testVerifyStart_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyStartTransition());

    assertEquals(CURRENT_STATE_IS_UNINITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testStop_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.stop(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testVerifyStop_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyStartTransition());

    assertEquals(CURRENT_STATE_IS_UNINITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testCleanup_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.cleanup(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testVerifyCleanup_error() {
    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyCleanupTransition());

    assertEquals(CURRENT_STATE_IS_UNINITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testLoad_thenInitialize_error() {
    runValidLoad();

    final Exception error = assertThrows(IllegalArgumentException.class, this::runValidInitialize);

    assertEquals(CURRENT_STATE_IS_INITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenCleanup_error() {
    runValidInitialize();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyCleanupTransition());

    assertEquals(CURRENT_STATE_IS_INITIALIZING_ERROR, error.getMessage());
  }

  @Test
  void testLoad_thenCleanup_error() {
    runValidLoad();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.cleanup(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testLoad_thenVerifyCleanup() {
    runValidLoad();

    assertDoesNotThrow(() -> simulatorManager.verifyCleanupTransition());
    assertDoesNotThrow(() -> simulatorManager.cleanup(PLACEHOLDER));
  }

  @Test
  void testInitialize_thenStart() {
    assertDoesNotThrow(this::runValidStart);
  }

  @Test
  void testInitialize_thenStart_thenStart_error() {
    runValidStart();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.start(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenVerifyStart_error() {
    runValidStart();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyStartTransition());

    assertEquals(CURRENT_STATE_IS_STARTED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenInitialize_error() {
    runValidStart();

    final Exception error = assertThrows(IllegalArgumentException.class,
      this::runValidInitialize);

    assertEquals(CURRENT_STATE_IS_STARTED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenCleanup_error() {
    runValidStart();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.cleanup(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenVerifyCleanup_error() {
    runValidStart();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyCleanupTransition());

    assertEquals(CURRENT_STATE_IS_STARTED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop() {
    assertDoesNotThrow(this::runValidStop);
  }

  @Test
  void testInitialize_thenStart_thenStop_thenStart() {
    assertDoesNotThrow(this::runValidRestart);
  }

  @Test
  void testInitialize_thenStart_thenStop_thenStart_thenStop_error() {
    runValidRestart();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.stop(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenVerifyStart_thenStop() {
    runValidRestart();

    assertDoesNotThrow(() -> simulatorManager.verifyStopTransition());
    assertDoesNotThrow(() -> simulatorManager.stop(PLACEHOLDER));
  }

  @Test
  void testInitialize_thenStart_thenStop_thenStop_error() {
    runValidStop();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.stop(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenVerifyStop_error() {
    runValidStop();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyStopTransition());

    assertEquals(CURRENT_STATE_IS_STOPPED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenInitialize_error() {
    runValidStop();

    final Exception error = assertThrows(IllegalArgumentException.class, this::runValidInitialize);

    assertEquals(CURRENT_STATE_IS_STOPPED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup() {
    assertDoesNotThrow(this::runValidCleanup);
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenInitialize() {
    runValidCleanup();

    assertDoesNotThrow(this::runValidInitialize);
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenStart_error() {
    runValidCleanup();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.start(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenVerifyStart_error() {
    runValidCleanup();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyStartTransition());

    assertEquals(CURRENT_STATE_IS_UNINITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenStop_error() {
    runValidCleanup();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.stop(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenVerifyStop_error() {
    runValidCleanup();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyStopTransition());

    assertEquals(CURRENT_STATE_IS_UNINITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenCleanup_error() {
    runValidCleanup();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.cleanup(PLACEHOLDER));

    assertEquals(STATE_TRANSITION_WAS_NOT_VERIFIED_ERROR, error.getMessage());
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenVerifyCleanup_error() {
    runValidCleanup();

    final Exception error = assertThrows(IllegalArgumentException.class,
      () -> simulatorManager.verifyCleanupTransition());

    assertEquals(CURRENT_STATE_IS_UNINITIALIZED_ERROR, error.getMessage());
  }

  @Test
  void testGetStatus() {
    final BridgedDataSourceSimulatorStatus result = simulatorManager.status(PLACEHOLDER);

    assertEquals(BridgedDataSourceSimulatorStatus.UNINITIALIZED, result);
  }

  @Test
  void testInitialize_thenGetStatus() {
    runValidInitialize();

    final BridgedDataSourceSimulatorStatus result = simulatorManager.status(PLACEHOLDER);

    assertEquals(BridgedDataSourceSimulatorStatus.INITIALIZING, result);
  }

  @Test
  void testLoad_thenGetStatus() {
    runValidLoad();

    final BridgedDataSourceSimulatorStatus result = simulatorManager.status(PLACEHOLDER);

    assertEquals(BridgedDataSourceSimulatorStatus.INITIALIZED, result);
  }

  @Test
  void testInitialize_thenStart_thenGetStatus() {
    runValidStart();

    final BridgedDataSourceSimulatorStatus result = simulatorManager.status(PLACEHOLDER);

    assertEquals(BridgedDataSourceSimulatorStatus.STARTED, result);
  }

  @Test
  void testInitialize_thenStart_thenStop_thenGetStatus() {
    runValidStop();

    final BridgedDataSourceSimulatorStatus result = simulatorManager.status(PLACEHOLDER);

    assertEquals(BridgedDataSourceSimulatorStatus.STOPPED, result);
  }

  @Test
  void testInitialize_thenStart_thenStop_thenCleanup_thenGetStatus() {
    runValidCleanup();

    final BridgedDataSourceSimulatorStatus result = simulatorManager.status(PLACEHOLDER);

    assertEquals(BridgedDataSourceSimulatorStatus.UNINITIALIZED, result);
  }

  private void runValidInitialize() {
    simulatorManager.verifyInitializeTransition(bridgedDataSourceSimulatorSpec);
    simulatorManager.initialize(bridgedDataSourceSimulatorSpec);
  }

  private void runValidLoad() {
    runValidInitialize();
    simulatorManager.verifyLoadTransition();
    simulatorManager.load(PLACEHOLDER);
  }

  private void runValidStart() {
    runValidLoad();
    simulatorManager.verifyStartTransition();
    simulatorManager.start(PLACEHOLDER);
  }

  private void runValidStop() {
    runValidStart();
    simulatorManager.verifyStopTransition();
    simulatorManager.stop(PLACEHOLDER);
  }

  private void runValidRestart() {
    runValidStop();
    simulatorManager.verifyStartTransition();
    simulatorManager.start(PLACEHOLDER);
  }

  private void runValidCleanup() {
    runValidStop();
    simulatorManager.verifyCleanupTransition();
    simulatorManager.cleanup(PLACEHOLDER);
  }

}
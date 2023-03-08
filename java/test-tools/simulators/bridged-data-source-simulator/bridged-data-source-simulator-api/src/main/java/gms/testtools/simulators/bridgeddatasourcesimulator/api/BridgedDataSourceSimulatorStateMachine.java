package gms.testtools.simulators.bridgeddatasourcesimulator.api;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.INITIALIZED;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.INITIALIZING;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.STARTED;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.STOPPED;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.UNINITIALIZED;

public class BridgedDataSourceSimulatorStateMachine implements
  BridgedDataSourceSimulator, BridgedDataSourceStatusCheck {

  protected static final String STATUS_TRANSITION_WAS_NOT_VERIFIED_ERROR = "Status Transition was not verified before attempting to execute transition.";
  private BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec;
  private final Map<BridgedDataSourceSimulatorTransition, Boolean> transitionVerification = new EnumMap<>(
    BridgedDataSourceSimulatorTransition.class);
  private BridgedDataSourceSimulatorStatus currentStatus = UNINITIALIZED;

  private BridgedDataSourceSimulatorStateMachine() {
    transitionVerification.put(BridgedDataSourceSimulatorTransition.INITIALIZE, false);
    transitionVerification.put(BridgedDataSourceSimulatorTransition.LOAD, false);
    transitionVerification.put(BridgedDataSourceSimulatorTransition.START, false);
    transitionVerification.put(BridgedDataSourceSimulatorTransition.STOP, false);
    transitionVerification.put(BridgedDataSourceSimulatorTransition.CLEANUP, false);
  }

  public static BridgedDataSourceSimulatorStateMachine create() {
    return new BridgedDataSourceSimulatorStateMachine();
  }

  public void verifyInitializeTransition(
    BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec) {
    Validate.notNull(bridgedDataSourceSimulatorSpec, "A Simulator Spec must be provided");
    validateStatusTransition(INITIALIZING, BridgedDataSourceSimulatorTransition.INITIALIZE);
  }

  /**
   * Verifies that the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#INITIALIZED} state using the {@link
   * BridgedDataSourceSimulatorTransition#INITIALIZE} transition.
   *
   * @param bridgedDataSourceSimulatorSpec - An {@link BridgedDataSourceSimulatorSpec} to provided
   * the simulation specification details.
   */
  @Override
  public void initialize(BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec) {
    verifyStatusTransitionWasVerified(BridgedDataSourceSimulatorTransition.INITIALIZE);
    this.currentStatus = INITIALIZING;
    this.bridgedDataSourceSimulatorSpec = bridgedDataSourceSimulatorSpec;
  }

  public void verifyLoadTransition() {
    validateStatusTransition(INITIALIZED, BridgedDataSourceSimulatorTransition.LOAD);
  }

  /**
   * Verifies that the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#INITIALIZED} state using the {@link
   * BridgedDataSourceSimulatorTransition#LOAD} transition.
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void load(String placeholder) {
    verifyStatusTransitionWasVerified(BridgedDataSourceSimulatorTransition.LOAD);
    this.currentStatus = INITIALIZED;

  }


  public void verifyStartTransition() {
    validateStatusTransition(STARTED, BridgedDataSourceSimulatorTransition.START);
  }

  /**
   * Verifies that the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#STARTED} state using the {@link
   * BridgedDataSourceSimulatorTransition#START} transition.
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void start(String placeholder) {
    verifyStatusTransitionWasVerified(BridgedDataSourceSimulatorTransition.START);
    this.currentStatus = STARTED;
  }

  public void verifyStopTransition() {
    validateStatusTransition(STOPPED, BridgedDataSourceSimulatorTransition.STOP);
  }

  /**
   * Verifies that the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#STOPPED} state using the {@link
   * BridgedDataSourceSimulatorTransition#STOP} transition.
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void stop(String placeholder) {
    verifyStatusTransitionWasVerified(BridgedDataSourceSimulatorTransition.STOP);
    this.currentStatus = STOPPED;
  }

  public void verifyCleanupTransition() {
    validateStatusTransition(UNINITIALIZED, BridgedDataSourceSimulatorTransition.CLEANUP);
  }

  /**
   * Verifies that the state machine can be transitioned to the {@link
   * BridgedDataSourceSimulatorStatus#UNINITIALIZED} state using the {@link
   * BridgedDataSourceSimulatorTransition#CLEANUP} transition.
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   */
  @Override
  public void cleanup(String placeholder) {
    verifyStatusTransitionWasVerified(BridgedDataSourceSimulatorTransition.CLEANUP);
    this.currentStatus = UNINITIALIZED;
    this.bridgedDataSourceSimulatorSpec = null;
  }

  public void verifyStatusTransitionWasVerified(BridgedDataSourceSimulatorTransition statusTransition) {
    Validate.isTrue(transitionVerification.get(statusTransition),
      STATUS_TRANSITION_WAS_NOT_VERIFIED_ERROR);
    transitionVerification.put(statusTransition, false);
  }

  /**
   * Returns the current status of the simulation.
   *
   * @param placeholder - Any string value. This required by the framework, but it will be ignored.
   * @return
   */
  @Override
  public BridgedDataSourceSimulatorStatus status(String placeholder) {
    return currentStatus;
  }

  protected BridgedDataSourceSimulatorSpec getBridgeSimulatorSpec() {
    verifySimulationSpecLoaded();
    return bridgedDataSourceSimulatorSpec;
  }

  private void validateStatusTransition(
    BridgedDataSourceSimulatorStatus nextStatus,
    BridgedDataSourceSimulatorTransition statusTransition) {
    Validate.isTrue(statusTransition.validateStatusTransition(currentStatus, nextStatus),
      "Invalid Status Transition Detected. The current status is %s. The next valid status transitions are %s.",
      currentStatus, BridgedDataSourceSimulatorTransition.getNextTransition(currentStatus));
    if (nextStatus == INITIALIZING) {
      verifySimulationNotSpecLoaded();
    } else {
      verifySimulationSpecLoaded();
    }
    transitionVerification.put(statusTransition, true);
  }

  private void verifySimulationNotSpecLoaded() {
    Validate.isTrue(!isSimulationSpecLoaded(),
      "Initialize was already called. Start must be called next.");
  }

  private void verifySimulationSpecLoaded() {
    Validate.isTrue(isSimulationSpecLoaded(),
      "Initialize was not called yet. Initialize must be called before other interactions.");
  }

  private boolean isSimulationSpecLoaded() {
    return this.bridgedDataSourceSimulatorSpec != null;
  }

  /**
   * An enum that defines the types of transitions, and the associated valid statuses to transition
   * from and to for each type of transition.
   */
  public enum BridgedDataSourceSimulatorTransition {

    INITIALIZE(List.of(UNINITIALIZED), INITIALIZING),
    LOAD(List.of(INITIALIZING), INITIALIZED),
    START(List.of(INITIALIZED, STOPPED), STARTED),
    STOP(List.of(STARTED), STOPPED),
    CLEANUP(List.of(INITIALIZED, STOPPED), UNINITIALIZED);

    private final List<BridgedDataSourceSimulatorStatus> validPreviousStatuses;
    private final BridgedDataSourceSimulatorStatus validNextStatus;

    /**
     * @param validPreviousStatuses - The current status of the state machine must be in this list for the selected transition to be allowed.
     * @param nextStatus - The status the state machine will be set to at the end of the selected transition.
     */
    BridgedDataSourceSimulatorTransition(List<BridgedDataSourceSimulatorStatus> validPreviousStatuses,
      BridgedDataSourceSimulatorStatus nextStatus) {
      this.validPreviousStatuses = validPreviousStatuses;
      this.validNextStatus = nextStatus;
    }

    protected List<BridgedDataSourceSimulatorStatus> getValidPreviousStatuses() {
      return validPreviousStatuses;
    }

    /**
     * Given a current status and a target next status, determine if the transition is allowed or not.
     *
     * @param currentStatus
     * @param nextStatus
     * @return
     */
    boolean validateStatusTransition(
      BridgedDataSourceSimulatorStatus currentStatus,
      BridgedDataSourceSimulatorStatus nextStatus) {
      return validPreviousStatuses.contains(currentStatus) && validNextStatus == nextStatus;
    }

    /**
     * Given a current status, determine the allowable state transitions.
     *
     * @param currentStatus
     * @return
     */
    static Collection<BridgedDataSourceSimulatorTransition> getNextTransition(
      BridgedDataSourceSimulatorStatus currentStatus) {
      return Arrays.stream(BridgedDataSourceSimulatorTransition.values())
        .filter(t -> t.getValidPreviousStatuses().contains(currentStatus))
        .collect(Collectors.toList());
    }
  }
}

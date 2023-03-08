package gms.testtools.simulators.bridgeddatasourcesimulator.api;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition.CLEANUP;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition.INITIALIZE;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition.LOAD;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition.START;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.BridgedDataSourceSimulatorStateMachine.BridgedDataSourceSimulatorTransition.STOP;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.INITIALIZED;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.INITIALIZING;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.STARTED;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.STOPPED;
import static gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus.UNINITIALIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BridgedDataSourceSimulatorStatusTransitionTest {

  @ParameterizedTest
  @MethodSource("statusTransitionsArguments")
  void testStateTransitions(
    BridgedDataSourceSimulatorTransition transition,
    BridgedDataSourceSimulatorStatus currentState,
    BridgedDataSourceSimulatorStatus targetState, boolean allowed) {
    assertEquals(allowed, transition.validateStatusTransition(currentState, targetState));
  }

  //Create a listing of (transition to apply, begin state, end state, valid result)
  //Arguments.arguments(INITIALIZE, UNINITIALIZED, UNINITIALIZED, false) would test if from
  //UNINITIALIZED if I run the INITIALIZE transition, will I end up in UNINITIALIZED, expecting false
  private static Stream<Arguments> statusTransitionsArguments() {
    return Stream.of(
      Arguments.arguments(INITIALIZE, UNINITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(INITIALIZE, UNINITIALIZED, INITIALIZED, false),
      Arguments.arguments(INITIALIZE, UNINITIALIZED, INITIALIZING, true),
      Arguments.arguments(INITIALIZE, UNINITIALIZED, STARTED, false),
      Arguments.arguments(INITIALIZE, UNINITIALIZED, STOPPED, false),
      Arguments.arguments(INITIALIZE, INITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(INITIALIZE, INITIALIZED, INITIALIZED, false),
      Arguments.arguments(INITIALIZE, INITIALIZED, STARTED, false),
      Arguments.arguments(INITIALIZE, INITIALIZED, STOPPED, false),
      Arguments.arguments(INITIALIZE, STARTED, UNINITIALIZED, false),
      Arguments.arguments(INITIALIZE, STARTED, INITIALIZED, false),
      Arguments.arguments(INITIALIZE, STARTED, STARTED, false),
      Arguments.arguments(INITIALIZE, STARTED, STOPPED, false),
      Arguments.arguments(INITIALIZE, STOPPED, UNINITIALIZED, false),
      Arguments.arguments(INITIALIZE, STOPPED, INITIALIZED, false),
      Arguments.arguments(INITIALIZE, STOPPED, STARTED, false),
      Arguments.arguments(INITIALIZE, STOPPED, STOPPED, false),

      Arguments.arguments(START, UNINITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(START, UNINITIALIZED, INITIALIZED, false),
      Arguments.arguments(START, UNINITIALIZED, STARTED, false),
      Arguments.arguments(START, UNINITIALIZED, STOPPED, false),
      Arguments.arguments(START, INITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(START, INITIALIZED, INITIALIZED, false),
      Arguments.arguments(START, INITIALIZED, STARTED, true),
      Arguments.arguments(START, INITIALIZED, STOPPED, false),
      Arguments.arguments(START, STARTED, UNINITIALIZED, false),
      Arguments.arguments(START, STARTED, INITIALIZED, false),
      Arguments.arguments(START, STARTED, STARTED, false),
      Arguments.arguments(START, STARTED, STOPPED, false),
      Arguments.arguments(START, STOPPED, UNINITIALIZED, false),
      Arguments.arguments(START, STOPPED, INITIALIZED, false),
      Arguments.arguments(START, STOPPED, STARTED, true),
      Arguments.arguments(START, STOPPED, STOPPED, false),

      Arguments.arguments(STOP, UNINITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(STOP, UNINITIALIZED, INITIALIZED, false),
      Arguments.arguments(STOP, UNINITIALIZED, STARTED, false),
      Arguments.arguments(STOP, UNINITIALIZED, STOPPED, false),
      Arguments.arguments(STOP, INITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(STOP, INITIALIZED, INITIALIZED, false),
      Arguments.arguments(STOP, INITIALIZED, STARTED, false),
      Arguments.arguments(STOP, INITIALIZED, STOPPED, false),
      Arguments.arguments(STOP, STARTED, UNINITIALIZED, false),
      Arguments.arguments(STOP, STARTED, INITIALIZED, false),
      Arguments.arguments(STOP, STARTED, STARTED, false),
      Arguments.arguments(STOP, STARTED, STOPPED, true),
      Arguments.arguments(STOP, STOPPED, UNINITIALIZED, false),
      Arguments.arguments(STOP, STOPPED, INITIALIZED, false),
      Arguments.arguments(STOP, STOPPED, STARTED, false),
      Arguments.arguments(STOP, STOPPED, STOPPED, false),

      Arguments.arguments(CLEANUP, UNINITIALIZED, UNINITIALIZED, false),
      Arguments.arguments(CLEANUP, UNINITIALIZED, INITIALIZED, false),
      Arguments.arguments(CLEANUP, UNINITIALIZED, STARTED, false),
      Arguments.arguments(CLEANUP, UNINITIALIZED, STOPPED, false),
      Arguments.arguments(CLEANUP, INITIALIZED, UNINITIALIZED, true),
      Arguments.arguments(CLEANUP, INITIALIZED, INITIALIZED, false),
      Arguments.arguments(CLEANUP, INITIALIZED, STARTED, false),
      Arguments.arguments(CLEANUP, INITIALIZED, STOPPED, false),
      Arguments.arguments(CLEANUP, STARTED, UNINITIALIZED, false),
      Arguments.arguments(CLEANUP, STARTED, INITIALIZED, false),
      Arguments.arguments(CLEANUP, STARTED, STARTED, false),
      Arguments.arguments(CLEANUP, STARTED, STOPPED, false),
      Arguments.arguments(CLEANUP, STOPPED, UNINITIALIZED, true),
      Arguments.arguments(CLEANUP, STOPPED, INITIALIZED, false),
      Arguments.arguments(CLEANUP, STOPPED, STARTED, false),
      Arguments.arguments(CLEANUP, STOPPED, STOPPED, false)
    );
  }

  @ParameterizedTest
  @MethodSource("nextTransitionsArguments")
  void testStateTransitions(BridgedDataSourceSimulatorStatus currentState,
    List<BridgedDataSourceSimulatorTransition> transitions) {
    assertEquals(transitions, BridgedDataSourceSimulatorTransition.getNextTransition(currentState));
  }

  private static Stream<Arguments> nextTransitionsArguments() {
    return Stream.of(
      Arguments.arguments(UNINITIALIZED, List.of(INITIALIZE)),
      Arguments.arguments(INITIALIZING, List.of(LOAD)),
      Arguments.arguments(INITIALIZED, List.of(START, CLEANUP)),
      Arguments.arguments(STARTED, List.of(STOP)),
      Arguments.arguments(STOPPED, List.of(START, CLEANUP))
    );
  }

}
package gms.testtools.simulators.bridgeddatasourcesimulator.api;

import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorStatus;

public interface BridgedDataSourceStatusCheck {

  /**
   * Returns the current status of the simulator
   *
   * @param placeholder - Any string value. This is required by the framework, but it will be *
   * ignored.
   * @return {@link BridgedDataSourceSimulatorStatus}
   */
  BridgedDataSourceSimulatorStatus status(String placeholder);
}

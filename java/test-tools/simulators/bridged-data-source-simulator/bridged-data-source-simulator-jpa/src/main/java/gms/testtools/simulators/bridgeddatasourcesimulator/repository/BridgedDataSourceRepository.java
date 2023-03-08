package gms.testtools.simulators.bridgeddatasourcesimulator.repository;

import java.util.List;

public interface BridgedDataSourceRepository {

  /**
   * Cleans up simulation data in the tables for a specific simulator using a stored procedure
   */
  void cleanupData();

  /**
   * Store the provided data in the simulation schema
   *
   * @param simulationData - A collection of data of type {@link T} to be stored in the simulation schema.
   * @param <T> the type of data to store
   */
  <T> void store(List<T> simulationData);

}

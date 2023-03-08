package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import java.time.Duration;
import java.time.Instant;

public interface AnalysisDataSimulator {

  /**
   * loads appropriate data into simulator database based on the given parameters.
   *
   * @param stageId - stageId string for querying particular stage
   * @param seedDataStartTime - start time of seed data to retrieve from bridged database
   * @param seedDataEndTime - end time of seed data to retrieve from bridged database
   * @param copiedDataTimeShift - amount of time to shift data by
   * to new ids generated for storing in the database
   */
  void loadData(String stageId, Instant seedDataStartTime, Instant seedDataEndTime, Duration copiedDataTimeShift);

  /**
   * remove relevant simulator data from simulator database
   */
  void cleanup();
}

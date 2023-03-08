package gms.testtools.simulators.bridgeddatasourcesimulator.application;


import gms.shared.frameworks.control.ControlFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class used to the start a Bridged Data Source Simulator Service
 */
public abstract class BridgedDataSourceSimulatorApplication {

  private static final Logger logger = LoggerFactory.getLogger(BridgedDataSourceSimulatorApplication.class);

  private BridgedDataSourceSimulatorApplication() {
  }

  public static void main(String[] args) {
    logger.info("Starting bridged-data-source-simulator-service");

    try {
      ControlFactory.runService(BridgedDataSourceSimulatorController.class);
    } catch (Exception ex) {
      logger.error("Error creating control", ex);
    }
  }
}

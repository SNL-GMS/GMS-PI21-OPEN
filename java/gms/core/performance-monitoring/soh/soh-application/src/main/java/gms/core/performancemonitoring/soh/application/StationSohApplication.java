package gms.core.performancemonitoring.soh.application;

import gms.core.performancemonitoring.soh.control.StationSohControl;
import gms.shared.frameworks.control.ControlFactory;
import gms.shared.frameworks.service.ServiceGenerator;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.function.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Kicks off the producer and consumer threads for taking station state of health and running
 * summary calculations on it.
 */
public class StationSohApplication {

  private static final Logger logger = LoggerFactory.getLogger(StationSohApplication.class);

  public static void main(String[] args) {

    logger.info("Starting StationSohApplication");

    try {

      var stationSohControlCreator = new StationSohControlCreator();

      final var retryPolicy = new RetryPolicy<>()
        .withBackoff(1, 60, ChronoUnit.SECONDS)
        .withMaxAttempts(15)
        .handle(List.of(IllegalStateException.class))
        .onFailedAttempt(e -> logger.warn(
          "Station Soh Control Configuration is still not loaded from configuration service, will try again"));
      Failsafe.with(retryPolicy).run(stationSohControlCreator);

      final var stationSohControl = stationSohControlCreator.getStationSohControl();

      logger.info("Starting consumer and producer threads");
      new Thread(stationSohControl::start).start();

      ServiceGenerator.runService(stationSohControl, stationSohControl.getSystemConfig());
      logger.info("Starting HTTP service threads");

    } catch (Exception e) {
      // Found that in the integration test, the control is created before the other containers
      // can supply the configuration. This results in an IllegalArgumentException. But, for some
      // reason, the application does not exit cleanly even though the only non-daemon thread is
      // the main thread. Remedy this by calling System.exit(), so a container restart is
      // triggered.
      logger.error("Error starting up the StationSohControl", e);
      System.exit(1);
    }
  }

  private static class StationSohControlCreator implements CheckedRunnable {

    private StationSohControl stationSohControl;

    @Override
    public void run() {
      stationSohControl = ControlFactory.createControl(StationSohControl.class);
      checkState(stationSohControl.hasNonEmptyConfiguration(),
        "SohControl configuration was not loaded!");
    }

    public StationSohControl getStationSohControl() {
      return stationSohControl;
    }
  }

}

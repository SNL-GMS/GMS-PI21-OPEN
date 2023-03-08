package gms.core.performancemonitoring.ssam.application;

import gms.core.performancemonitoring.ssam.control.ReactiveStationSohAnalysisManager;
import gms.shared.frameworks.control.ControlFactory;
import gms.shared.frameworks.messaging.ReactorKafkaUtilities;
import gms.shared.frameworks.service.ServiceGenerator;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import net.jodah.failsafe.function.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Kicks off consumer producer threads
 */
public class StationSohAnalysisManagerApplication {

  private static final Logger logger = LoggerFactory.getLogger(StationSohAnalysisManagerApplication.class);

  public static void main(String[] args) {

    logger.info("Starting StationSohAnalysisManagerApplication");

    var stationSohAnalysisManagerCreator = new ReactiveStationSohAnalysisManagerCreator();

    final var retryPolicy = new RetryPolicy<>()
      .withBackoff(1, 60, ChronoUnit.SECONDS)
      .withMaxAttempts(15)
      .handle(List.of(IllegalStateException.class))
      .onFailedAttempt(e -> logger.warn(
      "Station Soh Analysis Manager Application Configuration is still not loaded from configuration service, will try again"));
    Failsafe.with(retryPolicy).run(stationSohAnalysisManagerCreator);

    final var ssam = stationSohAnalysisManagerCreator.getStationSohAnalysisManager();

    var serviceMono = Mono.fromRunnable(() -> ServiceGenerator.runService(ssam,
      ssam.getSystemConfig())).retryWhen(ReactorKafkaUtilities.retryForever(logger));

    var hotCache = ssam.initializeCacheFromOsd().map(ssam::createAndInitializeKafkaUtility).share();
    var subscribeAll = hotCache.flatMap(ssam::setupReactiveProcessingPipeline).retryWhen(ReactorKafkaUtilities.retryForever(logger));

    logger.info("Starting HTTP service threads and the StationSohAnalysisManager control");
    serviceMono.subscribeOn(Schedulers.boundedElastic()).and(subscribeAll).block();

  }

  private static class ReactiveStationSohAnalysisManagerCreator implements CheckedRunnable {

    private ReactiveStationSohAnalysisManager stationSohAnalysisManager;

    @Override
    public void run() {
      stationSohAnalysisManager = ControlFactory.createControl(ReactiveStationSohAnalysisManager.class);
      checkState(stationSohAnalysisManager.hasNonEmptyConfiguration(),
        "StationSohAnalysisManager configuration was not loaded!");
    }

    public ReactiveStationSohAnalysisManager getStationSohAnalysisManager() {
      return stationSohAnalysisManager;
    }
  }

}

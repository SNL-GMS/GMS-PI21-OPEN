package gms.shared.workflow.manager;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component("interval-polling-scheduler")
public class IntervalPollingScheduler {

  static final String BRIDGE_POLLING_PERIOD_CONFIG = "workflow-manager.bridge-polling-period";
  static final String BRIDGE_POLLING_PERIOD = "bridgePollingPeriod";

  private final ConfigurationConsumerUtility processingConfig;
  private final IntervalPollingJob intervalPollingJob;
  private final TaskScheduler taskScheduler;

  @Autowired
  public IntervalPollingScheduler(ConfigurationConsumerUtility processingConfig, IntervalPollingJob intervalPollingJob,
    TaskScheduler taskScheduler) {
    this.processingConfig = processingConfig;
    this.intervalPollingJob = intervalPollingJob;
    this.taskScheduler = taskScheduler;
  }

  public void runPollingJob() {
    var bridgedPollingPeriodConfig = processingConfig.resolve(BRIDGE_POLLING_PERIOD_CONFIG, List.of());
    var bridgedPollingPeriod = Duration.parse(bridgedPollingPeriodConfig.get(BRIDGE_POLLING_PERIOD).toString());

    var pollingHandle = taskScheduler.scheduleAtFixedRate(intervalPollingJob::updateWorkflowIntervals,
      bridgedPollingPeriod);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> pollingHandle.cancel(true)));
  }
}

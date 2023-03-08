package gms.shared.workflow.manager;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IntervalPollingSchedulerTest {

  @Mock
  ConfigurationConsumerUtility processingConfig;

  @Mock
  IntervalPollingJob intervalPollingJob;

  @Mock
  TaskScheduler taskScheduler;

  @Mock
  ScheduledFuture<?> pollingHandle;

  @Captor
  ArgumentCaptor<Runnable> jobRunnableCaptor;

  @Test
  void testRunPollingJob() {
    var pollingPeriodDuration = Duration.ofSeconds(Integer.MAX_VALUE);
    Map<String, Object> pollingPeriodConfig = Map.of(IntervalPollingScheduler.BRIDGE_POLLING_PERIOD, pollingPeriodDuration);
    given(processingConfig.resolve(eq(IntervalPollingScheduler.BRIDGE_POLLING_PERIOD_CONFIG), anyList())).willReturn(pollingPeriodConfig);
    doReturn(pollingHandle).when(taskScheduler).scheduleAtFixedRate(jobRunnableCaptor.capture(), eq(pollingPeriodDuration));

    var worflowPollingExecutor = new IntervalPollingScheduler(processingConfig, intervalPollingJob, taskScheduler);
    worflowPollingExecutor.runPollingJob();

    jobRunnableCaptor.getValue().run();
    verify(intervalPollingJob).updateWorkflowIntervals();
  }
}
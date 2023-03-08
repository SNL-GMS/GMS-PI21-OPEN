package gms.shared.workflow.manager;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.system.events.SystemEventPublisher;
import gms.shared.workflow.accessor.WorkflowAccessor;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.MockIntervalData;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import gms.shared.workflow.repository.BridgedIntervalRepository;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IntervalPollingJobTest {

  @Mock
  WorkflowAccessor mockAccessor;

  @Mock
  BridgedIntervalRepository mockBridgedIntervalRepository;

  @Mock
  Workflow mockWorkflow;

  @Mock
  KafkaProducer<String, String> mockKafkaProducer;

  @Mock
  ConfigurationConsumerUtility mockProcessingConfig;

  @Test
  void testUpdateWorkflowIntervals() {
    var stageIds = Stream.of("Auto 1", "Test 1", "Auto 2", "Test 2")
      .map(WorkflowDefinitionId::from)
      .collect(toSet());

    var startTime = Instant.EPOCH;
    var endTime = Instant.EPOCH.plusSeconds(300);
    var timeLastPolled = Instant.EPOCH;
    var stageIntervals = MockIntervalData.get(startTime, endTime, stageIds);
    var newPollTime = Instant.EPOCH.plusSeconds(300);
    IntervalId cachedIntervalId = stageIntervals.get("Test 1").get(0).getIntervalId();
    IntervalId notCachedIntervalId = stageIntervals.get("Test 2").get(0).getIntervalId();

    Supplier<Instant> currentTimeSupplier = () -> Instant.EPOCH.plusSeconds(300);

    given(mockAccessor.getTimeLastPolled()).willReturn(timeLastPolled);
    given(mockAccessor.getWorkflow()).willReturn(mockWorkflow);
    willReturn(true).given(mockAccessor).isInCache(cachedIntervalId);
    willReturn(false).given(mockAccessor).isInCache(notCachedIntervalId);
    given(mockWorkflow.stageIds()).willReturn(stageIds.stream());
    given(mockBridgedIntervalRepository.findStageIntervalsByStageIdAndTime(startTime, endTime, stageIds, timeLastPolled))
      .willReturn(stageIntervals);

    var systemEventPublisher = new SystemEventPublisher(mockKafkaProducer);

    Duration start = Duration.ofSeconds(300);
    Duration end = Duration.ZERO;
    Map<String, Object> configMap = Map.ofEntries(
      Map.entry(IntervalPollingJob.OPERATIONAL_PERIOD_START, start.toString()),
      Map.entry(IntervalPollingJob.OPERATIONAL_PERIOD_END, end.toString()));
    given(mockProcessingConfig.resolve(IntervalPollingJob.OPERATIONAL_TIME_PERIOD_CONFIG, Collections.emptyList())).willReturn(configMap);

    var workflowPollingJob = new IntervalPollingJob(
      mockAccessor, mockBridgedIntervalRepository, systemEventPublisher,
      currentTimeSupplier, mockProcessingConfig);

    workflowPollingJob.updateWorkflowIntervals();

    verify(mockAccessor).pruneStageIntervals(startTime);
    verify(mockBridgedIntervalRepository).findStageIntervalsByStageIdAndTime(startTime, endTime, stageIds, timeLastPolled);
    verify(mockAccessor).cacheStageIntervals(Set.of(
      stageIntervals.get("Auto 1").get(0),
      stageIntervals.get("Auto 2").get(0),
      stageIntervals.get("Test 2").get(0)
    ));
    verify(mockAccessor).setTimeLastPolled(newPollTime);
  }
}

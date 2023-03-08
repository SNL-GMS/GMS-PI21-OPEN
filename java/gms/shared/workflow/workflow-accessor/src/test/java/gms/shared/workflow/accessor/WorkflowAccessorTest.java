package gms.shared.workflow.accessor;

import gms.shared.workflow.api.IntervalRepositoryInterface;
import gms.shared.workflow.cache.IntervalCache;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.MockIntervalData;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import net.jodah.failsafe.RetryPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkflowAccessorTest {

  @Mock
  Workflow mockWorkflow;

  @Mock
  IntervalRepositoryInterface mockRepository;

  @Mock
  IntervalCache mockCache;

  WorkflowAccessor workflowAccessor;

  @BeforeEach
  void setUp() {
    workflowAccessor = new WorkflowAccessor(mockWorkflow, mockRepository, mockCache);
  }

  @Test
  void testInitializeCache() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    Set<String> stageNames = Stream.of("AUTO-TEST", "TEST-1", "TEST-2").collect(toSet());
    Set<WorkflowDefinitionId> stageIds = stageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(toSet());
    Map<String, List<StageInterval>> expectedIntervals = MockIntervalData.get(startTime, endTime, stageIds);

    var expectedLatestStoredIntervalModTime = Instant.now().plusSeconds(10000);
    var newInterval = ((InteractiveAnalysisStageInterval) expectedIntervals.get("TEST-1")
      .get(0))
      .toBuilder()
      .setModificationTime(expectedLatestStoredIntervalModTime).build();

    var retryPolicy = new RetryPolicy<Map<String, List<StageInterval>>>();

    expectedIntervals.put("TEST-1", List.of(newInterval));

    given(mockWorkflow.stageIds()).willReturn(stageIds.stream());
    given(mockRepository.findStageIntervalsByStageIdAndTime(startTime, endTime, stageIds))
      .willReturn(expectedIntervals);
    given(mockCache.getAll(stageNames, startTime, endTime)).willReturn(List.of());


    workflowAccessor.initializeCache(startTime, endTime, retryPolicy);
    expectedIntervals.forEach((stageName, intervals) -> verify(mockCache).putAll(intervals));
    Assertions.assertEquals(expectedLatestStoredIntervalModTime, workflowAccessor.getTimeLastPolled());

  }

  @Test
  void testInitializeCacheRetryFailure() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    Set<String> stageNames = Stream.of("AUTO-TEST", "TEST-1", "TEST-2").collect(toSet());
    Set<WorkflowDefinitionId> stageIds = stageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(toSet());

    var retryPolicy = new RetryPolicy<Map<String, List<StageInterval>>>();

    given(mockWorkflow.stageIds()).willReturn(stageIds.stream());
    given(mockRepository.findStageIntervalsByStageIdAndTime(startTime, endTime, stageIds))
      .willThrow(new RuntimeException("BOOM"));


    workflowAccessor.initializeCache(startTime, endTime, retryPolicy);
    verify(mockCache, times(0)).putAll(anyCollection());
  }

  @Test
  void getWorkflow() {
    assertEquals(mockWorkflow, workflowAccessor.getWorkflow());
  }

  @Test
  void testFindStageIntervalsByStageIdAndTime() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    Set<String> stageNames = Stream.of("AUTO-TEST", "TEST-1", "TEST-2").collect(toSet());
    Set<WorkflowDefinitionId> stageIds = stageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(toSet());
    Map<String, List<StageInterval>> expectedIntervals = MockIntervalData.get(startTime, endTime, stageIds);

    given(mockCache.getAll(stageNames, startTime, endTime)).willReturn(
      expectedIntervals.values().stream().flatMap(List::stream).collect(Collectors.toList()));
    Map<String, List<StageInterval>> actualIntervals = workflowAccessor.findStageIntervalsByStageIdAndTime(startTime, endTime, stageIds);
    assertEquals(expectedIntervals, actualIntervals);
    verify(mockCache).getAll(stageNames, startTime, endTime);
  }

  @Test
  void testIsInCache() {

    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    var stageNames = Stream.of("AUTO-TEST").collect(toSet());
    var stageIds = stageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(toSet());
    var expectedIntervals = MockIntervalData.get(startTime, endTime, stageIds).get("AUTO-TEST");

    var intervalId = IntervalId.from(Instant.EPOCH, WorkflowDefinitionId.from("TEST-1"));
    given(mockCache.get(intervalId)).willReturn(Optional.of(expectedIntervals.get(0)));

    Assertions.assertTrue(workflowAccessor.isInCache(intervalId));

    var notCachedInterval = IntervalId.from(Instant.EPOCH, WorkflowDefinitionId.from("TEST-2"));
    Assertions.assertFalse(workflowAccessor.isInCache(notCachedInterval));

  }

  @Test
  void testFindStageIntervalById() {

    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    var stageNames = Stream.of("AUTO-TEST").collect(toSet());
    var stageIds = stageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(toSet());

    var expectedStageInterval = MockIntervalData.get(startTime, endTime, stageIds).get("AUTO-TEST").get(0);

    var intervalId = IntervalId.from(Instant.EPOCH, WorkflowDefinitionId.from("TEST-1"));
    given(mockCache.get(intervalId)).willReturn(Optional.of(expectedStageInterval));

    Assertions.assertEquals(Optional.of(expectedStageInterval), workflowAccessor.findStageIntervalById(intervalId));

    var notCachedInterval = IntervalId.from(Instant.EPOCH, WorkflowDefinitionId.from("TEST-2"));
    Assertions.assertEquals(Optional.empty(), workflowAccessor.findStageIntervalById(notCachedInterval));

  }

  @Test
  void testCacheStageIntervals() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(300);
    var stageNames = Stream.of("AUTO-TEST", "TEST-1", "TEST-2").collect(toSet());
    var stageIds = stageNames.stream()
      .map(WorkflowDefinitionId::from)
      .collect(toSet());
    var expectedIntervals = MockIntervalData.get(startTime, endTime, stageIds).values()
      .stream()
      .collect(flatMapping(List::stream, toList()));

    //No exceptions should be thrown
    Assertions.assertDoesNotThrow(() -> workflowAccessor.cacheStageIntervals(expectedIntervals));

    //accessor should have called mockCache.putAll once for all intervals
    verify(mockCache).putAll(expectedIntervals);
  }

  @Test
  void testUpdateCallsDelegate() {
    var stageIntervalId = IntervalId.from(Instant.EPOCH, WorkflowDefinitionId.from("TEST"));
    UnaryOperator<StageInterval> updateFn = s -> s;
    workflowAccessor.update(stageIntervalId, updateFn);
    verify(mockCache).update(stageIntervalId, updateFn);
  }

  @Test
  void testUpdateIfPresentCallsDelegate() {
    var stageIntervalId = IntervalId.from(Instant.EPOCH, WorkflowDefinitionId.from("TEST"));
    Function<StageInterval, Optional<StageInterval>> updateFn = Optional::of;
    workflowAccessor.updateIfPresent(stageIntervalId, updateFn);
    verify(mockCache).updateIfPresent(stageIntervalId, updateFn);
  }

  @Test
  void testPruneCallsDelegate() {
    Instant olderThan = Instant.EPOCH;
    workflowAccessor.pruneStageIntervals(olderThan);
    verify(mockCache).prune(olderThan);
  }
}
package gms.shared.workflow.cache;

import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.cache.utils.IgniteTestUtility;
import gms.shared.workflow.cache.util.WorkflowCacheFactory;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.MockIntervalData;
import gms.shared.workflow.coi.StageInterval;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("component")
class IntervalCacheTest {
  IntervalCache intervalCache;

  @TempDir
  static Path tempIgnitePath;

  @BeforeAll
  static void setup() {
    IgniteTestUtility.initializeLocally(tempIgnitePath, WorkflowCacheFactory.INTERVAL_CACHE);
  }

  @BeforeEach
  void setUp() {
    intervalCache = IntervalCache.create();
  }

  @AfterEach
  void tearDown() {
    intervalCache.clear();
  }

  @AfterAll
  static void afterAll() {
    IgniteConnectionManager.close();
  }

  @Test
  void testGet() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(500);
    String stageName = "Test Stage";
    var intervalData = MockIntervalData.get(
      startTime,
      endTime,
      Set.of(WorkflowDefinitionId.from(stageName)));
    StageInterval stageInterval = intervalData.get(stageName).get(0);
    IntervalId intervalId = stageInterval.getIntervalId();

    assertTrue(intervalCache.get(intervalId).isEmpty());
    assertTrue(intervalCache.get(stageName, startTime).isEmpty());

    intervalCache.put(stageInterval);
    intervalCache.get(intervalId).ifPresentOrElse(
      interval -> assertEquals(stageInterval, interval),
      Assertions::fail
    );
    intervalCache.get(stageName, startTime).ifPresentOrElse(
      interval -> assertEquals(stageInterval, interval),
      Assertions::fail
    );
  }

  @Test
  void testGetRange() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(500);
    String stageName = "Test Stage";
    var intervalData = MockIntervalData.get(
      startTime,
      endTime,
      Set.of(WorkflowDefinitionId.from(stageName)));
    var stageInterval = (InteractiveAnalysisStageInterval) intervalData.get(stageName).get(0);

    assertTrue(intervalCache.get(stageName, startTime, endTime).isEmpty());
    StageInterval nextInterval = stageInterval.toBuilder()
      .setStartTime(startTime.plusSeconds(500))
      .setEndTime(endTime.plusSeconds(500))
      .build();
    intervalCache.putAll(List.of(stageInterval, nextInterval));

    List<StageInterval> actualIntervals = intervalCache.get(stageName, startTime, endTime);
    assertEquals(List.of(stageInterval), actualIntervals);

    actualIntervals = intervalCache.get(stageName, startTime.plusSeconds(501), endTime.plusSeconds(300));
    assertEquals(List.of(nextInterval), actualIntervals);

    actualIntervals = intervalCache.get(stageName, startTime, endTime.plusSeconds(300));
    assertEquals(List.of(stageInterval, nextInterval), actualIntervals);
  }

  @Test
  void testGetAll() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(500);
    String firstStage = "Test 1";
    String secondStage = "Test 2";
    var intervalData = MockIntervalData.get(
      startTime,
      endTime,
      Set.of(WorkflowDefinitionId.from(firstStage), WorkflowDefinitionId.from(secondStage)));
    var firstInterval = (InteractiveAnalysisStageInterval) intervalData.get(firstStage).get(0);
    var secondInterval = (InteractiveAnalysisStageInterval) intervalData.get(secondStage).get(0);

    assertTrue(intervalCache.getAll(Set.of(firstStage, secondStage), startTime, endTime).isEmpty());

    intervalCache.put(firstInterval);
    intervalCache.put(secondInterval);

    var actualIntervals = intervalCache.getAll(Set.of(firstStage, secondStage), startTime, endTime);
    assertEquals(2, actualIntervals.size());
    assertTrue(actualIntervals.contains(firstInterval));
    assertTrue(actualIntervals.contains(secondInterval));
  }


  @Test
  void testPrune() {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(500);
    String firstStage = "Test 1";
    String secondStage = "Test 2";
    var intervalData = MockIntervalData.get(
      startTime,
      endTime,
      Set.of(WorkflowDefinitionId.from(firstStage), WorkflowDefinitionId.from(secondStage)));
    var firstInterval = (InteractiveAnalysisStageInterval) intervalData.get(firstStage).get(0);
    var nextFirstInterval = firstInterval.toBuilder()
      .setStartTime(startTime.plusSeconds(500))
      .setEndTime(endTime.plusSeconds(500))
      .build();
    var secondInterval = (InteractiveAnalysisStageInterval) intervalData.get(secondStage).get(0);
    var nextSecondInterval = secondInterval.toBuilder()
      .setStartTime(startTime.plusSeconds(500))
      .setEndTime(endTime.plusSeconds(500))
      .build();

    assertTrue(intervalCache.getAll(Set.of(firstStage, secondStage), startTime, endTime.plusSeconds(500)).isEmpty());
    intervalCache.putAll(Set.of(firstInterval, nextFirstInterval));
    intervalCache.putAll(Set.of(secondInterval, nextSecondInterval));

    //test if the intervals are cached
    var returnedIntervals = intervalCache.getAll(Set.of(firstStage, secondStage), startTime, endTime.plusSeconds(500));
    assertTrue(returnedIntervals.contains(firstInterval));
    assertTrue(returnedIntervals.contains(nextFirstInterval));
    assertTrue(returnedIntervals.contains(secondInterval));
    assertTrue(returnedIntervals.contains(nextSecondInterval));
    assertEquals(4, returnedIntervals.size());

    intervalCache.prune(startTime);

    returnedIntervals = intervalCache.getAll(Set.of(firstStage, secondStage), startTime, endTime.plusSeconds(500));
    assertTrue(returnedIntervals.contains(firstInterval));
    assertTrue(returnedIntervals.contains(nextFirstInterval));
    assertTrue(returnedIntervals.contains(secondInterval));
    assertTrue(returnedIntervals.contains(nextSecondInterval));
    assertEquals(4, returnedIntervals.size());


    intervalCache.prune(startTime.plusSeconds(500));
    returnedIntervals = intervalCache.getAll(Set.of(firstStage, secondStage), startTime, endTime.plusSeconds(500));
    assertTrue(returnedIntervals.contains(nextFirstInterval));
    assertTrue(returnedIntervals.contains(nextSecondInterval));
    assertEquals(2, returnedIntervals.size());
  }
}
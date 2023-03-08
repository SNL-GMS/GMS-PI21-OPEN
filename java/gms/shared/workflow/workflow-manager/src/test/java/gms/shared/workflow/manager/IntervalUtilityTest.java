package gms.shared.workflow.manager;

import gms.shared.frameworks.service.InvalidInputException;
import gms.shared.workflow.coi.ActivityInterval;
import gms.shared.workflow.coi.InteractiveAnalysisStageInterval;
import gms.shared.workflow.coi.Interval;
import gms.shared.workflow.coi.IntervalFixtures;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static gms.shared.workflow.coi.IntervalFixtures.ANALYST_1;
import static gms.shared.workflow.coi.IntervalFixtures.ANALYST_2;
import static gms.shared.workflow.coi.IntervalFixtures.activityInterval1Id;
import static gms.shared.workflow.coi.IntervalFixtures.activityInterval2Id;
import static gms.shared.workflow.coi.IntervalFixtures.completeActivityInterval1;
import static gms.shared.workflow.coi.IntervalFixtures.completedInteractiveAnalysisStageInterval;
import static gms.shared.workflow.coi.IntervalFixtures.inProgressActivityInterval1;
import static gms.shared.workflow.coi.IntervalFixtures.inProgressInteractiveAnalysisStageInterval;
import static gms.shared.workflow.coi.IntervalFixtures.notCompleteActivityInterval1;
import static gms.shared.workflow.coi.IntervalFixtures.notCompleteInteractiveAnalysisStageInterval;
import static gms.shared.workflow.coi.IntervalFixtures.notStartedActivityInterval2;
import static gms.shared.workflow.coi.IntervalFixtures.notStartedInteractiveAnalysisStageInterval;
import static gms.shared.workflow.coi.IntervalFixtures.partiallyCompleteInteractiveAnalysisStageInterval;
import static gms.shared.workflow.coi.IntervalFixtures.readyForCompleteInteractiveAnalysisStageInterval;
import static gms.shared.workflow.coi.IntervalFixtures.singleActivityInProgressInteractiveAnalysisStageInterval;
import static java.util.function.Predicate.isEqual;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class IntervalUtilityTest {

  @ParameterizedTest
  @MethodSource("openInteractiveStageIntervalsValidStatuses")
  void testOpenInteractiveStageIntervalsValidStatus(InteractiveAnalysisStageInterval validStageInterval,
    String userName) {

    var interactiveAnalysisStageIntervals = assertDoesNotThrow(() -> IntervalUtility
      .openInteractiveStageIntervals(List.of(validStageInterval), userName));

    assertTrue(interactiveAnalysisStageIntervals.stream()
      .flatMap(InteractiveAnalysisStageInterval::activityIntervals)
      .allMatch(activityInterval -> activityInterval.getActiveAnalysts().contains(ANALYST_2)));

    assertTrue(interactiveAnalysisStageIntervals.stream()
      .map(Interval::getStatus)
      .allMatch(isEqual(IntervalStatus.IN_PROGRESS)));

    assertTrue(interactiveAnalysisStageIntervals.stream()
      .flatMap(InteractiveAnalysisStageInterval::activityIntervals)
      .map(ActivityInterval::getStatus)
      .allMatch(isEqual(IntervalStatus.IN_PROGRESS)));
  }

  private static Stream<Arguments> openInteractiveStageIntervalsValidStatuses() {
    return Stream.of(
      arguments(notStartedInteractiveAnalysisStageInterval, ANALYST_2),
      arguments(inProgressInteractiveAnalysisStageInterval, ANALYST_2),
      arguments(notCompleteInteractiveAnalysisStageInterval, ANALYST_2),
      arguments(completedInteractiveAnalysisStageInterval, ANALYST_2)
    );
  }

  @Test
  void testOpenInteractiveStageIntervalNoOp() {
    var interactiveAnalysisStageIntervals = IntervalUtility.openInteractiveStageIntervals(
      List.of(inProgressInteractiveAnalysisStageInterval), ANALYST_1
    );
    assertEquals(List.of(inProgressInteractiveAnalysisStageInterval), interactiveAnalysisStageIntervals);
  }

  @Test
  void testCloseInteractiveStageInterval() {

    var closedStageIntervals = IntervalUtility.closeInteractiveStageIntervals(
      List.of(inProgressInteractiveAnalysisStageInterval), ANALYST_1);

    assertTrue(closedStageIntervals.stream()
      .flatMap(InteractiveAnalysisStageInterval::activityIntervals)
      .noneMatch(activityInterval -> activityInterval.getActiveAnalysts().contains(ANALYST_1)));

    assertEquals(1, closedStageIntervals.size());
    InteractiveAnalysisStageInterval closedStageInterval = closedStageIntervals.get(0);
    assertEquals(2, closedStageInterval.getActivityIntervals().size());
    assertEquals(IntervalStatus.IN_PROGRESS, closedStageInterval.getStatus());
    assertTrue(closedStageInterval.getActivityIntervals().get(0).getActiveAnalysts().contains(ANALYST_2));
    assertEquals(IntervalStatus.IN_PROGRESS, closedStageInterval.getActivityIntervals().get(0).getStatus());
    assertEquals(IntervalStatus.NOT_COMPLETE, closedStageInterval.getActivityIntervals().get(1).getStatus());

    closedStageIntervals = IntervalUtility.closeInteractiveStageIntervals(closedStageIntervals, ANALYST_2);
    assertEquals(1, closedStageIntervals.size());
    closedStageInterval = closedStageIntervals.get(0);

    assertEquals(IntervalStatus.NOT_COMPLETE, closedStageInterval.getStatus());

    closedStageIntervals = IntervalUtility.closeInteractiveStageIntervals(
      List.of(singleActivityInProgressInteractiveAnalysisStageInterval), ANALYST_1);

    assertEquals(1, closedStageIntervals.size());
    closedStageInterval = closedStageIntervals.get(0);
    assertEquals(2, closedStageInterval.getActivityIntervals().size());
    assertEquals(IntervalStatus.NOT_COMPLETE, closedStageInterval.getStatus());
    assertEquals(IntervalStatus.NOT_STARTED, closedStageInterval.getActivityIntervals().get(0).getStatus());
    assertEquals(IntervalStatus.NOT_COMPLETE, closedStageInterval.getActivityIntervals().get(1).getStatus());

    closedStageIntervals = IntervalUtility.closeInteractiveStageIntervals(
      List.of(partiallyCompleteInteractiveAnalysisStageInterval), ANALYST_1);

    assertEquals(1, closedStageIntervals.size());
    closedStageInterval = closedStageIntervals.get(0);
    assertEquals(2, closedStageInterval.getActivityIntervals().size());
    assertEquals(IntervalStatus.NOT_COMPLETE, closedStageInterval.getStatus());
    assertEquals(IntervalStatus.NOT_COMPLETE, closedStageInterval.getActivityIntervals().get(0).getStatus());
    assertEquals(IntervalStatus.COMPLETE, closedStageInterval.getActivityIntervals().get(1).getStatus());
  }

  @Test
  void testCloseInteractiveStageIntervalNoOp() {
    var interactiveAnalysisStageIntervals = IntervalUtility.closeInteractiveStageIntervals(
      List.of(notCompleteInteractiveAnalysisStageInterval), ANALYST_2
    );
    assertEquals(List.of(notCompleteInteractiveAnalysisStageInterval), interactiveAnalysisStageIntervals);
  }

  @Test
  void testCompleteInteractiveStageInterval() {

    var interactiveAnalysisStageIntervals = IntervalUtility.completeInteractiveStageIntervals(
      List.of(readyForCompleteInteractiveAnalysisStageInterval));
    assertEquals(1, interactiveAnalysisStageIntervals.size());
    assertEquals(IntervalStatus.COMPLETE, interactiveAnalysisStageIntervals.get(0).getStatus());
  }

  @Test
  void testCompleteInteractiveStageIntervalNoOp() {
    var interactiveAnalysisStageIntervals = IntervalUtility.completeInteractiveStageIntervals(
      List.of(completedInteractiveAnalysisStageInterval)
    );
    assertEquals(List.of(completedInteractiveAnalysisStageInterval), interactiveAnalysisStageIntervals);
  }

  @Test
  void testActivityIntervalNotFound() {
    var badTimeIntervalId = IntervalId.from(IntervalFixtures.START_TIME.plusSeconds(5), WorkflowDefinitionId.from(IntervalFixtures.ACTIVITY_1_NAME));
    var badActivityNameIntervalId = IntervalId.from(IntervalFixtures.START_TIME, WorkflowDefinitionId.from("BAD NAME"));
    List<ActivityInterval> activityIntervals = notStartedInteractiveAnalysisStageInterval.getActivityIntervals();

    Assertions.assertThrows(InvalidInputException.class, () -> IntervalUtility.findActivityInterval(
      activityIntervals, badTimeIntervalId));
    Assertions.assertThrows(InvalidInputException.class, () -> IntervalUtility.findActivityInterval(
      activityIntervals, badActivityNameIntervalId));
  }

  @Test
  void testOpenActivityInterval() {

    var interactiveAnalysisStageInterval = IntervalUtility.openActivityInterval(
      notStartedInteractiveAnalysisStageInterval, activityInterval1Id, ANALYST_1);

    assertEquals(IntervalStatus.IN_PROGRESS, interactiveAnalysisStageInterval.getStatus());

    List<ActivityInterval> activityIntervals = interactiveAnalysisStageInterval.getActivityIntervals();
    assertEquals(2, activityIntervals.size());
    assertEquals(IntervalStatus.IN_PROGRESS, activityIntervals.get(0).getStatus());
    assertTrue(activityIntervals.get(0).getActiveAnalysts().contains(ANALYST_1));

    assertEquals(notStartedActivityInterval2, activityIntervals.get(1));
  }

  @Test
  void testOpenActivityIntervalNoOp() {
    var interactiveAnalysisStageInterval = IntervalUtility.openActivityInterval(
      inProgressInteractiveAnalysisStageInterval, inProgressActivityInterval1.getIntervalId(), ANALYST_2
    );
    assertEquals(inProgressInteractiveAnalysisStageInterval, interactiveAnalysisStageInterval);
  }

  @Test
  void testCloseActivityInterval() {

    var actualStageInterval = IntervalUtility.closeActivityInterval(
      inProgressInteractiveAnalysisStageInterval, activityInterval1Id, ANALYST_1);
    assertEquals(IntervalStatus.IN_PROGRESS, actualStageInterval.getStatus());
    var actualActivityIntervals = actualStageInterval.getActivityIntervals();
    assertEquals(2, actualActivityIntervals.size());
    assertEquals(List.of(ANALYST_2), actualActivityIntervals.get(0).getActiveAnalysts());
    assertEquals(IntervalStatus.IN_PROGRESS, actualActivityIntervals.get(0).getStatus());

    actualStageInterval = IntervalUtility.closeActivityInterval(
      actualStageInterval, activityInterval2Id, ANALYST_1);
    assertEquals(IntervalStatus.IN_PROGRESS, actualStageInterval.getStatus());
    actualActivityIntervals = actualStageInterval.getActivityIntervals();
    assertEquals(2, actualActivityIntervals.size());
    assertEquals(IntervalStatus.NOT_COMPLETE, actualActivityIntervals.get(1).getStatus());

    actualStageInterval = IntervalUtility.closeActivityInterval(actualStageInterval, activityInterval1Id, ANALYST_2);
    assertEquals(IntervalStatus.NOT_COMPLETE, actualStageInterval.getStatus());

    actualStageInterval = IntervalUtility.closeActivityInterval(partiallyCompleteInteractiveAnalysisStageInterval,
      activityInterval2Id, ANALYST_1);
    assertEquals(IntervalStatus.NOT_COMPLETE, actualStageInterval.getStatus());
  }

  @Test
  void testCloseActivityIntervalNoOp() {
    var interactiveAnalysisStageInterval = IntervalUtility.closeActivityInterval(
      notCompleteInteractiveAnalysisStageInterval, notCompleteActivityInterval1.getIntervalId(), ANALYST_2
    );
    assertEquals(notCompleteInteractiveAnalysisStageInterval, interactiveAnalysisStageInterval);
  }

  @Test
  void testCompleteActivityInterval() {

    //TODO: test what happens to other analysts working activity
    var actualStageInterval = IntervalUtility.completeActivityInterval(
      inProgressInteractiveAnalysisStageInterval, activityInterval2Id, ANALYST_1);

    var actualActivityIntervals = actualStageInterval.getActivityIntervals();
    assertEquals(2, actualActivityIntervals.size());
    assertEquals(IntervalStatus.COMPLETE, actualActivityIntervals.get(1).getStatus());
    assertTrue(actualActivityIntervals.get(1).getActiveAnalysts().isEmpty());
  }

  @Test
  void testCompleteActivityIntervalNoOp() {
    var interactiveAnalysisStageInterval = IntervalUtility.completeActivityInterval(
      completedInteractiveAnalysisStageInterval, completeActivityInterval1.getIntervalId(), ANALYST_2
    );
    assertEquals(completedInteractiveAnalysisStageInterval, interactiveAnalysisStageInterval);
  }
}

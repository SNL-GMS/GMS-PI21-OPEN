package gms.shared.workflow.coi;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class MockWorkflowTests {

  @Test
  void testMockWorkflowSerialization() {
    TestUtilities.assertSerializes(MockWorkflow.get(), Workflow.class);
  }

  @Test
  void testMockIntervalDataSerialization() {
    List<WorkflowDefinitionId> stageIds = Stream.of("AL1", "AL2", "Auto Network", "Auto Post-AL1")
      .map(WorkflowDefinitionId::from)
      .collect(toList());

    var mockIntervals = MockIntervalData.get(Instant.EPOCH, Instant.EPOCH.plusSeconds(1), stageIds);

    for (List<StageInterval> stageIntervals : mockIntervals.values()) {
      TestUtilities.assertSerializes(stageIntervals.get(0), StageInterval.class);
    }
  }

}

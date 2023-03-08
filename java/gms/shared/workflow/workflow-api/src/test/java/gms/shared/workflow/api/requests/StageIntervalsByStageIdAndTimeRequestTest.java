package gms.shared.workflow.api.requests;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

class StageIntervalsByStageIdAndTimeRequestTest {

  @Test
  void testSerialization() throws IOException {
    Instant startTime = Instant.EPOCH;
    Instant endTime = startTime.plusSeconds(30);
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    StageIntervalsByStageIdAndTimeRequest request = StageIntervalsByStageIdAndTimeRequest.from(startTime, endTime, List.of(stageId));
    TestUtilities.assertSerializes(request, StageIntervalsByStageIdAndTimeRequest.class);
  }
}
package gms.shared.workflow.api.requests;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

class UpdateInteractiveAnalysisStageIntervalStatusRequestTest {

  @Test
  void testSerialization() throws IOException {

    var request = UpdateInteractiveAnalysisStageIntervalStatusRequest.builder()
      .setUserName("Test User")
      .setTime(Instant.MAX)
      .setStageIntervalId(IntervalId.from(Instant.now(), WorkflowDefinitionId.from("STAGE")))
      .setStatus(IntervalStatus.NOT_STARTED)
      .build();

    TestUtilities.assertSerializes(request, UpdateInteractiveAnalysisStageIntervalStatusRequest.class);
  }
}
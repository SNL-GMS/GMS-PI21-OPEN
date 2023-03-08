package gms.shared.workflow.api.requests;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.IntervalId;
import gms.shared.workflow.coi.IntervalStatus;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

class UpdateActivityIntervalStatusRequestTest {
  @Test
  void testSerialization() throws IOException {

    var request = UpdateActivityIntervalStatusRequest.builder()
      .setUserName("Test User")
      .setTime(Instant.MAX)
      .setStageIntervalId(IntervalId.from(Instant.now(), WorkflowDefinitionId.from("STAGE")))
      .setActivityIntervalId(IntervalId.from(Instant.now(), WorkflowDefinitionId.from("ACTIVITY")))
      .setStatus(IntervalStatus.NOT_STARTED)
      .build();

    TestUtilities.assertSerializes(request, UpdateActivityIntervalStatusRequest.class);
  }
}
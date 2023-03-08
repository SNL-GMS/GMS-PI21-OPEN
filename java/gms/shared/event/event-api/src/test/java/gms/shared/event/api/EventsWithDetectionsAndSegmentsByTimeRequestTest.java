package gms.shared.event.api;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

class EventsWithDetectionsAndSegmentsByTimeRequestTest {

  @Test
  void testSerialization() throws IOException {
    var startTime = Instant.EPOCH;
    var endTime = startTime.plusSeconds(300);
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    var request = EventsWithDetectionsAndSegmentsByTimeRequest.create(startTime, endTime, stageId);
    TestUtilities.assertSerializes(request, EventsWithDetectionsAndSegmentsByTimeRequest.class);
  }
}

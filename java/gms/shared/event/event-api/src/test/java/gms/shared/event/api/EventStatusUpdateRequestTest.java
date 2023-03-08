package gms.shared.event.api;

import gms.shared.event.coi.EventStatus;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

class EventStatusUpdateRequestTest {

  @Test
  void testSerialization() throws IOException {
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    var request = EventStatusUpdateRequest.from(
      stageId,
      UUID.randomUUID(),
      EventStatusInfo.from(EventStatus.IN_PROGRESS, List.of("analystID1", "analystID2")));
    TestUtilities.assertSerializes(request, EventStatusUpdateRequest.class);
  }
}

package gms.shared.event.api;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

class EventsByIdRequestTest {

  @Test
  void testSerialization() throws IOException {
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    var request = EventsByIdRequest.create(List.of(UUID.randomUUID(), UUID.randomUUID()), stageId);
    TestUtilities.assertSerializes(request, EventsByIdRequest.class);
  }
}

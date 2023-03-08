package gms.shared.event.api;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

class EventStatusInfoByStageIdAndEventIdsRequestTest {

  @Test
  void testSerialization() throws IOException {
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    var request = EventStatusInfoByStageIdAndEventIdsRequest.create(stageId, List.of(UUID.randomUUID()));
    TestUtilities.assertSerializes(request, EventStatusInfoByStageIdAndEventIdsRequest.class);
  }
}

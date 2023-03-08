package gms.shared.event.api;

import gms.shared.event.coi.EventStatus;
import gms.shared.event.coi.EventStatusInfo;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

class EventStatusInfoByStageIdAndEventIdsResponseTest {

  @Test
  void testSerialization() throws IOException {

    var stageId = WorkflowDefinitionId.from("test");
    var eventId = UUID.randomUUID();
    var eventStatusInfo = EventStatusInfo.from(EventStatus.COMPLETE, Arrays.asList("analyst1"));
    var eventStatusInfos = Map.of(eventId, eventStatusInfo);

    var eventStatusInfoByStageIdAndEventIdsResponse = EventStatusInfoByStageIdAndEventIdsResponse.builder()
      .setStageId(stageId)
      .setEventStatusInfoMap(eventStatusInfos)
      .build();
    TestUtilities.assertSerializes(eventStatusInfoByStageIdAndEventIdsResponse, EventStatusInfoByStageIdAndEventIdsResponse.class);
  }
}

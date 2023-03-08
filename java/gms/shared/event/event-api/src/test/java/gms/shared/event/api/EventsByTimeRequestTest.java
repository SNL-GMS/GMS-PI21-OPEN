package gms.shared.event.api;

import gms.shared.event.coi.Event;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

class EventsByTimeRequestTest {

  @Test
  void testSerialization() throws IOException {
    var startTime = Instant.EPOCH;
    var endTime = startTime.plusSeconds(300);
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    var facetingDefinition = FacetingDefinition.builder()
      .setClassType(Event.class.getSimpleName())
      .setPopulated(true)
      .build();
    var request = EventsByTimeRequest.create(startTime, endTime, stageId, facetingDefinition);
    TestUtilities.assertSerializes(request, EventsByTimeRequest.class);
  }
}

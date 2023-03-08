package gms.shared.event.api;

import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class EventsByAssociatedSignalDetectionHypothesesRequestTest {

  @Test
  void testSerialization() throws IOException {
    var sdHypotheses = List.of(SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS);
    WorkflowDefinitionId stageId = WorkflowDefinitionId.from("test");
    var request = EventsByAssociatedSignalDetectionHypothesesRequest.create(sdHypotheses, stageId);
    TestUtilities.assertSerializes(request, EventsByAssociatedSignalDetectionHypothesesRequest.class);
  }
}

package gms.shared.signaldetection.database.connector.config;

import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StageDatabaseAccountPairTest {

  @Test
  void testSerialization() throws IOException {
    StageDatabaseAccountPair pair = StageDatabaseAccountPair.create(WorkflowDefinitionId.from("test"), "test account", true);
    TestUtilities.assertSerializes(pair, StageDatabaseAccountPair.class);
  }

}
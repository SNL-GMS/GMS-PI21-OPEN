package gms.shared.signaldetection.database.connector.config;

import com.google.common.collect.ImmutableList;
import gms.shared.utilities.test.TestUtilities;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StagePersistenceDefinitionTest {

  private static final WorkflowDefinitionId workflowDefinitionID1 = WorkflowDefinitionId.from("Stage1");
  private static final WorkflowDefinitionId workflowDefinitionID2 = WorkflowDefinitionId.from("Stage2");
  private static final WorkflowDefinitionId workflowDefinitionID3 = WorkflowDefinitionId.from("Stage3");
  private static final String firstStageAcct = "Acc1";
  private static final String secondStageAcct = "Acc2";
  private static final String thirdStageAcct = "Acc3";

  private static final List<WorkflowDefinitionId> orderedStages =
    List.of(workflowDefinitionID1, workflowDefinitionID2, workflowDefinitionID3);

  private static final ImmutableList<StageDatabaseAccountPair> databaseAccountStages =
    ImmutableList.of(StageDatabaseAccountPair.create(workflowDefinitionID1, firstStageAcct, false),
      StageDatabaseAccountPair.create(workflowDefinitionID2, secondStageAcct, true),
      StageDatabaseAccountPair.create(workflowDefinitionID3, thirdStageAcct, true));

  @Test
  void testCreateInvalid() {

    var expectedMessage = "Database Accounts per stage have to be set";
    var emptyList = ImmutableList.<StageDatabaseAccountPair>of();

    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> StagePersistenceDefinition.create(emptyList));

    assertEquals(expectedMessage, exception.getMessage());

  }

  @Test
  void testCreateValid() {
    assertDoesNotThrow(() -> StagePersistenceDefinition.create(databaseAccountStages));
  }

  @Test
  void testSerialization() throws IOException {
    StagePersistenceDefinition definition = StagePersistenceDefinition.create(databaseAccountStages);
    TestUtilities.assertSerializes(definition, StagePersistenceDefinition.class);
  }

  @Test
  void testGetDatabaseAccountsByStageMap() {
    StagePersistenceDefinition definition = StagePersistenceDefinition.create(databaseAccountStages);
    assertEquals(3, definition.getDatabaseAccountsByStageMap().size());
  }

  @Test
  void testGetPreviousDatabaseAccountsByStageMap() {
    StagePersistenceDefinition definition = StagePersistenceDefinition.create(databaseAccountStages);
    assertEquals(2, definition.getPreviousDatabaseAccountsByStageMap().size());
  }
}


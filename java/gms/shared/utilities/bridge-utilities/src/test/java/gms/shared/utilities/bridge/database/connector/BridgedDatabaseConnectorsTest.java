package gms.shared.utilities.bridge.database.connector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class BridgedDatabaseConnectorsTest {
  @Mock
  DatabaseConnector databaseConnectorOne;
  @Mock
  DatabaseConnector databaseConnectorTwo;

  private static final String STAGE_ONE_ID = "STAGE_ONE";
  private static final String STAGE_TWO_ID = "STAGE_TWO";
  private static final String STAGE_THREE_ID = "STAGE_THREE";
  private static final String BAD_STAGE_ID = "BAD_STAGE";

  private BridgedDatabaseConnectors bridgedDatabaseConnectors;
  private DatabaseConnectorType<DatabaseConnector> databaseConnectorType;

  @BeforeEach
  void init() {
    databaseConnectorType = () -> DatabaseConnector.class;
    bridgedDatabaseConnectors = initializeBridgedDatabaseConnectors();
    bridgedDatabaseConnectors.addConnectorForCurrentStage(STAGE_ONE_ID, databaseConnectorOne);
    bridgedDatabaseConnectors.addConnectorForPreviousStage(STAGE_TWO_ID, databaseConnectorTwo);
    bridgedDatabaseConnectors.addConnectorForCurrentStage(STAGE_THREE_ID, null);
    bridgedDatabaseConnectors.addConnectorForPreviousStage(STAGE_THREE_ID, null);
  }

  @Test
  void testAddConnectorForCurrentStage() {
    String testStage1 = "TEST_STAGE_1";
    bridgedDatabaseConnectors.addConnectorForCurrentStage(testStage1, databaseConnectorOne);
    assertNotNull(bridgedDatabaseConnectors.getConnectorForCurrentStage(testStage1, databaseConnectorType));
  }

  @Test
  void testAddConnectorForPreviousStage() {
    String testStage2 = "TEST_STAGE_2";
    bridgedDatabaseConnectors.addConnectorForPreviousStage(testStage2, databaseConnectorTwo);
    assertTrue(bridgedDatabaseConnectors.connectorExistsForPreviousStage(testStage2, databaseConnectorType));
  }

  @Test
  void testGetConnectorForCurrentStageOrThrow() {
    assertNotNull(bridgedDatabaseConnectors.getConnectorForCurrentStageOrThrow(STAGE_ONE_ID, databaseConnectorType));
    assertThrows(IllegalArgumentException.class, () -> bridgedDatabaseConnectors.getConnectorForCurrentStageOrThrow(
      BAD_STAGE_ID, databaseConnectorType));
  }

  @Test
  void testGetConnectorForPreviousStageOrThrow() {
    assertNotNull(bridgedDatabaseConnectors.getConnectorForPreviousStageOrThrow(STAGE_TWO_ID, databaseConnectorType));
    assertThrows(IllegalArgumentException.class, () -> bridgedDatabaseConnectors.getConnectorForPreviousStageOrThrow(
      BAD_STAGE_ID, databaseConnectorType));
  }

  @Test
  void testGetConnectorForCurrentStage() {
    assertTrue(bridgedDatabaseConnectors.getConnectorForCurrentStage(STAGE_ONE_ID, databaseConnectorType).isPresent());
    assertTrue(bridgedDatabaseConnectors.getConnectorForCurrentStage(BAD_STAGE_ID, databaseConnectorType).isEmpty());
    assertTrue(bridgedDatabaseConnectors.getConnectorForCurrentStage(STAGE_THREE_ID, databaseConnectorType).isEmpty());
  }

  @Test
  void testGetConnectorForPreviousStage() {
    assertTrue(bridgedDatabaseConnectors.getConnectorForPreviousStage(STAGE_TWO_ID, databaseConnectorType).isPresent());
    assertTrue(bridgedDatabaseConnectors.getConnectorForPreviousStage(BAD_STAGE_ID, databaseConnectorType).isEmpty());
    assertTrue(bridgedDatabaseConnectors.getConnectorForPreviousStage(STAGE_THREE_ID, databaseConnectorType).isEmpty());
  }

  @Test
  void testConnectorExistsForPreviousStage() {
    DatabaseConnectorType<?> badConnectorType = () -> Object.class;
    assertTrue(bridgedDatabaseConnectors.connectorExistsForPreviousStage(STAGE_TWO_ID, databaseConnectorType));
    assertFalse(bridgedDatabaseConnectors.connectorExistsForPreviousStage(BAD_STAGE_ID, databaseConnectorType));
    assertFalse(bridgedDatabaseConnectors.connectorExistsForPreviousStage(STAGE_TWO_ID, badConnectorType));
  }

  @Test
  void testGetCurrentStageDatabaseConnectors() {
    assertEquals(2, bridgedDatabaseConnectors.getCurrentStageDatabaseConnectors().size());
  }

  @Test
  void testGetPreviousStageDatabaseConnectors() {
    assertEquals(2, bridgedDatabaseConnectors.getPreviousStageDatabaseConnectors().size());
  }

  /**
   * Initialize the abstract {@link BridgedDatabaseConnectors} with overriden methods for
   * populating the current and previous stage database connectors
   *
   * @return {@link BridgedDatabaseConnectors|}
   */
  private BridgedDatabaseConnectors initializeBridgedDatabaseConnectors() {
    return new BridgedDatabaseConnectors() {

      @Override
      public <T> Class<?> getClassForConnector(T databaseConnector) {
        return DatabaseConnector.class;
      }
    };
  }
}
package gms.shared.signaldetection.database.connector;

import com.google.common.collect.ImmutableMap;
import gms.shared.emf.staged.EntityManagerFactoriesByStageId;
import gms.shared.signaldetection.database.connector.config.SignalDetectionBridgeDefinition;
import gms.shared.utilities.bridge.database.connector.DatabaseConnectorType;
import gms.shared.utilities.javautilities.objectmapper.DatabaseLivenessCheck;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import javax.persistence.EntityManagerFactory;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.AMPLITUDE_CONNECTOR_TYPE;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.ARRIVAL_CONNECTOR_TYPE;
import static gms.shared.signaldetection.database.connector.SignalDetectionDatabaseConnectorTypes.ASSOC_CONNECTOR_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SignalDetectionBridgeDatabaseConnectorsTest {

  @Mock
  EntityManagerFactory entityManagerFactory;

  @Mock
  ObjectProvider<AmplitudeDatabaseConnector> amplitudeDatabaseConnectorProvider;

  @Mock
  AmplitudeDatabaseConnector amplitudeDatabaseConnector;

  @Mock
  ObjectProvider<ArrivalDatabaseConnector> arrivalDatabaseConnectorProvider;

  @Mock
  ArrivalDatabaseConnector arrivalDatabaseConnector;

  @Mock
  ObjectProvider<AssocDatabaseConnector> assocDatabaseConnectorProvider;

  @Mock
  AssocDatabaseConnector assocDatabaseConnector;

  @Mock
  Object unknownDatabaseConnector;

  @Mock
  DatabaseLivenessCheck databaseLivenessCheck;

  private static final boolean DATABASE_IS_ALIVE = true;
  private static final WorkflowDefinitionId STAGE_ONE_ID = WorkflowDefinitionId.from("STAGE_ONE");
  private static final WorkflowDefinitionId STAGE_TWO_ID = WorkflowDefinitionId.from("STAGE_TWO");
  private static final String STAGE_ONE_NAME = STAGE_ONE_ID.getName();
  private static final String STAGE_TWO_NAME = STAGE_TWO_ID.getName();
  private static final Duration measuredWaveformLeadDuration = Duration.ofMillis(500);
  private static final Duration measuredWaveformLagDuration = Duration.ofMillis(300);

  SignalDetectionBridgeDefinition signalDetectionBridgeDefinition;
  SignalDetectionBridgeDatabaseConnectors databaseConnectors;
  ImmutableMap<WorkflowDefinitionId, EntityManagerFactory> stageEmfMap;
  EntityManagerFactoriesByStageId entityManagerFactoriesByStageId;

  @BeforeEach
  void init() {

    // initialize db connectors
    doReturn(amplitudeDatabaseConnector)
      .when(amplitudeDatabaseConnectorProvider).getObject(any());
    doReturn(arrivalDatabaseConnector)
      .when(arrivalDatabaseConnectorProvider).getObject(any());
    doReturn(assocDatabaseConnector)
      .when(assocDatabaseConnectorProvider).getObject(any());

    // set the signal detection bridge definition for database accounts by stage
    String stageOneAccount = "stage_one_account";
    String stageTwoAccount = "stage_two_account";
    String monitoringOrganization = "MonitoringOrganization";
    signalDetectionBridgeDefinition = SignalDetectionBridgeDefinition.builder()
      .setMonitoringOrganization(monitoringOrganization)
      .setOrderedStages(List.of(STAGE_ONE_ID, STAGE_TWO_ID))
      .setDatabaseAccountByStage(
        Map.of(
          STAGE_ONE_ID, stageOneAccount,
          STAGE_TWO_ID, stageTwoAccount
        )
      )
      .setMeasuredWaveformLagDuration(measuredWaveformLagDuration)
      .setMeasuredWaveformLeadDuration(measuredWaveformLeadDuration)
      .build();

    stageEmfMap = ImmutableMap.of(
      STAGE_ONE_ID, entityManagerFactory,
      STAGE_TWO_ID, entityManagerFactory
    );

    entityManagerFactoriesByStageId = EntityManagerFactoriesByStageId.builder()
      .setStageIdEmfMap(stageEmfMap)
      .build();

    when(databaseLivenessCheck.isLive()).thenReturn(true);

    databaseConnectors = new SignalDetectionBridgeDatabaseConnectors(signalDetectionBridgeDefinition,
      amplitudeDatabaseConnectorProvider, arrivalDatabaseConnectorProvider, assocDatabaseConnectorProvider,
      entityManagerFactoriesByStageId, databaseLivenessCheck);

  }

  @Test
  void testGetClassForConnector() {
    assertThrows(IllegalArgumentException.class, () -> databaseConnectors.getClassForConnector(unknownDatabaseConnector));
  }

  @Test
  void testGetCurrentStageConnectorOrThrow() {
    AmplitudeDatabaseConnector amplitudeDatabaseConnector = databaseConnectors.getConnectorForCurrentStageOrThrow(
      STAGE_ONE_NAME, AMPLITUDE_CONNECTOR_TYPE);
    ArrivalDatabaseConnector arrivalDatabaseConnector = databaseConnectors.getConnectorForCurrentStageOrThrow(
      STAGE_ONE_NAME, ARRIVAL_CONNECTOR_TYPE);
    AssocDatabaseConnector assocDatabaseConnector = databaseConnectors.getConnectorForCurrentStageOrThrow(
      STAGE_ONE_NAME, ASSOC_CONNECTOR_TYPE);

    assertAll(() -> {
      assertNotNull(amplitudeDatabaseConnector);
      assertNotNull(arrivalDatabaseConnector);
      assertNotNull(assocDatabaseConnector);

      var badStage = "BAD_STAGE";
      assertThrows(IllegalArgumentException.class, () -> databaseConnectors.getConnectorForCurrentStageOrThrow(
        badStage, ARRIVAL_CONNECTOR_TYPE));
    });
  }

  @Test
  void testGetPreviousStageConnectorOrThrow() {
    AmplitudeDatabaseConnector amplitudeDatabaseConnector = databaseConnectors.getConnectorForPreviousStageOrThrow(
      STAGE_TWO_NAME, AMPLITUDE_CONNECTOR_TYPE);
    ArrivalDatabaseConnector arrivalDatabaseConnector = databaseConnectors.getConnectorForPreviousStageOrThrow(
      STAGE_TWO_NAME, ARRIVAL_CONNECTOR_TYPE);
    AssocDatabaseConnector assocDatabaseConnector = databaseConnectors.getConnectorForPreviousStageOrThrow(
      STAGE_TWO_NAME, ASSOC_CONNECTOR_TYPE);

    assertAll(() -> {
      assertNotNull(amplitudeDatabaseConnector);
      assertNotNull(arrivalDatabaseConnector);
      assertNotNull(assocDatabaseConnector);

      var badStage = "BAD_STAGE";
      assertThrows(IllegalArgumentException.class, () -> databaseConnectors.getConnectorForPreviousStageOrThrow(
        badStage, ARRIVAL_CONNECTOR_TYPE));
    });
  }

  @Test
  void testGetCurrentStageConnectors() {
    Optional<AmplitudeDatabaseConnector> amplitudeDatabaseConnector = databaseConnectors.getConnectorForCurrentStage(
      STAGE_ONE_NAME, AMPLITUDE_CONNECTOR_TYPE);
    Optional<ArrivalDatabaseConnector> arrivalDatabaseConnector = databaseConnectors.getConnectorForCurrentStage(
      STAGE_ONE_NAME, ARRIVAL_CONNECTOR_TYPE);
    Optional<AssocDatabaseConnector> assocDatabaseConnector = databaseConnectors.getConnectorForCurrentStage(
      STAGE_ONE_NAME, ASSOC_CONNECTOR_TYPE);

    var badStage = "BAD_STAGE";
    Optional<ArrivalDatabaseConnector> arrivalDatabaseConnectorBadStage = databaseConnectors.getConnectorForCurrentStage(
      badStage, ARRIVAL_CONNECTOR_TYPE);

    assertAll(() -> {
      assertTrue(amplitudeDatabaseConnector.isPresent());
      assertTrue(arrivalDatabaseConnector.isPresent());
      assertTrue(assocDatabaseConnector.isPresent());

      assertTrue(arrivalDatabaseConnectorBadStage.isEmpty());
    });
  }

  @Test
  void testGetPreviousStageConnectors() {
    Optional<AmplitudeDatabaseConnector> amplitudeDatabaseConnector = databaseConnectors.getConnectorForPreviousStage(
      STAGE_TWO_NAME, AMPLITUDE_CONNECTOR_TYPE);
    Optional<ArrivalDatabaseConnector> arrivalDatabaseConnector = databaseConnectors.getConnectorForPreviousStage(
      STAGE_TWO_NAME, ARRIVAL_CONNECTOR_TYPE);
    Optional<AssocDatabaseConnector> assocDatabaseConnector = databaseConnectors.getConnectorForPreviousStage(
      STAGE_TWO_NAME, ASSOC_CONNECTOR_TYPE);

    var badStage = "BAD_STAGE";
    Optional<ArrivalDatabaseConnector> arrivalDatabaseConnectorBadStage = databaseConnectors.getConnectorForPreviousStage(
      badStage, ARRIVAL_CONNECTOR_TYPE);

    assertAll(() -> {
      assertTrue(amplitudeDatabaseConnector.isPresent());
      assertTrue(arrivalDatabaseConnector.isPresent());
      assertTrue(assocDatabaseConnector.isPresent());

      assertTrue(arrivalDatabaseConnectorBadStage.isEmpty());
    });
  }

  @Test
  void testConnectorExistsForPreviousStage() {
    DatabaseConnectorType<?> badConnectorType = () -> Object.class;
    assertAll(() -> {
      assertTrue(databaseConnectors.connectorExistsForPreviousStage(STAGE_TWO_NAME, AMPLITUDE_CONNECTOR_TYPE));
      assertTrue(databaseConnectors.connectorExistsForPreviousStage(STAGE_TWO_NAME, ARRIVAL_CONNECTOR_TYPE));
      assertTrue(databaseConnectors.connectorExistsForPreviousStage(STAGE_TWO_NAME, ASSOC_CONNECTOR_TYPE));

      assertFalse(databaseConnectors.connectorExistsForPreviousStage(STAGE_TWO_NAME, badConnectorType));
      assertFalse(databaseConnectors.connectorExistsForPreviousStage("BAD_STAGE",
        ARRIVAL_CONNECTOR_TYPE));
    });
  }
}
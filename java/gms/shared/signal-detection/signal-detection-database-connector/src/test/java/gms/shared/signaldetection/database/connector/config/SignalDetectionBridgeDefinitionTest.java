package gms.shared.signaldetection.database.connector.config;


import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SignalDetectionBridgeDefinitionTest {

  private static final WorkflowDefinitionId firstStage = WorkflowDefinitionId.from("first");
  private static final WorkflowDefinitionId secondStage = WorkflowDefinitionId.from("second");
  private static final WorkflowDefinitionId thirdStage = WorkflowDefinitionId.from("third");
  private static final String monitoringOrganization = "monitoringOrganization";
  private static final String firstStageAccount = "firstStageAccount";
  private static final String secondStageAccount = "secondStageAccount";
  private static final String thirdStageAccount = "thirdStageAccount";

  private static final List<WorkflowDefinitionId> orderedStages = List.of(firstStage, secondStage, thirdStage);

  private static final Map<WorkflowDefinitionId, String> previousStageDatabaseAccountsByStage =
    Map.of(secondStage, firstStageAccount,
      thirdStage, secondStageAccount);

  private static final Map<WorkflowDefinitionId, String> databaseAccountByStage =
    Map.of(firstStage, firstStageAccount,
      secondStage, secondStageAccount,
      thirdStage, thirdStageAccount);

  private static final Duration measuredWaveformLeadDuration = Duration.ofMillis(500);
  private static final Duration measuredWaveformLagDuration = Duration.ofMillis(300);

  @ParameterizedTest
  @MethodSource("getBuildValidationArguments")
  void testBuildValidation(String expectedMessage,
    String monitoringOrganization,
    List<WorkflowDefinitionId> orderedStages,
    Map<WorkflowDefinitionId, String> databaseAccountByStage,
    Duration measuredWaveformLeadDuration,
    Duration measuredWaveformLagDuration) {

    SignalDetectionBridgeDefinition.Builder builder = SignalDetectionBridgeDefinition.builder()
      .setMonitoringOrganization(monitoringOrganization)
      .setOrderedStages(orderedStages)
      .setDatabaseAccountByStage(databaseAccountByStage)
      .setMeasuredWaveformLeadDuration(measuredWaveformLeadDuration)
      .setMeasuredWaveformLagDuration(measuredWaveformLagDuration);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> builder.build());
    assertEquals(expectedMessage, exception.getMessage());
  }

  static Stream<Arguments> getBuildValidationArguments() {
    return Stream.of(
      arguments("Monitoring organization cannot be empty",
        "",
        orderedStages,
        databaseAccountByStage,
        measuredWaveformLeadDuration,
        measuredWaveformLagDuration),
      arguments("Monitoring organization cannot be blank",
        "   \t\n",
        orderedStages,
        databaseAccountByStage,
        measuredWaveformLeadDuration,
        measuredWaveformLagDuration),
      arguments("Ordered stages cannot be empty",
        monitoringOrganization,
        List.of(),
        databaseAccountByStage,
        measuredWaveformLeadDuration,
        measuredWaveformLagDuration),
      arguments("Missing database account for stage(s): first, second, third",
        monitoringOrganization,
        orderedStages,
        Map.of(),
        measuredWaveformLeadDuration,
        measuredWaveformLagDuration));
  }

  @Test
  void testBuild() {
    SignalDetectionBridgeDefinition.Builder builder = SignalDetectionBridgeDefinition.builder()
      .setMonitoringOrganization(monitoringOrganization)
      .setOrderedStages(orderedStages)
      .setDatabaseAccountByStage(databaseAccountByStage)
      .setMeasuredWaveformLeadDuration(measuredWaveformLeadDuration)
      .setMeasuredWaveformLagDuration(measuredWaveformLagDuration);
    SignalDetectionBridgeDefinition definition = assertDoesNotThrow(() -> builder.build());
    assertNotNull(definition);
  }

}
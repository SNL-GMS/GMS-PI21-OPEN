package gms.shared.signaldetection.database.connector.config;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gms.shared.workflow.coi.WorkflowDefinitionId;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AutoValue
public abstract class SignalDetectionBridgeDefinition {

  public abstract String getMonitoringOrganization();

  public abstract ImmutableList<WorkflowDefinitionId> getOrderedStages();

  public abstract ImmutableMap<WorkflowDefinitionId, String> getDatabaseAccountByStage();

  public abstract Duration getMeasuredWaveformLeadDuration();

  public abstract Duration getMeasuredWaveformLagDuration();

  public static Builder builder() {
    return new AutoValue_SignalDetectionBridgeDefinition.Builder();
  }

  @AutoValue.Builder
  public interface Builder {

    Builder setMonitoringOrganization(String monitoringOrganization);

    Builder setOrderedStages(ImmutableList<WorkflowDefinitionId> orderedStages);

    default Builder setOrderedStages(List<WorkflowDefinitionId> orderedStages) {
      return setOrderedStages(ImmutableList.copyOf(orderedStages));
    }

    Builder setDatabaseAccountByStage(ImmutableMap<WorkflowDefinitionId, String> databaseAccountByStage);

    default Builder setDatabaseAccountByStage(Map<WorkflowDefinitionId, String> databaseAccountByStage) {
      return setDatabaseAccountByStage(ImmutableMap.copyOf(databaseAccountByStage));
    }

    Builder setMeasuredWaveformLeadDuration(Duration measuredWaveformLeadDuration);

    Builder setMeasuredWaveformLagDuration(Duration measuredWaveformLagDuration);

    SignalDetectionBridgeDefinition autobuild();

    default SignalDetectionBridgeDefinition build() {
      SignalDetectionBridgeDefinition definition = autobuild();

      Preconditions.checkState(!definition.getMonitoringOrganization().isEmpty(),
        "Monitoring organization cannot be empty");

      Preconditions.checkState(!definition.getMonitoringOrganization().isBlank(),
        "Monitoring organization cannot be blank");

      Preconditions.checkState(!definition.getOrderedStages().isEmpty(),
        "Ordered stages cannot be empty");

      List<WorkflowDefinitionId> workflowsMissingDatabase = definition.getOrderedStages().stream()
        .filter(stageId -> !definition.getDatabaseAccountByStage().containsKey(stageId))
        .collect(Collectors.toList());

      if (!workflowsMissingDatabase.isEmpty()) {
        throw new IllegalStateException("Missing database account for stage(s): " +
          workflowsMissingDatabase.stream()
            .map(WorkflowDefinitionId::getName)
            .collect(Collectors.joining(", ")));
      }

      return definition;
    }

  }
}

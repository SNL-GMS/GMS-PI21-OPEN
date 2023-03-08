package gms.shared.signaldetection.manager;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.signaldetection.database.connector.config.SignalDetectionBridgeDefinition;
import gms.shared.signaldetection.database.connector.config.StagePersistenceDefinition;
import gms.shared.signaldetection.database.connector.config.WaveformTrimDefinition;
import gms.shared.workflow.coi.WorkflowDefinition;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SignalDetectionBridgeConfiguration {

  private static final String MONITORING_ORG_CONFIG = "global.monitoring-org";
  private static final String ACCOUNTS_BY_STAGE_CONFIG = "global.stage-accounts";
  private static final String ORDERED_STAGES_CONFIG = "workflow-manager.workflow-definition";
  private static final String WAVEFORM_LEAD_LAG_CONFIG = "signal-detection.waveform-lead-lag";

  private final ConfigurationConsumerUtility configurationConsumerUtility;

  @Autowired
  public SignalDetectionBridgeConfiguration(ConfigurationConsumerUtility configurationConsumerUtility) {
    this.configurationConsumerUtility = configurationConsumerUtility;

  }

  public SignalDetectionBridgeDefinition getCurrentSignalDetectionBridgeDefinition() {
    var workflowDefinition = configurationConsumerUtility.resolve(
      ORDERED_STAGES_CONFIG,
      List.of(), WorkflowDefinition.class);
    var orderedAccountByStage = workflowDefinition.getStageNames().stream()
      .map(WorkflowDefinitionId::from).collect(Collectors.toList());

    var stagePersistenceDefinition = configurationConsumerUtility.resolve(
      ACCOUNTS_BY_STAGE_CONFIG,
      List.of(), StagePersistenceDefinition.class);

    var waveformTrimDefinition = configurationConsumerUtility.resolve(
      WAVEFORM_LEAD_LAG_CONFIG,
      List.of(), WaveformTrimDefinition.class);

    var monitoringOrganization = (String) configurationConsumerUtility.resolve(
      MONITORING_ORG_CONFIG, List.of()).get("monitoringOrganization");

    return SignalDetectionBridgeDefinition.builder()
      .setDatabaseAccountByStage(stagePersistenceDefinition.getDatabaseAccountsByStageMap())
      .setMonitoringOrganization(monitoringOrganization)
      .setOrderedStages(orderedAccountByStage)
      .setMeasuredWaveformLeadDuration(waveformTrimDefinition.getMeasuredWaveformLeadDuration())
      .setMeasuredWaveformLagDuration(waveformTrimDefinition.getMeasuredWaveformLagDuration())
      .build();
  }
}

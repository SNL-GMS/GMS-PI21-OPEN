package gms.shared.event.repository.config.processing;

import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventBridgeConfigurationTest {

  private ConfigurationConsumerUtility configurationConsumerUtility;

  @Mock
  SystemConfig systemConfig;

  @BeforeAll
  void init() {

    var configurationRoot = checkNotNull(
      Thread.currentThread().getContextClassLoader().getResource("configuration-base")
    ).getPath();

    configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(FileConfigurationRepository.create(new File(configurationRoot).toPath()))
      .retryConfiguration(RetryConfig.create(1, 2, ChronoUnit.SECONDS, 1))
      .build();
  }

  @Test
  void testDefaultEventBridgeDefinition() {

    when(systemConfig.getValue("account_one_jdbc_url")).thenReturn("jdbc:stage_one");
    when(systemConfig.getValue("account_two_jdbc_url")).thenReturn("jdbc:stage_two");
    when(systemConfig.getValue("account_three_jdbc_url")).thenReturn("jdbc:stage_three");
    when(systemConfig.getValue("account_four_jdbc_url")).thenReturn("jdbc:stage_four");

    var eventBridgeConfiguration = new EventBridgeConfiguration(configurationConsumerUtility, systemConfig);

    var stageOne = WorkflowDefinitionId.from("StageOne");
    var stageTwo = WorkflowDefinitionId.from("StageTwo");
    var stageThree = WorkflowDefinitionId.from("StageThree");
    var stageFour = WorkflowDefinitionId.from("StageFour");

    var expectedMonitoringOrganization = "MonitoringOrganization";

    var expectedOrderedStages = List.of(stageOne, stageTwo, stageThree, stageFour);

    var expectedDatabaseAccountsByStage = Map.of( stageOne, "jdbc:stage_one",
      stageTwo, "jdbc:stage_two",
      stageThree, "jdbc:stage_three",
      stageFour, "jdbc:stage_four"
    );

    var expectedPreviousDatabaseAccountsByStage = Map.of(
      stageTwo, "jdbc:stage_two",
      stageThree, "jdbc:stage_three",
      stageFour, "jdbc:stage_four"
    );

    var expectedEventBridgeDefinition = EventBridgeDefinition.builder()
      .setMonitoringOrganization(expectedMonitoringOrganization)
      .setOrderedStages(expectedOrderedStages)
      .setDatabaseUrlByStage(expectedDatabaseAccountsByStage)
      .setPreviousDatabaseUrlByStage(expectedPreviousDatabaseAccountsByStage)
      .build();

    var resolvedEventBridgeDefinition = eventBridgeConfiguration.getCurrentEventBridgeDefinition();

    assertEquals(expectedEventBridgeDefinition, resolvedEventBridgeDefinition);
  }

}

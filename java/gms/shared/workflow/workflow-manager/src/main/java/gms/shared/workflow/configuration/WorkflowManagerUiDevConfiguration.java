package gms.shared.workflow.configuration;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.workflow.accessor.WorkflowAccessor;
import gms.shared.workflow.api.IntervalRepositoryInterface;
import gms.shared.workflow.api.WorkflowAccessorInterface;
import gms.shared.workflow.coi.Workflow;
import gms.shared.workflow.repository.BridgedIntervalRepository;
import gms.shared.workflow.repository.MockedBridgedIntervalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Supplier;

@Configuration("workflow-managerConfiguration")
@EnableScheduling
@ConditionalOnProperty(prefix = "service.run-state", name = "manager-config", havingValue = "ui-dev")
@ComponentScan(basePackages = {"gms.shared.spring", "gms.shared.system.events"},
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "gms\\.shared\\.spring\\.persistence\\..*"))
public class WorkflowManagerUiDevConfiguration {

  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";

  /**
   * Provides the configured {@link Workflow} definition
   *
   * @param workflowConfiguration configuration to retrieve definition from
   * @return {@link Workflow} definition configured
   */
  @Bean
  @Autowired
  public Workflow workflow(WorkflowConfiguration workflowConfiguration) {
    return workflowConfiguration.resolveWorkflowDefinition();
  }

  /**
   * Provides the configured implementation of the {@link WorkflowAccessorInterface} for this configuration
   * <p>
   * NOTE: Alternative configurations must declare a bean with the same name to properly replace
   *
   * @param workflowAccessor Implementation of the {@link WorkflowAccessorInterface} configured
   * @return The configured implementation, allowing for alternative implementations to be declared per configuration
   */
  @Bean("workflow-accessor")
  @Autowired
  public WorkflowAccessorInterface workflowAccessorInterface(WorkflowAccessor workflowAccessor) {
    return workflowAccessor;
  }

  /**
   * Provides the configured implementation of the {@link IntervalRepositoryInterface} for this configuration
   * <p>
   * NOTE: Alternative configurations must declare a bean with the same name to properly replace
   *
   * @param processingConfig Config used to resolve operational time period to initialize repository with generated data
   * @return The configured implementation, allowing for alternative implementations to be declared per configuration
   */
  @Bean("interval-repository")
  @Autowired
  public BridgedIntervalRepository bridgedIntervalRepository(ConfigurationConsumerUtility processingConfig) {
    var operationalTimeConfig = processingConfig.resolve(OPERATIONAL_TIME_PERIOD_CONFIG,
      Collections.emptyList());
    var operationalStart = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_START).toString());
    var operationalEnd = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_END).toString());

    var now = Instant.now();
    return new MockedBridgedIntervalRepository(now.minus(operationalStart), now.minus(operationalEnd), Instant::now);
  }

  /**
   * Bean to provide {@link gms.shared.workflow.manager.IntervalPollingJob} with a supplier for determining the "current time"
   *
   * @return Supplier that returns "now" as the current time
   */
  @Bean
  Supplier<Instant> currentTimeSupplier(MockedBridgedIntervalRepository mockedBridgedIntervalRepository) {
    return mockedBridgedIntervalRepository::getLatestIntervalModTime;
  }
}

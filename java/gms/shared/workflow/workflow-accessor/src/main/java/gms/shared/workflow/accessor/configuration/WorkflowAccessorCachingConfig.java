package gms.shared.workflow.accessor.configuration;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.workflow.api.WorkflowAccessorInterface;
import gms.shared.workflow.coi.StageInterval;
import net.jodah.failsafe.RetryPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Convenience object for encapsulating arguments to initialize
 * {@link WorkflowAccessorInterface}'s caching.
 */
@Component
public class WorkflowAccessorCachingConfig {

  static final String OPERATIONAL_TIME_PERIOD_CONFIG = "global.operational-time-period";
  static final String OPERATIONAL_PERIOD_START = "operationalPeriodStart";
  static final String OPERATIONAL_PERIOD_END = "operationalPeriodEnd";
  static final String RETRY_INITIAL_DELAY = "retry-initial-delay";
  static final String RETRY_DELAY_UNITS = "retry-delay-units";
  static final String MAX_RETRY_ATTEMPTS = "retry-max-attempts";

  private final Duration operationalStart;
  private final Duration operationalEnd;
  private final RetryPolicy<Map<String, List<StageInterval>>> retryPolicy;

  @Autowired
  public WorkflowAccessorCachingConfig(SystemConfig systemConfig, ConfigurationConsumerUtility processingConfig) {
    var operationalTimeConfig = processingConfig.resolve(OPERATIONAL_TIME_PERIOD_CONFIG,
      Collections.emptyList());
    this.operationalStart = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_START).toString());
    this.operationalEnd = Duration.parse(operationalTimeConfig.get(OPERATIONAL_PERIOD_END).toString());
    var delayUnit = ChronoUnit.valueOf(systemConfig.getValue(RETRY_DELAY_UNITS));
    var initialDelay = systemConfig.getValueAsLong(RETRY_INITIAL_DELAY);
    var maxRetries = systemConfig.getValueAsInt(MAX_RETRY_ATTEMPTS);

    this.retryPolicy = new RetryPolicy<Map<String, List<StageInterval>>>()
      .withBackoff(initialDelay, initialDelay * 10, delayUnit)
      .withMaxAttempts(maxRetries);
  }

  /**
   * @return Operational start offset {@link Duration}
   */
  public Duration getOperationalStart() {
    return operationalStart;
  }

  /**
   * @return Operational end offset {@link Duration}
   */
  public Duration getOperationalEnd() {
    return operationalEnd;
  }

  /**
   * @return Retry policy used for retrieving {@link StageInterval}s to populate the cache
   */
  public RetryPolicy<Map<String, List<StageInterval>>> getRetryPolicy() {
    return retryPolicy;
  }
}

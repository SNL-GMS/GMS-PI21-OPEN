package gms.shared.workflow.accessor.configuration;

import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WorkflowAccessorCachingConfigTest {

  @Mock
  SystemConfig systemConfig;

  @Mock
  ConfigurationConsumerUtility processingConfig;

  @Test
  void testInstantiate() {
    Duration start = Duration.ofSeconds(1);
    Duration end = Duration.ZERO;
    var maxRetries = 1;
    var delay = 1L;
    Map<String, Object> configMap = Map.ofEntries(
      Map.entry(WorkflowAccessorCachingConfig.OPERATIONAL_PERIOD_START, start.toString()),
      Map.entry(WorkflowAccessorCachingConfig.OPERATIONAL_PERIOD_END, end.toString()));
    given(processingConfig.resolve(WorkflowAccessorCachingConfig.OPERATIONAL_TIME_PERIOD_CONFIG, Collections.emptyList())).willReturn(configMap);
    given(systemConfig.getValue(WorkflowAccessorCachingConfig.RETRY_DELAY_UNITS)).willReturn("SECONDS");
    given(systemConfig.getValueAsLong(WorkflowAccessorCachingConfig.RETRY_INITIAL_DELAY)).willReturn(delay);
    given(systemConfig.getValueAsInt(WorkflowAccessorCachingConfig.MAX_RETRY_ATTEMPTS)).willReturn(maxRetries);

    var config = new WorkflowAccessorCachingConfig(systemConfig, processingConfig);

    assertEquals(start, config.getOperationalStart());
    assertEquals(end, config.getOperationalEnd());
    assertEquals(Duration.ofSeconds(delay), config.getRetryPolicy().getDelay());
    assertEquals(maxRetries, config.getRetryPolicy().getMaxAttempts());
  }
}
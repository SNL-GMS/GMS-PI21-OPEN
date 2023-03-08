package gms.shared.frameworks.control;

import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;
import mockit.MockUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlContextTests {
  private static final String CONTROL_NAME = "control-name";

  private static final Duration EXPIRATION = Duration.ofDays(1);

  @Test
  void testBuilderValidatesParameters() {
    assertEquals("ControlContext.Builder requires non-null controlName",
      assertThrows(NullPointerException.class, () -> ControlContext.builder(null)).getMessage());
  }

  @Test
  void testBuilderOverrideSystemConfig() {
    final SystemConfig sysConfig = mockSystemConfigCreation();
    final ConfigurationRepository configRepo = mock(ConfigurationRepository.class);
    mockConfigurationConsumerUtilityCreation(configRepo);
    final ControlContext context = ControlContext.builder(CONTROL_NAME)
      .systemConfig(sysConfig)
      .processingConfigurationRepository(configRepo)
      .build();
    checkContextContains(context, sysConfig, configRepo);
  }

  @Test
  void testBuilderOverrideConfigRepo() {
    final SystemConfig sysConfig = mockSystemConfigCreation();
    final ConfigurationRepository configRepo = mock(ConfigurationRepository.class);
    mockConfigurationConsumerUtilityCreation(configRepo);
    final ControlContext context = ControlContext.builder(CONTROL_NAME)
      .processingConfigurationRepository(configRepo)
      .build();
    checkContextContains(context, sysConfig, configRepo);
  }

  @Test
  void testBuilderWithSystemConfigAndProcessingConfigRepo() {
    final SystemConfig sysConfig = mockSystemConfigCreation();
    final ConfigurationRepository configRepo = mock(ConfigurationRepository.class);
    mockConfigurationConsumerUtilityCreation(configRepo);
    final ControlContext context = ControlContext.builder(CONTROL_NAME)
      .systemConfig(sysConfig)
      .processingConfigurationRepository(configRepo)
      .build();
    checkContextContains(context, sysConfig, configRepo);
  }

  private static void checkContextContains(ControlContext context,
    SystemConfig sysConfig, ConfigurationRepository configRepo) {
    assertNotNull(context);
    assertEquals(sysConfig, context.getSystemConfig());
    assertEquals(configRepo, context.getProcessingConfigurationRepository());
  }

  private SystemConfig mockSystemConfigCreation() {
    final SystemConfig mockConfig = mock(SystemConfig.class);
    new MockUp<SystemConfig>() {
      @mockit.Mock
      public SystemConfig create(String name) {
        if (name.equals(CONTROL_NAME)) {
          return mockConfig;
        }
        throw new IllegalArgumentException("Expected to be passed " + CONTROL_NAME);
      }
    };
    given(mockConfig.getValueAsDuration(any())).willReturn(EXPIRATION);
    given(mockConfig.getValueAsInt("processing-retry-initial-delay")).willReturn(1);
    given(mockConfig.getValueAsInt("processing-retry-max-delay")).willReturn(10);
    given(mockConfig.getValue("processing-retry-delay-units")).willReturn("SECONDS");
    given(mockConfig.getValueAsInt("processing-retry-max-attempts")).willReturn(1);

    return mockConfig;
  }

  private void mockConfigurationConsumerUtilityCreation(ConfigurationRepository repo) {
    final ConfigurationConsumerUtility.Builder builder = mock(
      ConfigurationConsumerUtility.Builder.class);
    final ConfigurationConsumerUtility consumer = mock(ConfigurationConsumerUtility.class);
    when(builder.selectorCacheExpiration(EXPIRATION)).thenReturn(builder);
    when(builder.configurationNamePrefixes(List.of(CONTROL_NAME)))
      .thenReturn(builder);
    when(builder.retryConfiguration(any()))
      .thenReturn(builder);
    when(builder.build()).thenReturn(consumer);
    new MockUp<ConfigurationConsumerUtility>() {
      @mockit.Mock
      public ConfigurationConsumerUtility.Builder builder(ConfigurationRepository r) {
        if (r == repo) {
          return builder;
        }
        throw new IllegalArgumentException("Expected the provided config repo to be passed");
      }
    };
  }
}

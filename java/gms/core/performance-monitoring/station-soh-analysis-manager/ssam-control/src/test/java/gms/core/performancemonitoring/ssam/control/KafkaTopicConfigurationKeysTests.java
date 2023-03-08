package gms.core.performancemonitoring.ssam.control;

import com.google.common.collect.Streams;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.stream.Stream;

class KafkaTopicConfigurationKeysTests {

  @ParameterizedTest
  @MethodSource("getSystemConfigValueTestSource")
  void testGetSystemConfigValue(
    SystemConfig systemConfig,
    KafkaTopicConfigurationKeys configKey,
    String expectedConfigValue
  ) {

    Assertions.assertEquals(
      expectedConfigValue,
      configKey.getSystemConfigValue(systemConfig)
    );
  }

  private static Stream<Arguments> getSystemConfigValueTestSource() {

    return Streams.concat(
      Arrays.stream(KafkaTopicConfigurationKeys.values()).map(
        configKey -> Arguments.arguments(
          getMockSystemConfig(configKey, true),
          configKey,
          configKey.name()
        )
      ),
      Arrays.stream(KafkaTopicConfigurationKeys.values()).map(
        configKey -> Arguments.arguments(
          getMockSystemConfig(configKey, false),
          configKey,
          configKey.getDefaultValue()
        )
      )

    );
  }

  private static SystemConfig getMockSystemConfig(
    KafkaTopicConfigurationKeys configKey,
    boolean found
  ) {

    SystemConfig mockSystemConfing = Mockito.mock(SystemConfig.class);

    if (found) {
      Mockito.when(mockSystemConfing.getValue(configKey.getConfigKeyString()))
        .thenReturn(configKey.name());
    } else {
      Mockito.when(mockSystemConfing.getValue(configKey.getConfigKeyString()))
        .thenThrow(new MissingResourceException(
          "Dummy message", "Dummy classname", "dummy key"
        ));
    }

    return mockSystemConfing;
  }
}

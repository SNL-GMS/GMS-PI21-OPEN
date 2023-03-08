package gms.core.performancemonitoring.soh.control.configuration;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class ChannelSohDefinitionTests {

  @ParameterizedTest
  @MethodSource("validateTestProvider")
  void testValidation(
    String channelName,
    Set<SohMonitorType> sohMonitorTypesForRollup,
    Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> sohMonitorStatusThresholdDefinitionsBySohMonitorType,
    Class<? extends Throwable> expectedThrowable,
    String expectedMessage
  ) {

    Executable executable = () ->
      ChannelSohDefinition.create(
        channelName,
        sohMonitorTypesForRollup,
        sohMonitorStatusThresholdDefinitionsBySohMonitorType,
        0.0
      );

    if (expectedThrowable != null) {
      Throwable thrown = Assertions.assertThrows(expectedThrowable, executable);

      Assertions.assertEquals(expectedMessage, thrown.getMessage());
    } else {
      Assertions.assertDoesNotThrow(executable);
    }
  }

  static Stream<Arguments> validateTestProvider() {

    return Stream.of(
      Arguments.arguments(
        "X",
        Set.of(
          SohMonitorType.MISSING
        ),
        Map.of(
          SohMonitorType.MISSING,
          PercentSohMonitorStatusThresholdDefinition.create(
            0.0,
            1.0
          )
        ),
        null,
        null
      ),
      Arguments.arguments(
        "X",
        Set.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE
        ),
        Map.of(
          SohMonitorType.MISSING,
          PercentSohMonitorStatusThresholdDefinition.create(
            0.0,
            1.0
          )
        ),
        IllegalArgumentException.class,
        "Monitor types for rollup need to be associated with an SohMonitorStatusThresholdDefinition"
      ),
      Arguments.arguments(
        "",
        Set.of(
          SohMonitorType.MISSING
        ),
        Map.of(
          SohMonitorType.MISSING,
          PercentSohMonitorStatusThresholdDefinition.create(
            0.0,
            1.0
          )
        ),
        IllegalArgumentException.class,
        "Channel name can't be blank"
      )
    );

  }
}

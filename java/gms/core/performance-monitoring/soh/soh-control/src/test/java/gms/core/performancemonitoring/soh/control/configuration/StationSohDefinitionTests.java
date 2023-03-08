package gms.core.performancemonitoring.soh.control.configuration;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class StationSohDefinitionTests {

  @ParameterizedTest
  @MethodSource("validateTestProvider")
  void testValidation(
    String stationName,
    Set<SohMonitorType> sohMonitorTypesForRollup,
    Map<SohMonitorType, Set<String>> channelsBySohMonitorType,
    Set<ChannelSohDefinition> channelSohDefinitions,
    Map<SohMonitorType, TimeWindowDefinition> timeWindowBySohMonitorType,
    Class<? extends Throwable> expectedThrowable,
    String expectedMessage
  ) {

    Executable executable = () ->
      StationSohDefinition.create(
        stationName,
        sohMonitorTypesForRollup,
        channelsBySohMonitorType,
        channelSohDefinitions,
        timeWindowBySohMonitorType
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
        Set.of(SohMonitorType.MISSING),
        Map.of(SohMonitorType.MISSING, Set.of("XX")),
        Set.of(
          ChannelSohDefinition.create(
            "XX",
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition.create(
                0.0, 0.0
              )
            ),
            0.0
          )
        ),
        Map.of(
          SohMonitorType.MISSING,
          TimeWindowDefinition.create(
            Duration.ofDays(1),
            Duration.ofHours(1)
          )
        ),
        null,
        null
      ),
      Arguments.arguments(
        "X",
        Set.of(SohMonitorType.MISSING),
        Map.of(SohMonitorType.MISSING, Set.of("XX", "YY")),
        Set.of(
          ChannelSohDefinition.create(
            "YY",
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition.create(
                0.0, 0.0
              )
            ),
            0.0
          )
        ),
        Map.of(
          SohMonitorType.MISSING,
          TimeWindowDefinition.create(
            Duration.ofDays(1),
            Duration.ofHours(1)
          )
        ),
        IllegalArgumentException.class,
        "There are channels mapped to a SohMonitorType without configuration: [XX]"
      ),

      Arguments.arguments(
        "X",
        Set.of(SohMonitorType.ENV_BACKUP_POWER_UNSTABLE),
        Map.of(SohMonitorType.MISSING, Set.of("XX")),
        Set.of(
          ChannelSohDefinition.create(
            "XX",
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition.create(
                0.0, 0.0
              )
            ),
            0.0
          )
        ),
        Map.of(
          SohMonitorType.ENV_BACKUP_POWER_UNSTABLE,
          TimeWindowDefinition.create(
            Duration.ofDays(1),
            Duration.ofHours(1)
          )
        ),
        IllegalArgumentException.class,
        "Monitor types for rollup need to be associated with channels"
      ),

      Arguments.arguments(
        "",
        Set.of(SohMonitorType.MISSING),
        Map.of(SohMonitorType.MISSING, Set.of("XX")),
        Set.of(
          ChannelSohDefinition.create(
            "XX",
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition.create(
                0.0, 0.0
              )
            ),
            0.0
          )
        ),
        Map.of(
          SohMonitorType.MISSING,
          TimeWindowDefinition.create(
            Duration.ofDays(1),
            Duration.ofHours(1)
          )
        ),
        IllegalArgumentException.class,
        "Station name can't be blank"
      ),

      Arguments.arguments(
        "X",
        Set.of(SohMonitorType.MISSING),
        Map.of(SohMonitorType.MISSING, Set.of("XX")),
        Set.of(
          ChannelSohDefinition.create(
            "XX",
            Set.of(SohMonitorType.MISSING),
            Map.of(
              SohMonitorType.MISSING,
              PercentSohMonitorStatusThresholdDefinition.create(
                0.0, 0.0
              )
            ),
            0.0
          )
        ),
        Map.of(),
        IllegalArgumentException.class,
        "Monitor types for rollup need to be associated with calculation interval and backoff duration"
      )
    );
  }

}

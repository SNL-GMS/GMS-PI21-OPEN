package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.RollupOperatorConfigurationUtility.TerminalRollupOperatorResolver;
import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.MinGoodOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.WorstOfRollupOperator;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class RollupOperatorConfigurationUtilityTests {

  @ParameterizedTest
  @MethodSource("resolveOperatorTestSource")
  <T> void testResolveOperator(
    Map<String, Object> objectMap,
    List<T> ifEmptyList,
    TerminalRollupOperatorResolver terminalRollupOperatorResolver,
    RollupOperator expectedOperator
  ) {

    Assertions.assertEquals(
      expectedOperator,
      RollupOperatorConfigurationUtility.resolveOperator(
        objectMap,
        ifEmptyList,
        terminalRollupOperatorResolver
      )
    );
  }

  private static Stream<Arguments> resolveOperatorTestSource() {

    return Stream.of(

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "stationOperands", List.of("A", "B")
        ),
        List.of(),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        BestOfRollupOperator.from(
          List.of("A", "B"),
          List.of(),
          List.of(),
          List.of()
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF"
        ),
        List.of("A", "B"),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        BestOfRollupOperator.from(
          List.of("A", "B"),
          List.of(),
          List.of(),
          List.of()
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "channelOperands", List.of("A", "B")
        ),
        List.of(),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        WorstOfRollupOperator.from(
          List.of(),
          List.of("A", "B"),
          List.of(),
          List.of()
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF"
        ),
        List.of("A", "B"),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        WorstOfRollupOperator.from(
          List.of(),
          List.of("A", "B"),
          List.of(),
          List.of()
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "MIN_GOOD_OF",
          "goodThreshold", 2,
          "marginalThreshold", 1
        ),
        List.of("A", "B"),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        MinGoodOfRollupOperator.from(
          List.of(),
          List.of("A", "B"),
          List.of(),
          List.of(),
          2, 1
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "sohMonitorTypeOperands", List.of("MISSING", "LAG")
        ),
        List.of(),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        BestOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          List.of()
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "sohMonitorTypeOperands", List.of("MISSING", "LAG")
        ),
        List.of(),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        WorstOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          List.of()
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "MIN_GOOD_OF",
          "sohMonitorTypeOperands", List.of("MISSING", "LAG"),
          "goodThreshold", 2,
          "marginalThreshold", 1
        ),
        List.of(),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        MinGoodOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          List.of(),
          2, 1
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "MIN_GOOD_OF",
          "goodThreshold", 2,
          "marginalThreshold", 1
        ),
        List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        MinGoodOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          List.of(),
          2, 1
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "operatorType", "BEST_OF",
              "stationOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          )
        ),
        List.of(),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        BestOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            BestOfRollupOperator.from(
              List.of("A", "B"),
              List.of(),
              List.of(),
              List.of()
            ),
            WorstOfRollupOperator.from(
              List.of("C", "D"),
              List.of(),
              List.of(),
              List.of()
            )
          )
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "operatorType", "BEST_OF",
              "stationOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          )
        ),
        List.of(),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        WorstOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            BestOfRollupOperator.from(
              List.of("A", "B"),
              List.of(),
              List.of(),
              List.of()
            ),
            WorstOfRollupOperator.from(
              List.of("C", "D"),
              List.of(),
              List.of(),
              List.of()
            )
          )
        )
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "MIN_GOOD_OF",
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "operatorType", "BEST_OF",
              "stationOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          ),
          "goodThreshold", 2,
          "marginalThreshold", 1
        ),
        List.of(),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        MinGoodOfRollupOperator.from(
          List.of(),
          List.of(),
          List.of(),
          List.of(
            BestOfRollupOperator.from(
              List.of("A", "B"),
              List.of(),
              List.of(),
              List.of()
            ),
            WorstOfRollupOperator.from(
              List.of("C", "D"),
              List.of(),
              List.of(),
              List.of()
            )
          ),
          2, 1
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("exclusivityCheckTestSource")
  <T> void testExclusivityCheck(
    Map<String, Object> objectMap,
    TerminalRollupOperatorResolver terminalRollupOperatorResolver,
    String expectedExceptionMessage
  ) {

    List<Object> emptyList = List.of();
    Throwable exception = Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> RollupOperatorConfigurationUtility.resolveOperator(
        objectMap,
        emptyList,
        terminalRollupOperatorResolver
      )
    );

    if (expectedExceptionMessage != null) {
      Assertions.assertEquals(
        expectedExceptionMessage,
        exception.getMessage()
      );
    }
  }

  private static Stream<Arguments> exclusivityCheckTestSource() {

    return Stream.of(
      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "stationOperands", List.of("A", "B"),
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        "Only stationOperands can be specified for this RollupOperator, but channelOperands was specified: {channelOperands=[FF, GG], operatorType=BEST_OF, stationOperands=[A, B]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "stationOperands", List.of("A", "B"),
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "Only channelOperands can be specified for this RollupOperator, but stationOperands was specified: {channelOperands=[FF, GG], operatorType=BEST_OF, stationOperands=[A, B]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "sohMonitorTypeOperands", List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        "Only sohMonitorTypeOperands can be specified for this RollupOperator, but channelOperands was specified: {channelOperands=[FF, GG], operatorType=BEST_OF, sohMonitorTypeOperands=[MISSING, LAG]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "sohMonitorTypeOperands", List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          "stationOperands", List.of("A", "B")
        ),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        "Only sohMonitorTypeOperands can be specified for this RollupOperator, but stationOperands was specified: {operatorType=BEST_OF, sohMonitorTypeOperands=[MISSING, LAG], stationOperands=[A, B]}"
      ),

      //
      // Operands for operand type are not explicit
      //
      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        "Only stationOperands can be specified for this RollupOperator, but channelOperands was specified: {channelOperands=[FF, GG], operatorType=BEST_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "stationOperands", List.of("A", "B")
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "Only channelOperands can be specified for this RollupOperator, but stationOperands was specified: {operatorType=BEST_OF, stationOperands=[A, B]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        "Only sohMonitorTypeOperands can be specified for this RollupOperator, but channelOperands was specified: {channelOperands=[FF, GG], operatorType=BEST_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "stationOperands", List.of("A", "B")
        ),
        TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER,
        "Only sohMonitorTypeOperands can be specified for this RollupOperator, but stationOperands was specified: {operatorType=BEST_OF, stationOperands=[A, B]}"
      ),

      //
      // RollupOperator operands
      //
      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "stationOperands", List.of("A", "B"),
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "operatorType", "BEST_OF",
              "stationOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          )
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        "Only rollupOperatorOperands can be specified for this RollupOperator, but stationOperands was specified: {operatorType=WORST_OF, rollupOperatorOperands=[...], stationOperands=[A, B]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "operatorType", "BEST_OF",
              "channelOperands", List.of("FF", "GG"),
              "stationOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          )
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        "Only stationOperands can be specified for this RollupOperator, but channelOperands was specified: {channelOperands=[FF, GG], operatorType=BEST_OF, stationOperands=[A, B]}"
      ),

      //
      // Multiple inappropriate operand lists. Its not necessarily known what the exception message
      // will look like, because an exception is thrown on the first encounter of an inappropriate
      // operand list.
      //
      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "stationOperands", List.of("A", "B"),
          "sohMonitorTypeOperands", List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        null
      )
    );
  }

  @ParameterizedTest
  @MethodSource("otherChecksTestSource")
  void testOtherChecks(
    Map<String, Object> objectMap,
    TerminalRollupOperatorResolver terminalRollupOperatorResolver,
    String expectedExceptionMessage
  ) {
    List<Object> emptyList = List.of();
    Throwable exception = Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> RollupOperatorConfigurationUtility.resolveOperator(
        objectMap,
        emptyList,
        terminalRollupOperatorResolver
      )
    );

    Assertions.assertEquals(
      expectedExceptionMessage,
      exception.getMessage()
    );

  }

  private static Stream<Arguments> otherChecksTestSource() {

    return Stream.of(
      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "StaytioneOperands", List.of("A", "B"),
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "StaytioneOperands is not a valid field for rollup operators: {StaytioneOperands=[A, B], channelOperands=[FF, GG], operatorType=BEST_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "channelOperands", List.of("FF", "GG")
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "Missing operator type for this rollup operator: {channelOperands=[FF, GG]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "operatorType", "BEST_OF",
              "StaytioneOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          )
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        "StaytioneOperands is not a valid field for rollup operators: {StaytioneOperands=[A, B], operatorType=BEST_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "WORST_OF",
          "rollupOperatorOperands",
          List.of(
            Map.of(
              "stationOperands", List.of("A", "B")
            ),
            Map.of(
              "operatorType", "WORST_OF",
              "stationOperands", List.of("C", "D")
            )
          )
        ),
        TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER,
        "Missing operator type for this rollup operator: {stationOperands=[A, B]}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "MIN_GOOD_OF",
          "goodThreshold", 2
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "marginalThreshold is a required field for MIN_GOOD_OF but was not found: {goodThreshold=2, operatorType=MIN_GOOD_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "MIN_GOOD_OF",
          "marginalThreshold", 2
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "goodThreshold is a required field for MIN_GOOD_OF but was not found: {marginalThreshold=2, operatorType=MIN_GOOD_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "goodThreshold", 2
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "goodThreshold is only valid for MIN_GOOD_OF: {goodThreshold=2, operatorType=BEST_OF}"
      ),

      Arguments.arguments(
        Map.of(
          "operatorType", "BEST_OF",
          "marginalThreshold", 2
        ),
        TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER,
        "marginalThreshold is only valid for MIN_GOOD_OF: {marginalThreshold=2, operatorType=BEST_OF}"
      )
    );
  }
}

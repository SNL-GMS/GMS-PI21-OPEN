package gms.core.performancemonitoring.soh.control.configuration;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class RollupOperatorTests {

  @ParameterizedTest
  @MethodSource("validationTestSource")
  void testValidation(
    List<String> stationOperands,
    List<String> channelOperands,
    List<SohMonitorType> sohMonitorTypeOperands,
    List<RollupOperator> rollupOperatorOperands,
    String expectedMessage
  ) {

    if (expectedMessage == null) {

      Assertions.assertDoesNotThrow(
        () -> RollupOperator.validate(
          stationOperands,
          channelOperands,
          sohMonitorTypeOperands,
          rollupOperatorOperands
        )
      );
    } else {

      Throwable expectedThrowable = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> RollupOperator.validate(
          stationOperands,
          channelOperands,
          sohMonitorTypeOperands,
          rollupOperatorOperands
        )
      );

      Assertions.assertEquals(expectedMessage, expectedThrowable.getMessage());
    }
  }

  private static Stream<Arguments> validationTestSource() {

    return Stream.of(
      Arguments.arguments(
        List.of("A", "B"),
        List.of("A", "B"),
        List.of("A", "B"),
        List.of("A", "B"),
        "Only one parameter list can be nonempty. Sizes are: \n" +
          "   stationOperands: 2\n" +
          "   channelOperands: 2\n" +
          "   sohMonitorTypeOperands: 2\n" +
          "   rollupOperatorOperands: 2\n"
      ),
      Arguments.arguments(
        List.of(),
        List.of("A", "B"),
        List.of("A", "B"),
        List.of("A", "B"),
        "Only one parameter list can be nonempty. Sizes are: \n" +
          "   stationOperands: 0\n" +
          "   channelOperands: 2\n" +
          "   sohMonitorTypeOperands: 2\n" +
          "   rollupOperatorOperands: 2\n"
      ),
      Arguments.arguments(
        List.of("A", "B"),
        List.of(),
        List.of("A", "B"),
        List.of("A", "B"),
        "Only one parameter list can be nonempty. Sizes are: \n" +
          "   stationOperands: 2\n" +
          "   channelOperands: 0\n" +
          "   sohMonitorTypeOperands: 2\n" +
          "   rollupOperatorOperands: 2\n"
      ),
      Arguments.arguments(
        List.of("A", "B"),
        List.of("A", "B"),
        List.of(),
        List.of("A", "B"),
        "Only one parameter list can be nonempty. Sizes are: \n" +
          "   stationOperands: 2\n" +
          "   channelOperands: 2\n" +
          "   sohMonitorTypeOperands: 0\n" +
          "   rollupOperatorOperands: 2\n"
      ),
      Arguments.arguments(
        List.of("A", "B"),
        List.of("A", "B"),
        List.of("A", "B"),
        List.of(),
        "Only one parameter list can be nonempty. Sizes are: \n" +
          "   stationOperands: 2\n" +
          "   channelOperands: 2\n" +
          "   sohMonitorTypeOperands: 2\n" +
          "   rollupOperatorOperands: 0\n"
      ),
      Arguments.arguments(
        List.of("A", "B"),
        List.of(),
        List.of(),
        List.of(),
        null
      )
    );
  }
}

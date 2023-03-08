package gms.core.performancemonitoring.soh.control.capabilityrollup;

import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class RollupOperatorTypeTest {

  @ParameterizedTest
  @MethodSource("operationTestProvider")
  void testOperation(
    RollupOperatorType rollupOperatorType,
    List<SohStatus> input,
    int[] parameters,
    SohStatus expectedOutput
  ) {

    var actualOutput = rollupOperatorType.getOperation(parameters).apply(input);

    Assertions.assertSame(expectedOutput, actualOutput);
  }

  private static Stream<Arguments> operationTestProvider() {

    return Stream.of(
      Arguments.arguments(
        RollupOperatorType.BEST_OF,
        List.of(
          SohStatus.BAD,
          SohStatus.MARGINAL,
          SohStatus.BAD,
          SohStatus.GOOD
        ),
        new int[]{},
        SohStatus.GOOD
      ),

      Arguments.arguments(
        RollupOperatorType.WORST_OF,
        List.of(
          SohStatus.GOOD,
          SohStatus.MARGINAL,
          SohStatus.BAD,
          SohStatus.GOOD
        ),
        new int[]{},
        SohStatus.BAD
      ),

      Arguments.arguments(
        RollupOperatorType.MIN_GOOD_OF,
        List.of(
          SohStatus.GOOD,
          SohStatus.MARGINAL,
          SohStatus.BAD,
          SohStatus.GOOD
        ),
        new int[]{2, 1},
        SohStatus.GOOD
      ),

      Arguments.arguments(
        RollupOperatorType.MIN_GOOD_OF,
        List.of(
          SohStatus.BAD,
          SohStatus.MARGINAL,
          SohStatus.BAD,
          SohStatus.GOOD
        ),
        new int[]{2, 1},
        SohStatus.MARGINAL
      ),

      Arguments.arguments(
        RollupOperatorType.MIN_GOOD_OF,
        List.of(
          SohStatus.BAD,
          SohStatus.MARGINAL,
          SohStatus.BAD,
          SohStatus.MARGINAL
        ),
        new int[]{2, 1},
        SohStatus.BAD
      )
    );
  }
}

package gms.shared.utilities.signalprocessing.filter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IirTest {

  @Test
  void testFilter() {
    double[] aCoefficients = new double[]{1, .5, .5};
    double[] bCoefficients = new double[]{.25, .25};
    double[] input = new double[]{0, 0, 1, 0, 0};

    double[] expected = new double[]{0, 0, 0.25, 0.125, -0.1875};
    double[] actual = Iir.filter(input, aCoefficients, bCoefficients);

    assertArrayEquals(expected, actual, 10e-8);
  }

  @Test
  void testFilterNonNormalizedCoefficients() {
    double[] aCoefficients = new double[]{2, 1, 1};
    double[] bCoefficients = new double[]{.5, .5};
    double[] input = new double[]{0, 0, 1, 0, 0};

    double[] expected = new double[]{0, 0, 0.25, 0.125, -0.1875};
    double[] actual = Iir.filter(input, aCoefficients, bCoefficients);

    assertArrayEquals(expected, actual, 10e-8);
  }

  @ParameterizedTest
  @MethodSource("filterInvalidArguments")
  void testFilterArgumentValidation(Class<? extends Throwable> validationException, double[] input,
    double[] aCoefficients, double[] bCoefficients) {
    assertThrows(validationException, () -> Iir.filter(input, aCoefficients, bCoefficients));
  }

  private static Stream<Arguments> filterInvalidArguments() {
    return Stream.of(
      arguments(NullPointerException.class, null, new double[]{1.0}, new double[]{2.0}),
      arguments(NullPointerException.class, new double[]{1.0}, null, new double[]{2.0}),
      arguments(NullPointerException.class, new double[]{1.0}, new double[]{2.0}, null),
      arguments(IllegalArgumentException.class, new double[0], new double[]{1.0}, new double[]{2.0}),
      arguments(IllegalArgumentException.class, new double[]{1.0}, new double[0], new double[]{2.0}),
      arguments(IllegalArgumentException.class, new double[]{1.0}, new double[]{2.0}, new double[0])
    );
  }


}
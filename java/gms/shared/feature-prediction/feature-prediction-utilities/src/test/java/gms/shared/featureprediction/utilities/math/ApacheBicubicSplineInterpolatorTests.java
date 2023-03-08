package gms.shared.featureprediction.utilities.math;

import gms.shared.event.coi.featureprediction.FeaturePredictionDerivativeType;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ApacheBicubicSplineInterpolatorTests {

  @ParameterizedTest
  @MethodSource("validationTestSource")
  void testValidation(
    double[] xValues,
    double[] yValues,
    double[][] functionValues,
    Class<? extends Throwable> expectedException,
    String expectedMessage
  ) {

    var interpolator = new ApacheBicubicSplineInterpolator();

    var exception = Assertions.assertThrows(
      expectedException, () -> interpolator.interpolate(
        xValues, yValues, functionValues
      )
    );

    Assertions.assertEquals(expectedMessage, exception.getMessage());
  }

  private static Stream<Arguments> validationTestSource() {
    return Stream.of(
      Arguments.arguments(
        null,
        new double[]{1, 2, 3, 4, 5},
        new double[][]{
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5}
        },
        NullPointerException.class,
        "xValues is null"
      ),

      Arguments.arguments(
        new double[]{1, 2, 3, 4, 5},
        null,
        new double[][]{
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5}
        },
        NullPointerException.class,
        "yValues is null"
      ),

      Arguments.arguments(
        new double[]{1, 2, 3, 4, 5},
        new double[]{1, 2, 3, 4, 5},
        null,
        NullPointerException.class,
        "functionValues is null"
      ),

      Arguments.arguments(
        new double[]{1, 2, 3, 4},
        new double[]{1, 2, 3, 4, 5},
        new double[][]{
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5}
        },
        IllegalArgumentException.class,
        "not enough xValues, need at least 5"
      ),

      Arguments.arguments(
        new double[]{1, 2, 3, 4, 5},
        new double[]{1, 2, 3, 4},
        new double[][]{
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5}
        },
        IllegalArgumentException.class,
        "not enough yValues, need at least 5"
      ),

      Arguments.arguments(
        new double[]{1, 2, 3, 4, 5, 6},
        new double[]{1, 2, 3, 4, 5},
        new double[][]{
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5}
        },
        IllegalArgumentException.class,
        "Dimension mismatch: the length of xValues (6) does not match the first dimension of functionValues (5)"
      ),

      Arguments.arguments(
        new double[]{1, 2, 3, 4, 5},
        new double[]{1, 2, 3, 4, 5, 6},
        new double[][]{
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5},
          {1, 2, 3, 4, 5}
        },
        IllegalArgumentException.class,
        "Dimension mismatch: the length of yValues (6) does not match the second dimension of functionValues (5)"
      )
    );
  }

  @Test
  void testMatchesApache() {
    // Regression test, should match exactly what is returned by Apache because it is invoking Apache directly.

    var xValues = new double[]{1, 2, 3, 4, 5};
    var yValues = new double[]{1, 2, 3, 4, 5};
    var functionValues = new double[][]{
      {1, 2, 3, 4, 5},
      {1, 2, 3, 4, 5},
      {1, 2, 3, 4, 5},
      {1, 2, 3, 4, 5},
      {1, 2, 3, 4, 5}
    };

    var expectedValue = new PiecewiseBicubicSplineInterpolator().interpolate(
      xValues, yValues, functionValues
    ).value(3.5, 3.5);

    var actualValue = new ApacheBicubicSplineInterpolator().interpolate(
      xValues, yValues, functionValues
    ).value(3.5, 3.5);

    Assertions.assertEquals(expectedValue, actualValue);
  }

  @Test
  void testTravelTimeDistanceDerivative() {
    final double TOLERANCE = 1E-7;

    var interpolator = new ApacheBicubicSplineInterpolator();

    // The derivative of sin(x) + cos(y), with respect to x, is just cos(x)
    var function = (BivariateFunction) (distance, depth) -> Math.sin(distance) + Math.cos(depth);

    // This is effectively evaluating cos(0.0), which is 1.
    var derivative = interpolator.differentiateTravelTime(
      FeaturePredictionDerivativeType.TRAVEL_TIME_WITH_RESPECT_TO_DISTANCE, function, 0.0, 100.0
    );

    // Check within tolerance.
    Assertions.assertTrue(
      Math.abs(1.0 - derivative) <= TOLERANCE,
      "Not close enough to 1.0: " + derivative
    );
  }
}

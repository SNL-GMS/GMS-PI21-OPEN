package gms.shared.featureprediction.utilities.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.lang.Math.abs;

class RationalFunctionInterpolatorTests {

  private final double TOLERANCE = 1.0e-99;

  @Test
  void testLinearFit() {

    // y = 1 + 5x
    var xValues = new double[]{1, 2, 3, 4, 5};
    var yValues = new double[]{6, 11, 16, 21, 26};

    double x = 2.5;
    double expected = 1.0 + 5.0 * x;
    double result = RationalFunctionInterpolator.interpolate(xValues, yValues, x);
    Assertions.assertTrue(abs(expected - result) < TOLERANCE);
  }

  @Test
  void testQuadraticFit() {

    var xValues = new double[]{1, 2, 3, 4, 5};
    var yValues = new double[]{1, 4, 9, 16, 25};

    double x = 4.1;
    double expected = x * x;
    double actual = RationalFunctionInterpolator.interpolate(xValues, yValues, x);
    Assertions.assertTrue(abs(expected - actual) < TOLERANCE);
  }

}

package gms.shared.featureprediction.utilities.math;

import gms.shared.event.coi.featureprediction.FeaturePredictionDerivativeType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.analysis.BivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.analysis.differentiation.FiniteDifferencesDifferentiator;
import org.apache.commons.math3.analysis.differentiation.UnivariateFunctionDifferentiator;
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator;

/**
 * Utility class for performing Bicubic spline interpolation, as well as related (loosely or otherwise) operations
 * such as partial derivatives.
 */
public class ApacheBicubicSplineInterpolator {

  // This based on the limitation in PiecewiseBicubicSplineInterpolator
  public static final int MIN_GRID_SIZE = 5;

  public static final int NUMBER_OF_FINITE_DIFFERENCE_POINTS = 2;
  public static final double FINITE_DIFFERENCE_STEP_SIZE = 1E-9;

  private final UnivariateFunctionDifferentiator univariateFunctionDifferentiator;

  public ApacheBicubicSplineInterpolator() {
    // Do not need to initialize anything yet

    univariateFunctionDifferentiator = new FiniteDifferencesDifferentiator(
      NUMBER_OF_FINITE_DIFFERENCE_POINTS,
      FINITE_DIFFERENCE_STEP_SIZE
    );
  }

  /**
   * Interpolate on a grid of values.
   *
   * @param xValues values along the x-axis
   * @param yValues values along the y-axis
   * @param functionValues table of function values at lattice points.
   * @return a BivariateFunction that, given (x, y), interpolates on the table and returns an interpolated value.
   */
  BivariateFunction interpolate(
    double[] xValues,
    double[] yValues,
    double[][] functionValues
  ) {

    validateInput(
      xValues,
      yValues,
      functionValues
    );

    var interpolator = new PiecewiseBicubicSplineInterpolator();

    return interpolator.interpolate(
      xValues,
      yValues,
      functionValues
    );
  }

  /**
   * Differentiate a BivariateFunction that represents travel time at a distance and depth.
   *
   * @param derivativeType What kind of derivative to calculate
   * @param function The function to differentiate
   * @param distance Evaluate the derivative at this distance
   * @param depth Evaluate the derivative at this depth
   * @return The derivative at the given distance and depth
   */
  public double differentiateTravelTime(
    FeaturePredictionDerivativeType derivativeType, BivariateFunction function, double distance, double depth) {

    if (derivativeType == FeaturePredictionDerivativeType.TRAVEL_TIME_WITH_RESPECT_TO_DISTANCE) {
      return travelTimeWrtDistanceDerivative(function, distance, depth);
    } else {
      throw new NotImplementedException("Derivative for " + derivativeType + " not yet implemented");
    }
  }

  /**
   * Evaluate travel time derivative with respect to distance.
   */
  private double travelTimeWrtDistanceDerivative(
    BivariateFunction function, double distance, double depth
  ) {

    UnivariateFunction fixedDepthFunction = d -> function.value(d, depth);

    var differentiatedFunction = univariateFunctionDifferentiator.differentiate(fixedDepthFunction);

    return differentiatedFunction.value(
      new DerivativeStructure(1, 1, 0, distance)
    ).getPartialDerivative(1);
  }

  private static void validateInput(
    double[] xValues,
    double[] yValues,
    double[][] functionValues
  ) {

    Validate.notNull(xValues, "xValues is null");
    Validate.isTrue(xValues.length >= MIN_GRID_SIZE, "not enough xValues, need at least " + MIN_GRID_SIZE);

    Validate.notNull(yValues, "yValues is null");
    Validate.isTrue(yValues.length >= MIN_GRID_SIZE, "not enough yValues, need at least " + MIN_GRID_SIZE);

    Validate.notNull(functionValues, "functionValues is null");

    Validate.isTrue(
      xValues.length == functionValues.length,
      "Dimension mismatch: the length of xValues (%d) does not match the first dimension of functionValues (%d)",
      xValues.length, functionValues.length
    );

    Validate.isTrue(
      yValues.length == functionValues[0].length,
      "Dimension mismatch: the length of yValues (%d) does not match the second dimension of functionValues (%d)",
      yValues.length, functionValues[0].length
    );

  }
}

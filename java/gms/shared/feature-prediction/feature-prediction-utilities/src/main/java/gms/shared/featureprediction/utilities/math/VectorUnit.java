package gms.shared.featureprediction.utilities.math;

import static java.lang.Math.acos;
import static java.lang.Math.toDegrees;

public class VectorUnit {

  private VectorUnit() {
    // Static utility class
  }

  /**
   * Return the angular distance in degrees between two unit vectors.
   *
   * @param v0 a 3 component unit vector
   * @param v1 a 3 component unit vector
   * @return angular distance in degrees.
   */
  public static double angleDegrees(double[] v0, double[] v1) {
    double dot = v0[0] * v1[0] + v0[1] * v1[1] + v0[2] * v1[2];
    if (dot >= 1.) {
      return 0.;
    } else if (dot <= -1.) {
      return 180.;
    } else {
      return toDegrees(acos(dot));
    }
  }

}

package gms.shared.featureprediction.utilities.math;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;
import static java.lang.Math.toRadians;

/**
 * <p>
 * An Enumeration of Earth shapes including a spherical earth
 * and a number of different ellipsoids.
 *
 * @version 1.0
 */
public enum EarthShape {
  /**
   * The Earth is assumed to be a sphere of radius 6371 km.
   */
  SPHERE(Double.POSITIVE_INFINITY, 6371.),

  /**
   * The Earth is assumed to be an ellipsoid whose shape is defined by the
   * GRS80 ellipsoid specification.
   */
  GRS80(298.257222101, 6378.137),

  /**
   * A hybrid coordinate system where latitudes are converted between
   * geodetic and geocentric values using the GRS80 ellipsoid, but
   * conversions between depth and radius assume that the Earth has
   * constant radius of 6371 km.
   */
  GRS80_RCONST(298.257222101, 6371.),

  /**
   * The Earth is assumed to be an ellipsoid whose shape is defined by the
   * WGS84 ellipsoid specification.
   */
  WGS84(298.257223563, 6378.137),

  /**
   * A hybrid coordinate system where latitudes are converted between
   * geodetic and geocentric values using the WGS84 ellipsoid, but
   * conversions between depth and radius assume that the Earth has
   * constant radius of 6371 km.
   */
  WGS84_RCONST(298.257223563, 6371.),

  /**
   * The Earth is assumed to be an ellipsoid whose shape is defined by the
   * IERS ellipsoid specification.
   */
  IERS2003(298.25642, 6378.1366),

  /**
   * A hybrid coordinate system where latitudes are converted between
   * geodetic and geocentric values using the IERS ellipsoid, but
   * conversions between depth and radius assume that the Earth has
   * constant radius of 6371 km.
   */
  IERS2003_RCONST(298.25642, 6371.);

  /**
   * True for EarthShapes that assume that the Earth has constant radius
   * for purposes of converting between radius and depth.
   */
  public final boolean constantRadius;

  /**
   * The radius of the earth at the equator.
   */
  public final double equatorialRadius;

  /**
   * flattening equals [ 1 - b/a ] where a is equatorial radius and
   * b is the polar radius
   */
  public final double flattening;

  /**
   * [ 1./flattening ]
   */
  public final double inverseFlattening;

  /**
   * Eccentricity squared.
   * <p>
   * Equals [ flattening * (2. - flattening) ]
   * <p>
   * Also equals [ 1 - sqr(b)/sqr(a) ] where a is the
   * equatorial radius and b is the polar radius.
   */
  public final double eccentricitySqr;

  /**
   * Equals [ 1 - eccentricitySqr ]
   */
  public final double e1;

  /**
   * Equals [ eccentricitySqr / (1 - eccentricitySqr) ]
   */
  public final double e2;

  /**
   * constructor
   *
   * @param inverseFlattening double
   * @param equatorialRadius double
   */
  EarthShape(double inverseFlattening, double equatorialRadius) {
    this.equatorialRadius = equatorialRadius;

    this.inverseFlattening = inverseFlattening;
    if (inverseFlattening == Double.POSITIVE_INFINITY) {
      this.flattening = 0.;
      eccentricitySqr = 0.;
      e1 = 1.;
      e2 = 0.;
      this.constantRadius = true;
    } else {
      this.flattening = 1. / inverseFlattening;
      eccentricitySqr = this.flattening * (2. - this.flattening);
      e1 = 1. - this.eccentricitySqr;
      e2 = eccentricitySqr / (1. - eccentricitySqr);
      this.constantRadius = this.equatorialRadius < 6372.;
    }
  }

  /**
   * Get a unit vector corresponding to a point on the Earth
   * with the specified latitude and longitude.
   *
   * @param lat the geographic latitude, in degrees.
   * @param lon the geographic longitude, in degrees.
   * @return The returned unit vector.
   */
  public double[] getVectorDegrees(double lat, double lon) {
    var v = new double[3];
    getVector(toRadians(lat), toRadians(lon), v);
    return v;
  }

  /**
   * Get a unit vector corresponding to a point on the Earth
   * with the specified latitude and longitude.
   *
   * @param lat the geographic latitude, in radians.
   * @param lon the geographic longitude, in radians.
   * @param v the unit vector into which results will be copied.
   */
  public void getVector(double lat, double lon, double[] v) {
    lat = getGeocentricLat(lat);
    v[2] = sin(lat);
    lat = cos(lat);
    v[0] = lat * cos(lon);
    v[1] = lat * sin(lon);
  }

  /**
   * Convert geographicLat in radians to geocentricLat in radians.
   *
   * @param geographicLat
   * @return geocentricLat in radians.
   */
  public double getGeocentricLat(double geographicLat) {

    if (e1 == 1.) return geographicLat;

    return atan(tan(geographicLat) * e1);
  }

} // end of definition of enum EarthShape

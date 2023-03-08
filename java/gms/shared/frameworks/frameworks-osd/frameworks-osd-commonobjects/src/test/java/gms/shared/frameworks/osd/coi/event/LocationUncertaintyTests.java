package gms.shared.frameworks.osd.coi.event;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationUncertaintyTests {

  @Test
  void testFrom() {
    final LocationUncertainty locationUncertainty = LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet);
    final double TOLERANCE = 0.0000000001;
    assertEquals(EventTestFixtures.XX, locationUncertainty.getXx(), TOLERANCE);
    assertEquals(EventTestFixtures.XY, locationUncertainty.getXy(), TOLERANCE);
    assertEquals(EventTestFixtures.XZ, locationUncertainty.getXz(), TOLERANCE);
    assertEquals(EventTestFixtures.XT, locationUncertainty.getXt(), TOLERANCE);
    assertEquals(EventTestFixtures.YY, locationUncertainty.getYy(), TOLERANCE);
    assertEquals(EventTestFixtures.YZ, locationUncertainty.getYz(), TOLERANCE);
    assertEquals(EventTestFixtures.YT, locationUncertainty.getYt(), TOLERANCE);
    assertEquals(EventTestFixtures.ZZ, locationUncertainty.getZz(), TOLERANCE);
    assertEquals(EventTestFixtures.TT, locationUncertainty.getTt(), TOLERANCE);
    assertEquals(EventTestFixtures.ST_DEV_ONE_OBSERVATION,
      locationUncertainty.getStDevOneObservation(),
      TOLERANCE);
    assertEquals(EventTestFixtures.ellipseSet, locationUncertainty.getEllipses());
    assertEquals(EventTestFixtures.ellipsoidSet, locationUncertainty.getEllipsoids());
  }

  @Test
  void testEllipsesImmutable() {
    Set<Ellipse> ellipseSet = EventTestFixtures.LOCATION_UNCERTAINTY.getEllipses();
    assertThrows(UnsupportedOperationException.class,
      () -> ellipseSet.add(EventTestFixtures.ellipse));
  }

  @Test
  void testEllipsoidsImmutable() {
    Set<Ellipsoid> ellipsoidSet = EventTestFixtures.LOCATION_UNCERTAINTY.getEllipsoids();
    assertThrows(UnsupportedOperationException.class,
      () -> ellipsoidSet.add(EventTestFixtures.ellipsoid));
  }

  @Test
  void testNaNxx() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(Double.NaN, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNxy() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, Double.NaN, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNxz() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, Double.NaN, EventTestFixtures.XT,
        EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNxt() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, Double.NaN,
        EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNyy() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, Double.NaN,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNyz() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY,
        Double.NaN, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNyt() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY, EventTestFixtures.YZ, Double.NaN,
        EventTestFixtures.ZZ, EventTestFixtures.ZT, EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNzz() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, Double.NaN, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNzt() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, Double.NaN,
        EventTestFixtures.TT,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNtt() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        Double.NaN,
        EventTestFixtures.ST_DEV_ONE_OBSERVATION, EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testNaNstDevOneObservation() {
    assertThrows(IllegalArgumentException.class, () -> LocationUncertainty
      .from(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ,
        EventTestFixtures.XT, EventTestFixtures.YY,
        EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT,
        Double.NaN, EventTestFixtures.ellipseSet, EventTestFixtures.ellipsoidSet));
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities
      .testSerialization(EventTestFixtures.LOCATION_UNCERTAINTY, LocationUncertainty.class);
  }

  @Test
  void testGetCovarianceMatrix() {

    // To ensure all 10 values are unique.
    final Set<Double> valueSet = new HashSet<>();

    // What each element represents with the indices underneath
    // xx, xy, xz, xt, yy, yz, yt, zz, zt, tt
    //  0,  1,  2,  3,  4,  5,  6,  7,  8,  9
    final double[] values = new double[10];

    final SecureRandom random = new SecureRandom("8475894L".getBytes());
    for (int i = 0; i < values.length; i++) {
      Double d = null;
      do {
        d = random.nextDouble() * 5.0;
      } while (valueSet.contains(d));
      values[i] = d;
    }

    final LocationUncertainty locationUncertainty = LocationUncertainty
      .from(values[0], values[1], values[2], values[3], values[4],
        values[5], values[6], values[7], values[8], values[9],
        // Rest doesn't matter for this test.
        EventTestFixtures.ST_DEV_ONE_OBSERVATION,
        EventTestFixtures.ellipseSet,
        EventTestFixtures.ellipsoidSet);

    final List<List<Double>> covMatrix = locationUncertainty.getCovarianceMatrix();

    // Test the diagonal is what it's supposed to be.
    final double[] expectedDiagonal = new double[]{values[0], values[4], values[7], values[9]};
    for (int i = 0; i < 4; i++) {
      assertEquals(expectedDiagonal[i], covMatrix.get(i).get(i).doubleValue());
    }

    // Test that the values are symmetric about the diagonal, ie, Vij = Vji
    for (int i = 0; i < 4; i++) {
      for (int j = i + 1; j < 4; j++) {
        Double ij = covMatrix.get(i).get(j);
        Double ji = covMatrix.get(j).get(i);
        assertEquals(ij, ji);
      }
    }
  }

}

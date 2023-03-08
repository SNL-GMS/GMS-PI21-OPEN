package gms.shared.event.coi;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationUncertaintyTests {

  @ParameterizedTest
  @MethodSource("nanTestSource")
  void testNaNValidation(double xx, double xy, double xz, double xt, double yy, double yz, double yt, double zz,
    double zt, double tt, double stDevOneObservation) {
    var locationUncertaintyBuilder = LocationUncertainty.builder()
      .setXx(xx)
      .setXy(xy)
      .setXz(xz)
      .setXt(xt)
      .setYy(yy)
      .setYz(yz)
      .setYt(yt)
      .setZz(zz)
      .setZt(zt)
      .setTt(tt)
      .setStDevOneObservation(stDevOneObservation)
      .setEllipses(Collections.emptySet())
      .setEllipsoids(Collections.emptySet());

    assertThrows(IllegalStateException.class, locationUncertaintyBuilder::build);
  }

  private static Stream<Arguments> nanTestSource() {
    return Stream.of(
      Arguments.arguments(Double.NaN, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, Double.NaN, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, Double.NaN, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, Double.NaN,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        Double.NaN, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, Double.NaN, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, Double.NaN, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, Double.NaN, EventTestFixtures.ZT,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, Double.NaN,
        EventTestFixtures.TT, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        Double.NaN, EventTestFixtures.ST_DEV_ONE_OBSERVATION),
      Arguments.arguments(EventTestFixtures.XX, EventTestFixtures.XY, EventTestFixtures.XZ, EventTestFixtures.XT,
        EventTestFixtures.YY, EventTestFixtures.YZ, EventTestFixtures.YT, EventTestFixtures.ZZ, EventTestFixtures.ZT,
        EventTestFixtures.TT, Double.NaN)
    );
  }

  @Test
  void testSerialization() {
    TestUtilities.assertSerializes(EventTestFixtures.LOCATION_UNCERTAINTY, LocationUncertainty.class);
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
      double d;
      do {
        d = random.nextDouble() * 5.0;
      } while (valueSet.contains(d));
      values[i] = d;
    }

    final LocationUncertainty locationUncertainty =
      EventTestFixtures.LOCATION_UNCERTAINTY.toBuilder()
        .setXx(values[0])
        .setXy(values[1])
        .setXz(values[2])
        .setXt(values[3])
        .setYy(values[4])
        .setYz(values[5])
        .setYt(values[6])
        .setZz(values[7])
        .setZt(values[8])
        .setTt(values[9])
        .build();

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

  @Test
  void testDuplicateEllipseConstraintsException() {

    var ellipseOne = EventTestFixtures.ELLIPSE;
    var ellipseTwo = EventTestFixtures.ELLIPSE.toBuilder()
      .setSemiMajorAxisLengthKm(1.0)
      .build();


    var locationUncertaintyBuilder = EventTestFixtures.LOCATION_UNCERTAINTY.toBuilder()
      .setEllipses(Set.of(ellipseOne, ellipseTwo))
      .setEllipsoids(Set.of());

    assertThrows(IllegalStateException.class, locationUncertaintyBuilder::build,
      "Expected IllegalArgumentException when unique Ellipse constraints are not met");
  }

  @Test
  void testDuplicateEllipsoidsConstraintsException() {

    var ellipsoidOne = EventTestFixtures.ELLIPSOID;
    var ellipsoidTwo = EventTestFixtures.ELLIPSOID
      .toBuilder()
      .setSemiMajorAxisLengthKm(1.0)
      .build();

    var locationUncertaintyBuilder = EventTestFixtures.LOCATION_UNCERTAINTY.toBuilder()
      .setEllipses(Set.of())
      .setEllipsoids(Set.of(ellipsoidOne, ellipsoidTwo));

    assertThrows(IllegalStateException.class, locationUncertaintyBuilder::build,
      "Expected IllegalArgumentException when unique Ellipsoids constraints are not met");
  }

  @Test
  void testAddEllipse() {

    var locationUncertaintyBuilder = EventTestFixtures.LOCATION_UNCERTAINTY.toBuilder()
      .setEllipses(Set.of())
      .build();

    assertEquals(Set.of(), locationUncertaintyBuilder.getEllipses(), "Expected empty Ellipse set");
    var updatedLocationUncertaintyBuilder = locationUncertaintyBuilder.toBuilder()
      .addEllipse(EventTestFixtures.ELLIPSE)
      .build();
    assertEquals(Set.of(EventTestFixtures.ELLIPSE), updatedLocationUncertaintyBuilder.getEllipses(),
      "Expected Ellipse set with one element");

  }

  @Test
  void testAddEllipsoid() {

    var locationUncertaintyBuilder = EventTestFixtures.LOCATION_UNCERTAINTY.toBuilder()
      .setEllipsoids(Set.of())
      .build();

    assertEquals(Set.of(), locationUncertaintyBuilder.getEllipsoids(), "Expected empty Ellipse set");
    var updatedLocationUncertaintyBuilder = locationUncertaintyBuilder.toBuilder()
      .addEllipsoid(EventTestFixtures.ELLIPSOID)
      .build();
    assertEquals(Set.of(EventTestFixtures.ELLIPSOID), updatedLocationUncertaintyBuilder.getEllipsoids(),
      "Expected Ellipse set with one element");


  }
}
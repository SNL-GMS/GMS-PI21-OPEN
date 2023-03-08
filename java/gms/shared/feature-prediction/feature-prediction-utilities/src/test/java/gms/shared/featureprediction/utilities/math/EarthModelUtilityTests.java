package gms.shared.featureprediction.utilities.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EarthModelUtilityTests {

  @Test
  void testGridPointsNoHoles() {
    double[] distances = TestFixtures.distances;
    double[] depths = TestFixtures.depths;
    double[][] values = TestFixtures.tableNoHoles;

    var utility = new EarthModelUtility(
      TestFixtures.depths,
      TestFixtures.distances,
      TestFixtures.tableNoHoles,
      false
    );

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < depths.length; j++) {
        assertEquals(values[j][i],
          utility.interpolateEarthModel(depths[j], distances[i])[0], 0.0);
        wasExtrapolated = wasExtrapolated || utility.wasExtrapolated();
      }
    }

    assertFalse(wasExtrapolated);
  }

  @Test
  void testGridPointsNoHolesWithExtrapolationSet() {
    double[] distances = TestFixtures.distances;
    double[] depths = TestFixtures.depths;
    double[][] values = TestFixtures.tableNoHoles;

    var utility = new EarthModelUtility(
      TestFixtures.depths,
      TestFixtures.distances,
      TestFixtures.tableNoHoles,
      true
    );

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < depths.length; j++) {
        assertEquals(values[j][i],
          utility.interpolateEarthModel(depths[j], distances[i])[0], 0.0);
        wasExtrapolated = wasExtrapolated || utility.wasExtrapolated();
      }
    }

    assertFalse(wasExtrapolated);
  }

  @Test
  void testGridPointsHoles() {
    double[] distances = TestFixtures.distances;
    double[] depths = TestFixtures.depths;
    double[][] expectedValues = TestFixtures.tableNoHoles;

    var utility = new EarthModelUtility(
      TestFixtures.depths,
      TestFixtures.distances,
      TestFixtures.tableWithHoles,
      true
    );

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < depths.length; j++) {
        assertEquals(expectedValues[j][i],
          utility.interpolateEarthModel(depths[j], distances[i])[0], 10e-4);
        wasExtrapolated = wasExtrapolated || utility.wasExtrapolated();
      }
    }

    assertTrue(wasExtrapolated);
  }

  @Test
  void testOutsideDepthBounds() {
    double[] distances = TestFixtures.distances;
    double[] values = TestFixtures.depthExtrapolatedTravelTimes;

    var utility = new EarthModelUtility(
      TestFixtures.depths,
      TestFixtures.distances,
      TestFixtures.tableNoHoles,
      true
    );

    boolean wasExtrapolated = false;

    for (int i = 0; i < distances.length; i++) {
      assertEquals(values[i],
        utility.interpolateEarthModel(TestFixtures.extrapolatedDepth, distances[i])[0],
        0.1);
      wasExtrapolated = wasExtrapolated || utility.wasExtrapolated();
    }

    assertTrue(wasExtrapolated);
  }

  @Test
  void testOutsideDistanceBounds() {
    double[] depths = TestFixtures.depths;
    double[] values = TestFixtures.distanceExtrapolatedTravelTimes;

    var utility = new EarthModelUtility(
      TestFixtures.depths,
      TestFixtures.distances,
      TestFixtures.tableNoHoles,
      true
    );

    boolean wasExtrapolated = false;

    for (int i = 0; i < depths.length; i++) {
      assertEquals(values[i],
        utility.interpolateEarthModel(depths[i], TestFixtures.extrapolatedDistance)[0], 0.1);
      wasExtrapolated = wasExtrapolated || utility.wasExtrapolated();
    }

    assertTrue(wasExtrapolated);
  }


  @Test
  void testSomeValuesNoHoles() {
    var utility = new EarthModelUtility(
      TestFixtures.depths,
      TestFixtures.distances,
      TestFixtures.tableNoHoles,
      true
    );

    //TODO: may want to find more datapoints here
    double value = utility.interpolateEarthModel(70, 40.84073276581503)[0];
    assertEquals(454.769981628745, value, 10e-9);
  }

}

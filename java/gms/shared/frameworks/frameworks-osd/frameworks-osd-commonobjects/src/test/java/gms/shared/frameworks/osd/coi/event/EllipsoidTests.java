package gms.shared.frameworks.osd.coi.event;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EllipsoidTests {

  @Test
  void testFrom() {
    //kWeight = 0.0 is an acceptable value here
    //kWeight doesn't matter when scalingFactorType is set to CONFIDENCE
    //confidenceLevel does
    final Ellipsoid ellipsoid = Ellipsoid.from(
      EventTestFixtures.scalingFactorType, EventTestFixtures.K_WEIGHT,
      EventTestFixtures.CONFIDENCE_LEVEL,
      EventTestFixtures.MAJOR_AXIS_LENGTH, EventTestFixtures.MAJOR_AXIS_TREND,
      EventTestFixtures.MAJOR_AXIS_PLUNGE, EventTestFixtures.INTERMEDIATE_AXIS_LENGTH,
      EventTestFixtures.INTERMEDIATE_AXIS_TREND, EventTestFixtures.INTERMEDIATE_AXIS_PLUNGE,
      EventTestFixtures.MINOR_AXIS_LENGTH, EventTestFixtures.MINOR_AXIS_TREND,
      EventTestFixtures.MINOR_AXIS_PLUNGE, EventTestFixtures.timeUncertainty);
    assertNotNull(ellipsoid);
    assertEquals(EventTestFixtures.scalingFactorType, ellipsoid.getScalingFactorType());
    assertEquals(EventTestFixtures.timeUncertainty, ellipsoid.getTimeUncertainty());
    final double tolerance = 0.0000000001;
    assertEquals(EventTestFixtures.K_WEIGHT, ellipsoid.getkWeight(), tolerance);
    assertEquals(EventTestFixtures.CONFIDENCE_LEVEL, ellipsoid.getConfidenceLevel(), tolerance);
    assertEquals(EventTestFixtures.MAJOR_AXIS_LENGTH, ellipsoid.getMajorAxisLength(), tolerance);
    assertEquals(EventTestFixtures.MAJOR_AXIS_TREND, ellipsoid.getMajorAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.MAJOR_AXIS_PLUNGE, ellipsoid.getMajorAxisPlunge(), tolerance);
    assertEquals(EventTestFixtures.INTERMEDIATE_AXIS_LENGTH, ellipsoid.getIntermediateAxisLength(),
      tolerance);
    assertEquals(EventTestFixtures.INTERMEDIATE_AXIS_TREND, ellipsoid.getIntermediateAxisTrend(),
      tolerance);
    assertEquals(EventTestFixtures.INTERMEDIATE_AXIS_PLUNGE, ellipsoid.getIntermediateAxisPlunge(),
      tolerance);
    assertEquals(EventTestFixtures.MINOR_AXIS_LENGTH, ellipsoid.getMinorAxisLength(), tolerance);
    assertEquals(EventTestFixtures.MINOR_AXIS_TREND, ellipsoid.getMinorAxisTrend(), tolerance);
    assertEquals(EventTestFixtures.MINOR_AXIS_PLUNGE, ellipsoid.getMinorAxisPlunge(), tolerance);
  }

  @Test
  void testBadConfidenceLevelWithConfidenceScalingFactor() {
    assertThrows(IllegalArgumentException.class,
      () -> Ellipsoid.from(ScalingFactorType.CONFIDENCE, EventTestFixtures.K_WEIGHT,
        0.0,
        EventTestFixtures.MAJOR_AXIS_LENGTH, EventTestFixtures.MAJOR_AXIS_TREND,
        EventTestFixtures.MAJOR_AXIS_PLUNGE, EventTestFixtures.INTERMEDIATE_AXIS_LENGTH,
        EventTestFixtures.INTERMEDIATE_AXIS_TREND, EventTestFixtures.INTERMEDIATE_AXIS_PLUNGE,
        EventTestFixtures.MINOR_AXIS_LENGTH, EventTestFixtures.MINOR_AXIS_TREND,
        EventTestFixtures.MINOR_AXIS_PLUNGE, EventTestFixtures.timeUncertainty));
  }

  @Test
  void testNonInfiniteKWeightWithCoverageScalingFactor() {
    assertThrows(IllegalArgumentException.class,
      () -> Ellipsoid.from(ScalingFactorType.COVERAGE, 1.0,
        EventTestFixtures.CONFIDENCE_LEVEL,
        EventTestFixtures.MAJOR_AXIS_LENGTH, EventTestFixtures.MAJOR_AXIS_TREND,
        EventTestFixtures.MAJOR_AXIS_PLUNGE, EventTestFixtures.INTERMEDIATE_AXIS_LENGTH,
        EventTestFixtures.INTERMEDIATE_AXIS_TREND, EventTestFixtures.INTERMEDIATE_AXIS_PLUNGE,
        EventTestFixtures.MINOR_AXIS_LENGTH, EventTestFixtures.MINOR_AXIS_TREND,
        EventTestFixtures.MINOR_AXIS_PLUNGE, EventTestFixtures.timeUncertainty));
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.ellipsoid, Ellipsoid.class);
  }
}

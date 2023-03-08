package gms.shared.frameworks.osd.coi.event;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationBehaviorTests {

  @Test
  void testFrom() {
    LocationBehavior loc = LocationBehavior.from(EventTestFixtures.RESIDUAL, EventTestFixtures.WEIGHT,
      EventTestFixtures.IS_DEFINING, EventTestFixtures.FEATURE_PREDICTION, EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT);

    assertEquals(EventTestFixtures.RESIDUAL, loc.getResidual(), 0.001);
    assertEquals(EventTestFixtures.WEIGHT, loc.getWeight(), 0.001);
    assertEquals(EventTestFixtures.IS_DEFINING, loc.isDefining());
    assertEquals(EventTestFixtures.FEATURE_PREDICTION, loc.getFeaturePrediction());
    assertEquals(EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT, EventTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT);
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.LOCATION_BEHAVIOR, LocationBehavior.class);
  }
}

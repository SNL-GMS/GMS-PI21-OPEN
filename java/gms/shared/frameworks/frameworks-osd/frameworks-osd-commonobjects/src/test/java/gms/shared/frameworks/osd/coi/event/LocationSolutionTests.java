package gms.shared.frameworks.osd.coi.event;


import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationSolutionTests {

  @Test
  void testWithLocationAndRestraintOnly() {
    final LocationSolution loc = LocationSolution.withLocationAndRestraintOnly(
      EventTestFixtures.EVENT_LOCATION, EventTestFixtures.LOCATION_RESTRAINT);
    assertNotNull(loc.getId());
    assertEquals(EventTestFixtures.EVENT_LOCATION, loc.getLocation());
    assertEquals(EventTestFixtures.LOCATION_RESTRAINT, loc.getLocationRestraint());
    assertFalse(loc.getLocationUncertainty().isPresent());
    assertTrue(loc.getFeaturePredictions().isEmpty());
    assertTrue(loc.getLocationBehaviors().isEmpty());
  }

  @Test
  void testFrom() {
    final UUID id = UUID.randomUUID();
    final LocationSolution loc = LocationSolution.builder()
      .setId(id)
      .setLocation(EventTestFixtures.EVENT_LOCATION)
      .setLocationRestraint(EventTestFixtures.LOCATION_RESTRAINT)
      .setLocationUncertainty(Optional.of(EventTestFixtures.LOCATION_UNCERTAINTY))
      .setLocationBehaviors(EventTestFixtures.LOCATION_BEHAVIORS)
      .setFeaturePredictions(EventTestFixtures.FEATURE_PREDICTIONS)
      .setNetworkMagnitudeSolutions(EventTestFixtures.NETWORK_MAGNITUDE_SOLUTIONS)
      .build();
    assertEquals(id, loc.getId());
    assertEquals(EventTestFixtures.EVENT_LOCATION, loc.getLocation());
    assertEquals(EventTestFixtures.LOCATION_RESTRAINT, loc.getLocationRestraint());
    assertTrue(loc.getLocationUncertainty().isPresent());
    assertEquals(EventTestFixtures.ARRAY_LEN, loc.getFeaturePredictions().size());
    assertEquals(EventTestFixtures.ARRAY_LEN, loc.getLocationBehaviors().size());
    assertEquals(EventTestFixtures.LOCATION_UNCERTAINTY, loc.getLocationUncertainty().get());
    assertTrue(loc.getFeaturePredictions().contains(EventTestFixtures.FEATURE_PREDICTION));
    assertTrue(loc.getLocationBehaviors().contains(EventTestFixtures.LOCATION_BEHAVIOR));
    assertTrue(
      loc.getNetworkMagnitudeSolutions().contains(EventTestFixtures.NETWORK_MAGNITUDE_SOLUTION));
  }

  @Test
  void testCreate() {
    LocationSolution loc = LocationSolution.builder()
      .generateId()
      .setLocation(EventTestFixtures.EVENT_LOCATION)
      .setLocationRestraint(EventTestFixtures.LOCATION_RESTRAINT)
      .setLocationUncertainty(Optional.of(EventTestFixtures.LOCATION_UNCERTAINTY))
      .setLocationBehaviors(EventTestFixtures.LOCATION_BEHAVIORS)
      .setFeaturePredictions(EventTestFixtures.FEATURE_PREDICTIONS)
      .setNetworkMagnitudeSolutions(EventTestFixtures.NETWORK_MAGNITUDE_SOLUTIONS)
      .build();
    assertEquals(EventTestFixtures.EVENT_LOCATION, loc.getLocation());
    assertEquals(EventTestFixtures.LOCATION_RESTRAINT, loc.getLocationRestraint());
    assertTrue(loc.getLocationUncertainty().isPresent());
    assertEquals(EventTestFixtures.ARRAY_LEN, loc.getFeaturePredictions().size());
    assertEquals(EventTestFixtures.ARRAY_LEN, loc.getLocationBehaviors().size());
    assertEquals(EventTestFixtures.LOCATION_UNCERTAINTY, loc.getLocationUncertainty().get());
    assertTrue(loc.getFeaturePredictions().contains(EventTestFixtures.FEATURE_PREDICTION));
    assertTrue(loc.getLocationBehaviors().contains(EventTestFixtures.LOCATION_BEHAVIOR));
    assertTrue(
      loc.getNetworkMagnitudeSolutions().contains(EventTestFixtures.NETWORK_MAGNITUDE_SOLUTION));
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(EventTestFixtures.LOCATION_SOLUTION, LocationSolution.class);
  }

}

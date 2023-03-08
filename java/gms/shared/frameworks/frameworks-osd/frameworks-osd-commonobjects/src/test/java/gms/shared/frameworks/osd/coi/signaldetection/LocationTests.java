package gms.shared.frameworks.osd.coi.signaldetection;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LocationTests {
  private final double latitude = -1.2;
  private final double longitude = 3.4;
  private final double elevation = 5678.9;
  private final double depth = 123.4;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(SignalDetectionTestFixtures.LOCATION,
      Location.class);
  }

  @Test
  void testFrom() {
    Location location = Location.from(latitude, longitude, depth, elevation);
    assertEquals(latitude, location.getLatitudeDegrees());
    assertEquals(longitude, location.getLongitudeDegrees());
    assertEquals(elevation, location.getElevationKm());
    assertEquals(depth, location.getDepthKm());
  }
}

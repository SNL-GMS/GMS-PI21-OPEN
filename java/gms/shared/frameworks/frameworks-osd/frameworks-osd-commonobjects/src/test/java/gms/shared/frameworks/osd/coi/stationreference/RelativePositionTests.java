package gms.shared.frameworks.osd.coi.stationreference;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RelativePositionTests {

  private final double northDisplacementKm = 12.34;
  private final double eastDisplacementKm = 56.78;
  private final double verticalDisplacementKm = 91.23;
  private final double precision = 1E-5;

  @Test
  void testFrom() {
    RelativePosition relativePosition = RelativePosition
      .from(northDisplacementKm, eastDisplacementKm,
        verticalDisplacementKm);

    assertAll("RelativePosition from equality checks",
      () -> assertEquals(northDisplacementKm, relativePosition.getNorthDisplacementKm(),
        precision),
      () -> assertEquals(eastDisplacementKm, relativePosition.getEastDisplacementKm(),
        precision),
      () -> assertEquals(verticalDisplacementKm, relativePosition.getVerticalDisplacementKm(),
        precision));
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.POSITION, RelativePosition.class);
  }
}

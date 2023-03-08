package gms.shared.event.coi.featureprediction;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ElevationCorrectionDefinitionTest {


  @Test
  void testElevationCorrectionDefinition() {
    var elevationCorrectionDefinition = ElevationCorrectionDefinition.from("Iaspei");
    assertEquals("Iaspei", elevationCorrectionDefinition.getMediumVelocityEarthModel());
    assertEquals(FeaturePredictionComponentType.ELEVATION_CORRECTION, elevationCorrectionDefinition.getCorrectionType());
  }

  @Test
  void testSeriaization() {
    var elevationCorrectionDefinition = ElevationCorrectionDefinition.from("Iaspei");
    TestUtilities.assertSerializes(elevationCorrectionDefinition, ElevationCorrectionDefinition.class);
  }
}
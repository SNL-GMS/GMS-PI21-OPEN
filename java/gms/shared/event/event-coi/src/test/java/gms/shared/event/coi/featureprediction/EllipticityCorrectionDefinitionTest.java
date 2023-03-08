package gms.shared.event.coi.featureprediction;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EllipticityCorrectionDefinitionTest {


  @Test
  void testEllipticityCorrectionDefinition() {
    var elliptictyCorrectionDefinition = EllipticityCorrectionDefinition.from(EllipticityCorrectionType.DZIEWONSKI_GILBERT);
    assertEquals(EllipticityCorrectionType.DZIEWONSKI_GILBERT, elliptictyCorrectionDefinition.getEllipticityCorrectionType());
    assertEquals(FeaturePredictionComponentType.ELLIPTICITY_CORRECTION, elliptictyCorrectionDefinition.getCorrectionType());
  }

  @Test
  void testSeriaization() {
    var elliptictyCorrectionDefinition = EllipticityCorrectionDefinition.from(EllipticityCorrectionType.DZIEWONSKI_GILBERT);
    TestUtilities.assertSerializes(elliptictyCorrectionDefinition, EllipticityCorrectionDefinition.class);
  }

}
package gms.shared.signaldetection.coi.types;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

class ArrivalTimeMeasurementTypeTest {

  @Test
  void testSerialization() {
    ArrivalTimeMeasurementType type = FeatureMeasurementTypes.ARRIVAL_TIME;
    TestUtilities.assertSerializes(type, ArrivalTimeMeasurementType.class);
    TestUtilities.assertSerializes(type, FeatureMeasurementType.class);
  }

}
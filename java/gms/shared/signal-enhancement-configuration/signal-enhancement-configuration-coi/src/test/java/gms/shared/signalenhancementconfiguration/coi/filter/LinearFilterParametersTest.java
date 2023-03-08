package gms.shared.signalenhancementconfiguration.coi.filter;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

class LinearFilterParametersTest {

  @Test
  void serializationLinearFilterParameters() {
    TestUtilities.assertSerializes(FilterFixtures.LINEAR_FILTER_PARAMETERS, LinearFilterParameters.class);
  }
}
package gms.shared.signalenhancementconfiguration.coi.filter;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

class CascadeFilterParametersTest {

  @Test
  void serializationCascadeFiltersParameters() {
    TestUtilities.assertSerializes(FilterFixtures.CASCADED_FILTERS_PARAMETERS, CascadeFilterParameters.class);
  }
}

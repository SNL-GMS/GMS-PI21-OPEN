package gms.shared.signalenhancementconfiguration.coi.filter;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class FilterDefinitionTest {

  @Test
  void serializationLinearFilterDescription() {
    TestUtilities.assertSerializes(FilterFixtures.FILTER_DEFINITION_HAM_FIR_BP_0_40_3_50_HZ, FilterDefinition.class);
  }

  @Test
  void serializationCascadeFilterDescription() {
    FilterDefinition filterDefinition = FilterDefinition.from("Filter definition example",
      Optional.of("this is a test comment"), FilterFixtures.CASCADED_FILTERS_1_DESCRIPTION);

    TestUtilities.assertSerializes(filterDefinition, FilterDefinition.class);
  }
}

package gms.shared.signalenhancementconfiguration.coi.filter;

import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.List;

class FilterConfigurationTest {
  @Test
  void serialization() {
    FilterConfiguration filterConfiguration = FilterConfiguration.from(List.of(FilterFixtures.FILTER_DEFINITION_HAM_FIR_BP_0_40_3_50_HZ));

    TestUtilities.assertSerializes(filterConfiguration, FilterConfiguration.class);
  }
}
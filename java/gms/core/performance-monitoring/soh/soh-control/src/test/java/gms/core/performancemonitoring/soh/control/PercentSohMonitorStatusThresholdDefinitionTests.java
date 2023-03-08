package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PercentSohMonitorStatusThresholdDefinitionTests {
  @Test
  void testSerialization() throws IOException {

    TestUtilities.testSerialization(
      PercentSohMonitorStatusThresholdDefinition.create(
        0.0,
        1.1
      ),
      PercentSohMonitorStatusThresholdDefinition.class
    );
  }

}

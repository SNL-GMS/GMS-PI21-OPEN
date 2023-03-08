package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

class DurationSohMonitorStatusThresholdDefinitionTests {

  @Test
  void testSerialization() throws IOException {

    TestUtilities.testSerialization(
      DurationSohMonitorStatusThresholdDefinition.create(
        Duration.ofDays(1),
        Duration.ofMillis(122)
      ),
      DurationSohMonitorStatusThresholdDefinition.class
    );
  }
}


package gms.core.performancemonitoring.soh.control.configuration;

import gms.core.performancemonitoring.soh.control.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

class TimeWindowDefinitionTests {

  @Test
  void testSerialization() throws IOException {

    TestUtilities.testSerialization(
      TimeWindowDefinition.create(
        Duration.ofDays(2),
        Duration.ofHours(2)
      ),
      TimeWindowDefinition.class
    );
  }
}

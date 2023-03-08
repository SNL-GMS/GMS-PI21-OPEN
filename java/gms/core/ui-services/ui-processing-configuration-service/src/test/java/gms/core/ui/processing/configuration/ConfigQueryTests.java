package gms.core.ui.processing.configuration;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class ConfigQueryTests {
  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(TestFixture.query, ConfigQuery.class);
  }
}

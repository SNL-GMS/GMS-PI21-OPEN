package gms.shared.frameworks.osd.coi.provenance;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class InformationSourceTest {

  @Test
  void testParameters() throws Exception {
    TestUtilities.checkStaticMethodValidatesNullArguments(
      InformationSource.class, "from", "abc", Instant.now(), "xyz");
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(ProvenanceTestFixtures.informationSource, InformationSource.class);
  }
}

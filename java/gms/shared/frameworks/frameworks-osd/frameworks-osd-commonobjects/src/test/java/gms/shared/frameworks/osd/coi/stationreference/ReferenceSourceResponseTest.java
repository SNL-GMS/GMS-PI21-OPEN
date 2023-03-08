package gms.shared.frameworks.osd.coi.stationreference;


import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

class ReferenceSourceResponseTest {

  /**
   * Tests that the ReferenceSourceResponse object can be serialized and deserialized with the COI
   * object mapper.
   */
  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(StationReferenceTestFixtures.REFERENCE_SOURCE_RESPONSE, ReferenceSourceResponse.class);
  }

}


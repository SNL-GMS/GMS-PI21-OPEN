package gms.shared.frameworks.osd.api.stationreference.util;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

class ReferenceSiteMembershipTest {
  @Test
  void testSerialization() throws IOException {
    TestUtilities
      .testSerialization(ReferenceSiteMembershipRequest.create(UUID.randomUUID(), "Test Station"),
        ReferenceSiteMembershipRequest.class);
  }
}

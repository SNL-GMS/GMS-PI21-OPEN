package gms.shared.frameworks.osd.api.stationreference.util;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

class NetworkMembershipTest {

  @Test
  void testSerialization() throws IOException {
    TestUtilities
      .testSerialization(NetworkMembershipRequest.from(UUID.randomUUID(), UUID.randomUUID()),
        NetworkMembershipRequest.class);
  }
}

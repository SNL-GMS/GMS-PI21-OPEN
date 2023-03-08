package gms.shared.frameworks.osd.coi.soh;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_LAG_MISSING_CHANNEL_SOH;

class ChannelSohTests {

  @Test
  void testSerialization() throws IOException {

    TestUtilities.testSerialization(
      BAD_LAG_MISSING_CHANNEL_SOH,
      ChannelSoh.class
    );
  }
}

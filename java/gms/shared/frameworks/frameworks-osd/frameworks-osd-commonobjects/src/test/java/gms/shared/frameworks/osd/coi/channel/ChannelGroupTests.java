package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.channel.ChannelGroup.Type;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChannelGroupTests {

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(UtilsTestFixtures.channelGroup, ChannelGroup.class);
  }

  @Test
  void testEmptyNameThrowsException() {
    assertThrows(Exception.class, () -> ChannelGroup
      .from("",
        "",
        Location.from(100.0, 50.0, 50.0, 100.0),
        Type.PROCESSING_GROUP,
        List.of(UtilsTestFixtures.CHANNEL))
    );
  }

  @Test
  void testNullNameThrowsNullPointerException() {
    List<Channel> chanList = List.of(UtilsTestFixtures.CHANNEL);
    Location location = Location.from(100.0, 50.0, 50.0, 100.0);
    NullPointerException exception = assertThrows(NullPointerException.class, () -> ChannelGroup
      .from(null,
        "",
        location,
        Type.PROCESSING_GROUP,
        chanList)
    );
    assertEquals("Channel Group name must not be null", exception.getMessage());
  }

  @Test
  void testEmptyListOfChannelsThrowsException() {
    assertThrows(Exception.class, () -> ChannelGroup
      .from("Test Group",
        "Sample Processing Group",
        Location.from(100.0, 50.0, 50.0, 100.0),
        Type.PROCESSING_GROUP,
        List.of())
    );
  }

  @Test
  void testNullListOfChannelsThrowsException() {
    assertThrows(Exception.class, () -> ChannelGroup
      .from("Test Group",
        "Sample Processing Group",
        Location.from(100.0, 50.0, 50.0, 100.0),
        Type.PROCESSING_GROUP,
        null)
    );
  }

}

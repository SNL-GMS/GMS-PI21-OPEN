package gms.shared.frameworks.osd.coi.channel;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelOrientationTypeTests {

  @Test
  void testUnknownLiteral() {
    assertEquals('-', ChannelOrientationType.UNKNOWN.getCode());
  }

  @Test
  void testNoBlankCodes() {
    assertTrue(Arrays.stream(ChannelOrientationType.values())
      .map(ChannelOrientationType::getCode)
      .noneMatch(Character::isWhitespace));
  }

  @Test
  void testGetCode() {
    for (ChannelOrientationType type : ChannelOrientationType.values()) {
      assertEquals(type, ChannelOrientationType.fromCode(type.getCode()));
    }
  }

  @Test
  void testGetUnknownCode() {
    char unknown = '$';
    assertTrue(Arrays.stream(ChannelOrientationType.values())
      .noneMatch(orientation -> unknown == orientation.getCode()));

    assertEquals(ChannelOrientationType.UNKNOWN, ChannelOrientationType.fromCode(unknown));
  }
}
package gms.shared.stationdefinition.coi.channel;

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
      type.getInstrumentCodes().forEach(i -> assertEquals(type, ChannelOrientationType.fromCode(type.getCode(), i)));
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
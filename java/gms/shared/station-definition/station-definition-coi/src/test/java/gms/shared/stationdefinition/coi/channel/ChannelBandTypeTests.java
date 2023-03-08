package gms.shared.stationdefinition.coi.channel;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelBandTypeTests {

  @Test
  void testUnknownLiteral() {
    assertEquals('-', ChannelBandType.UNKNOWN.getCode());
  }

  @Test
  void testNoBlankCodes() {
    assertTrue(Arrays.stream(ChannelBandType.values())
      .map(ChannelBandType::getCode)
      .noneMatch(Character::isWhitespace));
  }

  @Test
  void testAllCodesUnique() {
    final long numUniqueCodes = Arrays.stream(ChannelBandType.values())
      .map(ChannelBandType::getCode)
      .distinct()
      .count();

    assertEquals(ChannelBandType.values().length, numUniqueCodes);
  }
}
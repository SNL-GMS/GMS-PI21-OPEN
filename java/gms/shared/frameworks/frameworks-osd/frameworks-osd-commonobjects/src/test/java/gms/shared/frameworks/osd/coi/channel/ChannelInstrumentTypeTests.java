package gms.shared.frameworks.osd.coi.channel;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelInstrumentTypeTests {

  @Test
  void testUnknownLiteral() {
    assertEquals('-', ChannelInstrumentType.UNKNOWN.getCode());
  }

  @Test
  void testNoBlankCodes() {
    assertTrue(Arrays.stream(ChannelInstrumentType.values())
      .map(ChannelInstrumentType::getCode)
      .noneMatch(Character::isWhitespace));
  }

  @Test
  void testAllCodesUnique() {
    final long numUniqueCodes = Arrays.stream(ChannelInstrumentType.values())
      .map(ChannelInstrumentType::getCode)
      .distinct()
      .count();

    assertEquals(ChannelInstrumentType.values().length, numUniqueCodes);
  }
}
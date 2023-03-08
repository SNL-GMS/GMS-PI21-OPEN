package gms.shared.signalenhancementconfiguration.coi.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterDefinitionUsageTest {
  private static final String WILD_CARD_ENUM = "WILD_CARD";
  private static final String DETECTION_ENUM = "DETECTION";
  private static final String FK_ENUM = "FK";
  private static final String ONSET_ENUM = "ONSET";
  private static final String WILD_CARD_STR = "*";
  private static final String DETECTION_STR = "Detect";
  private static final String FK_STR = "FK";
  private static final String ONSET_STR = "Onset";

  @Test
  void testFilterDefinitionUsageValues() {
    assertEquals(FilterDefinitionUsage.WILD_CARD, FilterDefinitionUsage.valueOf(WILD_CARD_ENUM));
    assertEquals(FilterDefinitionUsage.DETECTION, FilterDefinitionUsage.valueOf(DETECTION_ENUM));
    assertEquals(FilterDefinitionUsage.FK, FilterDefinitionUsage.valueOf(FK_ENUM));
    assertEquals(FilterDefinitionUsage.ONSET, FilterDefinitionUsage.valueOf(ONSET_ENUM));
  }

  @Test
  void testFilterDefinitionUsageStrings() {
    System.out.println("Detection = " + FilterDefinitionUsage.DETECTION.toString());
    assertEquals(WILD_CARD_STR, FilterDefinitionUsage.WILD_CARD.toString());
    assertEquals(DETECTION_STR, FilterDefinitionUsage.DETECTION.toString());
    assertEquals(FK_STR, FilterDefinitionUsage.FK.toString());
    assertEquals(ONSET_STR, FilterDefinitionUsage.ONSET.toString());
  }

  @Test
  void testGetFilterDefinitionUsage() {
    assertEquals(WILD_CARD_STR, FilterDefinitionUsage.WILD_CARD.getName());
    assertEquals(DETECTION_STR, FilterDefinitionUsage.DETECTION.getName());
    assertEquals(FK_STR, FilterDefinitionUsage.FK.getName());
    assertEquals(ONSET_STR, FilterDefinitionUsage.ONSET.getName());
  }

  @Test
  void testFilterDefinitionUsageFromStrings() {
    assertEquals(FilterDefinitionUsage.WILD_CARD, FilterDefinitionUsage.fromString(WILD_CARD_STR));
    assertEquals(FilterDefinitionUsage.DETECTION, FilterDefinitionUsage.fromString(DETECTION_STR));
    assertEquals(FilterDefinitionUsage.FK, FilterDefinitionUsage.fromString(FK_STR));
    assertEquals(FilterDefinitionUsage.ONSET, FilterDefinitionUsage.fromString(ONSET_STR));
  }

  @Test
  void errorWhenFilterDefinitionUsageMissing() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
      FilterDefinitionUsageTest::executeNonExistingFilterDefinitionUsage);
  }

  private static void executeNonExistingFilterDefinitionUsage() {
    FilterDefinitionUsage.fromString("Non existing filter definition usage");
  }
}

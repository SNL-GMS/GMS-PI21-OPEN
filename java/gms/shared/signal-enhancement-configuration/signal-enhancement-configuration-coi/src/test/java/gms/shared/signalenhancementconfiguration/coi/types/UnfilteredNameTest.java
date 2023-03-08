package gms.shared.signalenhancementconfiguration.coi.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UnfilteredNameTest {
  private static final String UNFILTERED = "Unfiltered";

  @Test
  void testFilterNames() {
    assertEquals(UnfilteredName.UNFILTERED, UnfilteredName.valueOf("UNFILTERED"));
  }

  @Test
  void testFilterNames2() {
    String name = UnfilteredName.UNFILTERED.name();
    assertEquals(UNFILTERED, UnfilteredName.UNFILTERED.toString());
  }

  @Test
  void testGetFilters() {
    assertEquals(UNFILTERED, UnfilteredName.UNFILTERED.getFilterName());
  }

  @Test
  void testFilterNamesFromStrings() {
    assertEquals(UnfilteredName.UNFILTERED, UnfilteredName.fromString(UNFILTERED));
  }

  @Test
  void errorWhenNamedFilteredEntryAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, UnfilteredNameTest::executeNonExistingUnfilteredName);

    Assertions.assertEquals("Unsupported Unfiltered: Non existing unfiltered", thrown.getMessage());
  }

  private static void executeNonExistingUnfilteredName() {
    UnfilteredName.fromString("Non existing unfiltered");
  }
}
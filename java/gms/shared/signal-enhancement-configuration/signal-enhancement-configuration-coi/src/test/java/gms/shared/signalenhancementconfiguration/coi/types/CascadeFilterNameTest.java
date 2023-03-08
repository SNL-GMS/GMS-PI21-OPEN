package gms.shared.signalenhancementconfiguration.coi.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CascadeFilterNameTest {
  private static final String CASCADE_FILTER_1_ENUM = "CASCADE_FILTER_1";
  private static final String CASCADE_FILTER_2_ENUM = "CASCADE_FILTER_2";
  private static final String CASCADE_FILTER_3_ENUM = "CASCADE_FILTER_3";
  private static final String CASCADE_FILTER_1_STR = "Cascade Filter 1";
  private static final String CASCADE_FILTER_2_STR = "Cascade Filter 2";
  private static final String CASCADE_FILTER_3_STR = "Cascade Filter 3";

  @Test
  void testFilterNameValues() {
    assertEquals(CascadeFilterName.CASCADE_FILTER_1, CascadeFilterName.valueOf(CASCADE_FILTER_1_ENUM));
    assertEquals(CascadeFilterName.CASCADE_FILTER_2, CascadeFilterName.valueOf(CASCADE_FILTER_2_ENUM));
    assertEquals(CascadeFilterName.CASCADE_FILTER_3, CascadeFilterName.valueOf(CASCADE_FILTER_3_ENUM));
  }

  @Test
  void testFilterNameStrings() {
    assertEquals(CASCADE_FILTER_1_STR, CascadeFilterName.CASCADE_FILTER_1.toString());
    assertEquals(CASCADE_FILTER_2_STR, CascadeFilterName.CASCADE_FILTER_2.toString());
    assertEquals(CASCADE_FILTER_3_STR, CascadeFilterName.CASCADE_FILTER_3.toString());
  }

  @Test
  void testGetFilterName() {
    assertEquals(CASCADE_FILTER_1_STR, CascadeFilterName.CASCADE_FILTER_1.getFilterName());
    assertEquals(CASCADE_FILTER_2_STR, CascadeFilterName.CASCADE_FILTER_2.getFilterName());
    assertEquals(CASCADE_FILTER_3_STR, CascadeFilterName.CASCADE_FILTER_3.getFilterName());
  }

  @Test
  void testFilterNamesFromStrings() {
    assertEquals(CascadeFilterName.CASCADE_FILTER_1, CascadeFilterName.fromString(CASCADE_FILTER_1_STR));
    assertEquals(CascadeFilterName.CASCADE_FILTER_2, CascadeFilterName.fromString(CASCADE_FILTER_2_STR));
    assertEquals(CascadeFilterName.CASCADE_FILTER_3, CascadeFilterName.fromString(CASCADE_FILTER_3_STR));
  }

  @Test
  void errorWhenNamedFilteredEntryAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
      CascadeFilterNameTest::executeNonExistingCascadeFilterName);

    Assertions.assertEquals("Unsupported Cascade Filter Name: Non existing cascade filter name", thrown.getMessage());
  }

  private static void executeNonExistingCascadeFilterName() {
    CascadeFilterName.fromString("Non existing cascade filter name");
  }

}
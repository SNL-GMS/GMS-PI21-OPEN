package gms.shared.signalenhancementconfiguration.coi.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterListNameTest {
  private static final String SEISMIC_ENUM = "SEISMIC";
  private static final String LONG_PERIOD_ENUM = "LONG_PERIOD";
  private static final String HYDRO_ENUM = "HYDRO";
  private static final String SEISMIC_STR = "Seismic";
  private static final String LONG_PERIOD_STR = "Long Period";
  private static final String HYDRO_STR = "Hydro";

  @Test
  void testFilterNameValues() {
    assertEquals(FilterListName.SEISMIC, FilterListName.valueOf(SEISMIC_ENUM));
    assertEquals(FilterListName.LONG_PERIOD, FilterListName.valueOf(LONG_PERIOD_ENUM));
    assertEquals(FilterListName.HYDRO, FilterListName.valueOf(HYDRO_ENUM));
  }

  @Test
  void testFilterNameStrings() {
    assertEquals(SEISMIC_STR, FilterListName.SEISMIC.toString());
    assertEquals(LONG_PERIOD_STR, FilterListName.LONG_PERIOD.toString());
    assertEquals(HYDRO_STR, FilterListName.HYDRO.toString());
  }

  @Test
  void testGetFilterName() {
    assertEquals(SEISMIC_STR, FilterListName.SEISMIC.getFilterName());
    assertEquals(LONG_PERIOD_STR, FilterListName.LONG_PERIOD.getFilterName());
    assertEquals(HYDRO_STR, FilterListName.HYDRO.getFilterName());
  }

  @Test
  void testFilterNamesFromStrings() {
    assertEquals(FilterListName.SEISMIC, FilterListName.fromString(SEISMIC_STR));
    assertEquals(FilterListName.LONG_PERIOD, FilterListName.fromString(LONG_PERIOD_STR));
    assertEquals(FilterListName.HYDRO, FilterListName.fromString(HYDRO_STR));
  }

  @Test
  void errorWhenNamedFilteredEntryAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, FilterListNameTest::executeNonExistingFilterNameList);

    Assertions.assertEquals("Unsupported Filter List: Non existing filter list", thrown.getMessage());
  }

  private static void executeNonExistingFilterNameList() {
    FilterListName.fromString("Non existing filter list");
  }
}

package gms.shared.signalenhancementconfiguration.coi.filter;

import gms.shared.signalenhancementconfiguration.coi.types.FilterDefinitionUsage;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterListEntryTest {

  private static void executeEmptyUnfiltered() {
    FilterListEntry.from(true, Optional.empty(), Optional.of(FilterDefinitionUsage.DETECTION),
      Optional.of(FilterFixtures.FILTER_DEFINITION_HAM_FIR_BP_0_40_3_50_HZ));
  }

  private static void executeEmptyNamedFilter() {
    FilterListEntry.from(true, Optional.of(true), Optional.empty(),
      Optional.of(FilterFixtures.FILTER_DEFINITION_HAM_FIR_BP_0_40_3_50_HZ));
  }

  private static void executeEmptyFilterDefinition() {
    FilterListEntry.from(true, Optional.of(true), Optional.of(FilterDefinitionUsage.DETECTION),
      Optional.empty());
  }

  private static void executeAllEmpty() {
    FilterListEntry.from(true, Optional.empty(), Optional.empty(),
      Optional.empty());
  }

  @Test
  void serialization() {
    TestUtilities.assertSerializes(FilterFixtures.FILTER_LIST_ENTRY, FilterListEntry.class);
  }

  @Test
  void errorWhenFilterDefinitionAndFilterNamedEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
      FilterListEntryTest::executeEmptyUnfiltered);

    Assertions.assertEquals("Only one filter entry must be populated at all times", thrown.getMessage());
  }

  @Test
  void errorWhenFilterDefinitionAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
      FilterListEntryTest::executeEmptyNamedFilter);

    Assertions.assertEquals("Only one filter entry must be populated at all times", thrown.getMessage());
  }

  @Test
  void errorWhenNamedFilteredEntryAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
      FilterListEntryTest::executeEmptyFilterDefinition);

    Assertions.assertEquals("Only one filter entry must be populated at all times", thrown.getMessage());
  }

  @Test
  void errorAllFilterEntriesAreEmpty() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
      FilterListEntryTest::executeAllEmpty);

    Assertions.assertEquals("All filter entries are empty. You must populate exactly one filter entry",
      thrown.getMessage());
  }

}
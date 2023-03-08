package gms.shared.signalenhancementconfiguration.coi.filter;

import com.google.common.collect.ImmutableList;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterListTest {
  private static void execute() {
    FilterList.from("nando", 2, ImmutableList.copyOf(new ArrayList<>()));
  }

  @Test
  void serialization() {
    FilterList filterList = FilterList.from("nando", 2, ImmutableList.copyOf(FilterFixtures.FILTER_LIST_ENTRY_LIST));

    TestUtilities.assertSerializes(filterList, FilterList.class);
  }

  @Test
  void errorWhenFilterListIsEmpty() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, FilterListTest::execute);

    Assertions.assertEquals("The filter list must contain at list one entry", thrown.getMessage());
  }

}

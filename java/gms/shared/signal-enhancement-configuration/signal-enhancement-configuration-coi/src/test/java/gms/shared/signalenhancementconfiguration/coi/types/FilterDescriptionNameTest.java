package gms.shared.signalenhancementconfiguration.coi.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterDescriptionNameTest {
  private static final String FILTER1_ENUM = "HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION";
  private static final String FILTER2_ENUM = "HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION";
  private static final String FILTER3_ENUM = "HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION";
  private static final String FILTER4_ENUM = "HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION";
  private static final String FILTER5_ENUM = "HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION";
  private static final String FILTER6_ENUM = "HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION";
  private static final String FILTER7_ENUM = "HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION";
  private static final String FILTER8_ENUM = "HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION";
  private static final String FILTER9_ENUM = "HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION";
  private static final String FILTER10_ENUM = "HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION";
  private static final String FILTER11_ENUM = "HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION";
  private static final String FILTER12_ENUM = "HAM_FIR_LP_4_20_HZ_DESCRIPTION";
  private static final String FILTER13_ENUM = "HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION";
  private static final String FILTER14_ENUM = "HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION";
  private static final String FILTER15_ENUM = "HAM_FIR_HP_0_30_HZ_DESCRIPTION";
  private static final String FILTER16_ENUM = "HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION";
  private static final String FILTER1_STR = "HAM FIR BP 0.70-2.00 Hz Description";
  private static final String FILTER2_STR = "HAM FIR BP 1.00-3.00 Hz Description";
  private static final String FILTER3_STR = "HAM FIR BP 4.00-8.00 Hz Description";
  private static final String FILTER4_STR = "HAM FIR BP 0.40-3.50 Hz Description";
  private static final String FILTER5_STR = "HAM FIR BP 0.50-4.00 Hz Description";
  private static final String FILTER6_STR = "HAM FIR BP 2.00-5.00 Hz Description";
  private static final String FILTER7_STR = "HAM FIR BP 1.50-3.00 Hz Description";
  private static final String FILTER8_STR = "HAM FIR BP 1.00-5.00 Hz Description";
  private static final String FILTER9_STR = "HAM FIR BP 0.50-2.50 Hz Description";
  private static final String FILTER10_STR = "HAM FIR BP 1.50-3.50 Hz Description";
  private static final String FILTER11_STR = "HAM FIR BP 1.70-3.20 Hz Description";
  private static final String FILTER12_STR = "HAM FIR LP 4.20 Hz Description";
  private static final String FILTER13_STR = "HAM FIR BP 0.50-1.50 Hz Description";
  private static final String FILTER14_STR = "HAM FIR BP 2.00-4.00 Hz Description";
  private static final String FILTER15_STR = "HAM FIR HP 0.30 Hz Description";
  private static final String FILTER16_STR = "HAM FIR BP 0.50-4.00 HZ non causal Description";

  @Test
  void testFilterNames() {
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER1_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER2_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER3_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER4_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER5_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER6_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER7_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER8_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER9_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER10_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER11_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_LP_4_20_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER12_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER13_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER14_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_HP_0_30_HZ_DESCRIPTION, FilterDescriptionName.valueOf(FILTER15_ENUM));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION, FilterDescriptionName.valueOf(FILTER16_ENUM));
  }

  @Test
  void testFilterNames2() {
    assertEquals(FILTER1_STR, FilterDescriptionName.HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER2_STR, FilterDescriptionName.HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER3_STR, FilterDescriptionName.HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER4_STR, FilterDescriptionName.HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION.toString());
    assertEquals(FILTER5_STR, FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER6_STR, FilterDescriptionName.HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER7_STR, FilterDescriptionName.HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER8_STR, FilterDescriptionName.HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER9_STR, FilterDescriptionName.HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION.toString());
    assertEquals(FILTER10_STR, FilterDescriptionName.HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION.toString());
    assertEquals(FILTER11_STR, FilterDescriptionName.HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION.toString());
    assertEquals(FILTER12_STR, FilterDescriptionName.HAM_FIR_LP_4_20_HZ_DESCRIPTION.toString());
    assertEquals(FILTER13_STR, FilterDescriptionName.HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION.toString());
    assertEquals(FILTER14_STR, FilterDescriptionName.HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION.toString());
    assertEquals(FILTER15_STR, FilterDescriptionName.HAM_FIR_HP_0_30_HZ_DESCRIPTION.toString());
    assertEquals(FILTER16_STR, FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION.toString());
  }

  @Test
  void testGetFilters() {
    assertEquals(FILTER1_STR,FilterDescriptionName.HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER2_STR, FilterDescriptionName.HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER3_STR, FilterDescriptionName.HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER4_STR, FilterDescriptionName.HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER5_STR, FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER6_STR, FilterDescriptionName.HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER7_STR, FilterDescriptionName.HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER8_STR, FilterDescriptionName.HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER9_STR, FilterDescriptionName.HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER10_STR, FilterDescriptionName.HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER11_STR, FilterDescriptionName.HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER12_STR, FilterDescriptionName.HAM_FIR_LP_4_20_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER13_STR, FilterDescriptionName.HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER14_STR, FilterDescriptionName.HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER15_STR, FilterDescriptionName.HAM_FIR_HP_0_30_HZ_DESCRIPTION.getFilterDescription());
    assertEquals(FILTER16_STR, FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION.getFilterDescription());
  }

  @Test
  void testFilterNamesFromStrings() {
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER1_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER2_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER3_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER4_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER5_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER6_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER7_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER8_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER9_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER10_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER11_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_LP_4_20_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER12_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER13_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER14_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_HP_0_30_HZ_DESCRIPTION, FilterDescriptionName.fromString(FILTER15_STR));
    assertEquals(FilterDescriptionName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION, FilterDescriptionName.fromString(FILTER16_STR));
  }

  @Test
  void errorWhenNamedFilteredEntryAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, FilterDescriptionNameTest::executeNonExistingFilterName);

    Assertions.assertEquals("Unsupported Filter Description: Non existing filter description", thrown.getMessage());
  }

  private static void executeNonExistingFilterName() {
    FilterDescriptionName.fromString("Non existing filter description");
  }

}
package gms.shared.signalenhancementconfiguration.coi.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilterNameTest {
  private static final String FILTER1_ENUM = "HAM_FIR_BP_0_70_2_00_HZ";
  private static final String FILTER2_ENUM = "HAM_FIR_BP_1_00_3_00_HZ";
  private static final String FILTER3_ENUM = "HAM_FIR_BP_4_00_8_00_HZ";
  private static final String FILTER4_ENUM = "HAM_FIR_BP_0_40_3_50_HZ";
  private static final String FILTER5_ENUM = "HAM_FIR_BP_0_50_4_00_HZ";
  private static final String FILTER6_ENUM = "HAM_FIR_BP_2_00_5_00_HZ";
  private static final String FILTER7_ENUM = "HAM_FIR_BP_1_50_3_00_HZ";
  private static final String FILTER8_ENUM = "HAM_FIR_BP_1_00_5_00_HZ";
  private static final String FILTER9_ENUM = "HAM_FIR_BP_0_50_2_50_HZ";
  private static final String FILTER10_ENUM = "HAM_FIR_BP_1_50_3_50_HZ";
  private static final String FILTER11_ENUM = "HAM_FIR_BP_1_70_3_20_HZ";
  private static final String FILTER12_ENUM = "HAM_FIR_LP_4_20_HZ";
  private static final String FILTER13_ENUM = "HAM_FIR_BP_0_50_1_50_HZ";
  private static final String FILTER14_ENUM = "HAM_FIR_BP_2_00_4_00_HZ";
  private static final String FILTER15_ENUM = "HAM_FIR_HP_0_30_HZ";
  private static final String FILTER16_ENUM = "HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL";
  private static final String FILTER1_STR = "HAM FIR BP 0.70-2.00 Hz";
  private static final String FILTER2_STR = "HAM FIR BP 1.00-3.00 Hz";
  private static final String FILTER3_STR = "HAM FIR BP 4.00-8.00 Hz";
  private static final String FILTER4_STR = "HAM FIR BP 0.40-3.50 Hz";
  private static final String FILTER5_STR = "HAM FIR BP 0.50-4.00 Hz";
  private static final String FILTER6_STR = "HAM FIR BP 2.00-5.00 Hz";
  private static final String FILTER7_STR = "HAM FIR BP 1.50-3.00 Hz";
  private static final String FILTER8_STR = "HAM FIR BP 1.00-5.00 Hz";
  private static final String FILTER9_STR = "HAM FIR BP 0.50-2.50 Hz";
  private static final String FILTER10_STR = "HAM FIR BP 1.50-3.50 Hz";
  private static final String FILTER11_STR = "HAM FIR BP 1.70-3.20 Hz";
  private static final String FILTER12_STR = "HAM FIR LP 4.20 Hz";
  private static final String FILTER13_STR = "HAM FIR BP 0.50-1.50 Hz";
  private static final String FILTER14_STR = "HAM FIR BP 2.00-4.00 Hz";
  private static final String FILTER15_STR = "HAM FIR HP 0.30 Hz";
  private static final String FILTER16_STR = "HAM FIR BP 0.50-4.00 HZ non causal";

  @Test
  void testFilterNames() {
    assertEquals(FilterName.HAM_FIR_BP_0_70_2_00_HZ, FilterName.valueOf(FILTER1_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_1_00_3_00_HZ, FilterName.valueOf(FILTER2_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_4_00_8_00_HZ, FilterName.valueOf(FILTER3_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_0_40_3_50_HZ, FilterName.valueOf(FILTER4_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_0_50_4_00_HZ, FilterName.valueOf(FILTER5_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_2_00_5_00_HZ, FilterName.valueOf(FILTER6_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_1_50_3_00_HZ, FilterName.valueOf(FILTER7_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_1_00_5_00_HZ, FilterName.valueOf(FILTER8_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_0_50_2_50_HZ, FilterName.valueOf(FILTER9_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_1_50_3_50_HZ, FilterName.valueOf(FILTER10_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_1_70_3_20_HZ, FilterName.valueOf(FILTER11_ENUM));
    assertEquals(FilterName.HAM_FIR_LP_4_20_HZ, FilterName.valueOf(FILTER12_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_0_50_1_50_HZ, FilterName.valueOf(FILTER13_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_2_00_4_00_HZ, FilterName.valueOf(FILTER14_ENUM));
    assertEquals(FilterName.HAM_FIR_HP_0_30_HZ, FilterName.valueOf(FILTER15_ENUM));
    assertEquals(FilterName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL, FilterName.valueOf(FILTER16_ENUM));
  }

  @Test
  void testFilterNames2() {
    assertEquals(FILTER1_STR, FilterName.HAM_FIR_BP_0_70_2_00_HZ.toString());
    assertEquals(FILTER2_STR, FilterName.HAM_FIR_BP_1_00_3_00_HZ.toString());
    assertEquals(FILTER3_STR, FilterName.HAM_FIR_BP_4_00_8_00_HZ.toString());
    assertEquals(FILTER4_STR, FilterName.HAM_FIR_BP_0_40_3_50_HZ.toString());
    assertEquals(FILTER5_STR, FilterName.HAM_FIR_BP_0_50_4_00_HZ.toString());
    assertEquals(FILTER6_STR, FilterName.HAM_FIR_BP_2_00_5_00_HZ.toString());
    assertEquals(FILTER7_STR, FilterName.HAM_FIR_BP_1_50_3_00_HZ.toString());
    assertEquals(FILTER8_STR, FilterName.HAM_FIR_BP_1_00_5_00_HZ.toString());
    assertEquals(FILTER9_STR, FilterName.HAM_FIR_BP_0_50_2_50_HZ.toString());
    assertEquals(FILTER10_STR, FilterName.HAM_FIR_BP_1_50_3_50_HZ.toString());
    assertEquals(FILTER11_STR, FilterName.HAM_FIR_BP_1_70_3_20_HZ.toString());
    assertEquals(FILTER12_STR, FilterName.HAM_FIR_LP_4_20_HZ.toString());
    assertEquals(FILTER13_STR, FilterName.HAM_FIR_BP_0_50_1_50_HZ.toString());
    assertEquals(FILTER14_STR, FilterName.HAM_FIR_BP_2_00_4_00_HZ.toString());
    assertEquals(FILTER15_STR, FilterName.HAM_FIR_HP_0_30_HZ.toString());
    assertEquals(FILTER16_STR, FilterName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL.toString());
  }

  @Test
  void testGetFilters() {
    assertEquals(FILTER1_STR, FilterName.HAM_FIR_BP_0_70_2_00_HZ.getFilter());
    assertEquals(FILTER2_STR, FilterName.HAM_FIR_BP_1_00_3_00_HZ.getFilter());
    assertEquals(FILTER3_STR, FilterName.HAM_FIR_BP_4_00_8_00_HZ.getFilter());
    assertEquals(FILTER4_STR, FilterName.HAM_FIR_BP_0_40_3_50_HZ.getFilter());
    assertEquals(FILTER5_STR, FilterName.HAM_FIR_BP_0_50_4_00_HZ.getFilter());
    assertEquals(FILTER6_STR, FilterName.HAM_FIR_BP_2_00_5_00_HZ.getFilter());
    assertEquals(FILTER7_STR, FilterName.HAM_FIR_BP_1_50_3_00_HZ.getFilter());
    assertEquals(FILTER8_STR, FilterName.HAM_FIR_BP_1_00_5_00_HZ.getFilter());
    assertEquals(FILTER9_STR, FilterName.HAM_FIR_BP_0_50_2_50_HZ.getFilter());
    assertEquals(FILTER10_STR, FilterName.HAM_FIR_BP_1_50_3_50_HZ.getFilter());
    assertEquals(FILTER11_STR, FilterName.HAM_FIR_BP_1_70_3_20_HZ.getFilter());
    assertEquals(FILTER12_STR, FilterName.HAM_FIR_LP_4_20_HZ.getFilter());
    assertEquals(FILTER13_STR, FilterName.HAM_FIR_BP_0_50_1_50_HZ.getFilter());
    assertEquals(FILTER14_STR, FilterName.HAM_FIR_BP_2_00_4_00_HZ.getFilter());
    assertEquals(FILTER15_STR, FilterName.HAM_FIR_HP_0_30_HZ.getFilter());
    assertEquals(FILTER16_STR, FilterName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL.getFilter());
  }

  @Test
  void testFilterNamesFromStrings() {
    assertEquals(FilterName.HAM_FIR_BP_0_70_2_00_HZ, FilterName.fromString(FILTER1_STR));
    assertEquals(FilterName.HAM_FIR_BP_1_00_3_00_HZ, FilterName.fromString(FILTER2_STR));
    assertEquals(FilterName.HAM_FIR_BP_4_00_8_00_HZ, FilterName.fromString(FILTER3_STR));
    assertEquals(FilterName.HAM_FIR_BP_0_40_3_50_HZ, FilterName.fromString(FILTER4_STR));
    assertEquals(FilterName.HAM_FIR_BP_0_50_4_00_HZ, FilterName.fromString(FILTER5_STR));
    assertEquals(FilterName.HAM_FIR_BP_2_00_5_00_HZ, FilterName.fromString(FILTER6_STR));
    assertEquals(FilterName.HAM_FIR_BP_1_50_3_00_HZ, FilterName.fromString(FILTER7_STR));
    assertEquals(FilterName.HAM_FIR_BP_1_00_5_00_HZ, FilterName.fromString(FILTER8_STR));
    assertEquals(FilterName.HAM_FIR_BP_0_50_2_50_HZ, FilterName.fromString(FILTER9_STR));
    assertEquals(FilterName.HAM_FIR_BP_1_50_3_50_HZ, FilterName.fromString(FILTER10_STR));
    assertEquals(FilterName.HAM_FIR_BP_1_70_3_20_HZ, FilterName.fromString(FILTER11_STR));
    assertEquals(FilterName.HAM_FIR_LP_4_20_HZ, FilterName.fromString(FILTER12_STR));
    assertEquals(FilterName.HAM_FIR_BP_0_50_1_50_HZ, FilterName.fromString(FILTER13_STR));
    assertEquals(FilterName.HAM_FIR_BP_2_00_4_00_HZ, FilterName.fromString(FILTER14_STR));
    assertEquals(FilterName.HAM_FIR_HP_0_30_HZ, FilterName.fromString(FILTER15_STR));
    assertEquals(FilterName.HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL, FilterName.fromString(FILTER16_STR));
  }

  @Test
  void errorWhenNamedFilteredEntryAndUnfilteredEntryIsPopulated() {
    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, FilterNameTest::executeNonExistingFilterName);

    Assertions.assertEquals("Unsupported Filter: Non existing filter", thrown.getMessage());
  }

  private static void executeNonExistingFilterName() {
    FilterName.fromString("Non existing filter");
  }
}


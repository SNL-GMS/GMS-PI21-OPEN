package gms.shared.signalenhancementconfiguration.coi.types;

import java.util.Arrays;

/**
 * Filter name class for resolving all signal enhancement filters
 * from processing configuration
 */
public enum FilterName {
  HAM_FIR_BP_0_40_3_50_HZ("HAM FIR BP 0.40-3.50 Hz"),
  HAM_FIR_BP_0_50_1_50_HZ("HAM FIR BP 0.50-1.50 Hz"),
  HAM_FIR_BP_0_50_2_50_HZ("HAM FIR BP 0.50-2.50 Hz"),
  HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL("HAM FIR BP 0.50-4.00 HZ non causal"),
  HAM_FIR_BP_0_50_4_00_HZ("HAM FIR BP 0.50-4.00 Hz"),
  HAM_FIR_BP_0_70_2_00_HZ("HAM FIR BP 0.70-2.00 Hz"),
  HAM_FIR_BP_1_00_3_00_HZ("HAM FIR BP 1.00-3.00 Hz"),
  HAM_FIR_BP_1_00_5_00_HZ("HAM FIR BP 1.00-5.00 Hz"),
  HAM_FIR_BP_1_50_3_00_HZ("HAM FIR BP 1.50-3.00 Hz"),
  HAM_FIR_BP_1_50_3_50_HZ("HAM FIR BP 1.50-3.50 Hz"),
  HAM_FIR_BP_1_70_3_20_HZ("HAM FIR BP 1.70-3.20 Hz"),
  HAM_FIR_BP_2_00_4_00_HZ("HAM FIR BP 2.00-4.00 Hz"),
  HAM_FIR_BP_2_00_5_00_HZ("HAM FIR BP 2.00-5.00 Hz"),
  HAM_FIR_BP_4_00_8_00_HZ("HAM FIR BP 4.00-8.00 Hz"),
  HAM_FIR_HP_0_30_HZ("HAM FIR HP 0.30 Hz"),
  HAM_FIR_LP_4_20_HZ("HAM FIR LP 4.20 Hz");

  private final String filter;

  FilterName(String filter) {
    this.filter = filter;
  }

  public String getFilter() {
    return filter;
  }

  public static FilterName fromString(String filterName) {
    return Arrays.stream(FilterName.values())
      .filter(v -> v.filter.equalsIgnoreCase(filterName))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported Filter: %s", filterName)));
  }

  @Override
  public String toString() {
    return filter;
  }
}

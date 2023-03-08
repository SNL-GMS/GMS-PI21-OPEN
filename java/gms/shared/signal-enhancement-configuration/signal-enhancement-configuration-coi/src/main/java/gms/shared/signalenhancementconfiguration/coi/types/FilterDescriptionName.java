package gms.shared.signalenhancementconfiguration.coi.types;

import java.util.Arrays;

public enum FilterDescriptionName {
  HAM_FIR_BP_0_70_2_00_HZ_DESCRIPTION("HAM FIR BP 0.70-2.00 Hz Description"),
  HAM_FIR_BP_1_00_3_00_HZ_DESCRIPTION("HAM FIR BP 1.00-3.00 Hz Description"),
  HAM_FIR_BP_4_00_8_00_HZ_DESCRIPTION("HAM FIR BP 4.00-8.00 Hz Description"),
  HAM_FIR_BP_0_40_3_50_HZ_DESCRIPTION("HAM FIR BP 0.40-3.50 Hz Description"),
  HAM_FIR_BP_0_50_4_00_HZ_DESCRIPTION("HAM FIR BP 0.50-4.00 Hz Description"),
  HAM_FIR_BP_0_50_4_00_HZ_NON_CAUSAL_DESCRIPTION("HAM FIR BP 0.50-4.00 HZ non causal Description"),
  HAM_FIR_BP_2_00_5_00_HZ_DESCRIPTION("HAM FIR BP 2.00-5.00 Hz Description"),
  HAM_FIR_BP_1_50_3_00_HZ_DESCRIPTION("HAM FIR BP 1.50-3.00 Hz Description"),
  HAM_FIR_BP_1_00_5_00_HZ_DESCRIPTION("HAM FIR BP 1.00-5.00 Hz Description"),
  HAM_FIR_BP_0_50_2_50_HZ_DESCRIPTION("HAM FIR BP 0.50-2.50 Hz Description"),
  HAM_FIR_BP_1_50_3_50_HZ_DESCRIPTION("HAM FIR BP 1.50-3.50 Hz Description"),
  HAM_FIR_BP_1_70_3_20_HZ_DESCRIPTION("HAM FIR BP 1.70-3.20 Hz Description"),
  HAM_FIR_LP_4_20_HZ_DESCRIPTION("HAM FIR LP 4.20 Hz Description"),
  HAM_FIR_BP_0_50_1_50_HZ_DESCRIPTION("HAM FIR BP 0.50-1.50 Hz Description"),
  HAM_FIR_BP_2_00_4_00_HZ_DESCRIPTION("HAM FIR BP 2.00-4.00 Hz Description"),
  HAM_FIR_HP_0_30_HZ_DESCRIPTION("HAM FIR HP 0.30 Hz Description");

  private final String filterDescription;

  FilterDescriptionName(String filterDescription) {
    this.filterDescription = filterDescription;
  }

  public String getFilterDescription() {
    return filterDescription;
  }

  public static FilterDescriptionName fromString(String filterDescriptionName) {
    return Arrays.stream(FilterDescriptionName.values())
      .filter(v -> v.filterDescription.equalsIgnoreCase(filterDescriptionName))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported Filter Description: %s", filterDescriptionName)));
  }

  @Override
  public String toString() {
    return filterDescription;
  }
}

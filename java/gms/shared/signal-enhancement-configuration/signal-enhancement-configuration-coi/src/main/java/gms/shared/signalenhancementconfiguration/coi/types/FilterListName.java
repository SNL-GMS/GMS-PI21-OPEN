package gms.shared.signalenhancementconfiguration.coi.types;

import java.util.Arrays;

/**
 * Filter list name class for resolving all signal enhancement filter list
 * from processing configuration
 */
public enum FilterListName {
  SEISMIC("Seismic"),
 LONG_PERIOD("Long Period"),
 HYDRO("Hydro");

  private final String filterName;

  FilterListName(String filterName) {
    this.filterName = filterName;
  }

  public String getFilterName() {
    return filterName;
  }

  public static FilterListName fromString(String s) {
    return Arrays.stream(FilterListName.values())
      .filter(v -> v.filterName.equalsIgnoreCase(s))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported Filter List: %s", s)));
  }

  @Override
  public String toString() {
    return filterName;
  }
}

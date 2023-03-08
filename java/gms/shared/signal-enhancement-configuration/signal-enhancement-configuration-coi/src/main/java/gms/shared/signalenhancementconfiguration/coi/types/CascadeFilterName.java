package gms.shared.signalenhancementconfiguration.coi.types;

import java.util.Arrays;

public enum CascadeFilterName {
  CASCADE_FILTER_1("Cascade Filter 1"),
  CASCADE_FILTER_2("Cascade Filter 2"),
  CASCADE_FILTER_3("Cascade Filter 3");

  private final String filterName;

  CascadeFilterName(String filterName) {
    this.filterName = filterName;
  }

  public String getFilterName() {
    return filterName;
  }

  public static CascadeFilterName fromString(String s) {
    return Arrays.stream(CascadeFilterName.values())
      .filter(v -> v.filterName.equalsIgnoreCase(s))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported Cascade Filter Name: %s", s)));
  }

  @Override
  public String toString() {
    return filterName;
  }
}

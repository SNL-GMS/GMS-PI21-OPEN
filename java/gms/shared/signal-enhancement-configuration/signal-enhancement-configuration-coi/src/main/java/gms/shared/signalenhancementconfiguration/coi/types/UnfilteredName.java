package gms.shared.signalenhancementconfiguration.coi.types;

import java.util.Arrays;

public enum UnfilteredName {
  UNFILTERED("Unfiltered");

  private final String filterName;

  UnfilteredName(String filterName) {
    this.filterName = filterName;
  }

  public String getFilterName() {
    return filterName;
  }

  public static UnfilteredName fromString(String s) {
    return Arrays.stream(UnfilteredName.values())
      .filter(v -> v.filterName.equalsIgnoreCase(s))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported Unfiltered: %s", s)));
  }

  @Override
  public String toString() {
    return filterName;
  }
}

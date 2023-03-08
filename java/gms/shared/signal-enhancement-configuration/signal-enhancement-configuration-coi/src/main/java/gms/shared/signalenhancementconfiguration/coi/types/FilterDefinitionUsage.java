package gms.shared.signalenhancementconfiguration.coi.types;

import java.util.Arrays;

/**
 * Filter definition usage processing configuration enums
 */
public enum FilterDefinitionUsage {
  WILD_CARD("*"),
  DETECTION("Detect"),
  FK("FK"),
  ONSET("Onset");

  private final String name;

  FilterDefinitionUsage(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static FilterDefinitionUsage fromString(String s) {
    return Arrays.stream(FilterDefinitionUsage.values())
      .filter(v -> v.name.equalsIgnoreCase(s))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported Filter Definition Usage: %s", s)));
  }

  @Override
  public String toString() {
    return name;
  }
}

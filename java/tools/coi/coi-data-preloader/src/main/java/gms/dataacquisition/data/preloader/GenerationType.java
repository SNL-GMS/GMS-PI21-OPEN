package gms.dataacquisition.data.preloader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Enumeration of supported data generation types
 */
public enum GenerationType {
  RAW_STATION_DATA_FRAME("RSDF", InitialCondition.STATION, InitialCondition.CHANNELS,
    InitialCondition.RECEPTION_DELAY),
  ACQUIRED_CHANNEL_ENV_ISSUE_ANALOG("ACEI_ANALOG", InitialCondition.CHANNEL),
  ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN("ACEI_BOOLEAN", InitialCondition.CHANNEL),
  STATION_SOH("STATION_SOH", InitialCondition.STATION, InitialCondition.CHANNELS),
  CAPABILITY_SOH_ROLLUP("ROLLUP", InitialCondition.STATION_GROUPS, InitialCondition.STATIONS);

  private final String name;
  private final Set<InitialCondition> initialConditions;

  GenerationType(String name, InitialCondition... initialConditions) {
    this.name = name;
    this.initialConditions = Set.of(initialConditions);
  }

  public static Stream<GenerationType> generationTypes() {
    return Arrays.stream(values());
  }

  public static GenerationType parseType(String name) {
    return generationTypes()
      .filter(type -> type.name.equalsIgnoreCase(name))
      .findAny()
      .orElseThrow(
        () -> new IllegalArgumentException(format("Invalid GenerationType %s", name)));
  }

  protected Set<InitialCondition> getInitialConditions() {
    return initialConditions;
  }

  public boolean hasConditions(Collection<InitialCondition> conditions) {
    return conditions.containsAll(initialConditions);
  }

  @Override
  public String toString() {
    return name;
  }
}

package gms.dataacquisition.data.preloader;

import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Enum describing all available initial conditions a {@link GenerationSpec} can contain
 */
public enum InitialCondition {

  STATION_GROUPS("stationGroups"),
  STATION("station"),
  STATIONS("stations"),
  CHANNEL("channel"),
  CHANNELS("channels"),
  RECEPTION_DELAY("receptionDelay"),
  DURATION_INCREMENT("durationIncrement"),
  BOOLEAN_INITIAL_STATUS("booleanInitialStatus"),
  MEAN_OCCURRENCES_PER_YEAR("meanOccurrencesPerYear"),
  MEAN_HOURS_OF_PERSISTENCE("meanHoursOfPersistence"),
  DURATION_ANALOG_STATUS_MIN("durationAnalogStatusMin"),
  DURATION_ANALOG_STATUS_MAX("durationAnalogStatusMax"),
  DURATION_BETA0("durationBeta0"),
  DURATION_BETA1("durationBeta1"),
  DURATION_STDERR("durationStderr"),
  DURATION_ANALOG_INITIAL_VALUE("durationAnalogInitialValue"),
  PERCENT_ANALOG_STATUS_MIN("percentAnalogStatusMin"),
  PERCENT_ANALOG_STATUS_MAX("percentAnalogStatusMax"),
  PERCENT_BETA0("percentBeta0"),
  PERCENT_BETA1("percentBeta1"),
  PERCENT_STDERR("percentStderr"),
  PERCENT_ANALOG_INITIAL_VALUE("percentAnalogInitialValue"),
  USE_CURATED_DATA_GENERATION("useCuratedDataGeneration");

  private final String name;

  InitialCondition(String name) {
    this.name = name;
  }

  public static Stream<InitialCondition> initialConditions() {
    return Arrays.stream(values());
  }

  public static InitialCondition parse(String condition) {
    return initialConditions()
      .filter(cond -> cond.name.equals(condition))
      .findFirst()
      .orElseThrow(
        () -> new IllegalArgumentException(format("Invalid InitialCondition %s", condition)));
  }

  @Override
  public String toString() {
    return name;
  }
}

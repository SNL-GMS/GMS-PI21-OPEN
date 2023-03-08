package gms.shared.signaldetection.dao.css.enums;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;

public enum AmplitudeUnits {

  NA("-"),
  NM("nm"),
  PASCALS("Pa"),
  MICROPASCALS("uPa"),
  HERTZ("Hz"),
  LOG_NM("log10Nm");

  private static final ImmutableMap<String, AmplitudeUnits> amplitudeUnitsbyName;

  static {
    amplitudeUnitsbyName = Arrays.stream(values())
      .collect(ImmutableMap.toImmutableMap(AmplitudeUnits::getName, Functions.identity()));
  }

  private final String name;

  AmplitudeUnits(String units) {
    this.name = units;
  }

  public String getName() {
    return name;
  }

  public static AmplitudeUnits fromString(String name) {
    if (name == null) {
      return null;
    } else {
      return amplitudeUnitsbyName.get(name);
    }
  }
}

package gms.shared.stationdefinition.dao.css.enums;

public enum Band {

  SHORT_BAND("s"),
  MID_PERIOD("m"),
  INTERMEDIATE_PERIOD("i"),
  LONG_PERIOD("l"),
  BROADBAND("b"),
  HIGH_FREQUENCY_VERY_SHORT_PERIOD("h"),
  VERY_LONG_PERIOD("v"),
  EXTREMELY_SHORT_PERIOD("e"),
  EXTREMELY_LONG_PERIOD("r"),
  ULTRA_LONG_PERIOD("u"),
  WEATHER_ENVIRONMENT("w");

  private final String name;

  Band(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

package gms.shared.frameworks.test.utils.services;

/**
 * OSD DB Table type used in the frameworks OSD performance tests
 */
public enum OsdTableType {
  ACEI_ANALOG("acei-analog"),
  ACEI_BOOLEAN("acei-boolean"),
  RAW_STATION_DATA_FRAME("raw-station-data-frame"),
  STATION_SOH("station-soh"),
  SOH_EXTRACT("soh-extract");

  private final String type;

  OsdTableType(String type) {
    this.type = type;
  }

  public static OsdTableType getEnum(String type) {
    for (OsdTableType osdTableType : values()) {
      if (osdTableType.toString().equalsIgnoreCase(type)) {
        return osdTableType;
      }
    }

    throw new IllegalArgumentException();
  }

  @Override
  public String toString() {
    return this.type;
  }
}

package gms.shared.frameworks.osd.coi.systemmessages;

public enum SystemMessageTagNames {
  STATION("station"), CHANNEL("channel"), STATION_GROUP("stationGroup"),
  MONITOR_TYPE("monitorType");

  private final String tagName;

  SystemMessageTagNames(String tagName) {
    this.tagName = tagName;
  }

  public String getTagName() {
    return tagName;
  }
}

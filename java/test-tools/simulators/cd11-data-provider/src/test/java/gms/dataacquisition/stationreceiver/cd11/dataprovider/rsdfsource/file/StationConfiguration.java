package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.file;

class StationConfiguration {

  private final String stationName;
  private final int rsdfCount;

  StationConfiguration(String stationName, int rsdfCount) {
    this.stationName = stationName;
    this.rsdfCount = rsdfCount;
  }

  public String getStationName() {
    return stationName;
  }

  public int getRsdfCount() {
    return rsdfCount;
  }
}

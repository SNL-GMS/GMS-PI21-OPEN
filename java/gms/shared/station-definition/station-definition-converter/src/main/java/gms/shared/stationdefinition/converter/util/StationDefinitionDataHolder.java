package gms.shared.stationdefinition.converter.util;


import gms.shared.stationdefinition.dao.css.InstrumentDao;
import gms.shared.stationdefinition.dao.css.SensorDao;
import gms.shared.stationdefinition.dao.css.SiteChanDao;
import gms.shared.stationdefinition.dao.css.SiteDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

import java.util.List;

public class StationDefinitionDataHolder {
  private List<SiteDao> siteDaos;
  private List<SiteChanDao> siteChanDaos;
  private List<SensorDao> sensorDaos;
  private List<InstrumentDao> instrumentDaos;
  private List<WfdiscDao> wfdiscVersions;

  public StationDefinitionDataHolder(List<SiteDao> siteDaos,
    List<SiteChanDao> siteChanDaos, List<SensorDao> sensorDaos,
    List<InstrumentDao> instrumentDaos,
    List<WfdiscDao> wfdiscVersions) {
    this.siteDaos = siteDaos;
    this.siteChanDaos = siteChanDaos;
    this.sensorDaos = sensorDaos;
    this.wfdiscVersions = wfdiscVersions;
    this.instrumentDaos = instrumentDaos;
  }

  public List<SiteDao> getSiteDaos() {
    return siteDaos;
  }

  public List<SiteChanDao> getSiteChanDaos() {
    return siteChanDaos;
  }

  public List<SensorDao> getSensorDaos() {
    return sensorDaos;
  }

  public List<InstrumentDao> getInstrumentDaos() {
    return instrumentDaos;
  }

  public List<WfdiscDao> getWfdiscVersions() {
    return wfdiscVersions;
  }
}
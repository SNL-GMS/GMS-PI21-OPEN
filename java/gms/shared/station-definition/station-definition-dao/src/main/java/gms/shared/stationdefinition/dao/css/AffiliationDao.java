package gms.shared.stationdefinition.dao.css;

import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;


@Entity
@Table(name = "affiliation")
public class AffiliationDao {

  private NetworkStationTimeKey networkStationTimeKey;
  private Instant endTime;
  private Instant loadDate;

  public AffiliationDao() {
    // JPA constructor
  }

  @EmbeddedId
  public NetworkStationTimeKey getNetworkStationTimeKey() {
    return networkStationTimeKey;
  }

  public void setNetworkStationTimeKey(NetworkStationTimeKey networkStationTimeKey) {
    this.networkStationTimeKey = networkStationTimeKey;
  }

  @Column(name = "endtime")
  @Convert(converter = InstantToDoubleConverterPositiveNa.class)
  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  @Column(name = "lddate")
  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant ldDate) {
    this.loadDate = ldDate;
  }

}

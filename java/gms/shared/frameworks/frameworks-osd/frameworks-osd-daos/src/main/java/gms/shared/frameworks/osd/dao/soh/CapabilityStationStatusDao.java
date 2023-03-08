package gms.shared.frameworks.osd.dao.soh;


import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.Type;

import gms.shared.frameworks.osd.coi.soh.SohStatus;

@Embeddable
public class CapabilityStationStatusDao {

  @Enumerated(EnumType.STRING)
  @Column(name = "soh_status", nullable = false, columnDefinition = "public.soh_status_enum")
  @Type(type = "pgsql_enum")
  private SohStatus stationSohStatus;

  @Column(name = "station_name")
  private String stationName;

  public SohStatus getStationSohStatus() {
    return this.stationSohStatus;
  }

  public void setStationSohStatus(SohStatus sohStatus) {
    this.stationSohStatus = sohStatus;
  }

  public String getStationName() {
    return this.stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CapabilityStationStatusDao)) {
      return false;
    }
    CapabilityStationStatusDao that = (CapabilityStationStatusDao) o;
    return Objects.equals(stationSohStatus, that.stationSohStatus) &&
      Objects.equals(stationName, that.stationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stationSohStatus, stationName);
  }

}

package gms.shared.stationdefinition.dao.css;

import gms.shared.stationdefinition.dao.css.converter.StationTypeConverter;
import gms.shared.stationdefinition.dao.css.enums.StaType;
import gms.shared.utilities.bridge.database.converter.JulianDateConverterPositiveNa;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "site")
public class SiteDao {

  @EmbeddedId
  private SiteKey id;

  @Column(name = "offdate", nullable = false)
  @Convert(converter = JulianDateConverterPositiveNa.class)
  private Instant offDate;

  @Column(name = "lat", nullable = false)
  private double latitude;

  @Column(name = "lon", nullable = false)
  private double longitude;

  @Column(name = "elev", nullable = false)
  private double elevation;

  @Column(name = "staname", nullable = false)
  private String stationName;

  @Column(name = "statype", nullable = false)
  @Convert(converter = StationTypeConverter.class)
  private StaType staType;

  @Column(name = "refsta", nullable = false)
  private String referenceStation;

  @Column(name = "dnorth", nullable = false)
  private double degreesNorth;

  @Column(name = "deast", nullable = false)
  private double degreesEast;

  @Column(name = "lddate", nullable = false)
  private Instant loadDate;

  public SiteDao() {
  }

  public SiteDao(SiteKey id, Instant offDate, double latitude, double longitude, double elevation,
    String stationName, StaType stationType, String referenceStation, double degreesNorth,
    double degreesEast, Instant loadDate) {
    this.id = id;
    this.offDate = offDate;
    this.latitude = latitude;
    this.longitude = longitude;
    this.elevation = elevation;
    this.stationName = stationName;
    this.staType = stationType;
    this.referenceStation = referenceStation;
    this.degreesNorth = degreesNorth;
    this.degreesEast = degreesEast;
    this.loadDate = loadDate;
  }

  public SiteDao(SiteDao copy) {
    this.id = new SiteKey(copy.id);
    this.offDate = copy.offDate;
    this.latitude = copy.latitude;
    this.longitude = copy.longitude;
    this.elevation = copy.elevation;
    this.stationName = copy.stationName;
    this.staType = copy.staType;
    this.referenceStation = copy.referenceStation;
    this.degreesNorth = copy.degreesNorth;
    this.degreesEast = copy.degreesEast;
    this.loadDate = copy.loadDate;
  }

  public SiteKey getId() {
    return id;
  }

  public void setId(SiteKey id) {
    this.id = id;
  }

  public Instant getOffDate() {
    return offDate;
  }

  public void setOffDate(Instant offDate) {
    this.offDate = offDate;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  public StaType getStaType() {
    return staType;
  }

  public void setStaType(StaType staType) {
    this.staType = staType;
  }

  public String getReferenceStation() {
    return referenceStation;
  }

  public void setReferenceStation(String referenceStation) {
    this.referenceStation = referenceStation;
  }

  public double getDegreesNorth() {
    return degreesNorth;
  }

  public void setDegreesNorth(double degreesNorth) {
    this.degreesNorth = degreesNorth;
  }

  public double getDegreesEast() {
    return degreesEast;
  }

  public void setDegreesEast(double degreesEast) {
    this.degreesEast = degreesEast;
  }

  public Instant getLoadDate() {
    return loadDate;
  }

  public void setLoadDate(Instant loadDate) {
    this.loadDate = loadDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SiteDao that = (SiteDao) o;
    return id.equals(that.id) && offDate.equals(that.offDate) && latitude == that.latitude
      && longitude == that.longitude && elevation == that.elevation
      && stationName.equals(that.stationName) && staType.equals(that.staType)
      && degreesNorth == that.degreesNorth && degreesEast == that.degreesEast
      && loadDate.equals(that.loadDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, offDate, latitude, longitude, elevation, stationName, staType,
      referenceStation, degreesNorth, degreesEast, loadDate);
  }

  @Override
  public String toString() {
    return "SiteDao{" +
      "id=" + id +
      '}';
  }
}

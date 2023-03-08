package gms.shared.frameworks.osd.dao.channel;

import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "station_channel_info")
public class StationChannelInfoDao {

  @EmbeddedId
  StationChannelInfoKey id;

  // TODO: Need to figure out a way to change this to use RelativePositionDao
  @Column(name = "north_displacement_km")
  private double northDisplacementKm;

  @Column(name = "east_displacement_km")
  private double eastDisplacementKm;

  @Column(name = "vertical_displacement_km")
  private double verticalDisplacementKm;

  public StationChannelInfoKey getId() {
    return id;
  }

  public void setId(
    StationChannelInfoKey id) {
    this.id = id;
  }

  public double getNorthDisplacementKm() {
    return northDisplacementKm;
  }

  public void setNorthDisplacementKm(double northDisplacementKm) {
    this.northDisplacementKm = northDisplacementKm;
  }

  public double getEastDisplacementKm() {
    return eastDisplacementKm;
  }

  public void setEastDisplacementKm(double eastDisplacementKm) {
    this.eastDisplacementKm = eastDisplacementKm;
  }

  public double getVerticalDisplacementKm() {
    return verticalDisplacementKm;
  }

  public void setVerticalDisplacementKm(double verticalDisplacementKm) {
    this.verticalDisplacementKm = verticalDisplacementKm;
  }

  public RelativePosition grabRelativePosition() {
    return RelativePosition.from(northDisplacementKm, eastDisplacementKm, verticalDisplacementKm);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StationChannelInfoDao)) {
      return false;
    }
    StationChannelInfoDao that = (StationChannelInfoDao) o;
    return Double.compare(that.northDisplacementKm, northDisplacementKm) == 0 &&
      Double.compare(that.eastDisplacementKm, eastDisplacementKm) == 0 &&
      Double.compare(that.verticalDisplacementKm, verticalDisplacementKm) == 0 &&
      Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, northDisplacementKm, eastDisplacementKm, verticalDisplacementKm);
  }

  @Override
  public String toString() {
    return "StationChannelInfoDao{" +
      "id=" + id +
      ", northDisplacementKm=" + northDisplacementKm +
      ", eastDisplacementKm=" + eastDisplacementKm +
      ", verticalDisplacementKm=" + verticalDisplacementKm +
      '}';
  }
}

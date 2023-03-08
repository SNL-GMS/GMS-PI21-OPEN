package gms.shared.frameworks.osd.dao.channel;

import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.signaldetection.Location;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LocationDao implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "depth")
  private Double depth;

  @Column(name = "elevation")
  private Double elevation;

  public LocationDao() {
  }

  public LocationDao(Location location) {
    if (location != null) {
      this.latitude = location.getLatitudeDegrees();
      this.longitude = location.getLongitudeDegrees();
      this.depth = location.getDepthKm();
      this.elevation = location.getElevationKm();
    }
  }

  /**
   * Create a DAO from the COI {@link Location}.
   *
   * @param location the location to convert
   * @return The Location converted to its DAO format
   */
  public static LocationDao from(Location location) {
    Preconditions.checkNotNull(location, "Cannot create dao from null Location");
    return new LocationDao(location);
  }

  public Location toCoi() {
    return Location.from(this.latitude, this.longitude, this.depth, this.elevation);
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

  public double getDepth() {
    return depth;
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }

  public double getElevation() {
    return elevation;
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LocationDao)) {
      return false;
    }
    LocationDao that = (LocationDao) o;
    return Objects.equals(latitude, that.latitude) &&
      Objects.equals(longitude, that.longitude) &&
      Objects.equals(depth, that.depth) &&
      Objects.equals(elevation, that.elevation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude, depth, elevation);
  }

  @Override
  public String toString() {
    return "LocationDao{" +
      "latitude=" + latitude +
      ", longitude=" + longitude +
      ", depth=" + depth +
      ", elevation=" + elevation +
      '}';
  }
}

package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the primary key into the origin record from the `origin` legacy table.
 */
@Embeddable
public class LatLonDepthTimeKey implements Serializable {

  private double latitude;
  private double longitude;
  private double depth;
  private double time;

  public LatLonDepthTimeKey() {
  }

  private LatLonDepthTimeKey(Builder builder) {
    this.latitude = builder.latitude;
    this.longitude = builder.longitude;
    this.depth = builder.depth;
    this.time = builder.time;
  }

  @Column(name = "lat")
  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  @Column(name = "lon")
  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Column(name = "depth")
  public double getDepth() {
    return depth;
  }

  public void setDepth(double depth) {
    this.depth = depth;
  }

  @Column(name = "time")
  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public static class Builder {

    private double latitude;
    private double longitude;
    private double depth;
    private double time;

    public Builder withLatitude(double latitude) {
      this.latitude = latitude;
      return this;
    }

    public Builder withLongitude(double longitude) {
      this.longitude = longitude;
      return this;
    }

    public Builder withDepth(double depth) {
      this.depth = depth;
      return this;
    }

    public Builder withTime(double time) {
      this.time = time;
      return this;
    }

    public LatLonDepthTimeKey build() {

      // -999.0 indicates NA value
      if (latitude != -999.0) {
        checkArgument((-90.0 <= latitude) && (latitude <= 90.0), "Latitude is " + latitude +
          DaoHelperUtility.createRangeStringDouble(-90, 90, '[', ']'));
      }

      // -999.0 indicates NA value
      if (longitude != -999.0) {
        checkArgument((-180.0 <= longitude) && (longitude <= 180.0), "Longitude is " + longitude +
          DaoHelperUtility.createRangeStringDouble(-180, 180, '[', ']'));
      }

      // -999.0 indicates NA value
      if (depth != -999.0) {
        checkArgument((-100.0 <= depth) && (depth < 1000.0), "Depth is " + depth +
          DaoHelperUtility.createRangeStringDouble(-100, 1000, '(', ']'));
      }

      // NA not allowed
      checkArgument(-9_999_999_999.999 < time, "Time is " + time +
        ".  It must be greater than -9,999,999,999.999.");

      return new LatLonDepthTimeKey(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LatLonDepthTimeKey that = (LatLonDepthTimeKey) o;
    return Double.compare(that.latitude, latitude) == 0 && Double.compare(that.longitude,
      longitude) == 0 && Double.compare(that.depth, depth) == 0
      && Double.compare(that.time,
      time) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude, depth, time);
  }

  @Override
  public String toString() {
    return "LatLonDepthTimeKey{" +
      "lat=" + latitude +
      ", lon=" + longitude +
      ", depth=" + depth +
      ", time=" + time +
      '}';
  }
}

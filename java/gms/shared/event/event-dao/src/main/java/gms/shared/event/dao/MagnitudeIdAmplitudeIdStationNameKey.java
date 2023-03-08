package gms.shared.event.dao;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents the primary key into the Station Magnitude record from the `stamag` legacy table.
 */
@Embeddable
public class MagnitudeIdAmplitudeIdStationNameKey implements Serializable {

  private long magnitudeId;
  private long amplitudeId;
  private String stationName;

  private MagnitudeIdAmplitudeIdStationNameKey() {
  }

  private MagnitudeIdAmplitudeIdStationNameKey(Builder builder) {
    this.magnitudeId = builder.magnitudeId;
    this.amplitudeId = builder.amplitudeId;
    this.stationName = builder.stationName;
  }

  @Column(name = "magid")
  public long getMagnitudeId() {
    return magnitudeId;
  }

  public void setMagnitudeId(long magnitudeId) {
    this.magnitudeId = magnitudeId;
  }

  @Column(name = "ampid")
  public long getAmplitudeId() {
    return amplitudeId;
  }

  public void setAmplitudeId(long amplitudeId) {
    this.amplitudeId = amplitudeId;
  }

  @Column(name = "sta")
  public String getStationName() {
    return stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  public static class Builder {

    private long magnitudeId;
    private long amplitudeId;
    private String stationName;

    public Builder withMagnitudeId(long magnitudeId) {
      this.magnitudeId = magnitudeId;
      return this;
    }

    public Builder withAmplitudeId(long amplitudeId) {
      this.amplitudeId = amplitudeId;
      return this;
    }

    public Builder withStationName(String stationName) {
      this.stationName = stationName;
      return this;
    }

    public MagnitudeIdAmplitudeIdStationNameKey build() {

      // -1 indicates NA value
      if (magnitudeId != -1) {
        checkArgument(0 <= magnitudeId, "Magnitude Id is " + magnitudeId +
          ".  It must be greater than or equal to zero.");
      }

      // -1 indicates NA value
      if (amplitudeId != -1) {
        checkArgument(0 < amplitudeId, "Amplitude Id is " + amplitudeId +
          ".  It must be greater than zero.");
      }

      checkNotNull(stationName, "Station name is null.");
      checkArgument(!stationName.isBlank(), "Station name is blank.");
      // NA not allowed
      checkArgument(stationName.length() <= 6, "Station name is " + stationName +
        DaoHelperUtility.createCharLengthString(6));

      return new MagnitudeIdAmplitudeIdStationNameKey(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MagnitudeIdAmplitudeIdStationNameKey)) {
      return false;
    }
    MagnitudeIdAmplitudeIdStationNameKey that = (MagnitudeIdAmplitudeIdStationNameKey) o;
    return magnitudeId == that.magnitudeId && amplitudeId == that.amplitudeId && stationName.equals(
      that.stationName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(magnitudeId, amplitudeId, stationName);
  }

  @Override
  public String toString() {
    return "MagnitudeIdAmplitudeIdStationNameKey{" +
      "magnitudeId=" + magnitudeId +
      ", amplitudeId=" + amplitudeId +
      ", stationName='" + stationName + '\'' +
      '}';
  }
}

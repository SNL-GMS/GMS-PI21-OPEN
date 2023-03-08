package gms.shared.stationdefinition.dao.css;

import gms.shared.utilities.bridge.database.converter.JulianDateConverterNegativeNa;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class SiteChanKey implements Serializable {
  @Column(name = "sta", nullable = false)
  private String stationCode;

  @Column(name = "chan", nullable = false)
  private String channelCode;

  @Column(name = "ondate", nullable = false)
  @Convert(converter = JulianDateConverterNegativeNa.class)
  private Instant onDate;

  public SiteChanKey() {
  }

  public SiteChanKey(String stationCode, String channelCode, Instant onDate) {
    this.stationCode = Validate.notBlank(stationCode, "SiteChanKey must be provided a station code");
    this.channelCode = Validate.notBlank(channelCode, "SiteChanKey must be provided a channel code");
    this.onDate = Validate.notNull(onDate, "SiteChanKey must be provided an on date");
  }

  public String getStationCode() {
    return stationCode;
  }

  public void setStationCode(String stationCode) {
    this.stationCode = stationCode;
  }

  public String getChannelCode() {
    return channelCode;
  }

  public void setChannelCode(String channelCode) {
    this.channelCode = channelCode;
  }

  public Instant getOnDate() {
    return onDate;
  }

  public void setOnDate(Instant onDate) {
    this.onDate = onDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SiteChanKey that = (SiteChanKey) o;
    return stationCode.equals(that.stationCode) &&
      channelCode.equals(that.channelCode) &&
      onDate.equals(that.onDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stationCode, channelCode, onDate);
  }

  @Override
  public String toString() {
    return this.stationCode + '.' + this.channelCode + '.' + this.onDate;
  }
}

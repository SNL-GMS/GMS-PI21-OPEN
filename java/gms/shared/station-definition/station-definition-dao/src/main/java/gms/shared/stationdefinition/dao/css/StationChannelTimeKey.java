package gms.shared.stationdefinition.dao.css;

import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class StationChannelTimeKey implements Serializable {

  private String stationCode;
  private String channelCode;
  private Instant time;

  public StationChannelTimeKey() {
  }

  public StationChannelTimeKey(String stationCode, String channelCode, Instant time) {
    this.stationCode = Validate.notBlank(stationCode, "StationChannelTimeKey must be provided a station code");
    this.channelCode = Validate.notBlank(channelCode, "StationChannelTimeKey must be provided a channel code");
    this.time = Validate.notNull(time, "StationChannelTimeKey must be provided a time");
  }

  @Column(name = "sta", nullable = false)
  public String getStationCode() {
    return stationCode;
  }

  public void setStationCode(String stationCode) {
    this.stationCode = stationCode;
  }

  @Column(name = "chan", nullable = false)
  public String getChannelCode() {
    return channelCode;
  }

  public void setChannelCode(String channelCode) {
    this.channelCode = channelCode;
  }

  @Column(name = "time", nullable = false)
  @Convert(converter = InstantToDoubleConverterNegativeNa.class)
  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationChannelTimeKey that = (StationChannelTimeKey) o;
    return stationCode.equals(that.stationCode) &&
      channelCode.equals(that.channelCode) &&
      time.equals(that.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stationCode, channelCode, time);
  }
}

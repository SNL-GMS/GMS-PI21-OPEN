package gms.shared.stationdefinition.dao.css;

import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterNegativeNa;
import gms.shared.utilities.bridge.database.converter.InstantToDoubleConverterPositiveNa;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class SensorKey implements Serializable {

  private String station;
  private String channel;
  private Instant time;
  private Instant endTime;

  public SensorKey() {
  }

  public SensorKey(String station, String channel, Instant time, Instant endTime) {
    this.station = Validate.notBlank(station, "SensorKey must be provided a station");
    this.channel = Validate.notBlank(channel, "SensorKey must be provided a channel");
    this.time = Validate.notNull(time, "SensorKey must be provided a time");
    this.endTime = Validate.notNull(endTime, "SensorKey must be provided a endTime");
  }

  @Column(name = "sta", nullable = false)
  public String getStation() {
    return station;
  }

  public void setStation(String station) {
    this.station = station;
  }

  @Column(name = "chan", nullable = false)
  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  @Column(name = "time")
  @Convert(converter = InstantToDoubleConverterNegativeNa.class)
  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  @Column(name = "endtime")
  @Convert(converter = InstantToDoubleConverterPositiveNa.class)
  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SensorKey sensorKey = (SensorKey) o;
    return station.equals(sensorKey.station) &&
      channel.equals(sensorKey.channel) &&
      time.equals(sensorKey.time) &&
      endTime.equals(sensorKey.endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, channel, time, endTime);
  }
}

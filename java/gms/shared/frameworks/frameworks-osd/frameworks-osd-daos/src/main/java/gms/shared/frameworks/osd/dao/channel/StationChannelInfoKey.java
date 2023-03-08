package gms.shared.frameworks.osd.dao.channel;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class StationChannelInfoKey implements Serializable {
  @ManyToOne
  @JoinColumn(
    referencedColumnName = "name",
    name = "station_name",
    nullable = false,
    updatable = false
  )
  private StationDao station;

  @ManyToOne
  @JoinColumn(
    referencedColumnName = "name",
    name = "channel_name",
    nullable = false,
    updatable = false
  )
  private ChannelDao channel;

  public StationChannelInfoKey() {
  }

  public StationChannelInfoKey(
    StationDao station,
    ChannelDao channel) {
    this.station = station;
    this.channel = channel;
  }

  public StationDao getStation() {
    return station;
  }

  public void setStation(
    StationDao station) {
    this.station = station;
  }

  public ChannelDao getChannel() {
    return channel;
  }

  public void setChannel(
    ChannelDao channel) {
    this.channel = channel;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationChannelInfoKey that = (StationChannelInfoKey) o;
    return station.equals(that.station) &&
      channel.equals(that.channel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(station, channel);
  }

  @Override
  public String toString() {
    return "StationChannelInfoKey{" +
      "station=" + station +
      ", channel=" + channel +
      '}';
  }
}

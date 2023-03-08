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
public class NetworkStationTimeKey implements Serializable {

  private String network;
  private String station;
  private Instant time;

  public NetworkStationTimeKey() {
  }

  public NetworkStationTimeKey(String network, String station, Instant time) {
    this.network = Validate.notBlank(network, "NetworkStationTimeKey must be provided a network");
    this.station = Validate.notBlank(station, "NetworkStationTimeKey must be provided a station");
    this.time = Validate.notNull(time, "NetworkStationTimeKey must be provided a time");
  }

  @Column(name = "net")
  public String getNetwork() {
    return network;
  }

  public void setNetwork(String network) {
    this.network = network;
  }

  @Column(name = "sta")
  public String getStation() {
    return station;
  }

  public void setStation(String station) {
    this.station = station;
  }

  @Column(name = "time")
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
    NetworkStationTimeKey that = (NetworkStationTimeKey) o;
    return network.equals(that.network) &&
      station.equals(that.station) &&
      time.equals(that.time);
  }

  @Override
  public int hashCode() {
    return Objects.hash(network, station, time);
  }
}

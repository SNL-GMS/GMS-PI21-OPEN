package gms.shared.frameworks.osd.dao.channel;

import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.stationreference.StationType;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "station")
public class StationDao implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "station_type")
  private StationType type;

  @Column(name = "description", length = 1024, nullable = false)
  private String description;

  @Embedded
  private LocationDao location;

  @OneToMany(mappedBy = "station", fetch = FetchType.LAZY)
  private List<ChannelGroupDao> channelGroups;

  public StationDao() {
  }

  private StationDao(
    String name,
    StationType type,
    String description,
    LocationDao location) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.location = location;
  }

  public static StationDao from(Station station) {
    return new StationDao(station.getName(), station.getType(),
      station.getDescription(), new LocationDao(station.getLocation()));
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public StationType getType() {
    return type;
  }

  public void setType(
    StationType type) {
    this.type = type;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocationDao getLocation() {
    return location;
  }

  public void setLocation(
    LocationDao location) {
    this.location = location;
  }

  public List<ChannelGroupDao> getChannelGroups() {
    return channelGroups;
  }

  public void setChannelGroups(
    List<ChannelGroupDao> channelGroups) {
    this.channelGroups = channelGroups;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StationDao)) {
      return false;
    }
    StationDao that = (StationDao) o;
    return Objects.equals(name, that.name) &&
      type == that.type &&
      Objects.equals(description, that.description) &&
      Objects.deepEquals(this.channelGroups, that.channelGroups) &&
      Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, description, location, channelGroups);
  }

  @Override
  public String toString() {
    return "StationDao{" +
      "name='" + name + '\'' +
      ", type=" + type +
      ", description='" + description + '\'' +
      ", location=" + location +
      '}';
  }
}

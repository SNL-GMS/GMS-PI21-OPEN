package gms.shared.frameworks.osd.dao.channel;

import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import org.apache.commons.lang3.Validate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "station_group")
@NamedEntityGraph(
  name = "station-group-graph",
  attributeNodes = {
    @NamedAttributeNode("stations")
  }
)
public class StationGroupDao {

  @Id
  @Column(name = "name", nullable = false, unique = true, updatable = false)
  private String name;

  @Column(name = "description", nullable = false, length = 1024)
  private String description;

  @ManyToMany
  @JoinTable(
    name = "station_group_stations",
    joinColumns = @JoinColumn(name = "station_group_name",
      table = "station_group", referencedColumnName = "name"),
    inverseJoinColumns = @JoinColumn(name = "station_name",
      table = "stations", referencedColumnName = "name")
  )
  private List<StationDao> stations;

  public StationGroupDao() {
  }

  private StationGroupDao(String name, String description,
    List<StationDao> stations) {
    this.name = name;
    this.description = description;
    this.stations = stations;
  }

  public static StationGroupDao from(String name, String description,
    List<StationDao> stations) {
    Validate.notEmpty(name);
    Validate.notNull(description);
    Validate.notEmpty(stations);
    return new StationGroupDao(name, description, stations);
  }

  public static StationGroupDao from(StationGroup stationGroup) {
    Validate.notNull(stationGroup);
    List<StationDao> stationDaos = new ArrayList<>();
    for (Station station : stationGroup.getStations()) {
      stationDaos.add(StationDao.from(station));
    }
    return new StationGroupDao(stationGroup.getName(), stationGroup.getDescription(),
      stationDaos);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<StationDao> getStations() {
    return stations;
  }

  public void setStations(
    List<StationDao> stations) {
    this.stations = stations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StationGroupDao)) {
      return false;
    }
    StationGroupDao that = (StationGroupDao) o;
    return Objects.equals(name, that.name) &&
      Objects.equals(description, that.description) &&
      Objects.equals(stations, that.stations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, stations);
  }

  @Override
  public String toString() {
    return "StationGroupDao{" +
      "name='" + name + '\'' +
      ", description='" + description + '\'' +
      ", stations=" + stations +
      '}';
  }
}

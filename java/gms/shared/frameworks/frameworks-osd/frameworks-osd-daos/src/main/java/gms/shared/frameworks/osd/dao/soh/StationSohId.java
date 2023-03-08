package gms.shared.frameworks.osd.dao.soh;

import java.io.Serializable;
import java.util.Objects;

public class StationSohId implements Serializable {

  private static final long serialVersionUID = 1L;

  private int id;

  private String stationName;

  public StationSohId() {
    // empty JPA constructor
  }

  public StationSohId(int id, String stationName) {
    this.id = id;
    this.stationName = stationName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getStationName() {
    return stationName;
  }

  public void setStationName(String stationName) {
    this.stationName = stationName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StationSohId that = (StationSohId) o;

    return getId() == that.getId() && stationName.equals(that.getStationName());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getStationName());
  }

}

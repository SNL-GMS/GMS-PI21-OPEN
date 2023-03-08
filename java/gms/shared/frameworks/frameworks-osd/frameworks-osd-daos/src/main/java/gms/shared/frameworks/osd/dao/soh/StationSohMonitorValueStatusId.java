package gms.shared.frameworks.osd.dao.soh;

import java.io.Serializable;
import java.util.Objects;

/**
 * Holds the Id for the StationSohMonitorValueStatusDao
 */
public class StationSohMonitorValueStatusId implements Serializable {

  public StationSohMonitorValueStatusId() {
    // empty JPA constructor
  }

  private static final long serialVersionUID = 1L;

  private int id;

  private StationSohDao stationSoh;

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public StationSohDao getStationSoh() {
    return this.stationSoh;
  }

  public void setStationSoh(StationSohDao stationSoh) {
    this.stationSoh = stationSoh;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StationSohMonitorValueStatusId that = (StationSohMonitorValueStatusId) o;
    return id == that.id && stationSoh.equals(that.stationSoh);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, stationSoh);
  }
}

package gms.shared.frameworks.osd.dao.soh;

import javax.persistence.Entity;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;


@Entity
@Table(name = "station_soh_monitor_value_status")
public class StationSohMonitorValueAndStatusDao extends SohMonitorValueAndStatusDao {

  public StationSohMonitorValueAndStatusDao() {
    //empty constructor for JPA
  }

  @JoinColumn(name = "station_soh_id", referencedColumnName = "id")
  @JoinColumn(name = "station_soh_station_name", referencedColumnName = "station_name")
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private StationSohDao stationSoh;

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
    if (!super.equals(o)) return false;
    StationSohMonitorValueAndStatusDao that = (StationSohMonitorValueAndStatusDao) o;
    return stationSoh.equals(that.stationSoh);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), stationSoh);
  }
}
